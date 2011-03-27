/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions;

/**
 *
 * @author layton
 */
public interface Function {
    public int numArgs();
    public Construct exec(Construct ... args);
}
