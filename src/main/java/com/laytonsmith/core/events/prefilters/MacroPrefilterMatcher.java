package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import static com.laytonsmith.core.events.prefilters.Prefilters.FastExpressionMatch;
import static com.laytonsmith.core.events.prefilters.Prefilters.FastRegexMatch;
import static com.laytonsmith.core.events.prefilters.Prefilters.FastStringMatch;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @param <T>
 */
public abstract class MacroPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class MacroPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "macro match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#macro match|Macro]]";
		}

		@Override
		public String docs() {
			return "A macro prefilter is a combination of three other prefilter types, expression, regex, and string."
					+ " Depending on the type of prefilter, only some of these may make sense. In general, the matcher"
					+ " used is determined by the surrounding characters of the prefilter string. If the prefilter"
					+ " is surrounded by parenthesis, it is an expression, if it is surrounded by forward slash (/)"
					+ " it is considered a regex, and if it isn't either of these, then it is considered a string"
					+ " match. For instance, array(prefilter: \"/myRegex/\") will use a regex match, and"
					+ " array(prefilter: \"myString\") will use a string match. Please see the other prefilter"
					+ " types for more information on the specific types.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new MacroPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(node.isConst()) {
			if(node.getData().val().isEmpty()) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
						new CompilerWarning("Hardcoded empty string, this will never match.",
								node.getTarget(), null));
			}
		}
	}

	@Override
	public boolean matches(Mixed value, T event, Target t) {
		String expression = value.val();
		String key = getKey();
		Object javaObject = getProperty();
		if(expression.isEmpty()) {
			return false;
		} else if(expression.charAt(0) == '(' && expression.charAt(expression.length() - 1) == ')') {
			try {
				return FastExpressionMatch(expression, key, (double) javaObject, t);
			} catch (ClassCastException ex) {
				throw new RuntimeException("Unexpected class type, please report this bug to the developers.", ex);
			}
		} else if(expression.charAt(0) == '/' && expression.charAt(expression.length() - 1) == '/') {
			return FastRegexMatch(expression, javaObject.toString());
		} else {
			return FastStringMatch(expression, javaObject.toString());
		}
	}

	protected abstract String getKey();

	/**
	 * The property to check against. While this returns Object, this should return either a double or a String.
	 *
	 * @return
	 */
	protected abstract Object getProperty();

}
