package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Sound;

public class BukkitMCSound extends MCSound<Sound> {

	public BukkitMCSound(MCVanillaSound vanillaSound, Sound sound) {
		super(vanillaSound, sound);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaSound.UNKNOWN ? concreteName() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		Sound concrete = getConcrete();
		if(concrete == null) {
			return "null";
		}
		return concrete.name();
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		NULL = new BukkitMCSound(MCVanillaSound.UNKNOWN, null);
		for(MCVanillaSound v : MCVanillaSound.values()) {
			if(v.existsInCurrent()) {
				Sound sound;
				try {
					sound = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit Sound for " + v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCSound wrapper = new BukkitMCSound(v, sound);
				MAP.put(v.name(), wrapper);
			}
		}
	}

	private static Sound getBukkitType(MCVanillaSound v) {
		return Sound.valueOf(v.name());
	}
}
