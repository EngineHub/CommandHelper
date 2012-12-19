

package com.laytonsmith.core.constructs;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

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

	private enum TokenVariant{
		ADDITIVE, EQUALITY, EXPONENTIAL, IDENTIFIER, LOGICAL_AND, LOGICAL_OR, 
		MULTIPLICATIVE, PLUS_MINUS, POSTFIX, RELATIONAL, SYMBOL, UNARY, ASSIGNMENT
	}
    public enum TType {

		//TODO: Comment this out once the compiler is replaced, and clean up unused ones
        UNKNOWN(TokenVariant.IDENTIFIER), 
		LSQUARE_BRACKET(), 
		RSQUARE_BRACKET(), 
		OPT_VAR_ASSIGN(),
		ALIAS_END(),
		COMMA(),
		SCOMMA(),
		FUNC_NAME(),
		FUNC_START,
        FUNC_END(),
		NEWLINE(),
		MULTILINE_START(),
		MULTILINE_END(),
		COMMAND(),
		SEPERATOR(),
		STRING(TokenVariant.IDENTIFIER),
		VARIABLE(TokenVariant.IDENTIFIER),
        IVARIABLE(TokenVariant.IDENTIFIER),
		FINAL_VAR(TokenVariant.IDENTIFIER),
		LIT(TokenVariant.IDENTIFIER),
		SMART_STRING(TokenVariant.IDENTIFIER),
		BARE_STRING(TokenVariant.IDENTIFIER),
		ROOT(),
		LABEL(),
		DEREFERENCE(),
		SLICE(),
		
		PLUS(TokenVariant.SYMBOL, TokenVariant.UNARY, TokenVariant.ADDITIVE, TokenVariant.PLUS_MINUS),
		MINUS(TokenVariant.SYMBOL, TokenVariant.UNARY, TokenVariant.ADDITIVE, TokenVariant.PLUS_MINUS),
        MULTIPLICATION(TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
		DIVISION(TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
		EQUALS(TokenVariant.SYMBOL, TokenVariant.EQUALITY),
		NOT_EQUALS(TokenVariant.SYMBOL, TokenVariant.EQUALITY),
        STRICT_EQUALS(TokenVariant.SYMBOL, TokenVariant.EQUALITY),
		STRICT_NOT_EQUALS(TokenVariant.SYMBOL, TokenVariant.EQUALITY),
		
		GT(TokenVariant.SYMBOL, TokenVariant.RELATIONAL),
		LT(TokenVariant.SYMBOL, TokenVariant.RELATIONAL),
		LTE(TokenVariant.SYMBOL, TokenVariant.RELATIONAL),
		GTE(TokenVariant.SYMBOL, TokenVariant.RELATIONAL),
		LOGICAL_AND(TokenVariant.SYMBOL, TokenVariant.LOGICAL_AND),
		LOGICAL_OR(TokenVariant.SYMBOL, TokenVariant.LOGICAL_OR),
		LOGICAL_NOT(TokenVariant.SYMBOL, TokenVariant.UNARY),
        INCREMENT(TokenVariant.SYMBOL, TokenVariant.POSTFIX, TokenVariant.UNARY),
		DECREMENT(TokenVariant.SYMBOL, TokenVariant.POSTFIX, TokenVariant.UNARY),
		MODULO(TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
		CONCAT(TokenVariant.SYMBOL, TokenVariant.ADDITIVE),
		EXPONENTIAL(TokenVariant.SYMBOL, TokenVariant.EXPONENTIAL),
		
		WHITESPACE(),
		LCURLY_BRACKET(),
		RCURLY_BRACKET(),
		IDENTIFIER(),
		
		DOUBLE(TokenVariant.IDENTIFIER),
		INTEGER(TokenVariant.IDENTIFIER),
		CONST_START(),
		ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL), 
		PLUS_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.ADDITIVE, TokenVariant.PLUS_MINUS),
		MINUS_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.ADDITIVE, TokenVariant.PLUS_MINUS),
		MULTIPLICATION_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
		DIVISION_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE), 
		CONCAT_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.ADDITIVE);
		
		private Set<TokenVariant> variants = EnumSet.noneOf(TokenVariant.class);
		private TType(TokenVariant ... variants){
			this.variants.addAll(Arrays.asList(variants));
		}

        public boolean isSymbol() {
            return this.variants.contains(TokenVariant.SYMBOL);
        }      

        public boolean isPostfix(){
            return this.variants.contains(TokenVariant.POSTFIX);
        }
        
        public boolean isUnary() {
            return this.variants.contains(TokenVariant.UNARY);
        }

        public boolean isMultaplicative() {
            return this.variants.contains(TokenVariant.MULTIPLICATIVE);
        }
        
        public boolean isAdditive(){
            return this.variants.contains(TokenVariant.ADDITIVE);
        }
        
        public boolean isRelational(){
            return this.variants.contains(TokenVariant.RELATIONAL);
        }
        
        public boolean isEquality(){
            return this.variants.contains(TokenVariant.EQUALITY);
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
            return this.variants.contains(TokenVariant.LOGICAL_AND);
        }
        
        public boolean isLogicalOr(){
            return this.variants.contains(TokenVariant.LOGICAL_OR);          
        }

        public boolean isPlusMinus() {
            return this.variants.contains(TokenVariant.PLUS_MINUS);
        }

        public boolean isIdentifier() {
            return this.variants.contains(TokenVariant.IDENTIFIER);
        }

        public boolean isExponential() {
            return this.variants.contains(TokenVariant.EXPONENTIAL);
        }
		
		public boolean isAssignment(){
			return this.variants.contains(TokenVariant.ASSIGNMENT);
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

    @Override
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
            return "\\n";
        }
        if (type.equals(TType.STRING)) {
            return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
        }
        return value;
    }

    public String toSimpleString() {
        if (type.equals(TType.STRING)) {
            return "'" + value + "'";
        }
        return value;
    }

    public String toOutputString() {
        if (type.equals(TType.STRING)) {
            return value.replace("\\", "\\\\").replace("'", "\\'");
        }
        return value;
    }
    
    public Target getTarget(){
        return target;
    }
}
