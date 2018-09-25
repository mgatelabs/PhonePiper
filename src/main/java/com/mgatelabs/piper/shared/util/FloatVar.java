package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/25/2018
 */
public class FloatVar implements Var {

    private final float data;

    public FloatVar(String data) {
        this.data = Float.parseFloat(data);
    }

    public FloatVar(float data) {
        this.data = data;
    }

    @Override
    public VarType getType() {
        return VarType.FLOAT;
    }

    @Override
    public Var add(Var other) {
        return new FloatVar(this.toFloat() + other.toFloat());
    }

    @Override
    public Var substract(Var other) {
        return new FloatVar(this.toFloat() - other.toFloat());
    }

    @Override
    public Var multiply(Var other) {
        return new FloatVar(this.toFloat() * other.toFloat());
    }

    @Override
    public Var divide(Var other) {
        return new FloatVar(this.toFloat() / other.toFloat());
    }

    @Override
    public Var mod(Var other) {
        throw new RuntimeException("You cannot mod a float based variable");
    }

    @Override
    public boolean greater(Var other) {
        return Float.compare(toFloat(), other.toFloat()) > 0;
    }

    @Override
    public boolean lesser(Var other) {
        return Float.compare(toFloat(), other.toFloat()) < 0;
    }

    @Override
    public boolean equals(Var other) {
        throw new RuntimeException("You cannot equals two float based variables");
    }

    @Override
    public float toFloat() {
        return data;
    }


    @Override
    public int toInt() {
        return (int)data;
    }

    @Override
    public Var asInt() {
        return null;
    }

    @Override
    public Var asFloat() {
        return this;
    }

    @Override
    public Var asString() {
        return new StringVar(toString());
    }

    @Override
    public String toString() {
        return Float.toString(data);
    }
}
