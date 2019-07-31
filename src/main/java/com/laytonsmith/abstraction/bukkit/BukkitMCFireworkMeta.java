package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkMeta;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCFireworkMeta extends BukkitMCItemMeta implements MCFireworkMeta {

	FireworkMeta fm;

	public BukkitMCFireworkMeta(FireworkMeta im) {
		super(im);
		fm = im;
	}

	@Override
	public FireworkMeta asItemMeta() {
		return fm;
	}

	@Override
	public int getStrength() {
		return fm.getPower();
	}

	@Override
	public void setStrength(int strength) {
		fm.setPower(strength);
	}

	@Override
	public List<MCFireworkEffect> getEffects() {
		List<MCFireworkEffect> effects = new ArrayList<>();
		for(FireworkEffect effect : fm.getEffects()) {
			effects.add(new BukkitMCFireworkEffect(effect));
		}
		return effects;
	}

	@Override
	public void addEffect(MCFireworkEffect effect) {
		fm.addEffect((FireworkEffect) effect.getHandle());
	}

	@Override
	public void clearEffects() {
		fm.clearEffects();
	}

}
