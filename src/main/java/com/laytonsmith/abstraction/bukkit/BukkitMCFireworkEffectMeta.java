package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;

public class BukkitMCFireworkEffectMeta extends BukkitMCItemMeta implements MCFireworkEffectMeta {

	FireworkEffectMeta fem;

	public BukkitMCFireworkEffectMeta(FireworkEffectMeta im) {
		super(im);
		fem = im;
	}

	@Override
	public boolean hasEffect() {
		return fem.hasEffect();
	}

	@Override
	public MCFireworkEffect getEffect() {
		FireworkEffect effect = fem.getEffect();
		if(effect == null) {
			return null;
		}
		return new BukkitMCFireworkEffect(fem.getEffect());
	}

	@Override
	public void setEffect(MCFireworkEffect effect) {
		fem.setEffect((FireworkEffect) effect.getHandle());
	}

}
