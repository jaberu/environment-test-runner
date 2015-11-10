package de.jaberu.test;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is a prototypic usage of the new environment variables.
 * Created by aherr on 10.11.2015.
 */
@RunWith(EnvironmentTestRunner.class)
@ResourceBundle("test")
public class DummyEnvironmentTest {

    @PropertyValue(Environment.PUBLICATION)
    private String publication;

    @PropertyValue(Environment.STAGE)
    private String stage;

    @PropertyValue("test.key")
    private String testKey;

    /**
     * Just write the config to test it in jenkins log
     */
    @Test
    public void testEnvironment() {
        System.out.println("DummyEnvironmentTest#testEnvironment----begin");
        System.out.println("publication: " + publication);
        System.out.println("stage: " + stage);
        System.out.println("testKey: " + testKey);
        System.out.println("DummyEnvironmentTest#testEnvironment----end");
    }
}
