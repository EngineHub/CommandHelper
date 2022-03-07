package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.events.BindableEvent;
import java.util.Objects;

/**
 * A Prefilter represents a single prefilter for an event object.
 * @param <T>
 */
@StandardField
public class Prefilter<T extends BindableEvent> {
	private final String prefilterName;
	private final String docs;
	private final PrefilterMatcher<T> matcher;

	public Prefilter(String prefilterName, String docs, PrefilterMatcher<T> matcher) {
		Objects.requireNonNull(prefilterName);
		Objects.requireNonNull(docs);
		Objects.requireNonNull(matcher);
		this.prefilterName = prefilterName;
		this.docs = docs;
		this.matcher = matcher;
	}

	public String getDocs() {
		return docs;
	}

	public PrefilterMatcher<T> getMatcher() {
		return matcher;
	}

	public String getPrefilterName() {
		return prefilterName;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return ObjectHelpers.DoEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public String toString() {
		return ObjectHelpers.DoToString(this);
	}

}
