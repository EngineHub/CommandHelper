package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.events.MCPlayerEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 * A PlayerPrefilterMatcher is a relatively specialized PrefilterMatcher which only works with MCPlayerEvent subtypes.
 * It simply matches the player name against the specified input.
 *
 * @param <T>
 */
public class PlayerPrefilterMatcher<T extends MCPlayerEvent> extends MacroICPrefilterMatcher<T> {

	@api
	public static class PlayerPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "player match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#player match|Player Match]]";
		}

		@Override
		public String docs() {
			return "A player match is a macro ic string match on the player's name.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new PlayerPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getDeclaredType(env).doesExtend(CString.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Expected a string (player) here, this may not perform as expected.",
							node.getTarget(), null));
		}
	}

	@Override
	protected Object getProperty(T event) {
		return event.getPlayer().getName();
	}

}
