
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * 
 */
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
		return getConcrete() == null ? "null" : getConcrete().name();
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
		// Insert a switch statement for values that don't translate directly to a Bukkit value
		try {
			return Biome.valueOf(v.name());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
}
