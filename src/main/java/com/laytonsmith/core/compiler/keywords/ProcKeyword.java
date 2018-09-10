package com.laytonsmith.core.compiler.keywords;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DataHandling;
import java.util.List;

/**
 *
 */
@Keyword.keyword("proc")
public class ProcKeyword extends Keyword {

	private static final String PROC = new DataHandling.proc().getName();

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		if(list.get(keywordPosition).getData() instanceof CKeyword) {
			// It's a lone keyword, so we expect some function to follow, which is the proc name + variables
			if(list.get(keywordPosition + 1).getData() instanceof CFunction) {
				ParseTree proc = new ParseTree(new CFunction(PROC, list.get(keywordPosition).getTarget()), list.get(keywordPosition).getFileOptions());
				proc.addChild(new ParseTree(new CString(list.get(keywordPosition + 1).getData().val(),
						list.get(keywordPosition + 1).getTarget()), list.get(keywordPosition + 1).getFileOptions()));
				// Grab the functions children, and put them on the stack
				for(ParseTree child : list.get(keywordPosition + 1).getChildren()) {
					proc.addChild(child);
				}
				if(list.size() > keywordPosition + 2) {
					validateCodeBlock(list.get(keywordPosition + 2), "Expected braces to follow proc definition");
					proc.addChild(getArgumentOrNull(list.get(keywordPosition + 2)));
				} else {
					throw new ConfigCompileException("Expected braces to follow proc definition", list.get(keywordPosition + 1).getTarget());
				}
				list.remove(keywordPosition); // Remove the keyword
				list.remove(keywordPosition); // Remove the function definition
				list.remove(keywordPosition); // Remove the cbrace
				list.add(keywordPosition, proc); // Add in the new proc definition
			} else {
				throw new ConfigCompileException("Unexpected use of \"proc\" keyword", list.get(keywordPosition).getTarget());
			}

		} else if(nodeIsProcFunction(list.get(keywordPosition))) {
			// It's the functional usage, possibly followed by a cbrace. If so, pull the cbrace in, and that's it
			if(list.size() > keywordPosition + 1) {
				if(isValidCodeBlock(list.get(keywordPosition + 1))) {
					list.get(keywordPosition).addChild(getArgumentOrNull(list.get(keywordPosition + 1)));
					list.remove(keywordPosition + 1);
				}
			}
		} else {
			// Random keyword in the middle of nowhere
			throw new ConfigCompileException("Unexpected use of \"proc\" keyword", list.get(keywordPosition).getTarget());
		}
		return keywordPosition;
	}

	private boolean nodeIsProcFunction(ParseTree node) {
		return node.getData() instanceof CFunction && node.getData().val().equals(PROC);
	}

	@Override
	public String docs() {
		return "Defines a procedure, which can be called from elsewhere in code.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

}
