/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.constructs;

import java.io.File;

/**
 *
 * @author layton
 */
public class IVariable extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    private Construct var_value;
    final private String name;

    public IVariable(String name, int line_num, File file){
        super(name, ConstructType.IVARIABLE, line_num, file);
        this.var_value = new CString("", line_num, file);
        this.name = name;
    }
    public IVariable(String name, Construct value, int line_num, File file){
        super(name, ConstructType.IVARIABLE, line_num, file);
        this.var_value = value;
        this.name = name;
    }
    @Override
    public String val(){
        return var_value.val();
    }
    public Construct ival(){
        return var_value;
    }
    public String getName(){
        return name;
    }
    public void setIval(Construct c){
        var_value = c;
    }

    @Override
    public String toString() {
        return this.name + ":(" + this.ival().getClass().getSimpleName() + ") '" + this.ival().val() + "'";
    }
    
    
    @Override
    public IVariable clone() throws CloneNotSupportedException{
        IVariable clone = (IVariable) super.clone();
        if(this.var_value != null) clone.var_value = this.var_value.clone();
        return (IVariable) clone;
    }

}
