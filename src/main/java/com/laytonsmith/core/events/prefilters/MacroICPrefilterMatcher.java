package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import static com.laytonsmith.core.events.Prefilters.FastExpressionMatch;
import static com.laytonsmith.core.events.Prefilters.FastRegexMatch;
import static com.laytonsmith.core.events.Prefilters.FastStringMatch;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @param <T>
 */
public abstract class MacroICPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class MacroPrefilterDocs implements PrefilterMatcher.PrefilterDocs {

		@Override
		public String getName() {
			return "macro ic match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#macro ic match|Macro]]";
		}

		@Override
		public String docs() {
			return "A macro ic prefilter the same as a macro match, but if the string match function is used,"
					+ " it ignores case. NOTE: Regex and expression is still case sensitive.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterMatcher.PrefilterDocs getDocsObject() {
		return new MacroPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, CClassType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(node.isConst()) {
			if(node.getData().val().isEmpty()) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
						new CompilerWarning("Hardcoded empty string, this will never match.", node.getTarget(), null));
			}
		}
	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t) {
		String expression = value.val();
		Object javaObject = getProperty(event);
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
			return FastStringMatch(expression.toLowerCase(), javaObject.toString().toLowerCase());
		}
	}

	/**
	 * The property to check against.While this returns Object, this should return either a double or a String.
	 *
	 * @param event
	 * @return
	 */
	protected abstract Object getProperty(T event);

	@Override
	public int getPriority() {
		return 1;
	}

}
