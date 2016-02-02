package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.BasicLogic;
import java.util.List;

/**
 *
 */
@Keyword.keyword("if")
public class IfKeyword extends Keyword {

	private final static String IFELSE = new BasicLogic.ifelse().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		ParseTree node = list.get(keywordPosition);
		Target t = node.getTarget();
		if(list.size() > keywordPosition + 1){
			if(this.isValidCodeBlock(list.get(keywordPosition + 1))){
				// We're using keyword notation, so check for if(@a, @b){ }, as this
				// is a compile error.
				if(node.getChildren().size() != 1){
					throw new ConfigCompileException("Unexpected parameters passed to \"if\" clause, exactly 1 argument"
							+ " must be provided", t);
				}
				// Check to see if we are followed by "else if". If so, we want to
				// use ifelse from the outset
				try {
					if(nodeIsElseKeyword(list.get(keywordPosition + 2))
							&& nodeIsIfFunction(list.get(keywordPosition + 3))){
						// It is, convert this into an ifelse
						ParseTree newNode = new ParseTree(new CFunction(IFELSE, t), node.getFileOptions());
						newNode.setChildren(node.getChildren());
						list.set(keywordPosition, newNode);
						node = newNode;
					}
				} catch(IndexOutOfBoundsException ex){
					// Doesn't matter, we're apparently at the end of the stream
				}
				node.addChild(getArgumentOrNull(list.get(keywordPosition + 1)));
				list.remove(keywordPosition + 1);
			}

			while(list.size() > keywordPosition + 1){
				// Now check for elses. Since we've removed the cbrace following the if from the tree, we can continue from keywordPostion + 1
				if(nodeIsElseKeyword(list.get(keywordPosition + 1))){
					try {
						if(isCodeBlock(list.get(keywordPosition + 2))){
							// So ends the chain
							validateCodeBlock(list.get(keywordPosition + 2), "");
							node.addChild(getArgumentOrNull(list.get(keywordPosition + 2)));
							// remove the else keyword + the brace
							list.remove(keywordPosition + 1);
							list.remove(keywordPosition + 1);
							break;
						} else if(nodeIsIfFunction(list.get(keywordPosition + 2))){
							// Since we are in keyword syntax mode, we won't allow pure functional syntax for this if statement,
							// that is, it may only have one argument, so something like this will be a compile error
							// if(@a){ } else if(@b, @c)
							if(list.get(keywordPosition + 2).getChildren().size() != 1){
								throw new ConfigCompileException("Unexpected parameters passed to \"if\" clause, exactly 1 argument"
										+ " must be provided",
										list.get(keywordPosition + 2).getTarget());
							}
							if(!isCodeBlock(list.get(keywordPosition + 3))){
								throw new ConfigCompileException("Expecting braces after \"if\" clause", list.get(keywordPosition + 3).getTarget());
							}
							// Ok, checks are complete, so we can actually construct the arguments now
							node.addChild(list.get(keywordPosition + 2).getChildAt(0));
							node.addChild(getArgumentOrNull(list.get(keywordPosition + 3)));
							// Remove the else, if function, and braces
							list.remove(keywordPosition + 1);
							list.remove(keywordPosition + 1);
							list.remove(keywordPosition + 1);
						} else {
							// Anything else is unexpected.
							throw new IndexOutOfBoundsException();
						}
					} catch(IndexOutOfBoundsException ex){
						throw new ConfigCompileException("Expecting either braces, or continuing if statement after \"else\" keyword",
								list.get(keywordPosition + 1).getTarget());
					}
				} else {
					// Done with the if else chain
					break;
				}
			}
		}
		return keywordPosition;
	}

	private boolean nodeIsElseKeyword(ParseTree node){
		return node.getData() instanceof CKeyword && node.getData().val().equals("else");
	}

	private boolean nodeIsIfFunction(ParseTree node){
		return node.getData() instanceof CFunction && node.getData().val().equals("if");
	}

}
