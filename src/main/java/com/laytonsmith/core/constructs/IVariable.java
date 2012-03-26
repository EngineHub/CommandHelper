/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.constructs;

/**
 *
 * @author layton
 */
public class IVariable extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    private Construct var_value;
    final private String name;

    public IVariable(String name, Target t){
        super(name, ConstructType.IVARIABLE, t);
        this.var_value = new CString("", t);
        this.name = name;
    }
    public IVariable(String name, Construct value, Target t){
        super(name, ConstructType.IVARIABLE, t);
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

    @Override
    public boolean isDynamic() {
        return true;
    }

}
