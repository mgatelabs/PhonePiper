package com.mgatelabs.piper.shared.util;

import com.mgatelabs.piper.shared.details.VarType;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by @mgatelabs (Michael Fuller) on 9/28/2018.
 */
public class Mather {

    public static Var evaluate(String input, VarType expectedType) {

        // Handle Parentheses
        while (input.contains("(")) {
            int startingIndex = input.indexOf("(");
            int stack = 0;
            // Modify to remove parentheses
            for (int i = startingIndex + 1; i < input.length(); i++) {
                if (input.charAt(i) == '(') {
                    stack++;
                } else if (input.charAt(i) == ')') {
                    if (stack > 0) {
                        stack--;
                    } else if (stack < 0) {
                      throw new RuntimeException("Too many )'s in expression");
                    } else {
                        String inner = input.substring(startingIndex+1, i);
                        Var innerValue = evaluate(inner, expectedType);
                        input = input.substring(0, startingIndex) + innerValue.toString() + input.substring(i+1);
                        break;
                    }
                }
            }
        }

        if (input.contains("*")) {
            int multiplyIndex = input.indexOf('*');
            String left = input.substring(0, multiplyIndex);
            String right = input.substring(multiplyIndex + 1);
            Var l = evaluate(left, expectedType);
            Var r = evaluate(right, expectedType);
            return l.multiply(r);
        }

        if (input.contains("/")) {
            int multiplyIndex = input.indexOf('/');
            String left = input.substring(0, multiplyIndex);
            String right = input.substring(multiplyIndex + 1);
            Var l = evaluate(left, expectedType);
            Var r = evaluate(right, expectedType);
            return l.divide(r);
        }

        if (input.contains("+")) {
            int multiplyIndex = input.indexOf('+');
            String left = input.substring(0, multiplyIndex);
            String right = input.substring(multiplyIndex + 1);
            Var l = evaluate(left, expectedType);
            Var r = evaluate(right, expectedType);
            return l.add(r);
        }

        if (input.contains("-")) {
            int multiplyIndex = input.indexOf('-');
            String left = input.substring(0, multiplyIndex);
            String right = input.substring(multiplyIndex + 1);
            Var l = evaluate(left, expectedType);
            Var r = evaluate(right, expectedType);
            return l.substract(r);
        }

        if (input.contains("%")) {
            int multiplyIndex = input.indexOf('%');
            String left = input.substring(0, multiplyIndex);
            String right = input.substring(multiplyIndex + 1);
            Var l = evaluate(left, expectedType);
            Var r = evaluate(right, expectedType);
            return l.mod(r);
        }

        input = input.trim();

        if (StringUtils.isNotBlank(input)) {
            return expectedType.cast(new StringVar(input));
        }

        return IntVar.ZERO;
    }

}
