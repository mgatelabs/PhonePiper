package com.mgatelabs.piper.shared.util;

import com.google.common.base.Preconditions;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/25/2018 for Phone-Piper
 */
public class VarTest {

    @Test
    public void add() {
        Var a = new IntVar(0);
        Var b = new IntVar(0);
        Assert.assertTrue(a.add(b).toInt() == 0);

        a = new IntVar(1);
        b = new IntVar(2);
        Assert.assertTrue(a.add(b).toInt() == 3);

        a = new IntVar(0);
        b = new IntVar(1);
        Assert.assertTrue(a.add(b).toInt() == 1);

        a = new IntVar(1);
        b = new IntVar(0);
        Assert.assertTrue(a.add(b).toInt() == 1);
    }

    @Test
    public void substract() {
        Var a = new IntVar(0);
        Var b = new IntVar(0);
        Assert.assertTrue(a.substract(b).toInt() == 0);

        a = new IntVar(1);
        b = new IntVar(2);
        Assert.assertTrue(a.substract(b).toInt() == -1);

        a = new IntVar(0);
        b = new IntVar(1);
        Assert.assertTrue(a.substract(b).toInt() == -1);

        a = new IntVar(1);
        b = new IntVar(0);
        Assert.assertTrue(a.substract(b).toInt() == 1);
    }

    @Test
    public void multiply() {
        Var a = new IntVar(0);
        Var b = new IntVar(0);
        Assert.assertTrue(a.multiply(b).toInt() == 0);

        a = new IntVar(2);
        b = new IntVar(2);
        Assert.assertTrue(a.multiply(b).toInt() == 4);

        a = new IntVar(0);
        b = new IntVar(1);
        Assert.assertTrue(a.multiply(b).toInt() == 0);

        a = new IntVar(1);
        b = new IntVar(0);
        Assert.assertTrue(a.multiply(b).toInt() == 0);
    }

    @Test
    public void divide() {
    }

    @Test
    public void mod() {
        Var a = new IntVar(0);
        Var b = new IntVar(1);
        Assert.assertTrue(a.mod(b).toInt() == 0);

        a = new IntVar(1);
        b = new IntVar(2);
        Assert.assertTrue(a.mod(b).toInt() == 1);

        a = new IntVar(3);
        b = new IntVar(2);
        Assert.assertTrue(a.mod(b).toInt() == 1);

        a = new IntVar(2);
        b = new IntVar(2);
        Assert.assertTrue(a.mod(b).toInt() == 0);
    }

    @Test
    public void greater() {
        Var a = new IntVar(0);
        Var b = new IntVar(0);
        Assert.assertTrue(!a.greater(b));

        a = new IntVar(1);
        b = new IntVar(0);
        Assert.assertTrue(a.greater(b));

        a = new IntVar(0);
        b = new IntVar(1);
        Assert.assertTrue(!a.greater(b));
    }

    @Test
    public void lesser() {
        Var a = new IntVar(0);
        Var b = new IntVar(0);
        Assert.assertTrue(!a.lesser(b));

        a = new IntVar(1);
        b = new IntVar(0);
        Assert.assertTrue(!a.lesser(b));

        a = new IntVar(0);
        b = new IntVar(1);
        Assert.assertTrue(a.lesser(b));
    }

    @Test
    public void equals() {
        Var a = new IntVar(0);
        Var b = new IntVar(0);
        Assert.assertTrue(a.equals(b));

        a = new IntVar(1);
        b = new IntVar(0);
        Assert.assertTrue(!a.equals(b));

        a = new IntVar(0);
        b = new IntVar(1);
        Assert.assertTrue(!a.equals(b));
    }

    @Test
    public void conversions() {
        Var a, b;

        // From Int

        a = new IntVar(1);
        b = a.asInt();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toInt() == 1);

        b = a.asFloat();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toFloat() > 0.999f);
        Assert.assertTrue(b.toFloat() < 1.0001f);

        b = a.asString();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toString().equalsIgnoreCase("1"));

        // From String

        a = new StringVar("2");
        b = a.asInt();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toInt() == 2);

        b = a.asFloat();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toFloat() > 1.999f);
        Assert.assertTrue(b.toFloat() < 2.0001f);

        b = a.asString();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toString().equalsIgnoreCase("2"));

        // From Float

        a = new FloatVar(3.0f);
        b = a.asInt();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toInt() == 3);

        b = a.asFloat();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toFloat() > 2.999f);
        Assert.assertTrue(b.toFloat() < 3.0001f);

        b = a.asString();
        Assert.assertNotNull(b);
        Assert.assertTrue(b.toString().startsWith("3"));
    }
}