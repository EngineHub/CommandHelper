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
public class Token{
    public TType type;
    public String value;
    public int line_num;
    public File file;
    
    public enum TType{
        UNKNOWN, LSQUARE_BRACKET, RSQUARE_BRACKET, OPT_VAR_ASSIGN, ALIAS_END, COMMA, FUNC_NAME, FUNC_START,
        FUNC_END, STRING, NEWLINE, MULTILINE_START, MULTILINE_END, COMMAND, SEPERATOR, VARIABLE,
        IVARIABLE, FINAL_VAR, LIT, ROOT, IDENT
    }
    public Token(TType type, String value, int line_num, File file) {
        this.type = type;
        this.value = value;
        this.line_num = line_num;
        this.file = file;
    }
    
    public String val(){
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
    
    public boolean equals(Object o){
        if(o instanceof Token){
            Token t = (Token)o;
            return (this.type.equals(t.type) && this.value.equals(t.value));
        }
        return false;
    }

    @Override
    public String toString() {
        if (type.equals(TType.NEWLINE)) {
            return "newline";
        }
        if (type.equals(TType.STRING)) {
            return "string:'" + value + "'";
        }
        return type + ":" + value;
    }

    public String toSimpleString() {
        if (type.equals(TType.STRING)) {
            return "'" + value + "'";
        }
        return value;
    }

    public String toOutputString() {
        if (type.equals(TType.STRING)) {
            return value.replace("'", "\\'");
        }
        return value;
    }
}
