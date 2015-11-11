package de.jaberu.test;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * This is a custom junit runner extending the default {@link BlockJUnit4ClassRunner}.
 *
 * With the runner environment-specific properties can be used in the test. To use those
 * properties the test class should define the resource to load from by {@link ResourceBundle}
 * annotation on class level. Properties of the test class can be marked with the
 * {@link PropertyValue} annotation to say what property value to inject.
 *
 * For each found annotation we serve properties in the following order. Assuming we defined a
 * resource bundle with the name <code>test</code>, the value is taken from:
 * <ol>
 *     <li>from the system properties directly</li>
 *     <li><code>&lt;{@value Environment#STAGE}&gt;/test.properties</code> from the classpath directly. That only works if the env was given as system property</li>
 *     <li><code>test.properties</code> from the classpath directly</li>
 * </ol>
 *
 * The runner also supports the custom {@link Ignore} annotation.
 *
 * Created by aherr on 09.11.2015.
 */
public class EnvironmentTestRunner extends BlockJUnit4ClassRunner {

    /**
     * local properties instance
     */
    private Properties properties = new Properties();

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
     * @param method test method
     * @param notifier used to ignore
     */
    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        Ignore ignore = method.getAnnotation(Ignore.class);
        if (ignore != null) {
            // check environment specific overrides
            String env = getEnvironment();
            for (String ignoreIn : ignore.value()) {
                if (ignoreIn.equals(env)) {
                    notifier.fireTestIgnored(description);
                    return;
                }
            }
        }
        super.runChild(method, notifier);
    }

    /**
     * Create a test by injecting properties marked with {@link PropertyValue}.
     *
     * @return test
     * @throws Exception from BlockJUnit4ClassRunner
     */
    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        loadProperties(test);
        injectProperties(test);
        return test;
    }

    private void injectProperties(Object test) throws IllegalArgumentException, IllegalAccessException {
        Class clazz = test.getClass();
        do {
            for (Field field : clazz.getDeclaredFields()) {
                PropertyValue property = field.getAnnotation(PropertyValue.class);
                if (property != null) {
                    Object value = resolvePropertyValue(property.value());
                    field.setAccessible(true);
                    field.set(test, value);
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));
    }

    /**
     * First we check if a system property with the name exists. Only if not we check the already loaded
     * properties instance for the key.
     *
     * @param property name of the property
     * @return property or null if not found
     */
    private Object resolvePropertyValue(String property) {
        Object systemProperty = System.getProperty(property);
        if (systemProperty != null) {
            return systemProperty;
        }
        return properties.getProperty(property); // might be null
    }

    /**
     * Here the resources bundles (if defined) are loaded into the local properties instance.
     *
     * @param test current unit test instance
     * @return the local properties instance
     * @throws IOException must not exists since we check the existence, maybe if one
     *  of the properties files was broken
     */
    private Properties loadProperties(Object test) throws IOException {
        Class<?> clazz = test.getClass();
        do {
            // check for resource bundles
            ResourceBundle resource = clazz.getAnnotation(ResourceBundle.class);
            if (resource != null) {
                for (String bundle : resource.value()) {
                    addResourceBundle(clazz, bundle);
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));
        return properties;
    }

    /**
     * Here we add a bundle by first loading the base bundle. Afterwards we check if an environment-specific bundle
     * exists. If existing we override the already loaded values from the base bundle.
     *
     * Since we assume that the resource is accessible by the same classloader like the test class, we use the
     * test class for loading the resource.
     *
     * @param loader the test class to load the resource with
     * @param resource the bundles name
     * @return local properties instance
     * @throws IOException from {@link Properties#load(InputStream)}
     */
    private Properties addResourceBundle(Class<?> loader, String resource) throws IOException {
        // loads the base resource
        StringBuilder builder = new StringBuilder(SLASH);
        builder.append(resource);
        builder.append(PROPERTIES);
        properties.load(loader.getResourceAsStream(builder.toString()));
        // check environment specific overrides
        String env = System.getProperty(Environment.STAGE);
        if (env != null) {
            builder = new StringBuilder(SLASH);
            builder.append(env.toLowerCase());
            builder.append(SLASH);
            builder.append(resource);
            builder.append(PROPERTIES);
            InputStream publicationSpecific = loader.getResourceAsStream(builder.toString());
            if (publicationSpecific != null) {
                // might be null if nothing specific for the environment exists
                properties.load(publicationSpecific);
            }
            String publication = System.getProperty(Environment.PUBLICATION);
            if (publication != null) {
                builder = new StringBuilder(SLASH);
                builder.append(env.toLowerCase());
                builder.append(SLASH);
                builder.append(publication.toLowerCase());
                builder.append(SLASH);
                builder.append(resource);
                builder.append(PROPERTIES);
                InputStream stageSpecific = loader.getResourceAsStream(builder.toString());
                if (stageSpecific != null) {
                    // might be null if nothing specific for the environment exists
                    properties.load(stageSpecific);
                }
            }
        }
        return properties;
    }

    private String getEnvironment() {
        return System.getProperty(Environment.STAGE);
    }

    private static final String SLASH = "/";
    private static final String PROPERTIES = ".properties";
}
