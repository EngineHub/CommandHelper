package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCOminousBottleMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;

public class BukkitMCOminousBottleMeta extends BukkitMCItemMeta implements MCOminousBottleMeta {

	OminousBottleMeta obm;

	public BukkitMCOminousBottleMeta(OminousBottleMeta im) {
		super(im);
		this.obm = im;
	}

	@Override
	public boolean hasAmplifier() {
		return this.obm.hasAmplifier();
	}

	@Override
	public int getAmplifier() {
		return this.obm.getAmplifier();
	}

	@Override
	public void setAmplifier(int value) {
		this.obm.setAmplifier(value);
	}
}
