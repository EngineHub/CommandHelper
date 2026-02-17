package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.EarlyBindingKeyword;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DataHandling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 */
@Keyword.keyword("switch")
public class SwitchKeyword extends EarlyBindingKeyword {

	@Override
	public int process(TokenStream stream, Environment env, int keywordPosition)
			throws ConfigCompileException {
		// First, we need to check if this is functional notation, if so, we stop.
		int codeStart = -1;
		{
			int parenthesisStack = 0;
			for(ListIterator<Token> it = stream.listIterator(keywordPosition); it.hasNext(); ) {
				int ind = it.nextIndex();
				Token token = it.next();
				Token next = null;
				if(it.hasNext()) {
					next = it.next();
					it.previous(); // Switch iteration direction.
					it.previous(); // Return to previous element.
					it.next(); // Switch iteration direction.
				}
				if(token.type == TType.FUNC_START) {
					parenthesisStack++;
				}
				if(token.type == TType.FUNC_END) {
					parenthesisStack--;
					if(parenthesisStack == 0 && next != null && next.type == TType.LCURLY_BRACKET) {
						codeStart = ind + 1;
						break;
					} else if(parenthesisStack == 0) {
						// Functional usage, do nothing
						return keywordPosition;
					}
				}
			}
			if(codeStart == -1) {
				// We've run out of stream and not found a bracket, so return
				return keywordPosition;
			}
		}

		// It has a code block, time to re-write. General rewrite format is:
		// switch(condition, caseLabel, caseCode, array(caseLabel1, caseLabel2), case1and2code, defaultCode)
		// First token should be case or default
		List<Token> newStream = new ArrayList<>();
		int codeEnd = -1;
		Comparator<List<Token>> tokenComparator = (List<Token> o1, List<Token> o2) -> {
			// Order doesn't matter, just needs to be deterministic
			return o1.toString().compareTo(o2.toString());
		};
		SortedSet<List<Token>> caseLabels = new TreeSet<>(tokenComparator);
		boolean inCase = false;
		boolean inDefault = false;
		boolean inCode = false;
		boolean pastDefault = false;
		boolean firstComma = false;
		int parenthesisStack = 0;
		int braceStack = 0;
		int bracketStack = 0;
		OUTER:
		for(ListIterator<Token> it = stream.listIterator(codeStart + 1); it.hasNext(); ) {
			int i = it.nextIndex();
			Token t = it.next();
			if(inCode) {
				if(null != t.type) {
					// If we're in code, we need to balance all parenthesis, braces, and brackets, and not parse
					// any code inside of those. Then, we are looking for the next case or default statement.
					switch(t.type) {
						case FUNC_START -> {
							parenthesisStack++;
							newStream.add(t);
							continue;
						}
						case FUNC_END -> {
							parenthesisStack--;
							newStream.add(t);
							continue;
						}
						case LSQUARE_BRACKET -> {
							bracketStack++;
							newStream.add(t);
							continue;
						}
						case RSQUARE_BRACKET -> {
							bracketStack--;
							newStream.add(t);
							continue;
						}
						case LCURLY_BRACKET -> {
							braceStack++;
							newStream.add(t);
							continue;
						}
						case RCURLY_BRACKET -> {
							braceStack--;
							if(braceStack == -1) {
								codeEnd = i;
								break OUTER;
							}
							newStream.add(t);
							continue;
						}
					}
				}
			} else {
				if(t.type == TType.COMMENT || t.type == TType.SMART_COMMENT) {
					// Nothing in a switch is commentable, so always throw out any comments not in code.
					continue;
				}
				if(t.type == TType.RCURLY_BRACKET) {
					braceStack--;
					codeEnd = i;
					break;
				}
			}
			if(parenthesisStack > 0 || braceStack > 0 || bracketStack > 0) {
				newStream.add(t);
				continue;
			}
			if(i == codeStart + 1) {
				// First token must be a case/default
				if(!CKeyword.isKeyword(t, "case") && !CKeyword.isKeyword(t, "default")) {
					throw new ConfigCompileException("Expected a case or default keyword here", t.target);
				}
			}
			if(CKeyword.isKeyword(t, "case")) {
				inCase = true;
				inCode = false;
				continue;
			}
			if(CKeyword.isKeyword(t, "default")) {
				if(!caseLabels.isEmpty()) {
					// Ignore these case labels, they are being removed
					caseLabels = new TreeSet<>(tokenComparator);
				}
				if(!it.hasNext()) {
					throw new ConfigCompileException("Expected colon after default keyword", t.target);
				}
				Token next = it.next();
				if(next.type != TType.LABEL) {
					throw new ConfigCompileException("Expected colon after default keyword", next.target);
				}
				inDefault = true;
				inCode = true;
				continue;
			}
			if(!caseLabels.isEmpty() && (inCode || inDefault)) {
				if(firstComma) {
					newStream.add(new Token(TType.COMMA, ",", Target.UNKNOWN));
				}
				firstComma = true;
				// Write out the case labels, we're actually done with them now
				if(caseLabels.size() > 1) {
					newStream.add(new Token(TType.FUNC_NAME, DataHandling.array.NAME, Target.UNKNOWN));
					newStream.add(new Token(TType.FUNC_START, "(", Target.UNKNOWN));
				}

				boolean first = true;
				for(List<Token> r : caseLabels) {
					if(!first) {
						newStream.add(new Token(TType.COMMA, ",", Target.UNKNOWN));
					}
					first = false;
					for(Token tt : r) {
						if(tt.type == TType.SMART_STRING) {
							tt = com.laytonsmith.core.functions.Compiler.__smart_string__.getDumbStringOrFail(tt);
						}
						newStream.add(tt);
					}
				}

				if(caseLabels.size() > 1) {
					newStream.add(new Token(TType.FUNC_END, ")", Target.UNKNOWN));
				}

				newStream.add(new Token(TType.COMMA, ",", Target.UNKNOWN));
				caseLabels = new TreeSet<>(tokenComparator);
				if(inDefault) {
					inCode = true;
					inDefault = false;
					newStream.add(t);
					continue;
				} else {
					newStream.add(t);
					continue;
				}
			}
			if(inCase) {
				if(pastDefault) {
					throw new ConfigCompileException("Unexpected case clause. Default case must come last.", t.target);
				}
				List<Token> caseTokens = new ArrayList<>();
				while(t.type != TType.LABEL) {
					if(t.type != TType.LIT && t.type != TType.SLICE && t.type != TType.STRING
							&& t.type != TType.INTEGER && t.type != TType.KEYWORD && t.type != TType.DOT
							&& t.type != TType.SMART_STRING) {
						throw new ConfigCompileException("Unsupported type in case clause", t.target);
					}
					caseTokens.add(t);
					if(!it.hasNext()) {
						throw new ConfigCompileException("Incomplete case clause", t.target);
					}
					t = it.next();
				}
				caseLabels.add(caseTokens);
				inCase = false;
				inCode = true;
				continue;
			}

			if(inDefault && !pastDefault) {
				newStream.add(new Token(TType.COMMA, ",", Target.UNKNOWN));
				pastDefault = true;
			}

			newStream.add(t);
		}
		if(braceStack != -1) {
			throw new ConfigCompileException("Missing end brace", stream.get(codeStart).target);
		}
		// Leftover case labels are discarded, because that meant they were empty, i.e. switch(@c) { case 'abcd': }
		// We do however warn.
		if(!caseLabels.isEmpty()) {
			Token first = new ArrayList<>(caseLabels).get(0).get(0);
			CompilerWarning warning = new CompilerWarning("Useless case clause",
					first.target, FileOptions.SuppressWarning.UselessCode);
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(stream.getFileOptions(), warning);
		}
		// Replace code start - 1 with a comma, and then the whole code block with the new tokens, and finally a right
		// parenthesis.
		ListIterator<Token> it;
		if(!newStream.isEmpty()) {
			it = stream.listIterator(codeStart - 1);
			Token token = it.next();
			it.set(new Token(TType.COMMA, ",", token.target.copy()));
		} else {
			it = stream.listIterator(codeStart);
		}
		for(int i = codeStart; i <= codeEnd; i++) {
			it.next(); // Select index codeStart.
			it.remove(); // Remove index codeStart.
		}
		if(!newStream.isEmpty()) {
			it.add(new Token(TType.FUNC_END, ")", Target.UNKNOWN)); // Add at index codeStart.
			it.previous(); // Select index codeStart + 1 <---.
			it.previous(); // Select index codeStart <---.
			it.next(); // Select index codeStart --->.
		}
		for(ListIterator<Token> it2 = newStream.listIterator(); it2.hasNext(); ) {
			it.add(it2.next());
		}

		return keywordPosition;
	}

	@Override
	public String docs() {
		return "Defines a switch block, which is a more efficient, but narrower version of an if/else if/else chain.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}
}
