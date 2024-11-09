package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCPotionEffectType extends MCPotionEffectType<PotionEffectType> {

	private static final Map<PotionEffectType, MCPotionEffectType> BUKKIT_MAP = new HashMap<>();

	public BukkitMCPotionEffectType(MCVanillaPotionEffectType vanillaEffect, PotionEffectType effect) {
		super(vanillaEffect, effect);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaPotionEffectType.UNKNOWN ? getConcrete().getName() : getAbstracted().name();
	}

	public static MCPotionEffectType valueOfConcrete(PotionEffectType test) {
		MCPotionEffectType type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit PotionEffectType missing in BUKKIT_MAP: " + test.getName(),
					Target.UNKNOWN);
			return new BukkitMCPotionEffectType(MCVanillaPotionEffectType.UNKNOWN, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaPotionEffectType v : MCVanillaPotionEffectType.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				PotionEffectType effect = getBukkitType(v);
				if(effect == null) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find a Bukkit potion effect type for " + v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCPotionEffectType wrapper = new BukkitMCPotionEffectType(v, effect);
				MAP.put(v.name(), wrapper);
				ID_MAP.put(v.getId(), wrapper);
				BUKKIT_MAP.put(effect, wrapper);
			}
		}
		for(PotionEffectType pe : PotionEffectType.values()) {
			if(pe != null && !BUKKIT_MAP.containsKey(pe)) {
				MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find MCPotionEffectTYpe for " + pe.getName(), Target.UNKNOWN);
				MCPotionEffectType wrapper = new BukkitMCPotionEffectType(MCVanillaPotionEffectType.UNKNOWN, pe);
				MAP.put(pe.getName(), wrapper);
				ID_MAP.put(pe.getId(), wrapper);
				BUKKIT_MAP.put(pe, wrapper);
			}
		}
	}

	private static PotionEffectType getBukkitType(MCVanillaPotionEffectType v) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_20_6)) {
			switch(v) {
				case SLOWNESS:
					return PotionEffectType.getByName("SLOW");
				case HASTE:
					return PotionEffectType.getByName("FAST_DIGGING");
				case MINING_FATIGUE:
					return PotionEffectType.getByName("SLOW_DIGGING");
				case STRENGTH:
					return PotionEffectType.getByName("INCREASE_DAMAGE");
				case INSTANT_HEALTH:
					return PotionEffectType.getByName("HEAL");
				case INSTANT_DAMAGE:
					return PotionEffectType.getByName("HARM");
				case JUMP_BOOST:
					return PotionEffectType.getByName("JUMP");
				case NAUSEA:
					return PotionEffectType.getByName("CONFUSION");
				case RESISTANCE:
					return PotionEffectType.getByName("DAMAGE_RESISTANCE");
			}
		}
		if(v == MCVanillaPotionEffectType.BAD_LUCK) {
			return PotionEffectType.UNLUCK;
		}
		return PotionEffectType.getByName(v.name());
	}
}
