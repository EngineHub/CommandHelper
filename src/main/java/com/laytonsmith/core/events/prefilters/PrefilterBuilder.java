package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.core.events.BindableEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
		return set(prefilterName, docs, matcher, null);
	}

	/**
	 * Adds another prefilter to the PrefilterBuilder.
	 *
	 * @param prefilterName The name of the prefilter.
	 * @param matcher The matcher object. This should override a single method, the one that corresponds to the
	 * specified type of this prefilter.
	 * @param docs The documentation for this prefilter.
	 * @param priority Sets the match ordering priority.
	 * @return {@code this} for easy chaining.
	 */
	public PrefilterBuilder<T> set(String prefilterName, String docs, PrefilterMatcher<T> matcher, int priority) {
		return set(prefilterName, docs, matcher, null, priority);
	}

	/**
	 * Adds another prefilter to the PrefilterBuilder.
	 *
	 * @param prefilterName The name of the prefilter.
	 * @param matcher The matcher object. This should override a single method, the one that corresponds to the
	 * specified type of this prefilter.
	 * @param docs The documentation for this prefilter.
	 * @param status Status flags that apply to this prefilter.
	 * @return {@code this} for easy chaining.
	 */
	public PrefilterBuilder<T> set(String prefilterName, String docs, PrefilterMatcher<T> matcher, Set<PrefilterStatus> status) {
		if(builder == null) {
			builder = MapBuilder.start(prefilterName, new Prefilter<>(prefilterName, docs, matcher, status, matcher.getPriority()));
		} else {
			builder.set(prefilterName, new Prefilter<>(prefilterName, docs, matcher, status, matcher.getPriority()));
		}
		return this;
	}

	/**
	 * Adds another prefilter to the PrefilterBuilder.
	 *
	 * @param prefilterName The name of the prefilter.
	 * @param matcher The matcher object. This should override a single method, the one that corresponds to the
	 * specified type of this prefilter.
	 * @param docs The documentation for this prefilter.
	 * @param status Status flags that apply to this prefilter.
	 * @param priority Sets the match ordering priority.
	 * @return {@code this} for easy chaining.
	 */
	public PrefilterBuilder<T> set(String prefilterName, String docs, PrefilterMatcher<T> matcher, Set<PrefilterStatus> status, int priority) {
		if(builder == null) {
			builder = MapBuilder.start(prefilterName, new Prefilter<>(prefilterName, docs, matcher, status, priority));
		} else {
			builder.set(prefilterName, new Prefilter<>(prefilterName, docs, matcher, status, priority));
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
		Map<String, Prefilter<T>> prefilters = builder.build();
		// Sort our prefilters based on prefilter priority
		List<Entry<String, Prefilter<T>>> list = new ArrayList<>(prefilters.entrySet());
		list.sort(Map.Entry.comparingByValue());
		Map<String, Prefilter<T>> sortedPrefilters = new LinkedHashMap<>(list.size());
		for(Map.Entry<String, Prefilter<T>> entry : list) {
			sortedPrefilters.put(entry.getKey(), entry.getValue());
		}
		return sortedPrefilters;
	}

}
