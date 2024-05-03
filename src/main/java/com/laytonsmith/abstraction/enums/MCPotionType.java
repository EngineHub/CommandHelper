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

@MDynamicEnum("com.commandhelper.PotionType")
public abstract class MCPotionType<Concrete> extends DynamicEnum<MCPotionType.MCVanillaPotionType, Concrete> {

	protected static final Map<String, MCPotionType> MAP = new HashMap<>();

	public MCPotionType(MCVanillaPotionType mcVanillaType, Concrete concrete) {
		super(mcVanillaType, concrete);
	}

	public static MCPotionType valueOf(String test) throws IllegalArgumentException {
		MCPotionType ret = MAP.get(test);
		if(ret == null) {
			MCVanillaPotionType oldType = MCVanillaPotionType.valueOf(test);
			if(oldType.rename != null) {
				return MAP.get(oldType.rename);
			}
			throw new IllegalArgumentException("Unknown potion type: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaPotionType s : MCVanillaPotionType.values()) {
				if(!s.equals(MCVanillaPotionType.UNCRAFTABLE) && s.existsIn(MCVersion.CURRENT)) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCPotionType> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCPotionType> dummy = new ArrayList<>();
			for(final MCVanillaPotionType s : MCVanillaPotionType.values()) {
				if(s.equals(MCVanillaPotionType.UNCRAFTABLE) || !s.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCPotionType<>(s, null) {
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

	public enum MCVanillaPotionType {
		AWKWARD,
		FIRE_RESISTANCE,
		LONG_FIRE_RESISTANCE(MCVersion.MC1_20_2),
		INFESTED(MCVersion.MC1_20_6),
		INSTANT_DAMAGE(MCVersion.MC1_0, MCVersion.MC1_20_4, "HARMING"),
		HARMING(MCVersion.MC1_20_6),
		STRONG_HARMING(MCVersion.MC1_20_2),
		INSTANT_HEAL(MCVersion.MC1_0, MCVersion.MC1_20_4, "HEALING"),
		HEALING(MCVersion.MC1_20_6),
		STRONG_HEALING(MCVersion.MC1_20_2),
		INVISIBILITY,
		LONG_INVISIBILITY(MCVersion.MC1_20_2),
		JUMP(MCVersion.MC1_0, MCVersion.MC1_20_4, "LEAPING"),
		LEAPING(MCVersion.MC1_20_6),
		LONG_LEAPING(MCVersion.MC1_20_2),
		STRONG_LEAPING(MCVersion.MC1_20_2),
		LUCK,
		MUNDANE,
		NIGHT_VISION,
		LONG_NIGHT_VISION(MCVersion.MC1_20_2),
		OOZING(MCVersion.MC1_20_6),
		POISON,
		LONG_POISON(MCVersion.MC1_20_2),
		STRONG_POISON(MCVersion.MC1_20_2),
		REGEN(MCVersion.MC1_0, MCVersion.MC1_20_4, "REGENERATION"),
		REGENERATION(MCVersion.MC1_20_6),
		LONG_REGENERATION(MCVersion.MC1_20_2),
		STRONG_REGENERATION(MCVersion.MC1_20_2),
		SLOWNESS,
		LONG_SLOWNESS(MCVersion.MC1_20_2),
		STRONG_SLOWNESS(MCVersion.MC1_20_2),
		SLOW_FALLING,
		LONG_SLOW_FALLING(MCVersion.MC1_20_2),
		SPEED(MCVersion.MC1_0, MCVersion.MC1_20_4, "SWIFTNESS"),
		SWIFTNESS(MCVersion.MC1_20_6),
		LONG_SWIFTNESS(MCVersion.MC1_20_2),
		STRONG_SWIFTNESS(MCVersion.MC1_20_2),
		STRENGTH,
		LONG_STRENGTH(MCVersion.MC1_20_2),
		STRONG_STRENGTH(MCVersion.MC1_20_2),
		THICK,
		TURTLE_MASTER,
		LONG_TURTLE_MASTER(MCVersion.MC1_20_2),
		STRONG_TURTLE_MASTER(MCVersion.MC1_20_2),
		UNCRAFTABLE(MCVersion.MC1_0, MCVersion.MC1_20_4),
		WATER,
		WATER_BREATHING,
		LONG_WATER_BREATHING(MCVersion.MC1_20_2),
		WEAKNESS,
		LONG_WEAKNESS(MCVersion.MC1_20_2),
		WEAVING(MCVersion.MC1_20_6),
		WIND_CHARGED(MCVersion.MC1_20_6),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;
		private final MCVersion until;
		private final String rename;

		MCVanillaPotionType() {
			this(MCVersion.MC1_0);
		}

		MCVanillaPotionType(MCVersion since) {
			this(since, MCVersion.FUTURE);
		}

		MCVanillaPotionType(MCVersion since, MCVersion until) {
			this(since, until, null);
		}

		MCVanillaPotionType(MCVersion since, MCVersion until, String rename) {
			this.since = since;
			this.until = until;
			this.rename = rename;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since) && version.lte(until);
		}
	}
}
