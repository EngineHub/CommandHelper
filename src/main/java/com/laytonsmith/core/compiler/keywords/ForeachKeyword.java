package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DataHandling;
import java.util.List;

/**
 *
 */
@Keyword.keyword("foreach")
public class ForeachKeyword extends Keyword {

	private final static String FOREACHELSE = new DataHandling.foreachelse().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		ParseTree foreach = list.get(keywordPosition);
		Target t = foreach.getTarget();
		if(list.size() > keywordPosition + 1){
			// This portion handles the initial code block, i.e. foreach(...){ }
			ParseTree codeBlock = list.get(keywordPosition + 1);
			if(isCodeBlock(codeBlock)){
				validateCodeBlock(codeBlock, "");
				foreach.addChild(getArgumentOrNull(codeBlock));
				list.remove(keywordPosition + 1);
			}
		}
		if(list.size() > keywordPosition + 1){
			// This part handles the else keyword, i.e. foreach(...){ } else { }
			ParseTree elseKeyword = list.get(keywordPosition + 1);
			// If it's not an else keyword, then we'll leave it alone, and be done.
			if(elseKeyword.getData() instanceof CKeyword && elseKeyword.getData().val().equals("else")){
				list.remove(keywordPosition + 1);
				ParseTree codeBlock = list.get(keywordPosition + 1);
				if(isCodeBlock(codeBlock)){
					validateCodeBlock(codeBlock, "");
					foreach.addChild(getArgumentOrNull(codeBlock));
				}
				// We also have to refactor this into a foreachelse, instead of a foreach.
				list.get(keywordPosition).setData(new CFunction(FOREACHELSE, t));
				list.remove(keywordPosition + 1);
			}
		}
		return keywordPosition;
	}

}
