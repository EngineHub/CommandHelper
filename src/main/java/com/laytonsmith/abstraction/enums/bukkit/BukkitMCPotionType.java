package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCPotionType extends MCPotionType<PotionType> {

	private static final Map<PotionType, MCPotionType> BUKKIT_MAP = new HashMap<>();

	public BukkitMCPotionType(MCVanillaPotionType vanillaEffect, PotionType effect) {
		super(vanillaEffect, effect);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaPotionType.UNKNOWN ? getConcrete().name() : getAbstracted().name();
	}

	public static MCPotionType valueOfConcrete(PotionType test) {
		MCPotionType type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit PotionType missing in BUKKIT_MAP: " + test.name(),
					Target.UNKNOWN);
			return new BukkitMCPotionType(MCVanillaPotionType.UNKNOWN, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaPotionType v : MCVanillaPotionType.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				PotionType type = getBukkitType(v);
				if(type == null) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit potion type for " + v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCPotionType wrapper = new BukkitMCPotionType(v, type);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(PotionType pt : PotionType.values()) {
			if(pt != null && !BUKKIT_MAP.containsKey(pt)) {
				MAP.put(pt.name(), new BukkitMCPotionType(MCVanillaPotionType.UNKNOWN, pt));
				BUKKIT_MAP.put(pt, new BukkitMCPotionType(MCVanillaPotionType.UNKNOWN, pt));
			}
		}
	}

	private static PotionType getBukkitType(MCVanillaPotionType v) {
		return PotionType.valueOf(v.name());
	}
}
