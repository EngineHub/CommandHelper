package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitMCBiomeType extends MCBiomeType<Biome> {

	private static final Map<Biome, MCBiomeType> BUKKIT_MAP = new HashMap<>();

	public BukkitMCBiomeType(MCVanillaBiomeType vanillaBiomeType, Biome biome) {
		super(vanillaBiomeType, biome);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaBiomeType.UNKNOWN) {
			// changed from enum to interface in 1.21.3
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, getConcrete(), "getKey");
			return key.getKey().toUpperCase(Locale.ROOT);
		}
		return getAbstracted().name();
	}

	public static MCBiomeType valueOfConcrete(Biome test) {
		MCBiomeType type = BUKKIT_MAP.get(test);
		if(type == null) {
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, test, "getKey");
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit BiomeType missing in BUKKIT_MAP: "
					+ key.getKey().toUpperCase(Locale.ROOT), Target.UNKNOWN);
			return new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, test);
		}
		return type;
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		for(MCVanillaBiomeType v : MCVanillaBiomeType.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Biome type = Registry.BIOME.get(NamespacedKey.minecraft(v.name().toLowerCase(Locale.ROOT)));
				if(type == null) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find a Bukkit BiomeType for " + v.name(),
							Target.UNKNOWN);
					continue;
				}
				BukkitMCBiomeType wrapper = new BukkitMCBiomeType(v, type);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(Biome b : Registry.BIOME) {
			if(!BUKKIT_MAP.containsKey(b)) {
				NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, b, "getKey");
				MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find MCBiomeType for "
						+ key.getKey().toUpperCase(Locale.ROOT), Target.UNKNOWN);
				MCBiomeType wrapper = new BukkitMCBiomeType(MCVanillaBiomeType.UNKNOWN, b);
				MAP.put(key.getKey().toUpperCase(Locale.ROOT), wrapper);
				BUKKIT_MAP.put(b, wrapper);
			}
		}
	}
}
