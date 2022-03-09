package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.events.BindableEvent;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Prefilter represents a single prefilter for an event object.
 * @param <T>
 */
@StandardField
public class Prefilter<T extends BindableEvent> {
	private final String prefilterName;
	private final String docs;
	private final PrefilterMatcher<T> matcher;
	private final Set<PrefilterStatus> status;

	public Prefilter(String prefilterName, String docs, PrefilterMatcher<T> matcher, Set<PrefilterStatus> status) {
		Objects.requireNonNull(prefilterName);
		Objects.requireNonNull(docs);
		Objects.requireNonNull(matcher);
		this.prefilterName = prefilterName;
		this.docs = docs;
		this.matcher = matcher;
		this.status = status == null ? EnumSet.noneOf(PrefilterStatus.class) : status;
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

	public Set<PrefilterStatus> getStatus() {
		return EnumSet.copyOf(status);
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
