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
public class Token {

    public TType type;
    public String value;
    public final int line_num;
    public final int column;
    public final File file;
    public final Target target;

    public enum TType {

        UNKNOWN, LSQUARE_BRACKET, RSQUARE_BRACKET, OPT_VAR_ASSIGN, ALIAS_END, COMMA, SCOMMA, FUNC_NAME, FUNC_START,
        FUNC_END, STRING, NEWLINE, MULTILINE_START, MULTILINE_END, COMMAND, SEPERATOR, VARIABLE,
        IVARIABLE, FINAL_VAR, LIT, ROOT, LABEL, DEREFERENCE, SMART_STRING, SLICE, 
        MULTIPLICATION, SUBTRACTION, DIVISION, ADDITION, NOT_EQUALS, EQUALS, STRICT_NOT_EQUALS, 
        STRICT_EQUALS, GT, LT, LTE, GTE, LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT,
        INCREMENT, DECREMENT, MODULO, CONCAT, WHITESPACE, LCURLY_BRACKET, RCURLY_BRACKET, IDENTIFIER, EXPONENTIAL;

        public boolean isSymbol() {
            if(this.equals(TType.ADDITION) || this.equals(TType.SUBTRACTION) || this.equals(TType.MULTIPLICATION) 
                    || this.equals(TType.DIVISION) || this.equals(TType.EQUALS) || this.equals(TType.NOT_EQUALS) ||
                    this.equals(TType.STRICT_EQUALS) || this.equals(TType.STRICT_NOT_EQUALS) || this.equals(TType.GT) ||
                    this.equals(TType.LT) || this.equals(TType.GTE) || this.equals(TType.LTE)
                    || this.equals(TType.LOGICAL_AND) || this.equals(TType.LOGICAL_OR) || this.equals(TType.LOGICAL_NOT)
                    //|| this.equals(TType.BIT_AND) || this.equals(TType.BIT_OR) || this == BIT_XOR
                    || this == EXPONENTIAL
                    || this == INCREMENT || this == DECREMENT || this == MODULO || this == CONCAT){
                return true;
            } else {
                return false;
            }
        }      

        public boolean isPostfix(){
            return (this == INCREMENT || this == DECREMENT);
        }
        
        public boolean isUnary() {
            return (this == LOGICAL_NOT || this == ADDITION || this == SUBTRACTION
                    || this == INCREMENT || this == DECREMENT);
        }

        public boolean isMultaplicative() {
            return (this == MULTIPLICATION || this == DIVISION || this == MODULO);
        }
        
        public boolean isAdditive(){
            //String concatenation happens at the same level
            return (this == ADDITION || this == SUBTRACTION || this == CONCAT);
        }
        
        public boolean isRelational(){
            return (this == LT || this == GT || this == LTE || this == GTE);
        }
        
        public boolean isEquality(){
            return (this == STRICT_EQUALS || this == STRICT_NOT_EQUALS || this == EQUALS || this == NOT_EQUALS);
        }
        
//        public boolean isBitwiseAnd(){
//            return (this == BIT_AND);
//        }
//        
//        public boolean isBitwiseXor(){
//            return (this == BIT_XOR);
//        }
//        
//        public boolean isBitwiseOr(){
//            return (this == BIT_OR);
//        }
        
        public boolean isLogicalAnd(){
            return this == LOGICAL_AND;
        }
        
        public boolean isLogicalOr(){
            return this == LOGICAL_OR;
        }

        public boolean isPlusMinus() {
            return this == ADDITION || this == SUBTRACTION;
        }

        public boolean isIdentifier() {
            return this == UNKNOWN || this == LIT || this == IVARIABLE 
                    || this == VARIABLE || this == FINAL_VAR || this == STRING
                    || this == SMART_STRING;
        }

        boolean isExponential() {
            return this == EXPONENTIAL;
        }
        
    }

    public Token(TType type, String value, Target t) {
        this.type = type;
        this.value = value;
        this.line_num = t.line();
        this.file = t.file();
        this.column = t.col();
        this.target = t;
    }

    public String val() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object o) {
        if (o instanceof Token) {
            Token t = (Token) o;
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
    
    public Target getTarget(){
        return target;
    }
}
