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
    Construct var_value;
    public IVariable(String name, int line_num){
        super(name, "", line_num);
        this.ctype = ConstructType.IVARIABLE;
    }
    public IVariable(String name, Construct value, int line_num){
        super(name, value.val(), line_num);
        this.var_value = value;
        this.ctype = ConstructType.IVARIABLE;
    }
    public Construct ival(){
        return var_value;
    }
}
