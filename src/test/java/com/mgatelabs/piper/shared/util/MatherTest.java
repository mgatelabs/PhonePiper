package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/28/2018 for Phone-Piper
 */
public class MatherTest {

    @Test
    public void evaluate() {

        Var result;

        result = Mather.evaluate("(1+1)*(2+2)", VarType.INT);
        Assert.assertTrue(result.toInt() == 8);

        result = Mather.evaluate("1%2", VarType.INT);
        Assert.assertTrue(result.toInt() == 1);

        result = Mather.evaluate("0%2", VarType.INT);
        Assert.assertTrue(result.toInt() == 0);

        result = Mather.evaluate("1+2", VarType.INT);
        Assert.assertTrue(result.toInt() == 3);

        result = Mather.evaluate("1*2", VarType.INT);
        Assert.assertTrue(result.toInt() == 2);

        result = Mather.evaluate("10/2", VarType.INT);
        Assert.assertTrue(result.toInt() == 5);

        result = Mather.evaluate("(100/10)*(100/10)", VarType.INT);
        Assert.assertTrue(result.toInt() == 100);

        result = Mather.evaluate("-1", VarType.INT);
        Assert.assertTrue(result.toInt() == -1);

        result = Mather.evaluate("(-1) + 1", VarType.INT);
        Assert.assertTrue(result.toInt() == 0);

        result = Mather.evaluate("1/10", VarType.FLOAT);
        Assert.assertTrue(result.toFloat() > 0.099);
        Assert.assertTrue(result.toFloat() < 0.101);

        result = Mather.evaluate("(1/10) * 10", VarType.FLOAT);
        Assert.assertTrue(result.toFloat() > 0.99);
        Assert.assertTrue(result.toFloat() < 1.01);

        result = Mather.evaluate("Hello+World", VarType.STRING);
        Assert.assertTrue(result.toString().equals("HelloWorld"));
    }
}