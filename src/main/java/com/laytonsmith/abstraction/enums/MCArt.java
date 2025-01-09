package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.abstraction.enums.MCArt.MCVanillaArt;
import com.laytonsmith.annotations.MDynamicEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@MDynamicEnum("com.commandhelper.Art")
public abstract class MCArt<Concrete> extends DynamicEnum<MCVanillaArt, Concrete> {

	protected static final Map<String, MCArt> MAP = new HashMap<>();

	public MCArt(MCVanillaArt mcVanillaArt, Concrete concrete) {
		super(mcVanillaArt, concrete);
	}

	public static MCArt valueOf(String test) throws IllegalArgumentException {
		MCArt ret = MAP.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown art: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaArt s : MCVanillaArt.values()) {
				if(s != MCVanillaArt.UNKNOWN) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCArt> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCArt> dummy = new ArrayList<>();
			for(final MCVanillaArt s : MCVanillaArt.values()) {
				if(s == MCVanillaArt.UNKNOWN) {
					continue;
				}
				dummy.add(new MCArt<>(s, null) {
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

	public enum MCVanillaArt {
		// 1x1
		KEBAB,
		AZTEC,
		ALBAN,
		AZTEC2,
		BOMB,
		PLANT,
		WASTELAND,
		MEDITATIVE(MCVersion.MC1_21),
		// 2x1
		POOL,
		COURBET,
		SEA,
		SUNSET,
		CREEBET,
		// 1x2
		WANDERER,
		GRAHAM,
		PRAIRIE_RIDE(MCVersion.MC1_21),
		// 2x2
		MATCH,
		BUST,
		STAGE,
		VOID,
		SKULL_AND_ROSES,
		WITHER,
		BAROQUE(MCVersion.MC1_21),
		HUMBLE(MCVersion.MC1_21),
		EARTH(MCVersion.MC1_19),
		WIND(MCVersion.MC1_19),
		WATER(MCVersion.MC1_19),
		FIRE(MCVersion.MC1_19),
		// 4x2
		FIGHTERS,
		CHANGING(MCVersion.MC1_21),
		FINDING(MCVersion.MC1_21),
		LOWMIST(MCVersion.MC1_21),
		PASSAGE(MCVersion.MC1_21),
		// 3x3
		BOUQUET(MCVersion.MC1_21),
		CAVEBIRD(MCVersion.MC1_21),
		COTAN(MCVersion.MC1_21),
		ENDBOSS(MCVersion.MC1_21),
		FERN(MCVersion.MC1_21),
		OWLEMONS(MCVersion.MC1_21),
		SUNFLOWERS(MCVersion.MC1_21),
		TIDES(MCVersion.MC1_21),
		// 4x3
		SKELETON,
		DONKEY_KONG,
		// 3x4
		BACKYARD(MCVersion.MC1_21),
		POND(MCVersion.MC1_21),
		// 4x4
		POINTER,
		PIGSCENE,
		BURNING_SKULL,
		ORB(MCVersion.MC1_21),
		UNPACKED(MCVersion.MC1_21),

		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;

		MCVanillaArt() {
			this(MCVersion.MC1_0);
		}

		MCVanillaArt(MCVersion since) {
			this.since = since;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since);
		}
	}
}
