package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.abstraction.events.MCPlayerEvent;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * A PlayerPrefilterMatcher is a relatively specialized PrefilterMatcher which only works with MCPlayerEvent subtypes.
 * It simply matches the player name against the specified input.
 * @param <T>
 */
public class PlayerPrefilterMatcher<T extends MCPlayerEvent> implements PrefilterMatcher<T> {

	@Override
	public String filterType() {
		return "player match";
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getType(env).doesExtend(CString.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
				new CompilerWarning("Expected a string (player) here, this may not perform as expected.",
						node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(Mixed value, T event, Target t) {
		return value.val().equals(event.getPlayer().getName());
	}

}
