package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @param <T>
 */
public abstract class RegexPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class RegexPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "regex match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#Regex|Regex]]";
		}

		@Override
		public String docs() {
			return "A regex match uses the specified regex to check if the string value matches. If the regex matches,"
					+ " the the prefilter passes.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new RegexPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getType(env).doesExtend(CString.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Expecting a string (regex) type here.",
							node.getTarget(), null));
		} else if(node.isConst()) {
			try {
				Pattern.compile(node.getData().val());
			} catch (PatternSyntaxException ex) {
				throw new ConfigCompileException(ex.getMessage(), node.getTarget());
			}
		}
	}

	@Override
	public boolean matches(Mixed value, T event, Target t) {
		return getProperty(event).matches(value.val());
	}

	protected abstract String getProperty(T event);

}
