package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Exceptions._try;
import com.laytonsmith.core.functions.Exceptions.complex_try;

import java.util.List;

/**
 *
 */
@Keyword.keyword("try")
public class TryKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		/**
		 * There are several different cases we need to consider. try/catch blocks are fairly complex, and we must
		 * carefully consider each case.
		 *
		 * The simplest case: try { } catch { } In this case, both try and catch will be CKeywords.
		 *
		 * A case with a specific exception type. In this case, catch will be a CFunction try { } catch (ExceptionType
		 *
		 * @e) { }
		 *
		 * A multi catch try { } catch (ExceptionType1 @e) { } catch (ExceptionType2 @e) { } As many blocks as desired can
		 * be added here.
		 *
		 * TODO: Consider in the future allowing just try { }
		 *
		 * We also must keep in mind that try() is a function, and while this keyword internally rewrites to
		 * complex_try, we must test for the old functional use as well.
		 */

		// If it's the old version, and a function
		if(list.get(keywordPosition).getData() instanceof CFunction
				&& list.get(keywordPosition).getData().val().equals(_try.NAME)) {
			return keywordPosition;
		}
		// Otherwise it's not, and we can continue on, assuming keyword usage.
		this.validateCodeBlock(list.get(keywordPosition + 1), "Expecting braces after try keyword");

		ParseTree complexTry = new ParseTree(new CFunction(complex_try.NAME,
				list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		complexTry.addChild(getArgumentOrNull(list.get(keywordPosition + 1)));

		// For now, we won't allow try {}, so this must be followed by a catch keyword. This restriction is somewhat artificial, and
		// if we want to remove it in the future, we can do so by removing this code block.
		{
			if(!(list.size() > keywordPosition + 2 && (nodeIsCatchKeyword(list.get(keywordPosition + 2))
					|| nodeIsFinallyKeyword(list.get(keywordPosition + 2))))) {
				throw new ConfigCompileException("Expecting \"catch\" or \"finally\" keyword to follow try,"
						+ " but none found", complexTry.getTarget());
			}
		}

		// We can have any number of catch statements after the try, so we loop through until we run out.
		int numHandledChildren = 2; // The "try" keyword and try code block have already been handled.
		for(int i = keywordPosition + 2; i < list.size(); i += 2) {
			if(!nodeIsCatchKeyword(list.get(i)) && !nodeIsFinallyKeyword(list.get(i))) {
				// End of the chain, stop processing.
				break;
			}
			if(list.size() > i + 1) {
				this.validateCodeBlock(list.get(i + 1), "Expecting code block after catch, but none found");
			} else {
				throw new ConfigCompileException("catch must be followed by a code block, but none was found", list.get(i).getTarget());
			}
			if(list.get(i).getData() instanceof CFunction) {
				// We have something like catch (Exception @e) { }
				ParseTree n = list.get(i);
				if(n.getChildren().size() != 1) {
					throw new ConfigCompileException("Unexpected parameters passed to the \"catch\" clause."
							+ " Exactly one argument must be passed.", n.getTarget());
				}
				complexTry.addChild(n.getChildAt(0));
				complexTry.addChild(getArgumentOrNull(list.get(i + 1)));
			} else {
				// We have something like finally { }. In this case, this must be the final
				// clause statement, and we need to verify that there isn't a catch following it.
				if(list.size() > i + 2) {
					if(nodeIsCatchKeyword(list.get(i + 2))) {
						throw new ConfigCompileException("A finally block must be the final"
								+ " clause in the try/[catch]/finally statement", list.get(i + 2).getTarget());
					}
				}
				// Passed the inspection.
				complexTry.addChild(getArgumentOrNull(list.get(i + 1)));
			}

			// Mark catch keyword and code block as handled.
			numHandledChildren += 2;
		}

		// Replace the "try" keyword, try block and all other handled blocks with the new function.
		for(int i = 0; i < numHandledChildren - 1; i++) {
			list.remove(keywordPosition);
		}
		list.set(keywordPosition, complexTry);

		return keywordPosition;
	}

	private boolean nodeIsCatchKeyword(ParseTree node) {
		return node.getData() instanceof CFunction && node.getData().val().equals("catch");
	}

	private boolean nodeIsFinallyKeyword(ParseTree node) {
		return node.getData() instanceof CKeyword && node.getData().val().equals("finally");
	}

	@Override
	public String docs() {
		return "Used in combination with catch and finally, provides a mechanism to catch and handle"
				+ " expected and unexpected exceptions, and using finally, provides a mechanism to"
				+ " always run certain code, no matter the result of the code in the try block.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

}
