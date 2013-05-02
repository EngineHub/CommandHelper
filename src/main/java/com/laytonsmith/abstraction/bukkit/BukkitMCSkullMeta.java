package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCSkullMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class BukkitMCSkullMeta extends BukkitMCItemMeta implements MCSkullMeta {

	SkullMeta sm;
	public BukkitMCSkullMeta(SkullMeta im) {
		super(im);
		this.sm = im;
	}

	public BukkitMCSkullMeta(AbstractionObject o) {
		super(o);
		this.sm = (SkullMeta) o;
	}

	public boolean hasOwner() {
		return sm.hasOwner();
	}

	public String getOwner() {
		return sm.getOwner();
	}

	public boolean setOwner(String owner) {
		return sm.setOwner(owner);
	}

}
