package com.laytonsmith.core.compiler.keywords;

import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CIClosure;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 */
@Keyword.keyword("iclosure")
public class IClosureKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		try {
			if(list.get(keywordPosition).getData() instanceof CFunction) {
				// It's a function, so do the old processing
				SimpleBlockKeywordFunction.doProcess(this.getKeywordName(), null, true, list, keywordPosition);
				// However, if we are proceeded by a ClassType, this is the return type of the closure, and it is
				// easiest if we do the conversion here.
				try {
					if(list.get(keywordPosition - 1).getData() instanceof CClassType) {
						ParseTree type = list.remove(keywordPosition - 1);
						List<ParseTree> children = list.get(keywordPosition - 1).getChildren();
						children.add(0, type);
						list.get(keywordPosition - 1).setChildren(children);
						return keywordPosition - 1;
					}
				} catch (IndexOutOfBoundsException ex) {
					// Ignore, it's not a typed closure
				}
				return keywordPosition;
			} else {
				// Else it's standalone, so this should be treated as the closure ClassType
				list.set(keywordPosition, new ParseTree(CIClosure.TYPE, list.get(keywordPosition).getFileOptions()));
				return keywordPosition;
			}
		} catch (IndexOutOfBoundsException ex) {
			throw new ConfigCompileException("Unexpected \"iclosure\" reference", list.get(keywordPosition).getTarget());
		}
	}

	@Override
	public String docs() {
		return "Creates an iclosure (an isolated closure) with the given code.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
