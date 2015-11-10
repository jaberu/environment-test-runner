package de.jaberu.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Here we test if properties from custom environments are loaded.
 *
 * We set the system property directly before we run the test. Take
 * care that this test must run in a separate test class since the
 * properties are loaded per test.
 *
 * Created by aherr on 09.11.2015.
 */
@RunWith(EnvironmentTestRunner.class)
@ResourceBundle("test")
public class DevEnvironmentTest {

    /**
     * store previous environment
     */
    static Object previousEnv;

    /**
     * injection to test
     */
    @PropertyValue("test.key")
    private String value;

    /**
     * Set the dev environment for testing
     */
    @BeforeClass
    public static void setEnv() {
        previousEnv = System.getProperties().setProperty(Environment.STAGE, "dev");
    }

    /**
     * Resets to previous environment (if any).
     */
    @AfterClass
    public static void unsetEnv() {
        if (previousEnv != null) {
            System.getProperties().setProperty(Environment.STAGE, previousEnv.toString());
        } else {
            System.getProperties().remove(Environment.STAGE);
        }
    }

    /**
     * Check if we got the overriden value from dev environment.
     * Note that the base value is tested in another test class.
     * @see EnvironmentTest#testValue()
     */
    @Test
    public void testValue() {
        Assert.assertEquals("dev", value);
    }

    /**
     * Tests the ignore annotation.
     * Since we set the dev profile here, it should not be executed. Therefore the test just fails.
     */
    @Test
    @Ignore("dev")
    public void testIngore() {
        Assert.fail("ignore does not work");
    }
}
