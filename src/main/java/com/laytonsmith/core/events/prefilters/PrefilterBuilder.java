package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.core.events.BindableEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @param <T>
 */
public final class PrefilterBuilder<T extends BindableEvent> {

	/**
	 * If the event has no prefilters, this should be used.
	 */
	public static final PrefilterBuilder<BindableEvent> EMPTY = new PrefilterBuilder<>();

	public PrefilterBuilder() {
	}

	private MapBuilder<String, Prefilter<T>> builder;

	/**
	 * Adds another prefilter to the PrefilterBuilder.
	 *
	 * @param prefilterName The name of the prefilter.
	 * @param matcher The matcher object. This should override a single method, the one that corresponds to the
	 * specified type of this prefilter.
	 * @param docs The documentation for this prefilter.
	 * @return {@code this} for easy chaining.
	 */
	public PrefilterBuilder<T> set(String prefilterName, String docs, PrefilterMatcher<T> matcher) {
		if(builder == null) {
			builder = MapBuilder.start(prefilterName, new Prefilter<>(prefilterName, docs, matcher));
		} else {
			builder.set(prefilterName, new Prefilter<>(prefilterName, docs, matcher));
		}
		return this;
	}

	/**
	 * Builds a Map object based on the configured parameters.
	 *
	 * @return
	 */
	public Map<String, Prefilter<T>> build() {
		if(builder == null) {
			return new HashMap<>();
		}
		return builder.build();
	}

}
