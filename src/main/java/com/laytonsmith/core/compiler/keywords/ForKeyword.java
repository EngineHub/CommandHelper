package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CSemicolon;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Compiler.__autoconcat__;
import com.laytonsmith.core.functions.DataHandling;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Keyword.keyword("for")
public class ForKeyword extends Keyword {

	private final static String FORELSE = new DataHandling.forelse().getName();
	private final static String AUTOCONCAT = new __autoconcat__().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		FileOptions opt = list.get(keywordPosition).getFileOptions();
		ParseTree _for = list.get(keywordPosition);
		// We also want to support standard for(x;y;z){ } syntax, so we want to manually
		// parse for semicolons in the passed in data.
		if(list.get(keywordPosition).numberOfChildren() == 1 && CFunction.isFunction(list.get(keywordPosition).getChildAt(0), __autoconcat__.class)){
			boolean inFirstNode = true;
			boolean inSecondNode = false;
			ParseTree firstNode = new ParseTree(new CFunction(AUTOCONCAT, Target.UNKNOWN), opt);
			ParseTree secondNode = new ParseTree(new CFunction(AUTOCONCAT, Target.UNKNOWN), opt);
			ParseTree thirdNode = new ParseTree(new CFunction(AUTOCONCAT, Target.UNKNOWN), opt);
			List<ParseTree> values = new ArrayList<>();
			for(ParseTree node : list.get(keywordPosition).getChildAt(0).getChildren()){
				if(node.getData() instanceof CSemicolon){
					if(inFirstNode){
						inFirstNode = false;
						inSecondNode = true;
						if(values.isEmpty()){
							throw new ConfigCompileException("Empty statement in first portion of for clause", node.getTarget());
						}
						firstNode.setChildren(new ArrayList<>(values));
						values.clear();
						continue;
					}
					if(inSecondNode){
						inSecondNode = false;
						if(values.isEmpty()){
							throw new ConfigCompileException("Empty statement in second portion of for clause", node.getTarget());
						}
						secondNode.setChildren(new ArrayList<>(values));
						values.clear();
						continue;
					}
					if(!inFirstNode && !inSecondNode){
						throw new ConfigCompileException("Unexpected semicolon in third portion of for clause", node.getTarget());
					}
				}
				values.add(node);
			}
			if(values.isEmpty()){
				throw new ConfigCompileException("Empty statement in third portion of for clause", list.get(keywordPosition).getTarget());
			}
			thirdNode.setChildren(new ArrayList<>(values));
			list.get(keywordPosition).setChildren(new ArrayList<>(Arrays.asList(new ParseTree[]{firstNode, secondNode, thirdNode})));
		}
		Target t = _for.getTarget();
		if(list.size() > keywordPosition + 1){
			// This portion handles the initial code block, i.e. foreach(...){ }
			ParseTree codeBlock = list.get(keywordPosition + 1);
			if(isCodeBlock(codeBlock)){
				validateCodeBlock(codeBlock, "");
				_for.addChild(getArgumentOrNull(codeBlock));
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
					_for.addChild(getArgumentOrNull(codeBlock));
				}
				// We also have to refactor this into a foreachelse, instead of a foreach.
				list.get(keywordPosition).setData(new CFunction(FORELSE, t));
				list.remove(keywordPosition + 1);
			}
		}
		return keywordPosition;
	}

}
