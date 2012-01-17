/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

/**
 *
 * @author layton
 */
public class Pair<A extends Object, B extends Object> {

    public final A fst;
    public final B snd;

    public Pair(A a, B b) {
        fst = a;
        snd = b;
    }

    @Override
    public String toString() {
        return "<" + fst.toString() + ", " + snd.toString() + ">";
    }
}
