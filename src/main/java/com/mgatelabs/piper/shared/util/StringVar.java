package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/25/2018
 */
public class StringVar implements Var {

    private final String data;

    public StringVar(String data) {
        this.data = data;
    }

    @Override
    public VarType getType() {
        return VarType.STRING;
    }

    @Override
    public Var add(Var other) {
        return new StringVar(this.toString() + other.toString());
    }

    @Override
    public Var substract(Var other) {
        throw new RuntimeException("You cannot subtract a String based variable");
    }

    @Override
    public Var multiply(Var other) {
        throw new RuntimeException("You cannot multiply a String based variable");
    }

    @Override
    public Var divide(Var other) {
        throw new RuntimeException("You cannot divide a String based variable");
    }

    @Override
    public Var mod(Var other) {
        throw new RuntimeException("You cannot mod a String based variable");
    }

    @Override
    public boolean greater(Var other) {
        return toString().compareTo(other.toString()) > 0;
    }

    @Override
    public boolean lesser(Var other) {
        return toString().compareTo(other.toString()) < 0;
    }

    @Override
    public boolean equals(Var other) {
        return toString().compareTo(other.toString()) == 0;
    }

    @Override
    public float toFloat() {
        return Float.parseFloat(data);
    }


    @Override
    public int toInt() {
        return Integer.parseInt(data);
    }

    @Override
    public Var asInt() {
        return new IntVar(data);
    }

    @Override
    public Var asFloat() {
        return new FloatVar(data);
    }

    @Override
    public Var asString() {
        return this;
    }

    @Override
    public String toString() {
        return data;
    }
}
