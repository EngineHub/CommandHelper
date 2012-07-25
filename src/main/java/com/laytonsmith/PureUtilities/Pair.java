
package com.laytonsmith.PureUtilities;

import java.util.Map;

/**
 *
 * @author layton
 */
public class Pair<A, B> implements Map.Entry<A, B> {

    private final A fst;
    private B snd;

    public Pair(A a, B b) {
        fst = a;
        snd = b;
    }

    @Override
    public String toString() {
        return "<" + fst.toString() + ", " + snd.toString() + ">";
    }

    public A getKey() {
        return fst;
    }

    public B getValue() {
        return snd;
    }

    public B setValue(B value) {
        B old = snd;
        snd = (B)value;
        return old;
    }
}
