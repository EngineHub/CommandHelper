/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import java.io.Serializable;

/**
 *
 * @author layton
 */
public class Construct implements Serializable{
    
    public enum ConstructType{
        TOKEN, COMMAND, FUNCTION, VARIABLE, LITERAL, ARRAY, MAP, ENTRY, INT, DOUBLE, BOOLEAN, NULL, STRING, VOID, IVARIABLE
    }

    public ConstructType ctype;
    protected String value = "";
    public int line_num;

    public Construct(String value, ConstructType ctype, int line_num){
        this.value = value;
        this.ctype = ctype;
        this.line_num = line_num;
    }

    public String val(){
        return value;
    }
    
    @Override
    public String toString(){
        return value;
    }
}
