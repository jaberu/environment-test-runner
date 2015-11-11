package de.jaberu.test;

/**
 * Created by aherr on 11.11.2015.
 */

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the basic java types.
 */
@RunWith(EnvironmentTestRunner.class)
@ResourceBundle("test")
public class TypesTest {

    /**
     * test.int=1
     */
    @PropertyValue("test.int")
    private int testInt;

    /**
     * test.bool=true
     */
    @PropertyValue("test.bool")
    private boolean testBool;

    /**
     * test.float=1.2
     */
    @PropertyValue("test.float")
    private float testFloat;

    /**
     * test.double=1.234e2
     */
    @PropertyValue("test.double")
    private double testDouble;

    /**
     * test.long=1234567890
     */
    @PropertyValue("test.long")
    private long testLong;

    @Test
    public void testInt() {
        Assert.assertEquals(1, testInt);
    }

    @Test
    public void testBool() {
        Assert.assertEquals(true, testBool);
    }

    @Test
    public void testFloat() {
        Assert.assertEquals(1.2f, testFloat, 0.00001);
    }

    @Test
    public void testDouble() {
        Assert.assertEquals(1.234e2, testDouble, 0.00001);
    }

    @Test
    public void testLong() {
        Assert.assertEquals(1234567890l, testLong);
    }
}
