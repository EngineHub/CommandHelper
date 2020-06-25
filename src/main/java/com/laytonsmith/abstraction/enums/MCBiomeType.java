package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MDynamicEnum("com.commandhelper.BiomeType")
public abstract class MCBiomeType<Concrete> extends DynamicEnum<MCBiomeType.MCVanillaBiomeType, Concrete> {

	protected static final Map<String, MCBiomeType> MAP = new HashMap<>();

	@SuppressWarnings("checkstyle:staticvariablename") // Fixing this violation might break dependents.
	public static MCBiomeType NULL = null;

	public MCBiomeType(MCVanillaBiomeType mcVanillaBiomeType, Concrete concrete) {
		super(mcVanillaBiomeType, concrete);
	}

	public static MCBiomeType valueOf(String test) throws IllegalArgumentException {
		MCBiomeType ret = MAP.get(test);
		if(ret == null) {
			switch(test) {
				case "NETHER":
					MSLog.GetLogger().e(MSLog.Tags.GENERAL,
							"NETHER biome type was renamed in 1.16. Converted to NETHER_WASTES.", Target.UNKNOWN);
					return MAP.get("NETHER_WASTES");
			}
			throw new IllegalArgumentException("Unknown biome type: " + test);
		}
		return ret;
	}

	/**
	 * @return Names of available biome types
	 */
	public static Set<String> types() {
		if(NULL == null) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaBiomeType t : MCVanillaBiomeType.values()) {
				if(t.existsIn(MCVersion.CURRENT)) {
					dummy.add(t.name());
				}
			}
			return dummy;
		}
		return MAP.keySet();
	}

	/**
	 * @return Our own MCBiomeType list
	 */
	public static List<MCBiomeType> values() {
		if(NULL == null) { // docs mode
			ArrayList<MCBiomeType> dummy = new ArrayList<>();
			for(final MCVanillaBiomeType t : MCVanillaBiomeType.values()) {
				if(!t.existsIn(MCVersion.CURRENT)) {
					continue;
				}
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
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaBiomeType {
		OCEAN,
		PLAINS,
		DESERT,
		MOUNTAINS,
		FOREST,
		TAIGA,
		SWAMP,
		RIVER,
		NETHER(MCVersion.MC1_0, MCVersion.MC1_15_X), // renamed to NETHER_WASTES
		THE_END,
		FROZEN_OCEAN,
		FROZEN_RIVER,
		SNOWY_TUNDRA,
		SNOWY_MOUNTAINS,
		MUSHROOM_FIELDS,
		MUSHROOM_FIELD_SHORE,
		BEACH,
		DESERT_HILLS,
		WOODED_HILLS,
		TAIGA_HILLS,
		MOUNTAIN_EDGE,
		JUNGLE,
		JUNGLE_HILLS,
		JUNGLE_EDGE,
		DEEP_OCEAN,
		STONE_SHORE,
		SNOWY_BEACH,
		BIRCH_FOREST,
		BIRCH_FOREST_HILLS,
		DARK_FOREST,
		SNOWY_TAIGA,
		SNOWY_TAIGA_HILLS,
		GIANT_TREE_TAIGA,
		GIANT_TREE_TAIGA_HILLS,
		WOODED_MOUNTAINS,
		SAVANNA,
		SAVANNA_PLATEAU,
		BADLANDS,
		WOODED_BADLANDS_PLATEAU,
		BADLANDS_PLATEAU,
		SUNFLOWER_PLAINS,
		DESERT_LAKES,
		FLOWER_FOREST,
		TAIGA_MOUNTAINS,
		ICE_SPIKES,
		MODIFIED_JUNGLE,
		MODIFIED_JUNGLE_EDGE,
		SNOWY_TAIGA_MOUNTAINS,
		SHATTERED_SAVANNA,
		SHATTERED_SAVANNA_PLATEAU,
		ERODED_BADLANDS,
		MODIFIED_WOODED_BADLANDS_PLATEAU,
		MODIFIED_BADLANDS_PLATEAU,
		TALL_BIRCH_FOREST,
		TALL_BIRCH_HILLS,
		DARK_FOREST_HILLS,
		GIANT_SPRUCE_TAIGA,
		GRAVELLY_MOUNTAINS,
		MODIFIED_GRAVELLY_MOUNTAINS,
		SWAMP_HILLS,
		GIANT_SPRUCE_TAIGA_HILLS,
		THE_VOID,
		SMALL_END_ISLANDS,
		END_MIDLANDS,
		END_HIGHLANDS,
		END_BARRENS,
		WARM_OCEAN,
		LUKEWARM_OCEAN,
		COLD_OCEAN,
		DEEP_WARM_OCEAN,
		DEEP_LUKEWARM_OCEAN,
		DEEP_COLD_OCEAN,
		DEEP_FROZEN_OCEAN,
		BAMBOO_JUNGLE(MCVersion.MC1_14),
		BAMBOO_JUNGLE_HILLS(MCVersion.MC1_14),
		NETHER_WASTES(MCVersion.MC1_16),
		SOUL_SAND_VALLEY(MCVersion.MC1_16),
		CRIMSON_FOREST(MCVersion.MC1_16),
		WARPED_FOREST(MCVersion.MC1_16),
		BASALT_DELTAS(MCVersion.MC1_16),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;
		private final MCVersion until;

		MCVanillaBiomeType() {
			this(MCVersion.MC1_0);
		}

		MCVanillaBiomeType(MCVersion since) {
			this(since, MCVersion.FUTURE);
		}

		MCVanillaBiomeType(MCVersion since, MCVersion until) {
			this.since = since;
			this.until = until;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since) && version.lte(until);
		}
	}
}
