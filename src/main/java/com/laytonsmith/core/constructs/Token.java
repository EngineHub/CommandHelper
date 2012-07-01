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

    public enum TType {

        ADDITION, ALIAS_END, COMMA, COMMAND, CONCAT, DECREMENT, DEREFERENCE, DIVISION, EQUALS,
        EXPONENTIAL, FINAL_VAR, FUNC_END, FUNC_NAME, FUNC_START, GT, GTE, IDENTIFIER,
        INCREMENT, IVARIABLE, LABEL, LCURLY_BRACKET, LIT, LOGICAL_AND, LOGICAL_NOT, LOGICAL_OR, 
        LSQUARE_BRACKET, LT, LTE, MODULO, MULTILINE_END, MULTILINE_START, MULTIPLICATION, 
        NEWLINE, NOT_EQUALS, OPT_VAR_ASSIGN, RCURLY_BRACKET, ROOT, RSQUARE_BRACKET, SCOMMA, SEPERATOR,
        SLICE, SMART_STRING, STRICT_EQUALS, STRICT_NOT_EQUALS, STRING, SUBTRACTION, UNKNOWN, VARIABLE, WHITESPACE;

        public boolean isAdditive(){
            //String concatenation happens at the same level
            return (this == ADDITION || this == SUBTRACTION || this == CONCAT);
        }      

        public boolean isEquality(){
            return (this == STRICT_EQUALS || this == STRICT_NOT_EQUALS || this == EQUALS || this == NOT_EQUALS);
        }
        
        boolean isExponential() {
            return this == EXPONENTIAL;
        }

        public boolean isIdentifier() {
            return this == UNKNOWN || this == LIT || this == IVARIABLE 
                    || this == VARIABLE || this == FINAL_VAR || this == STRING
                    || this == SMART_STRING;
        }
        
        public boolean isLogicalAnd(){
            return this == LOGICAL_AND;
        }
        
        public boolean isLogicalOr(){
            return this == LOGICAL_OR;
        }
        
        public boolean isMultaplicative() {
            return (this == MULTIPLICATION || this == DIVISION || this == MODULO);
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
        
        public boolean isPlusMinus() {
            return this == ADDITION || this == SUBTRACTION;
        }
        
        public boolean isPostfix(){
            return (this == INCREMENT || this == DECREMENT);
        }

        public boolean isRelational(){
            return (this == LT || this == GT || this == LTE || this == GTE);
        }

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

        public boolean isUnary() {
            return (this == LOGICAL_NOT || this == ADDITION || this == SUBTRACTION
                    || this == INCREMENT || this == DECREMENT);
        }
        
    }
    public final int column;
    public final File file;
    public final int line_num;
    public final Target target;
    public TType type;

    public String value;

    public Token(TType type, String value, Target t) {
        this.type = type;
        this.value = value;
        this.line_num = t.line();
        this.file = t.file();
        this.column = t.col();
        this.target = t;
    }

    public boolean equals(Object o) {
        if (o instanceof Token) {
            Token t = (Token) o;
            return (this.type.equals(t.type) && this.value.equals(t.value));
        }
        return false;
    }

    public Target getTarget(){
        return target;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    public String toOutputString() {
        if (type.equals(TType.STRING)) {
            return value.replace("'", "\\'");
        }
        return value;
    }

    public String toSimpleString() {
        if (type.equals(TType.STRING)) {
            return "'" + value + "'";
        }
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
    
    public String val() {
        return value;
    }
}
