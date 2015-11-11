package de.jaberu.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Here the resource bundle to load is configured.
 *
 * The value is the location of the resource without any protocol.
 * That resource bundle is taken in order to look for test specific
 * properties.
 *
 * Bundles can be overridden for different environments.
 *
 * This annotation only works if the {@link EnvironmentTestRunner} is used.
 *
 * Created by aherr on 09.11.2015.
 * @see EnvironmentTestRunner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceBundle {

    /**
     * @return the bundle name (without .properties suffix!)
     */
    String[] value();
}
