package de.jaberu.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by aherr on 10.11.2015.
 */
@RunWith(EnvironmentTestRunner.class)
@ResourceBundle("test")
public class HaoEnvironmentTest {

    /**
     * store previous stage
     */
    static Object previousStage;

    /**
     * store previous publication
     */
    static Object previousPublication;

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
        previousStage = System.getProperties().setProperty(Environment.STAGE, "dev");
        previousPublication = System.getProperties().setProperty(Environment.PUBLICATION, "hao");
    }

    /**
     * Resets to previous environment (if any).
     */
    @AfterClass
    public static void unsetEnv() {
        if (previousPublication != null) {
            System.getProperties().setProperty(Environment.PUBLICATION, previousPublication.toString());
        } else {
            System.getProperties().remove(Environment.PUBLICATION);
        }

        if (previousStage != null) {
            System.getProperties().setProperty(Environment.STAGE, previousStage.toString());
        } else {
            System.getProperties().remove(Environment.STAGE);
        }
    }

    /**
     * Check if we got the overriden value from hao environment what is located under dev.
     * Note that the base value is tested in another test class.
     * @see EnvironmentTest#testValue()
     */
    @Test
    public void testValue() {
        Assert.assertEquals("hao", value);
    }
}
