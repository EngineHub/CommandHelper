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
	public void addEffect(MCFireworkEffect effect) {
		fm.addEffect(((BukkitMCFireworkEffect) effect).getEffect());
	}

	@Override
	public void addEffects(MCFireworkEffect... effects) {
		for (MCFireworkEffect e : effects) {
			fm.addEffect(((BukkitMCFireworkEffect) e).getEffect());
		}
	}

	@Override
	public void clearEffects() {
		fm.clearEffects();
	}

	@Override
	public MCFireworkMeta clone() {
		return new BukkitMCFireworkMeta(fm.clone());
	}

	@Override
	public List<MCFireworkEffect> getEffects() {
		List<MCFireworkEffect> ret = new ArrayList<>();
		for (FireworkEffect e : fm.getEffects()) {
			ret.add(new BukkitMCFireworkEffect(e));
		}
		return ret;
	}

	@Override
	public int getEffectsSize() {
		return fm.getEffectsSize();
	}

	@Override
	public boolean hasEffects() {
		return fm.hasEffects();
	}

	@Override
	public void removeEffect(int index) {
		fm.removeEffect(index);
	}
}
