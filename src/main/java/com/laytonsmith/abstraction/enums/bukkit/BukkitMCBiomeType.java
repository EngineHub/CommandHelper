package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
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
		return getAbstracted() == MCVanillaBiomeType.UNKNOWN ? getConcrete().name() : getAbstracted().name();
	}

	public static MCBiomeType valueOfConcrete(Biome test) {
		MCBiomeType type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit BiomeType missing in BUKKIT_MAP: " + test.name(),
					Target.UNKNOWN);
			return new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, test);
		}
		return type;
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		for(MCVanillaBiomeType v : MCVanillaBiomeType.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Biome type;
				try {
					type = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find a Bukkit BiomeType for " + v.name(),
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
				MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find MCBiomeType for " + b.name(), Target.UNKNOWN);
				MCBiomeType wrapper = new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, b);
				MAP.put(b.name(), wrapper);
				BUKKIT_MAP.put(b, wrapper);
			}
		}
	}

	private static Biome getBukkitType(MCVanillaBiomeType v) {
		return Biome.valueOf(v.name());
	}
}
