package de.jaberu.test;

import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is a custom junit runner extending the default {@link BlockJUnit4ClassRunner}.
 * <p>
 * With the runner environment-specific properties can be used in the test. To use those
 * properties the test class should define the resource to load from by {@link ResourceBundle}
 * annotation on class level. Properties of the test class can be marked with the
 * {@link PropertyValue} annotation to say what property value to inject.
 * <p>
 * For each found annotation we serve properties in the following order. Assuming we defined a
 * resource bundle with the name <code>test</code>, the value is taken from:
 * <ol>
 * <li>from the system properties directly</li>
 * <li><code>&lt;{@value Environment#STAGE}&gt;/test.properties</code> from the classpath directly. That only works if the env was given as system property</li>
 * <li><code>test.properties</code> from the classpath directly</li>
 * </ol>
 * <p>
 * The runner also supports the custom {@link Ignore} annotation.
 * <p>
 * Created by aherr on 09.11.2015.
 */
public class EnvironmentTestRunner extends BlockJUnit4ClassRunner {

    private static final Method withRulesMethod;

    static {
        try {
            withRulesMethod = ReflectionUtils.findMethod(BlockJUnit4ClassRunner.class, "withRules", FrameworkMethod.class, Object.class, Statement.class);
            if (withRulesMethod == null) {
                throw new IllegalStateException(
                        "Failed to find withRules() method: BlockJUnit4ClassRunner requires JUnit 4.9 or higher.");
            }
            withRulesMethod.setAccessible(true);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Here we create a new runner and initialize the properties to serve from.
     *
     * @param klass test class
     * @throws InitializationError if properties were not found
     */
    public EnvironmentTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * Before running the test method we check if an {@link Ignore} annotation
     * exists and if one of the environments is equal to the current environment.
     * If so, we ignore this test.
     *
     * @param method   test method
     * @param notifier used to ignore
     */
    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        Ignore ignore = method.getAnnotation(Ignore.class);
        if (ignore != null) {
            // check environment specific overrides
            String env = getStage();
            for (String ignoreIn : ignore.value()) {
                if (ignoreIn.equals(env)) {
                    notifier.fireTestIgnored(description);
                    return;
                }
            }
        }
        super.runChild(method, notifier);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> originalMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<FrameworkMethod> publicationMethods = new ArrayList<FrameworkMethod>();
        String publication = getPublication();
        String stage = getStage();
        if (publication != null) {
            String[] publications = publication.split(",");
            for (FrameworkMethod method : originalMethods) {
                for (String pub : publications) {
                    publicationMethods.add(new EnvironmentFrameworkMethod(method, stage, pub));
                }
            }
            return publicationMethods;
        } else if (stage != null) {
            for (FrameworkMethod method : originalMethods) {
                publicationMethods.add(new EnvironmentFrameworkMethod(method, stage));
            }
        }
        return originalMethods;
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
            test = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }
        EnvironmentFrameworkMethod envMethod;
        if (method instanceof EnvironmentFrameworkMethod) {
            // since we overrode the getChildren method we should always get the type, why cast is safe
            envMethod = (EnvironmentFrameworkMethod) method;
        } else {
            // however that does not work in tests were we set the stage in before class,
            // since before class functions are called AFTER the child list was created.
            // Therefore the child list does not include EnvironmentFrameworkMethod, but
            // we need one to process. So try again to read the environment settings
            envMethod = new EnvironmentFrameworkMethod(method, getStage(), getPublication());
        }
        Properties properties;
        try {
            properties = loadProperties(test, envMethod.getStage(), envMethod.getPublication());
        } catch (IOException ioe) {
            return new Fail(ioe);
        }
        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingExceptions(method, test, statement);
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);
        statement = withRulesReflectively(method, test, statement);
        statement = withAllPublications(test, properties, envMethod, statement);
        return statement;
    }

    /**
     * Invoke JUnit's private {@code withRules()} method using reflection.
     */
    private Statement withRulesReflectively(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        try {
            return (Statement) withRulesMethod.invoke(this, frameworkMethod, testInstance, statement);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected Statement withAllPublications(
            final Object test,
            final Properties properties,
            final EnvironmentFrameworkMethod method,
            final Statement testMethod) {
        return new Statement() {
            public void evaluate() throws Throwable {
                String stage = method.getStage();
                String publication = method.getPublication();

                injectProperties(test, properties);
                injectProperty(test, Environment.PUBLICATION, publication);
                injectProperty(test, Environment.STAGE, stage);
                testMethod.evaluate();
            }
        };
    }

    /**
     * Here the resources bundles (if defined) are loaded into the local properties instance.
     *
     * @param test current unit test instance
     * @return the local properties instance
     * @throws IOException must not exists since we check the existence, maybe if one
     *                     of the properties files was broken
     */
    private Properties loadProperties(Object test, String stage, String publication) throws IOException {
        Properties properties = new Properties();
        Class<?> clazz = test.getClass();
        do {
            // check for resource bundles
            ResourceBundle resource = clazz.getAnnotation(ResourceBundle.class);
            if (resource != null) {
                for (String bundle : resource.value()) {
                    addResourceBundle(properties, clazz, bundle, stage, publication);
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));
        return properties;
    }

    private Properties addResourceBundle(Properties properties, Class<?> loader, String resource, String stage, String publication) throws IOException{
        Properties bundle = PropertyCache.getProperties(loader, resource, stage, publication);
        properties.putAll(bundle);
        return bundle;
    }

    private void injectProperty(Object test, String name, Object publication) throws IllegalArgumentException, IllegalAccessException {
        Class clazz = test.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                PropertyValue property = field.getAnnotation(PropertyValue.class);
                if (property != null && name.equals(property.value())) {
                    field.setAccessible(true);
                    field.set(test, publication);
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));
    }

    /**
     * Inject all values for fields annotated with {@link PropertyValue}.
     *
     * First we check if a system property with the name exists. Only if not we check the already loaded
     * properties instance for the key.
     *
     * @param test the test class instance to inject into
     * @param properties loaded properties instance
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void injectProperties(Object test, Properties properties) throws IllegalArgumentException, IllegalAccessException {
        Class clazz = test.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                PropertyValue propertyValue = field.getAnnotation(PropertyValue.class);
                if (propertyValue != null) {
                    String property = propertyValue.value();
                    // prefer system property
                    Object value = System.getProperty(property);
                    if (value == null) {
                        value = properties.getProperty(property); // might still be null
                    }
                    field.setAccessible(true);
                    Object typeSafeValue = convertValue(field, value);
                    field.set(test, typeSafeValue);
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));
    }

    /**
     * Since we read property files, we will always get strings. However to allow other types
     * to inject we check the field type here and try a conversion, at least for the basic
     * java types.
     *
     * @param field field we want to inject into
     * @param value the value (propably a string)
     * @return converted value matching the field type
     */
    private Object convertValue(Field field, Object value) {
        if (field.getType() == Integer.class || field.getType() == int.class) {
            return Integer.valueOf(value.toString());
        }
        if (field.getType() == Double.class || field.getType() == double.class) {
            return Double.valueOf(value.toString());
        }
        if (field.getType() == Float.class || field.getType() == float.class) {
            return Float.valueOf(value.toString());
        }
        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        if (field.getType() == Long.class || field.getType() == long.class) {
            return Long.valueOf(value.toString());
        }
        return value;
    }

    private String getStage() {
        return System.getProperty(Environment.STAGE);
    }

    private String getPublication() {
        return System.getProperty(Environment.PUBLICATION);
    }


}

