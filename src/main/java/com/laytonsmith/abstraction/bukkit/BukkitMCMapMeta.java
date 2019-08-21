package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCMapMeta;
import org.bukkit.inventory.meta.MapMeta;

public class BukkitMCMapMeta extends BukkitMCItemMeta implements MCMapMeta {

	MapMeta mm;

	public BukkitMCMapMeta(MapMeta im) {
		super(im);
		mm = im;
	}

	@Override
	public boolean hasMapId() {
		return mm.hasMapId();
	}

	@Override
	public int getMapId() {
		return mm.getMapId();
	}

	@Override
	public void setMapId(int id) {
		mm.setMapId(id);
	}

	@Override
	public MCColor getColor() {
		if(mm.hasColor()) {
			return BukkitMCColor.GetMCColor(mm.getColor());
		}
		return null;
	}

	@Override
	public void setColor(MCColor color) {
		mm.setColor(BukkitMCColor.GetColor(color));
	}

}
