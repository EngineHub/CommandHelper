/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author layton
 */
public class Construct implements Serializable{
    
    public static final long serialVersionUID = 1L;
    
    public enum ConstructType{
        TOKEN, COMMAND, FUNCTION, VARIABLE, LITERAL, ARRAY, MAP, ENTRY, INT, DOUBLE, BOOLEAN, NULL, STRING, VOID, IVARIABLE, CLOSURE
    }

    public ConstructType ctype;
    protected String value = "";
    public int line_num;
    transient public File file;

    public Construct(String value, ConstructType ctype, int line_num, File file){
        this.value = value;
        this.ctype = ctype;
        this.line_num = line_num;
        this.file = file;
    }

    public String val(){
        return value;
    }
    
    @Override
    public String toString(){
        return value;
    }
}
