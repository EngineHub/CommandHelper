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

	public MCBiomeType(MCVanillaBiomeType mcVanillaBiomeType, Concrete concrete) {
		super(mcVanillaBiomeType, concrete);
	}

	public static MCBiomeType valueOf(String test) throws IllegalArgumentException {
		MCBiomeType ret = MAP.get(test);
		if(ret == null) {
			MCVanillaBiomeType oldType = MCVanillaBiomeType.valueOf(test);
			if(oldType.newType != null) {
				MSLog.GetLogger().e(MSLog.Tags.GENERAL, test + " biome type was renamed after " + oldType.until.name()
						+ ". Converted to " + oldType.newType, Target.UNKNOWN);
				return MAP.get(oldType.newType);
			}
			throw new IllegalArgumentException("Unknown biome type: " + test);
		}
		return ret;
	}

	/**
	 * @return Names of available biome types
	 */
	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
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
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCBiomeType> dummy = new ArrayList<>();
			for(final MCVanillaBiomeType t : MCVanillaBiomeType.values()) {
				if(!t.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCBiomeType<>(t, null) {
					@Override
					public String name() {
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
		MOUNTAINS(MCVersion.MC1_0, MCVersion.MC1_17_X, "WINDSWEPT_HILLS"),
		FOREST,
		TAIGA,
		SWAMP,
		RIVER,
		NETHER(MCVersion.MC1_0, MCVersion.MC1_15_X, "NETHER_WASTES"),
		THE_END,
		FROZEN_OCEAN,
		FROZEN_RIVER,
		SNOWY_TUNDRA(MCVersion.MC1_0, MCVersion.MC1_17_X, "SNOWY_PLAINS"),
		SNOWY_MOUNTAINS(MCVersion.MC1_0, MCVersion.MC1_17_X),
		MUSHROOM_FIELDS,
		MUSHROOM_FIELD_SHORE(MCVersion.MC1_0, MCVersion.MC1_17_X),
		BEACH,
		DESERT_HILLS(MCVersion.MC1_1, MCVersion.MC1_17_X),
		WOODED_HILLS,
		TAIGA_HILLS(MCVersion.MC1_1, MCVersion.MC1_17_X),
		MOUNTAIN_EDGE(MCVersion.MC1_1, MCVersion.MC1_17_X),
		JUNGLE,
		JUNGLE_HILLS(MCVersion.MC1_2, MCVersion.MC1_17_X),
		JUNGLE_EDGE(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "SPARSE_JUNGLE"),
		DEEP_OCEAN,
		STONE_SHORE(MCVersion.MC1_0, MCVersion.MC1_17_X, "STONY_SHORE"),
		SNOWY_BEACH,
		BIRCH_FOREST,
		BIRCH_FOREST_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		DARK_FOREST,
		SNOWY_TAIGA,
		SNOWY_TAIGA_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		GIANT_TREE_TAIGA(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "OLD_GROWTH_PINE_TAIGA"),
		GIANT_TREE_TAIGA_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		WOODED_MOUNTAINS(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "WINDSWEPT_FOREST"),
		SAVANNA,
		SAVANNA_PLATEAU,
		BADLANDS,
		WOODED_BADLANDS_PLATEAU(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "WOODED_BADLANDS"),
		BADLANDS_PLATEAU(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		SUNFLOWER_PLAINS,
		DESERT_LAKES(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		FLOWER_FOREST,
		TAIGA_MOUNTAINS(MCVersion.MC1_1, MCVersion.MC1_17_X),
		ICE_SPIKES,
		MODIFIED_JUNGLE(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		MODIFIED_JUNGLE_EDGE(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		SNOWY_TAIGA_MOUNTAINS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		SHATTERED_SAVANNA(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "WINDSWEPT_SAVANNA"),
		SHATTERED_SAVANNA_PLATEAU(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		ERODED_BADLANDS,
		MODIFIED_WOODED_BADLANDS_PLATEAU(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		MODIFIED_BADLANDS_PLATEAU(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		TALL_BIRCH_FOREST(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "OLD_GROWTH_BIRCH_FOREST"),
		TALL_BIRCH_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		DARK_FOREST_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		GIANT_SPRUCE_TAIGA(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "OLD_GROWTH_SPRUCE_TAIGA"),
		GRAVELLY_MOUNTAINS(MCVersion.MC1_7_2, MCVersion.MC1_17_X, "WINDSWEPT_GRAVELLY_HILLS"),
		MODIFIED_GRAVELLY_MOUNTAINS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		SWAMP_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		GIANT_SPRUCE_TAIGA_HILLS(MCVersion.MC1_7_2, MCVersion.MC1_17_X),
		THE_VOID,
		SMALL_END_ISLANDS,
		END_MIDLANDS,
		END_HIGHLANDS,
		END_BARRENS,
		WARM_OCEAN,
		LUKEWARM_OCEAN,
		COLD_OCEAN,
		DEEP_WARM_OCEAN(MCVersion.MC1_13, MCVersion.MC1_17_X),
		DEEP_LUKEWARM_OCEAN,
		DEEP_COLD_OCEAN,
		DEEP_FROZEN_OCEAN,
		BAMBOO_JUNGLE(MCVersion.MC1_14),
		BAMBOO_JUNGLE_HILLS(MCVersion.MC1_14, MCVersion.MC1_17_X),
		NETHER_WASTES(MCVersion.MC1_16),
		SOUL_SAND_VALLEY(MCVersion.MC1_16),
		CRIMSON_FOREST(MCVersion.MC1_16),
		WARPED_FOREST(MCVersion.MC1_16),
		BASALT_DELTAS(MCVersion.MC1_16),
		CUSTOM(MCVersion.MC1_16_X),
		DRIPSTONE_CAVES(MCVersion.MC1_17),
		LUSH_CAVES(MCVersion.MC1_17),
		FROZEN_PEAKS(MCVersion.MC1_18),
		GROVE(MCVersion.MC1_18),
		JAGGED_PEAKS(MCVersion.MC1_18),
		MEADOW(MCVersion.MC1_18),
		OLD_GROWTH_BIRCH_FOREST(MCVersion.MC1_18),
		OLD_GROWTH_PINE_TAIGA(MCVersion.MC1_18),
		OLD_GROWTH_SPRUCE_TAIGA(MCVersion.MC1_18),
		SNOWY_PLAINS(MCVersion.MC1_18),
		SNOWY_SLOPES(MCVersion.MC1_18),
		SPARSE_JUNGLE(MCVersion.MC1_18),
		STONY_PEAKS(MCVersion.MC1_18),
		STONY_SHORE(MCVersion.MC1_18),
		WINDSWEPT_FOREST(MCVersion.MC1_18),
		WINDSWEPT_GRAVELLY_HILLS(MCVersion.MC1_18),
		WINDSWEPT_HILLS(MCVersion.MC1_18),
		WINDSWEPT_SAVANNA(MCVersion.MC1_18),
		WOODED_BADLANDS(MCVersion.MC1_18),
		MANGROVE_SWAMP(MCVersion.MC1_19),
		DEEP_DARK(MCVersion.MC1_19),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;
		private final MCVersion until;
		private final String newType;

		MCVanillaBiomeType() {
			this(MCVersion.MC1_0);
		}

		MCVanillaBiomeType(MCVersion since) {
			this(since, MCVersion.FUTURE);
		}

		MCVanillaBiomeType(MCVersion since, MCVersion until) {
			this.since = since;
			this.until = until;
			this.newType = null;
		}

		MCVanillaBiomeType(MCVersion since, MCVersion until, String newType) {
			this.since = since;
			this.until = until;
			this.newType = newType;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since) && version.lte(until);
		}
	}
}
