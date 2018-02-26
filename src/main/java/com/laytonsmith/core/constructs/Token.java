package com.laytonsmith.core.constructs;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 *
 */
public class Token {

    public TType type;
    public String value;
    public final int line_num;
    public final int column;
    public final File file;
    public final Target target;

    private enum TokenVariant {
	ADDITIVE, EQUALITY, EXPONENTIAL, IDENTIFIER, LOGICAL_AND, LOGICAL_OR, DEFAULT_AND, DEFAULT_OR,
	MULTIPLICATIVE, PLUS_MINUS, POSTFIX, RELATIONAL, SYMBOL, UNARY, ASSIGNMENT,
	SEPARATOR, ATOMIC_LIT, WHITESPACE, KEYWORD, COMMENT
    }

    public enum TType {

	//TODO: Comment this out once the compiler is replaced, and clean up unused ones
	UNKNOWN(TokenVariant.IDENTIFIER),
	LSQUARE_BRACKET(TokenVariant.SEPARATOR),
	RSQUARE_BRACKET(TokenVariant.SEPARATOR),
	OPT_VAR_ASSIGN(TokenVariant.SEPARATOR),
	ALIAS_END(TokenVariant.SEPARATOR),
	COMMA(TokenVariant.SEPARATOR),
	FUNC_NAME(),
	FUNC_START(TokenVariant.SEPARATOR),
	FUNC_END(TokenVariant.SEPARATOR),
	NEWLINE(TokenVariant.WHITESPACE),
	MULTILINE_START(TokenVariant.SEPARATOR),
	MULTILINE_END(TokenVariant.SEPARATOR),
	COMMENT(TokenVariant.COMMENT),
	SMART_COMMENT(TokenVariant.COMMENT),
	COMMAND(),
	SEPERATOR(TokenVariant.SEPARATOR),
	STRING(TokenVariant.IDENTIFIER, TokenVariant.ATOMIC_LIT),
	VARIABLE(TokenVariant.IDENTIFIER),
	IVARIABLE(TokenVariant.IDENTIFIER),
	FINAL_VAR(TokenVariant.IDENTIFIER),
	LIT(TokenVariant.IDENTIFIER, TokenVariant.ATOMIC_LIT),
	DOT(TokenVariant.IDENTIFIER),
	SMART_STRING(TokenVariant.IDENTIFIER, TokenVariant.ATOMIC_LIT),
	BARE_STRING(TokenVariant.IDENTIFIER, TokenVariant.ATOMIC_LIT),
	ROOT(),
	LABEL(TokenVariant.SEPARATOR),
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
	DEFAULT_AND(TokenVariant.SYMBOL, TokenVariant.DEFAULT_AND),
	DEFAULT_OR(TokenVariant.SYMBOL, TokenVariant.DEFAULT_OR),
	LOGICAL_NOT(TokenVariant.SYMBOL, TokenVariant.UNARY),
	INCREMENT(TokenVariant.SYMBOL, TokenVariant.POSTFIX, TokenVariant.UNARY),
	DECREMENT(TokenVariant.SYMBOL, TokenVariant.POSTFIX, TokenVariant.UNARY),
	MODULO(TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
	CONCAT(TokenVariant.SYMBOL, TokenVariant.ADDITIVE),
	EXPONENTIAL(TokenVariant.SYMBOL, TokenVariant.EXPONENTIAL),
	WHITESPACE(TokenVariant.WHITESPACE),
	LCURLY_BRACKET(TokenVariant.SEPARATOR),
	RCURLY_BRACKET(TokenVariant.SEPARATOR),
	IDENTIFIER(),
	DOUBLE(TokenVariant.IDENTIFIER, TokenVariant.ATOMIC_LIT),
	INTEGER(TokenVariant.IDENTIFIER, TokenVariant.ATOMIC_LIT),
	CONST_START(),
	ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL),
	PLUS_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.ADDITIVE, TokenVariant.PLUS_MINUS),
	MINUS_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.ADDITIVE, TokenVariant.PLUS_MINUS),
	MULTIPLICATION_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
	DIVISION_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.MULTIPLICATIVE),
	CONCAT_ASSIGNMENT(TokenVariant.ASSIGNMENT, TokenVariant.SYMBOL, TokenVariant.ADDITIVE),
	SEMICOLON(TokenVariant.SEPARATOR),
	KEYWORD(TokenVariant.KEYWORD),
	FILE_OPTIONS_START(TokenVariant.SEPARATOR),
	FILE_OPTIONS_STRING(),
	FILE_OPTIONS_END(TokenVariant.SEPARATOR);

	private final Set<TokenVariant> variants = EnumSet.noneOf(TokenVariant.class);

	private TType(TokenVariant... variants) {
	    this.variants.addAll(Arrays.asList(variants));
	}

	/**
	 * True if the token is a symbol, i.e. + - =
	 *
	 * @return
	 */
	public boolean isSymbol() {
	    return this.variants.contains(TokenVariant.SYMBOL);
	}

	/**
	 * Returns true if the token is a postfix operator. This implies it is a prefix operator as well. -- ++
	 *
	 * @return
	 */
	public boolean isPostfix() {
	    return this.variants.contains(TokenVariant.POSTFIX);
	}

	/**
	 * Returns true if this symbol is a unary operator, ! - + -- ++
	 *
	 * @return
	 */
	public boolean isUnary() {
	    return this.variants.contains(TokenVariant.UNARY);
	}

	/**
	 * Returns true if this symbol is multiplicative
	 *
	 * @return
	 */
	public boolean isMultaplicative() {
	    return this.variants.contains(TokenVariant.MULTIPLICATIVE);
	}

	/**
	 * Returns true if this symbol is additive + += . etc
	 *
	 * @return
	 */
	public boolean isAdditive() {
	    return this.variants.contains(TokenVariant.ADDITIVE);
	}

	/**
	 * Returns true if this symbol is relational, &lt; &gt;
	 *
	 * @return
	 */
	public boolean isRelational() {
	    return this.variants.contains(TokenVariant.RELATIONAL);
	}

	/**
	 * Returns true if this symbol is equalitative, === == != !==
	 *
	 * @return
	 */
	public boolean isEquality() {
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
	/**
	 * Returns true if this is a logical and
	 *
	 * @return
	 */
	public boolean isLogicalAnd() {
	    return this.variants.contains(TokenVariant.LOGICAL_AND);
	}

	/**
	 * Returns true if this is a default and
	 * @return
	 */
	public boolean isDefaultAnd() {
	    return this.variants.contains(TokenVariant.DEFAULT_AND);
	}

	/**
	 * Return true if this is a logical or
	 *
	 * @return
	 */
	public boolean isLogicalOr() {
	    return this.variants.contains(TokenVariant.LOGICAL_OR);
	}

	/**
	 * Returns true if this is a default or
	 * @return
	 */
	public boolean isDefaultOr() {
	    return this.variants.contains(TokenVariant.DEFAULT_OR);
	}

	/**
	 * Returns true if this is a plus/minus operator + - += -=
	 *
	 * @return
	 */
	public boolean isPlusMinus() {
	    return this.variants.contains(TokenVariant.PLUS_MINUS);
	}

	/**
	 * Returns true if this is an identifier. Variables, strings, bare strings, integers, doubles, etc. Unknown
	 * tokens are assumed to be identifiers.
	 *
	 * @return
	 */
	public boolean isIdentifier() {
	    return this.variants.contains(TokenVariant.IDENTIFIER);
	}

	/**
	 * Returns true if this is an exponential symbol
	 *
	 **
	 * @return
	 */
	public boolean isExponential() {
	    return this.variants.contains(TokenVariant.EXPONENTIAL);
	}

	/**
	 * Returns true if this is an assigment operator = += etc
	 *
	 * @return
	 */
	public boolean isAssignment() {
	    return this.variants.contains(TokenVariant.ASSIGNMENT);
	}

	/**
	 * Returns true if this is a separator, that is, it ends a logical statement. [ ] { } ( ) , ; etc
	 *
	 * @return
	 */
	public boolean isSeparator() {
	    return this.variants.contains(TokenVariant.SEPARATOR);
	}

	/**
	 * Returns true if this is an atomic literal, that is a string (including bare strings), integer, or double
	 * only. Variables are not included.
	 *
	 * @return
	 */
	public boolean isAtomicLit() {
	    return this.variants.contains(TokenVariant.ATOMIC_LIT);
	}

	/**
	 * Returns true if this is a whitespace token, i.e. space, tab, or newline.
	 *
	 * @return
	 */
	public boolean isWhitespace() {
	    return this.variants.contains(TokenVariant.WHITESPACE);
	}

	/**
	 * Returns true if this is a keyword token, i.e. if, else, try, etc
	 * @return
	 */
	public boolean isKeyword() {
	    return this.variants.contains(TokenVariant.KEYWORD);
	}

	/**
	 * Returns true if this is a comment token, either starting with # or // or a smart or non smart
	 * block comment.
	 * @return
	 */
	public boolean isComment() {
	    return this.variants.contains(TokenVariant.COMMENT);
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
	    return "newline";
	}
	if (type.equals(TType.STRING)) {
	    return "'" + value + "'";
	}
	return type + ":" + value;
    }

    public String toSimpleString() {
	if (type.equals(TType.STRING)) {
	    return "'" + value + "'";
	}
	return value;
    }

    /**
     * Returns a string that can be output as useable code. Values that had been escaped internally will
     * be escaped again. This only affects strings and smart strings. All other tokens return the
     * same as {@link #val()}
     * @return
     */
    public String toOutputString() {
	if (type.equals(TType.STRING)) {
	    return value.replace("\\", "\\\\").replace("'", "\\'");
	} else if(type.equals(TType.SMART_STRING)){
	    return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
	return value;
    }

    public Target getTarget() {
	return target;
    }
}
