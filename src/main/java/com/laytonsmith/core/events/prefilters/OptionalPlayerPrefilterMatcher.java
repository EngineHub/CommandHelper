package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.events.MCEntityEvent;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.prefilters.PlayerPrefilterMatcher.PlayerPrefilterDocs;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * A OptionalPlayerPrefilterMatcher is a relatively specialized PrefilterMatcher which only works with MCEntityEvent subtypes.
 * It simply checks that entity is a player and matches the player name against the specified input.
 *
 * @param <T>
 */
public class OptionalPlayerPrefilterMatcher<T extends MCEntityEvent> extends MacroICPrefilterMatcher<T> {

	@Override
	public PrefilterDocs getDocsObject() {
		return new PlayerPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, CClassType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!nodeType.doesExtend(CString.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Expected a string (player) here, this may not perform as expected.",
							node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t) {
		Object prop = getProperty(event);
		if(prop == null) {
			return CNull.NULL.equals(value);
		}

		return super.matches(key, value, event, t);
	}

	@Override
	protected Object getProperty(T event) {
		MCEntity entity = event.getEntity();
		if(entity instanceof MCPlayer player) {
			return player.getName();
		}

		return null;
	}
}
