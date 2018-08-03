package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCBiomeType extends MCBiomeType<Biome> {

	private static final Map<Biome, MCBiomeType> BUKKIT_MAP = new HashMap<>();

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
		if(b == null) {
			return "null";
		}
		return b.name();
	}

	public static MCBiomeType valueOfConcrete(Biome test) {
		MCBiomeType type = BUKKIT_MAP.get(test);
		if(type == null) {
			return NULL;
		}
		return type;
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		NULL = new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, null);
		for(MCVanillaBiomeType v : MCVanillaBiomeType.values()) {
			if(v.existsInCurrent()) {
				Biome type;
				try {
					type = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					CHLog.GetLogger().w(CHLog.Tags.RUNTIME, "Could not find a Bukkit BiomeType for " + v.name(),
							Target.UNKNOWN);
					continue;
				}
				BukkitMCBiomeType wrapper = new BukkitMCBiomeType(v, type);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(Biome b : Biome.values()) {
			if(!BUKKIT_MAP.containsKey(b)) {
				MAP.put(b.name(), new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, b));
				BUKKIT_MAP.put(b, new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, b));
			}
		}
	}

	private static Biome getBukkitType(MCVanillaBiomeType v) {
		return Biome.valueOf(v.name());
	}
}
