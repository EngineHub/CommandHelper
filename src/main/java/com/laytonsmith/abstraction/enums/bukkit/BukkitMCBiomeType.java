package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.HashMap;

public class BukkitMCBiomeType extends MCBiomeType<Biome> {

	public BukkitMCBiomeType(MCVanillaBiomeType vanillaBiomeType, Biome biome) {
		super(vanillaBiomeType, biome);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaBiomeType.UNKNOWN ? concreteName() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		Biome b = getConcrete();
		if (b == null) {
			return "null";
		}
		return b.name();
	}

	public static BukkitMCBiomeType valueOfConcrete(Biome test) {
		for (MCBiomeType t : mappings.values()) {
			if (((BukkitMCBiomeType) t).getConcrete().equals(test)) {
				return (BukkitMCBiomeType) t;
			}
		}
		return (BukkitMCBiomeType) NULL;
	}

	public static BukkitMCBiomeType valueOfConcrete(String test) {
		try {
			return valueOfConcrete(Biome.valueOf(test));
		} catch (IllegalArgumentException iae) {
			return (BukkitMCBiomeType) NULL;
		}
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		vanilla = new HashMap<>();
		mappings = new HashMap<>();
		NULL = new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, null);
		ArrayList<Biome> counted = new ArrayList<>();
		for (MCVanillaBiomeType v : MCVanillaBiomeType.values()) {
			if (v.existsInCurrent()) {
				Biome type = getBukkitType(v);
				if (type == null) {
					CHLog.GetLogger().e(CHLog.Tags.RUNTIME, "Could not find a matching biome type for " + v.name()
							+ ". This is an error, please report this to the bug tracker.", Target.UNKNOWN);
					continue;
				}
				BukkitMCBiomeType wrapper = new BukkitMCBiomeType(v, type);
				vanilla.put(v, wrapper);
				mappings.put(v.name(), wrapper);
				counted.add(type);
			}
		}
		for (Biome b : Biome.values()) {
			if (!counted.contains(b)) {
				mappings.put(b.name(), new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, b));
			}
		}
	}

	private static Biome getBukkitType(MCVanillaBiomeType v) {
		// A switch statement for values that don't translate directly to a Bukkit value
		if (Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)) {
			switch (v) {
				case ICE_PLAINS:
					return Biome.ICE_FLATS;
				case MUSHROOM_SHORE:
					return Biome.MUSHROOM_ISLAND_SHORE;
				case BEACH:
					return Biome.BEACHES;
				case SMALL_MOUNTAINS:
					return Biome.SMALLER_EXTREME_HILLS;
				case COLD_TAIGA:
					return Biome.TAIGA_COLD;
				case COLD_TAIGA_HILLS:
					return Biome.TAIGA_COLD_HILLS;
				case MEGA_TAIGA:
					return Biome.REDWOOD_TAIGA;
				case MEGA_TAIGA_HILLS:
					return Biome.REDWOOD_TAIGA_HILLS;
				case EXTREME_HILLS_PLUS:
					return Biome.EXTREME_HILLS_WITH_TREES;
				case SAVANNA_PLATEAU:
					return Biome.SAVANNA_ROCK;
				case JUNGLE_MOUNTAINS:
					return Biome.MUTATED_JUNGLE;
				case JUNGLE_EDGE_MOUNTAINS:
					return Biome.MUTATED_JUNGLE_EDGE;
				case MEGA_SPRUCE_TAIGA:
					return Biome.MUTATED_REDWOOD_TAIGA;
				case MEGA_SPRUCE_TAIGA_HILLS:
					return Biome.MUTATED_REDWOOD_TAIGA_HILLS;
				case BIRCH_FOREST_MOUNTAINS:
					return Biome.MUTATED_BIRCH_FOREST;
				case BIRCH_FOREST_HILLS_MOUNTAINS:
					return Biome.MUTATED_BIRCH_FOREST_HILLS;
				case SAVANNA_MOUNTAINS:
					return Biome.MUTATED_SAVANNA;
				case SAVANNA_PLATEAU_MOUNTAINS:
					return Biome.MUTATED_SAVANNA_ROCK;
				case MESA_PLATEAU_FOREST:
					return Biome.MESA_ROCK;
				case MESA_PLATEAU:
					return Biome.MESA_CLEAR_ROCK;
				case SUNFLOWER_PLAINS:
					return Biome.MUTATED_PLAINS;
				case DESERT_MOUNTAINS:
					return Biome.MUTATED_DESERT;
				case SWAMPLAND_MOUNTAINS:
					return Biome.MUTATED_SWAMPLAND;
				case ROOFED_FOREST_MOUNTAINS:
					return Biome.MUTATED_ROOFED_FOREST;
				case TAIGA_MOUNTAINS:
					return Biome.MUTATED_TAIGA;
				case COLD_TAIGA_MOUNTAINS:
					return Biome.MUTATED_TAIGA_COLD;
				case FLOWER_FOREST:
					return Biome.MUTATED_FOREST;
				case ICE_PLAINS_SPIKES:
					return Biome.MUTATED_ICE_FLATS;
				case EXTREME_HILLS_MOUNTAINS:
					return Biome.MUTATED_EXTREME_HILLS;
				case EXTREME_HILLS_PLUS_MOUNTAINS:
					return Biome.MUTATED_EXTREME_HILLS_WITH_TREES;
				case MESA_BRYCE:
					return Biome.MUTATED_MESA;
				case MESA_PLATEAU_MOUNTAINS:
					return Biome.MUTATED_MESA_CLEAR_ROCK;
				case MESA_PLATEAU_FOREST_MOUNTAINS:
					return Biome.MUTATED_MESA_ROCK;
			}
		}
		try {
			return Biome.valueOf(v.name());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
}
