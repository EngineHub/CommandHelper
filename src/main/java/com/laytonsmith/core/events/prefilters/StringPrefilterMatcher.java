package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.LeftHandSideType;
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
public abstract class StringPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class StringPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "string match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#string match|String Match]]";
		}

		@Override
		public String docs() {
			return "A string match is a simple string match, that does NOT ignore case. That is, \"aSdF\" and \"asdf\" do not match,"
					+ " nor do \"asd\" and \"asdf\".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new StringPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, LeftHandSideType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!nodeType.doesExtend(CString.TYPE, env)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Expecting a string type here.", node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t, Environment env) {
		String prop = getProperty(event);
		if(prop == null) {
			return CNull.NULL.equals(value);
		}
		return prop.equals(value.val());
	}

	protected abstract String getProperty(T event);

}
