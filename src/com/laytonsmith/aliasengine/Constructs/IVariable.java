/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

/**
 *
 * @author layton
 */
public class IVariable extends Variable{
    public IVariable(String name, int line_num){
        super(name, "", line_num);
        this.ctype = ConstructType.IVARIABLE;
    }
    public IVariable(String name, String value, int line_num){
        super(name, value, line_num);
        this.ctype = ConstructType.IVARIABLE;
    }
}
