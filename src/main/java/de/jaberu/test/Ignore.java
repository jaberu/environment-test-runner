package de.jaberu.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a custom ignore annotation allowing to define a list of environments where a test method
 * should be ignored. The method will run in any environment except those mentioned in the value list.
 *
 * This annotation only works if the {@link EnvironmentTestRunner} is used.
 *
 * Created by aherr on 09.11.2015.
 * @see EnvironmentTestRunner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Ignore {

    /**
     * @return the environments were the test should be ignored
     */
    String[] value();
}
