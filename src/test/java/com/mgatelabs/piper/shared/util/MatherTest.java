package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/28/2018.
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
    }
}