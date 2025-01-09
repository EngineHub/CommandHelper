package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@MDynamicEnum("com.commandhelper.TrimPattern")
public abstract class MCTrimPattern<Concrete> extends DynamicEnum<MCTrimPattern.MCVanillaTrimPattern, Concrete> {

	protected static final Map<String, MCTrimPattern> MAP = new HashMap<>();

	public MCTrimPattern(MCVanillaTrimPattern mcVanillaTrimPattern, Concrete concrete) {
		super(mcVanillaTrimPattern, concrete);
	}

	public static MCTrimPattern valueOf(String test) throws IllegalArgumentException {
		MCTrimPattern ret = MAP.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown trim pattern type: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaTrimPattern s : MCVanillaTrimPattern.values()) {
				if(s != MCVanillaTrimPattern.UNKNOWN) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCTrimPattern> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCTrimPattern> dummy = new ArrayList<>();
			for(final MCVanillaTrimPattern s : MCVanillaTrimPattern.values()) {
				if(s == MCVanillaTrimPattern.UNKNOWN) {
					continue;
				}
				dummy.add(new MCTrimPattern<>(s, null) {
					@Override
					public String name() {
						return s.name();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaTrimPattern {
		BOLT(MCVersion.MC1_20_6),
		COAST,
		DUNE,
		EYE,
		FLOW(MCVersion.MC1_20_6),
		HOST,
		RAISER,
		RIB,
		SENTRY,
		SHAPER,
		SILENCE,
		SNOUT,
		SPIRE,
		TIDE,
		VEX,
		WARD,
		WAYFINDER,
		WILD,
		UNKNOWN;

		private final MCVersion since;

		MCVanillaTrimPattern() {
			this(MCVersion.MC1_19_4);
		}

		MCVanillaTrimPattern(MCVersion since) {
			this.since = since;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(this.since);
		}
	}
}
