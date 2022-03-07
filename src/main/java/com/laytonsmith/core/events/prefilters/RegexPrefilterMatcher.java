package com.laytonsmith.core.events.prefilters;

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
public abstract class RegexPrefilterMatcher<T extends BindableEvent> implements PrefilterMatcher<T> {

	@Override
	public String filterType() {
		return "regex match";
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
