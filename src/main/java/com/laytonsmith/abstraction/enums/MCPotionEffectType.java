package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.core.Static;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@MDynamicEnum("com.commandhelper.PotionEffectType")
public abstract class MCPotionEffectType<Concrete> extends DynamicEnum<MCPotionEffectType.MCVanillaPotionEffectType, Concrete> {

	protected static final Map<String, MCPotionEffectType> MAP = new HashMap<>();
	protected static final Map<Integer, MCPotionEffectType> ID_MAP = new HashMap<>();

	public MCPotionEffectType(MCVanillaPotionEffectType mcVanillaEffect, Concrete concrete) {
		super(mcVanillaEffect, concrete);
	}

	public static MCPotionEffectType valueOf(String test) throws IllegalArgumentException {
		MCPotionEffectType ret = MAP.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown potion effect type: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaPotionEffectType s : MCVanillaPotionEffectType.values()) {
				if(!s.equals(MCVanillaPotionEffectType.UNKNOWN)) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static Collection<MCPotionEffectType> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCPotionEffectType> dummy = new ArrayList<>();
			for(final MCVanillaPotionEffectType s : MCVanillaPotionEffectType.values()) {
				if(s.equals(MCVanillaPotionEffectType.UNKNOWN)) {
					continue;
				}
				dummy.add(new MCPotionEffectType<Object>(s, null) {
					@Override
					public String name() {
						return s.name();
					}

					@Override
					public String concreteName() {
						return s.name();
					}
				});
			}
			return dummy;
		}
		return MAP.values();
	}

	public static MCPotionEffectType getById(int id) {
		MCPotionEffectType ret = ID_MAP.get(id);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown potion effect id: " + id);
		}
		return ret;
	}

	public int getId() {
		return getAbstracted().getId();
	}

	public enum MCVanillaPotionEffectType {
		UNKNOWN(0, MCVersion.NEVER),
		SPEED(1),
		SLOWNESS(2),
		HASTE(3),
		MINING_FATIGUE(4),
		STRENGTH(5),
		INSTANT_HEALTH(6),
		INSTANT_DAMAGE(7),
		JUMP_BOOST(8),
		NAUSEA(9),
		REGENERATION(10),
		RESISTANCE(11),
		FIRE_RESISTANCE(12),
		WATER_BREATHING(13),
		INVISIBILITY(14),
		BLINDNESS(15),
		NIGHT_VISION(16),
		HUNGER(17),
		WEAKNESS(18),
		POISON(19),
		WITHER(20),
		HEALTH_BOOST(21),
		ABSORPTION(22),
		SATURATION(23),
		GLOWING(24),
		LEVITATION(25),
		LUCK(26),
		BAD_LUCK(27),
		SLOW_FALLING(28),
		CONDUIT_POWER(29),
		DOLPHINS_GRACE(30);

		private final int id;
		private final MCVersion since;

		MCVanillaPotionEffectType(int id) {
			this.id = id;
			this.since = MCVersion.MC1_0;
		}

		MCVanillaPotionEffectType(int id, MCVersion version) {
			this.id = id;
			this.since = version;
		}

		public int getId() {
			return this.id;
		}

		public boolean existsInCurrent() {
			return Static.getServer().getMinecraftVersion().gte(since);
		}
	}
}
