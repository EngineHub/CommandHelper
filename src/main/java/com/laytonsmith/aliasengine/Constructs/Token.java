/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

/**
 *
 * @author layton
 */
public class Token{
    public TType type;
    public String value;
    public int line_num;
    
    public enum TType{
        UNKNOWN, OPT_VAR_START, OPT_VAR_END, OPT_VAR_ASSIGN, ALIAS_END, COMMA, FUNC_NAME, FUNC_START,
        FUNC_END, STRING, NEWLINE, MULTILINE_START, MULTILINE_END, COMMAND, SEPERATOR, VARIABLE,
        IVARIABLE, FINAL_VAR, LIT, ROOT, IDENT
    }
    public Token(TType type, String value, int line_num) {
        this.type = type;
        this.value = value;
        this.line_num = line_num;
    }
    
    public String val(){
        return value;
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
