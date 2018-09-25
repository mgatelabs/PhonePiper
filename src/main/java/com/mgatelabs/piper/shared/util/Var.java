package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;

/**
 * @author <a href="mailto:mfuller@acteksoft.com">Michael Fuller</a>
 * Creation Date: 9/25/2018
 */
public interface Var {

    VarType getType();

    Var add(final Var other);
    Var substract(final Var other);
    Var multiply(final Var other);
    Var divide(final Var other);
    Var mod(final Var other);

    boolean greater(final Var other);
    boolean lesser(final Var other);
    boolean equals(final Var other);

    // Raw values
    @Override
    String toString();
    float toFloat();
    int toInt();

    // Keep it in the family
    Var asInt();
    Var asFloat();
    Var asString();
}
