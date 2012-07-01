/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;

/**
 *
 * @author layton
 */
public class Variable extends Construct {
    
    public static final long serialVersionUID = 1L;

    private String def;
    private boolean final_var;
    final private String name;
    private boolean optional;
    private Construct var_value;
    

    public Variable(String name, String def, boolean optional, boolean final_var, Target t) {
        super(name, ConstructType.VARIABLE, t);
        this.name = name;
        this.def = def;
        this.final_var = final_var;
        this.optional = optional;
        this.var_value = Static.resolveConstruct(def, t);
    }
    
    public Variable(String name, String def, Target t){
        this(name, def, false, false, t);
    }

    @Override
    public Variable clone() throws CloneNotSupportedException{
        Variable clone = (Variable) super.clone();
        if(this.var_value != null) clone.var_value = var_value;
        return clone;
    }
    public String getDefault(){
        return def;
    }
    public String getName(){
        return name;
    }
    @Override
    public boolean isDynamic() {
        return true;
    }
    public boolean isFinal(){
        return final_var;
    }
    public boolean isOptional(){
        return optional;
    }
    public void setDefault(String def){
        this.def = def;
    }
    public void setFinal(boolean final_var){
        this.final_var = final_var;
    }
    public void setOptional(boolean optional){
        this.optional = optional;
    }
    public void setVal(Construct val){
        this.var_value = val;
    }
    @Override
    public String toString() {
        return "var:" + name;
    }

    @Override
    public String val(){
        return var_value.toString();
    }

}
