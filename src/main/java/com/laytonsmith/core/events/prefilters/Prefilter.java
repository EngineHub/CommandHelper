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
public class Prefilter<T extends BindableEvent> implements Comparable<Prefilter<? extends BindableEvent>> {
	private final String prefilterName;
	private final String docs;
	private final PrefilterMatcher<T> matcher;
	private final Set<PrefilterStatus> status;
	private final int priority;

	public Prefilter(String prefilterName, String docs, PrefilterMatcher<T> matcher, Set<PrefilterStatus> status, int priority) {
		Objects.requireNonNull(prefilterName);
		Objects.requireNonNull(docs);
		Objects.requireNonNull(matcher);
		this.prefilterName = prefilterName;
		this.docs = docs;
		this.matcher = matcher;
		this.status = status == null ? EnumSet.noneOf(PrefilterStatus.class) : status;
		this.priority = priority;
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

	/**
	 * The priority is the order in which prefilters should be matched against. Some prefilters are more expensive
	 * than others, and so should not be run if a cheaper prefilter doesn't match. Prefilter types can define
	 * their own default priority, but if they don't, the default is 0, where prefilters with a lower value (i.e.
	 * negative) are run after prefilters that have a higher priority. For prefilters with
	 * the same priority, the ordering is deterministic but undefined.
	 * @return
	 */
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(Prefilter<? extends BindableEvent> o) {
		return Integer.compare(this.priority, o.priority);
	}

}
