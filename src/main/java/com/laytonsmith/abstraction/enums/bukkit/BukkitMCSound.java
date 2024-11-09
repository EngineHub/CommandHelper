package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Sound;

public class BukkitMCSound extends MCSound<Sound> {

	public BukkitMCSound(MCVanillaSound vanillaSound, Sound sound) {
		super(vanillaSound, sound);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaSound.UNKNOWN ? getConcrete().name() : getAbstracted().name();
	}

	public static void build() {
		for(MCVanillaSound v : MCVanillaSound.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
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
