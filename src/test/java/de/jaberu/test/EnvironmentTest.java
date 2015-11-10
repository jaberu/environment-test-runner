package de.jaberu.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test to verify that the injection if properties into test classes works.
 *  *
 * Created by aherr on 09.11.2015.
 * @see EnvironmentTestRunner
 */
@RunWith(EnvironmentTestRunner.class)
@ResourceBundle("test")
public class EnvironmentTest {

    /**
     * store previous environment
     */
    static Object previousEnv;


    /**
     * injection to test
     */
    @PropertyValue("test.key")
    public String value;

    /**
     * Set the dev environment for testing
     */
    @BeforeClass
    public static void setEnv() {
        previousEnv = System.getProperties().remove(Environment.STAGE);
    }

    /**
     * Resets to previous environment (if any).
     */
    @AfterClass
    public static void unsetEnv() {
        if (previousEnv != null) {
            System.getProperties().setProperty(Environment.STAGE, previousEnv.toString());
        }
    }

    /**
     * Simple test to make sure that the value was initialized by the {@link PropertyValue} annotation.
     */
    @Test
    public void testValue() {
        Assert.assertEquals("base", value);
    }
}
