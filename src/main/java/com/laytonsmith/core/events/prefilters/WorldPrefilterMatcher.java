package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @param <T>
 */
public abstract class WorldPrefilterMatcher<T extends BindableEvent> extends StringPrefilterMatcher<T> {

	@api
	public static class WorldPrefilterDocs implements PrefilterDocs {

		@Override
		public String getNameWiki() {
			return "[[Prefilters#world match|World Match]]";
		}

		@Override
		public String getName() {
			return "world match";
		}

		@Override
		public String docs() {
			return "A simple case sensitive string match on the name of the world.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new WorldPrefilterDocs();
	}

	@Override
	protected String getProperty(T event) {
		return getWorld(event).getName();
	}

	protected abstract MCWorld getWorld(T event);
}
