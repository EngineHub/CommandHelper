package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import com.laytonsmith.abstraction.MCItemMeta;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;

public class BukkitMCFireworkEffectMeta extends BukkitMCItemMeta implements MCFireworkEffectMeta {

	FireworkEffectMeta meta;

	public BukkitMCFireworkEffectMeta(FireworkEffectMeta im) {
		super(im);
		meta = im;
	}

	@Override
	public MCFireworkEffect getEffect() {
		return new BukkitMCFireworkEffect(meta.getEffect());
	}

	@Override
	public boolean hasEffect() {
		return meta.hasEffect();
	}

	@Override
	public void setEffect(MCFireworkEffect eff) {
		meta.setEffect(eff == null ? null : (FireworkEffect) eff.getHandle());
	}
}
