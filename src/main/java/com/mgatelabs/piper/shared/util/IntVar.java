package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;

/**
 *
 * Created by @mgatelabs (Michael Fuller) on 9/25/2018
 */
public class IntVar implements Var {

    public static final IntVar ZERO = new IntVar(0);
    public static final IntVar ONE = new IntVar(1);
    public static final IntVar TWO = new IntVar(2);
    public static final IntVar THREE = new IntVar(3);
    public static final IntVar FOUR = new IntVar(4);
    public static final IntVar FIVE = new IntVar(5);
    public static final IntVar TEN = new IntVar(10);
    public static final IntVar THOUSAND = new IntVar(1000);

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
