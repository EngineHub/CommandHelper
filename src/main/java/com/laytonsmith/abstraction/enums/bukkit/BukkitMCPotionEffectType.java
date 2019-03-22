package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.core.MSLog;
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
		return getAbstracted() == MCVanillaPotionEffectType.UNKNOWN ? concreteName() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		return getConcrete().getName();
	}

	public static MCPotionEffectType valueOfConcrete(PotionEffectType test) {
		return BUKKIT_MAP.get(test);
	}

	public static void build() {
		for(MCVanillaPotionEffectType v : MCVanillaPotionEffectType.values()) {
			if(v.existsInCurrent()) {
				PotionEffectType effect = getBukkitType(v);
				if(effect == null) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit potion effect type for " + v.name(), Target.UNKNOWN);
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
				MAP.put(pe.getName(), new BukkitMCPotionEffectType(MCVanillaPotionEffectType.UNKNOWN, pe));
				ID_MAP.put(pe.getId(), new BukkitMCPotionEffectType(MCVanillaPotionEffectType.UNKNOWN, pe));
				BUKKIT_MAP.put(pe, new BukkitMCPotionEffectType(MCVanillaPotionEffectType.UNKNOWN, pe));
			}
		}
	}

	private static PotionEffectType getBukkitType(MCVanillaPotionEffectType v) {
		switch(v) {
			case SLOWNESS:
				return PotionEffectType.SLOW;
			case HASTE:
				return PotionEffectType.FAST_DIGGING;
			case MINING_FATIGUE:
				return PotionEffectType.SLOW_DIGGING;
			case STRENGTH:
				return PotionEffectType.INCREASE_DAMAGE;
			case INSTANT_HEALTH:
				return PotionEffectType.HEAL;
			case INSTANT_DAMAGE:
				return PotionEffectType.HARM;
			case JUMP_BOOST:
				return PotionEffectType.JUMP;
			case NAUSEA:
				return PotionEffectType.CONFUSION;
			case RESISTANCE:
				return PotionEffectType.DAMAGE_RESISTANCE;
			case BAD_LUCK:
				return PotionEffectType.UNLUCK;
			default:
				return PotionEffectType.getByName(v.name());
		}
	}
}
