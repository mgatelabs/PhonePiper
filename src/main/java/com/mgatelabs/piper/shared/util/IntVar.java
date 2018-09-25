package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;

/**
 * @author <a href="mailto:developer@mgatelabs.com">Michael Fuller</a>
 * Creation Date: 9/25/2018
 */
public class IntVar implements Var {

    public static final IntVar ZERO = new IntVar(0);

    private final int data;

    public IntVar(String data) {
        this.data = Integer.parseInt(data);
    }

    public IntVar(int data) {
        this.data = data;
    }

    @Override
    public VarType getType() {
        return VarType.INT;
    }

    @Override
    public Var add(Var other) {
        return new IntVar(this.toInt() + other.toInt());
    }

    @Override
    public Var substract(Var other) {
        return new IntVar(this.toInt() - other.toInt());
    }

    @Override
    public Var multiply(Var other) {
        return new IntVar(this.toInt() * other.toInt());
    }

    @Override
    public Var divide(Var other) {
        return new IntVar(this.toInt() / other.toInt());
    }

    @Override
    public Var mod(Var other) {
        return new IntVar(this.toInt() % other.toInt());
    }

    @Override
    public boolean greater(Var other) {
        return Integer.compare(toInt(), other.toInt()) > 0;
    }

    @Override
    public boolean lesser(Var other) {
        return Integer.compare(toInt(), other.toInt()) < 0;
    }

    @Override
    public boolean equals(Var other) {
        return Integer.compare(toInt(), other.toInt()) == 0;
    }

    @Override
    public float toFloat() {
        return (float) data;
    }


    @Override
    public int toInt() {
        return data;
    }

    @Override
    public Var asInt() {
        return this;
    }

    @Override
    public Var asFloat() {
        return new FloatVar(toFloat());
    }

    @Override
    public Var asString() {
        return new StringVar(toString());
    }

    @Override
    public String toString() {
        return Integer.toString(data);
    }
}
