/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

import java.io.File;

/**
 *
 * @author layton
 */
public class Variable extends Construct {

    public String name;
    public String def;
    public boolean optional;
    public boolean final_var = false;

    public Variable(String name, String def, int line_num, File file) {
        super(name, ConstructType.VARIABLE, line_num, file);
        this.name = name;
        this.def = def;
    }

    @Override
    public String toString() {
        return "var:" + name;
    }
    public String getName(){
        return name;
    }
    @Override
    public String val(){
        return def;
    }
}
