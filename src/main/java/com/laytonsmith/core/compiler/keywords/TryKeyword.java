package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Exceptions;
import java.util.List;

/**
 * 
 */
@Keyword.keyword("try")
public class TryKeyword extends Keyword {

	private static final String COMPLEX_TRY = new Exceptions.complex_try().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		/**
		 * There are several different cases we need to consider. try/catch blocks are fairly complex, and we must carefully
		 * consider each case.
		 *
		 * The simplest case:
		 * try { } catch { }
		 * In this case, both try and catch will be CKeywords.
		 *
		 * A case with a specific exception type. In this case, catch will be a CFunction
		 * try { } catch(ExceptionType @e) { }
		 *
		 * A multi catch
		 * try { } catch(ExceptionType1 @e) { } catch(ExceptionType2 @e) { }
		 * As many blocks as desired can be added here.
		 *
		 * TODO: Consider in the future allowing just try { }
		 *
		 * We also must keep in mind that try() is a function, and while this keyword internally rewrites
		 * to complex_try, we must test for the old functional use as well.
		 */

		// If it's the old version, and a function
		if(list.get(keywordPosition).getData() instanceof CFunction && list.get(keywordPosition).getData().val().equals("try")){
			return keywordPosition;
		}
		// Otherwise it's not, and we can continue on, assuming keyword usage.
		this.validateCodeBlock(list.get(keywordPosition + 1), "Expecting braces after try keyword");

		ParseTree complex_try = new ParseTree(new CFunction(COMPLEX_TRY, list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
		complex_try.addChild(getArgumentOrNull(list.get(keywordPosition + 1)));
		// Remove the keyword and the try block
		list.remove(keywordPosition);
		list.remove(keywordPosition);
		
		// For now, we won't allow try {}, so this must be followed by a catch keyword. This restriction is somewhat artificial, and
		// if we want to remove it in the future, we can do so by removing this code block.
		{
			if(!(list.size() > keywordPosition && (nodeIsCatchKeyword(list.get(keywordPosition)) || nodeIsFinallyKeyword(list.get(keywordPosition))))){
				throw new ConfigCompileException("Expecting \"catch\" or \"finally\" keyword to follow try, but none found", complex_try.getTarget());
			}
		}
		
		// We can have any number of catch statements after the try, so we loop through until we run out.
		for(int i = keywordPosition; i < list.size(); i++){
			if(!nodeIsCatchKeyword(list.get(i)) && !nodeIsFinallyKeyword(list.get(i))){
				// End of the chain, stop processing.
				break;
			}
			if(list.size() > i + 1){
				this.validateCodeBlock(list.get(i + 1), "Expecting code block after catch, but none found");
			} else {
				throw new ConfigCompileException("catch must be followed by a code block, but none was found", list.get(i).getTarget());
			}
			if(list.get(i).getData() instanceof CFunction){
				// We have something like catch(Exception @e) { }
				ParseTree n = list.get(i);
				if(n.getChildren().size() != 1){
					throw new ConfigCompileException("Unexpected parameters passed to the \"catch\" clause."
							+ " Exactly one argument must be passed.", n.getTarget());
				}
				complex_try.addChild(n.getChildAt(0));
				complex_try.addChild(getArgumentOrNull(list.get(i + 1)));
			} else {
				// We have something like finally { }. In this case, this must be the final
				// clause statement, and we need to verify that there isn't a catch following it.
				if(list.size() > i + 2){
					if(nodeIsCatchKeyword(list.get(i + 2))){
						throw new ConfigCompileException("A finally block must be the final"
								+ " clause in the try/[catch]/finally statement", list.get(i + 2).getTarget());
					}
				}
				// Passed the inspection.
				complex_try.addChild(getArgumentOrNull(list.get(i + 1)));
			}
			// remove the catch keyword and the code block
			list.remove(i);
			list.remove(i);
			--i;
		}

		// Set the new function into place
		list.add(keywordPosition, complex_try);

		return keywordPosition;
	}

	private boolean nodeIsCatchKeyword(ParseTree node){
		return node.getData() instanceof CFunction && node.getData().val().equals("catch");
	}

	private boolean nodeIsFinallyKeyword(ParseTree node){
		return node.getData() instanceof CKeyword && node.getData().val().equals("finally");
	}

}
