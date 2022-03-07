package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @param <T>
 */
public abstract class AbstractPrefilterMatcher<T extends BindableEvent> implements PrefilterMatcher<T> {

	@Override
	public String getName() {
		return getDocsObject().getName();
	}

	@Override
	public String docs() {
		return getDocsObject().docs();
	}

	@Override
	public Version since() {
		return getDocsObject().since();
	}

	@Override
	public String getNameWiki() {
		return getDocsObject().getNameWiki();
	}



}
