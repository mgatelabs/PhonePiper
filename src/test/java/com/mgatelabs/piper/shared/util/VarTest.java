package com.mgatelabs.piper.shared.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:developer@mgatelabs.com">Michael Fuller</a>
 * Creation Date: 9/25/2018
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
}