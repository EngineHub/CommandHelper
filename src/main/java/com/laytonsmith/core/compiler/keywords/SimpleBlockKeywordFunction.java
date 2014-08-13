package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 * A wrapper for simple function/keywords, where a code block merely
 * follows the function call, and adds the code block to the end of
 * the function. For instance, the transformation where myFunctionKeyword(@arg){code();}
 * should be turned into myFunctionKeyword(@arg, code()).
 */
public abstract class SimpleBlockKeywordFunction extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		Target t = list.get(keywordPosition).getTarget();
		if(list.size() > keywordPosition + 1){
			ParseTree code = list.get(keywordPosition + 1);
			if(this.isCodeBlock(code)){
				// This is a valid format, but we need to make sure that there is only one argument passed
				// to the while so far.
				Integer[] validArgs = getFunctionArgumentCount();
				// If this is null, we don't care about argument count.
				if(validArgs != null){
					// If the valid argument count is only 1, we will use that value
					// in the error message to make it more precise. Otherwise, use a more
					// generic error message
					int firstClauseArgumentCount = list.get(keywordPosition).getChildren().size();
					if(validArgs.length == 1){
						if(firstClauseArgumentCount != validArgs[0]){
							throw new ConfigCompileException("\"" + getKeywordName() + "\" blocks "
									+ (firstClauseArgumentCount > validArgs[0] ? "may only" : "must") + " have " + validArgs[0]
									+ " argument" + (validArgs[0] == 1 ? "" : "s") + " passed to the"
									+ " " + getKeywordName() + " condition, " + firstClauseArgumentCount + " found.", t);
						}
					} else {
						boolean error = true;
						for(int i : validArgs){
							if(firstClauseArgumentCount == i){
								error = false;
								break;
							}
						}
						if(error){
							throw new ConfigCompileException("\"" + getKeywordName() + "\" blocks may not have " + firstClauseArgumentCount
								+ " argument" + (firstClauseArgumentCount == 1 ? "" : "s") + " passed to the "
								+ getKeywordName() + " condition", t);
						}
					}
				}
				list.get(keywordPosition).addChild(getArgumentOrNull(code));
				list.remove(keywordPosition + 1);
			}
		} else {
			if(!isStandaloneFunction()){
				throw new ConfigCompileException("Missing code block, following \"" + getKeywordName() + "\"", t);
			}
		}
		return keywordPosition;
	}

	/**
	 * Functions may be standalone, that is, myFunctionKeyword() may be valid without
	 * a code block behind it. If this is the case, this should return true. By
	 * default, we return true always.
	 * @return
	 */
	protected boolean isStandaloneFunction(){
		return true;
	}

	/**
	 * Returns the number of arguments that are allowed to be passed to the function portion
	 * of the keyword. For instance, if myFunctionKeyword(@a){ } is the format that
	 * is used, then this should return {1}. This is independent of the function's
	 * normal argument count, as this allows for more precise error messages. If the
	 * number of arguments can vary, send null.
	 * @return
	 */
	protected abstract Integer[] getFunctionArgumentCount();

}
