package com.laytonsmith.core.compiler.keywords;


import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CBareString;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Compiler;
import java.util.List;

/**
 *
 */
@Keyword.keyword("proc")
public class ProcKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		if(list.get(keywordPosition).getData() instanceof CKeyword) {
			// It's a lone keyword, so we expect some function to follow, which is the proc name + variables
			FileOptions options = list.get(keywordPosition).getFileOptions();

			// Validate minimal required number of nodes.
			if(list.size() <= keywordPosition + 1) {
				throw new ConfigCompileException("Unexpected keyword", list.get(keywordPosition).getTarget());
			}

			/*
			 * Parse:
			 *     "proc _procName(params) { code }" to "proc(_procName, params, code)". Params and code are optional.
			 *     "proc _procName(params)" to "proc(_procName, params, noop())". Params are optional.
			 *     "proc _procName" to "get_proc(_procName)".
			 */
			if(list.get(keywordPosition + 1).getData() instanceof CFunction) {

				// Create proc function node.
				ParseTree procNode = new ParseTree(new CFunction(
						DataHandling.proc.NAME, list.get(keywordPosition).getTarget()), options);
				procNode.getNodeModifiers().merge(list.get(keywordPosition).getNodeModifiers());

				// Add proc name to proc function node. Proc name node is currently a function node.
				procNode.addChild(new ParseTree(new CString(list.get(keywordPosition + 1).getData().val(),
						list.get(keywordPosition + 1).getTarget()), options));

				// Move proc name function node children to proc function node.
				for(ParseTree child : list.get(keywordPosition + 1).getChildren()) {
					procNode.addChild(child);
				}

				// Get code block from __cbrace__ function node. Define as forward declaration if code block is missing.
				if(list.size() > keywordPosition + 2
						&& list.get(keywordPosition + 2).getData() instanceof CFunction cf
						&& com.laytonsmith.core.functions.Compiler.__cbrace__.NAME.equals(cf.val())) {

					// Validate code block and add to proc function node.
					validateCodeBlock(list.get(keywordPosition + 2), "Expected braces to follow proc definition");
					procNode.addChild(getArgumentOrNoop(list.get(keywordPosition + 2)));

					// Remove processed nodes from AST.
					list.remove(keywordPosition); // Remove keyword node.
					list.remove(keywordPosition); // Remove proc name function node (with proc parameters).
					list.remove(keywordPosition); // Remove __cbrace__ function node.
				} else {

					// Define as forward declaration by adding an exception throw as code block.
					FileOptions forwardImplFileOptions = list.get(keywordPosition + 1).getFileOptions();
					ParseTree throwNode = new ParseTree(new CFunction(Exceptions._throw.NAME, Target.UNKNOWN),
							forwardImplFileOptions, true);
					throwNode.addChild(new ParseTree(
							new CString(CREInvalidProcedureException.TYPE.getSimpleName(), Target.UNKNOWN),
							options, true));
					throwNode.addChild(new ParseTree(
							new CString("Cannot invoke procedure forward declaration.", Target.UNKNOWN),
							options, true));
					ParseTree statement = new ParseTree(new CFunction(Compiler.__statements__.NAME, Target.UNKNOWN),
							forwardImplFileOptions, true);
					statement.addChild(throwNode);
					procNode.addChild(statement);

					// Remove processed nodes from AST.
					list.remove(keywordPosition); // Remove keyword node.
					list.remove(keywordPosition); // Remove proc name function node (with proc parameters).
				}

				// Add proc function node to AST.
				list.add(keywordPosition, procNode);

			} else if(list.get(keywordPosition + 1).getData() instanceof CBareString name) {

				// Parse "proc _procName" to "get_proc(_procName)".
				list.remove(keywordPosition); // Remove keyword node.
				list.remove(keywordPosition); // Remove proc name node.

				// Add get_proc function node with proc name child node to AST.
				ParseTree getProc = new ParseTree(new CFunction(DataHandling.get_proc.NAME, Target.UNKNOWN), options, true);
				getProc.addChild(new ParseTree(new CString(name.val(), name.getTarget()), options));
				list.add(keywordPosition, getProc);

			} else {

				// Proc keyword used incorrectly.
				throw new ConfigCompileException("Unexpected use of \"proc\" keyword", list.get(keywordPosition).getTarget());
			}

		} else if(nodeIsProcFunction(list.get(keywordPosition))) {

			// Parse "proc(_procName, params) { code }" to "proc(_procName, params, code)". Params are optional.
			if(list.size() > keywordPosition + 1 && isValidCodeBlock(list.get(keywordPosition + 1))) {

				// Pull in __cbrace__ function node as proc function node code block.
				list.get(keywordPosition).addChild(getArgumentOrNoop(list.get(keywordPosition + 1)));
				list.remove(keywordPosition + 1); // Remove __cbrace__ function node.
			}
		} else {

			// Random proc keyword in the middle of nowhere.
			throw new ConfigCompileException("Unexpected use of \"proc\" keyword", list.get(keywordPosition).getTarget());
		}
		return keywordPosition;
	}

	private boolean nodeIsProcFunction(ParseTree node) {
		return node.getData() instanceof CFunction && node.getData().val().equals(DataHandling.proc.NAME);
	}

	@Override
	public String docs() {
		return "Defines a procedure, which can be called from elsewhere in code.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

}
