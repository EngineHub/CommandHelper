package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class Token {

	public TType type;
	public String value;
	public final int lineNum;
	public final int column;
	public final File file;
	public final Target target;

	private enum TokenVariant {
		ADDITIVE, EQUALITY, EXPONENTIAL, IDENTIFIER, LOGICAL_AND, LOGICAL_OR, DEFAULT_AND, DEFAULT_OR,
		MULTIPLICATIVE, PLUS_MINUS, POSTFIX, RELATIONAL, SYMBOL, UNARY, ASSIGNMENT,
		SEPARATOR, ATOMIC_LIT, WHITESPACE, KEYWORD, COMMENT, FILE_OPTION, LEFT_BALANCE_TOKEN, RIGHT_BALANCE_TOKEN
	}

	public enum TType {

		//TODO: Comment this out once the compiler is replaced, and clean up unused ones
		UNKNOWN(TokenVariant.IDENTIFIER),
		LSQUARE_BRACKET(TokenVariant.SEPARATOR, TokenVariant.LEFT_BALANCE_TOKEN),
		RSQUARE_BRACKET(TokenVariant.SEPARATOR, TokenVariant.RIGHT_BALANCE_TOKEN),
		OPT_VAR_ASSIGN(TokenVariant.SEPARATOR),
		ALIAS_END(TokenVariant.SEPARATOR),
		COMMA(TokenVariant.SEPARATOR),
		FUNC_NAME(),
		FUNC_START(TokenVariant.SEPARATOR, TokenVariant.LEFT_BALANCE_TOKEN),
		FUNC_END(TokenVariant.SEPARATOR, TokenVariant.RIGHT_BALANCE_TOKEN),
		NEWLINE(TokenVariant.WHITESPACE),
		MULTILINE_START(TokenVariant.SEPARATOR, TokenVariant.LEFT_BALANCE_TOKEN),
		MULTILINE_END(TokenVariant.SEPARATOR, TokenVariant.RIGHT_BALANCE_TOKEN),
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
		LCURLY_BRACKET(TokenVariant.SEPARATOR, TokenVariant.LEFT_BALANCE_TOKEN),
		RCURLY_BRACKET(TokenVariant.SEPARATOR, TokenVariant.RIGHT_BALANCE_TOKEN),
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
		FILE_OPTIONS_START(TokenVariant.SEPARATOR, TokenVariant.FILE_OPTION, TokenVariant.LEFT_BALANCE_TOKEN),
		FILE_OPTIONS_STRING(TokenVariant.FILE_OPTION, TokenVariant.RIGHT_BALANCE_TOKEN),
		FILE_OPTIONS_END(TokenVariant.SEPARATOR, TokenVariant.FILE_OPTION),
		ANNOTATION(TokenVariant.COMMENT);

		private static final List<Pair<TType, TType>> BALANCE_TOKENS = Arrays.asList(
				new Pair<>(LSQUARE_BRACKET, RSQUARE_BRACKET),
				new Pair<>(FUNC_START, FUNC_END),
				new Pair<>(MULTILINE_START, MULTILINE_END),
				new Pair<>(LCURLY_BRACKET, RCURLY_BRACKET),
				new Pair<>(FILE_OPTIONS_START, FILE_OPTIONS_END)
		);

		/**
		 * Given a balance token on either side, returns the opposing balance token type.
		 * @param forType The token type.
		 * @return The opposing token type. For instance, passing in FUNC_START returns FUNC_END, and passing in
		 * RSQUARE_BRACKET returns LSQUARE_BRACKET.
		 * @throws IllegalArgumentException If the token type is not a balance token. This can be checked by seeing
		 * if one of either {@link #isLeftBalanceToken()} or {@link #isRightBalanceToken()} returns true.
		 */
		public static TType getBalanceToken(TType forType) {
			for(Pair<TType, TType> types : BALANCE_TOKENS) {
				if(types.getKey() == forType) {
					return types.getValue();
				} else if(types.getValue() == forType) {
					return types.getKey();
				}
			}
			throw new IllegalArgumentException("Cannot call getBalanceToken on " + forType);
		}

		public static class BalanceMap {
			private final Map<TType, Integer> map = getEmptyBalanceTokenMap();

			/**
			 * Given any token type, processes it. If it's a left balance token, increments it, if it's
			 * a right one, decrements. Otherwise, it is ignored.
			 * @param token
			 * @throws ConfigCompileException
			 */
			public void process(Token token) throws ConfigCompileException {
				if(token.type.isLeftBalanceToken()) {
					inc(token);
				} else if(token.type.isRightBalanceToken()) {
					dec(token);
				}
			}

			/**
			 * Decrements the internal counter for the given token type, if it is a balance token.
			 * Should the internal counter be caused to drop below 0, a compile exception is thrown
			 * automatically.
			 * @param token
			 * @throws ConfigCompileException
			 */
			public void dec(Token token) throws ConfigCompileException {
				TType type = token.type;
				if(!type.isLeftBalanceToken() || !type.isRightBalanceToken()) {
					return;
				}
				map.put(type, map.get(type) - 1);
				if(map.get(type) < 0) {
					throw new ConfigCompileException("Unexpected " + type.name(), token.target);
				}
			}

			/**
			 * Increments the internal counter for the given token type, if it is a balance token.
			 * @param token
			 */
			public void inc(Token token) {
				TType type = token.type;
				if(!type.isLeftBalanceToken() || !type.isRightBalanceToken()) {
					return;
				}
				map.put(type, map.get(type) + 1);
			}

			/**
			 * Returns true if it is currently balanced, or more specifically, if all integer
			 * values in the map are 0.
			 * @return True if it is balanced.
			 */
			public boolean isBalanced() {
				for(Integer i : map.values()) {
					if(i != 0) {
						return false;
					}
				}
				return true;
			}

			/**
			 * Returns the current count of the given {@link TokenVariant#LEFT_BALANCE_TOKEN} type.
			 * @param type
			 * @return
			 */
			public int getCount(TType type) {
				return map.get(type);
			}

			@Override
			public String toString() {
				StringBuilder b = new StringBuilder();
				for(Map.Entry<TType, Integer> entry : map.entrySet()) {
					b.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
				}
				return b.toString().trim();
			}

		}

		/**
		 * Returns a new balance token map. This contains the left hand side of all balance token types,
		 * which map to an integer, initially zero. This can be used to easily and generically keep track
		 * of all balance tokens.
		 * @return
		 */
		private static Map<TType, Integer> getEmptyBalanceTokenMap() {
			Map<TType, Integer> map = new HashMap<>();
			for(Pair<TType, TType> pairs : BALANCE_TOKENS) {
				map.put(pairs.getKey(), 0);
			}
			return map;
		}

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

//		public boolean isBitwiseAnd(){
//			return (this == BIT_AND);
//		}
//
//		public boolean isBitwiseXor(){
//			return (this == BIT_XOR);
//		}
//
//		public boolean isBitwiseOr(){
//			return (this == BIT_OR);
//		}
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
		 *
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
		 *
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
		 * Returns true if this is an assignment operator = += etc
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
		 *
		 * @return
		 */
		public boolean isKeyword() {
			return this.variants.contains(TokenVariant.KEYWORD);
		}

		/**
		 * Returns true if this is a comment token, either starting with # or // or a smart or non smart block comment.
		 *
		 * @return
		 */
		public boolean isComment() {
			return this.variants.contains(TokenVariant.COMMENT);
		}

		/**
		 * Returns true if this is a file option related token, i.e. file option start, end or string
		 *
		 * @return
		 */
		public boolean isFileOption() {
			return this.variants.contains(TokenVariant.FILE_OPTION);
		}

		/**
		 * Returns true if this is a left balance token, meaning that it needs an equivalent right balance
		 * token to be considered "balanced".
		 * @return
		 */
		public boolean isLeftBalanceToken() {
			return this.variants.contains(TokenVariant.LEFT_BALANCE_TOKEN);
		}

		/**
		 * Returns true if this is a right balance token, meaning that it needs an equivalent right balance
		 * token to be considered "balanced".
		 * @return
		 */
		public boolean isRightBalanceToken() {
			return this.variants.contains(TokenVariant.RIGHT_BALANCE_TOKEN);
		}

	}

	public Token(TType type, String value, Target t) {
		this.type = type;
		this.value = value;
		this.lineNum = t.line();
		this.file = t.file();
		this.column = t.col();
		this.target = t;
		t.setLength(value.length());
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
		if(o instanceof Token) {
			Token t = (Token) o;
			return (this.type.equals(t.type) && this.value.equals(t.value));
		}
		return false;
	}

	@Override
	public String toString() {
		if(type.equals(TType.NEWLINE)) {
			return "newline";
		}
		if(type.equals(TType.STRING)) {
			return "'" + value + "'";
		}
		return type + ":" + value;
	}

	public String toSimpleString() {
		if(type.equals(TType.STRING)) {
			return "'" + value + "'";
		}
		return value;
	}

	/**
	 * Returns a string that can be output as usable code. Values that had been escaped internally will be escaped
	 * again. This only affects strings and smart strings. All other tokens return the same as {@link #val()}
	 *
	 * @return
	 */
	public String toOutputString() {
		if(type.equals(TType.STRING)) {
			return value.replace("\\", "\\\\").replace("'", "\\'");
		} else if(type.equals(TType.SMART_STRING)) {
			return value.replace("\\", "\\\\").replace("\"", "\\\"");
		}
		return value;
	}

	public Target getTarget() {
		return target;
	}
}
