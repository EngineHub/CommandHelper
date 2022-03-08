package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CBoolean;
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
public abstract class BooleanPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class BooleanPrefilterDocsObject implements PrefilterDocs {
		@Override
		public String getName() {
			return "boolean match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#boolean match|Boolean Match]]";
		}

		@Override
		public String docs() {
			return "A boolean prefilter matches if the boolean value is the same. In general, this is a booleanish value,"
					+ " but if a non-boolean is provided, a compiler warning is issued, as this indicates possibly"
					+ " suspect code. Null is considered false.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new BooleanPrefilterDocsObject();
	}


	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getType(env).doesExtend(CBoolean.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
				new CompilerWarning("Expected a boolean here, this may not perform as expected.",
						node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t) {
		return ArgumentValidation.getBooleanish(value, t) == getProperty(event);
	}

	protected abstract boolean getProperty(T event);
}
