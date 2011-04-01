/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

/**
 *
 * @author layton
 */
public class CFunction extends Construct {

    public CFunction(String name, int line_num) {
        super(TType.CONSTRUCT, name, ConstructType.FUNCTION, line_num);
    }

    public String toString() {
        return this.value;
    }
}
