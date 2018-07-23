package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DataHandling;
import java.util.List;

/**
 *
 */
@Keyword.keyword("do")
public class DoKeyword extends Keyword {

	private static final String WHILE = new DataHandling._while().getName();
	private static final String DOWHILE = new DataHandling._dowhile().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		// We expect the format to be "do" __cbracket__ while, so if this is not the case, we will
		// always throw an exception.
		Target t = list.get(keywordPosition).getTarget();
		try {
			ParseTree codeTree = list.get(keywordPosition + 1);
			ParseTree whileTree = list.get(keywordPosition + 2);
			this.validateCodeBlock(codeTree, "Missing brace following \"do\" keyword");
			if(!(whileTree.getData() instanceof CFunction) || !whileTree.getData().val().equals(WHILE)) {
				throw new ConfigCompileException("Missing while clause following \"do\" keyword", t);
			}
			if(whileTree.getChildren().isEmpty()) {
				throw new ConfigCompileException("Missing argument to while clause", whileTree.getTarget());
			}
			ParseTree dowhile = new ParseTree(new CFunction(DOWHILE, t), list.get(keywordPosition).getFileOptions());
			dowhile.addChild(Keyword.getArgumentOrNull(codeTree));
			dowhile.addChild(whileTree.getChildAt(0));
			list.set(keywordPosition, dowhile);
			list.remove(keywordPosition + 2);
			list.remove(keywordPosition + 1);
		} catch (IndexOutOfBoundsException ex) {
			throw new ConfigCompileException("Unexpected keyword \"do\"", t);
		}
		return keywordPosition;
	}

	@Override
	public String docs() {
		return "Defines a block of code that runs once, then continually while the accompanying while condition is"
				+ " remains true.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
