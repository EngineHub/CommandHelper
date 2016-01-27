package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.core.Static;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
@MDynamicEnum("BiomeType")
public abstract class MCBiomeType<Concrete> extends DynamicEnum<MCBiomeType.MCVanillaBiomeType, Concrete> {

	// To be filled by the implementer
	protected static Map<String, MCBiomeType> mappings;
	protected static Map<MCVanillaBiomeType, MCBiomeType> vanilla;

	public static MCBiomeType NULL = null;

	public MCBiomeType(MCVanillaBiomeType mcVanillaBiomeType, Concrete concrete) {
		super(mcVanillaBiomeType, concrete);
	}

	public static MCBiomeType valueOf(String test) throws IllegalArgumentException {
		if (mappings == null) {
			return null;
		}
		MCBiomeType ret = mappings.get(test);
		if (ret == null) {
			throw new IllegalArgumentException("Unknown biome type: " + test);
		}
		return ret;
	}

	public static MCBiomeType valueOfVanillaType(MCVanillaBiomeType type) {
		return vanilla.get(type);
	}

	/**
	 * @return Names of available entity types
	 */
	public static Set<String> types() {
		if (NULL == null) { // docs mode
			Set<String> dummy = new HashSet<>();
			for (final MCVanillaBiomeType t : MCVanillaBiomeType.values()) {
				dummy.add(t.name());
			}
			return dummy;
		}
		return mappings.keySet();
	}

	/**
	 * @return Our own EntityType list
	 */
	public static Collection<MCBiomeType> values() {
		if (NULL == null) { // docs mode
			ArrayList<MCBiomeType> dummy = new ArrayList<>();
			for (final MCVanillaBiomeType t : MCVanillaBiomeType.values()) {
				dummy.add(new MCBiomeType<Object>(t, null) {
					@Override
					public String name() {
						return t.name();
					}

					@Override
					public String concreteName() {
						return t.name();
					}
				});
			}
			return dummy;
		}
		return mappings.values();
	}

	@MEnum("VanillaBiomeType")
	public enum MCVanillaBiomeType {
		OCEAN,
		PLAINS,
		DESERT,
		EXTREME_HILLS,
		FOREST,
		TAIGA,
		SWAMPLAND,
		RIVER,
		HELL,
		SKY,
		FROZEN_OCEAN,
		FROZEN_RIVER,
		ICE_PLAINS,
		ICE_MOUNTAINS,
		MUSHROOM_ISLAND,
		MUSHROOM_SHORE,
		BEACH(MCVersion.MC1_1),
		DESERT_HILLS(MCVersion.MC1_1),
		FOREST_HILLS(MCVersion.MC1_1),
		TAIGA_HILLS(MCVersion.MC1_1),
		SMALL_MOUNTAINS(MCVersion.MC1_1),
		JUNGLE(MCVersion.MC1_2),
		JUNGLE_HILLS(MCVersion.MC1_2),
		JUNGLE_EDGE(MCVersion.MC1_7_2),
		DEEP_OCEAN(MCVersion.MC1_7_2),
		STONE_BEACH(MCVersion.MC1_7_2),
		COLD_BEACH(MCVersion.MC1_7_2),
		BIRCH_FOREST(MCVersion.MC1_7_2),
		BIRCH_FOREST_HILLS(MCVersion.MC1_7_2),
		ROOFED_FOREST(MCVersion.MC1_7_2),
		COLD_TAIGA(MCVersion.MC1_7_2),
		COLD_TAIGA_HILLS(MCVersion.MC1_7_2),
		MEGA_TAIGA(MCVersion.MC1_7_2),
		MEGA_TAIGA_HILLS(MCVersion.MC1_7_2),
		EXTREME_HILLS_PLUS(MCVersion.MC1_7_2),
		SAVANNA(MCVersion.MC1_7_2),
		SAVANNA_PLATEAU(MCVersion.MC1_7_2),
		MESA(MCVersion.MC1_7_2),
		MESA_PLATEAU_FOREST(MCVersion.MC1_7_2),
		MESA_PLATEAU(MCVersion.MC1_7_2),
		SUNFLOWER_PLAINS(MCVersion.MC1_7_2),
		DESERT_MOUNTAINS(MCVersion.MC1_7_2),
		FLOWER_FOREST(MCVersion.MC1_7_2),
		TAIGA_MOUNTAINS(MCVersion.MC1_7_2),
		ICE_PLAINS_SPIKES(MCVersion.MC1_7_2),
		JUNGLE_MOUNTAINS(MCVersion.MC1_7_2),
		JUNGLE_EDGE_MOUNTAINS(MCVersion.MC1_7_2),
		COLD_TAIGA_MOUNTAINS(MCVersion.MC1_7_2),
		SAVANNA_MOUNTAINS(MCVersion.MC1_7_2),
		SAVANNA_PLATEAU_MOUNTAINS(MCVersion.MC1_7_2),
		MESA_BRYCE(MCVersion.MC1_7_2),
		MESA_PLATEAU_FOREST_MOUNTAINS(MCVersion.MC1_7_2),
		MESA_PLATEAU_MOUNTAINS(MCVersion.MC1_7_2),
		BIRCH_FOREST_MOUNTAINS(MCVersion.MC1_7_2),
		BIRCH_FOREST_HILLS_MOUNTAINS(MCVersion.MC1_7_2),
		ROOFED_FOREST_MOUNTAINS(MCVersion.MC1_7_2),
		MEGA_SPRUCE_TAIGA(MCVersion.MC1_7_2),
		EXTREME_HILLS_MOUNTAINS(MCVersion.MC1_7_2),
		EXTREME_HILLS_PLUS_MOUNTAINS(MCVersion.MC1_7_2),
		SWAMPLAND_MOUNTAINS(MCVersion.MC1_7_2),
		MEGA_SPRUCE_TAIGA_HILLS(MCVersion.MC1_7_2),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;

		MCVanillaBiomeType() {
			this(MCVersion.MC1_0);
		}

		MCVanillaBiomeType(MCVersion since) {
			this.since = since;
		}

		public boolean existsInCurrent() {
			return Static.getServer().getMinecraftVersion().gte(since);
		}
	}
}
