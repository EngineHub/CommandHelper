package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @param <T>
 */
public abstract class StringICPrefilterMatcher<T extends BindableEvent> implements PrefilterMatcher<T> {

	@Override
	public String filterType() {
		return "string ic match";
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getType(env).doesExtend(CString.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
				new CompilerWarning("Expecting a string type here.",
						node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(Mixed value, T event, Target t) {
		String prop = getProperty(event);
		if(prop == null) {
			return CNull.NULL.equals(value);
		}
		return prop.equalsIgnoreCase(value.val());
	}

	protected abstract String getProperty(T event);




}
