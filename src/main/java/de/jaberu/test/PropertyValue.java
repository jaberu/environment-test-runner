package de.jaberu.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A unit test can mark on of its properties with this annotation to force
 * initialization from an external source.
 *
 * Per default the external source is only the system properties. However
 * by using the {@link ResourceBundle} annotation on the enclosing test,
 * custom resource locations can be defined. See {@link EnvironmentTest}
 * for a running example.
 *
 * This annotation only works if the {@link EnvironmentTestRunner} is used.
 *
 * Created by aherr on 09.11.2015.
 * @see EnvironmentTestRunner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyValue {

    /**
     * @return property key
     */
    String value();

}
