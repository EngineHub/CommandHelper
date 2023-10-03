package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 */
public abstract class CustomPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	// no @api
	public static class CustomPrefilterDocs implements PrefilterDocs {

		@Override
		public String getNameWiki() {
			return "custom prefilter";
		}

		@Override
		public String getName() {
			return "custom prefilter";
		}

		@Override
		public String docs() {
			return "A custom prefilter check.";
		}

		@Override
		public Version since() {
			return MSVersion.V0_0_0;
		}

	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new CustomPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, LeftHandSideType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		// No validation supported here by default, though it can be overridden if relevant.
	}

}
