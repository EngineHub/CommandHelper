package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPainting;
import com.laytonsmith.abstraction.enums.MCArt;
import org.bukkit.Art;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;

import java.util.Locale;

public class BukkitMCPainting extends BukkitMCHanging implements MCPainting {

	Painting p;

	public BukkitMCPainting(Entity painting) {
		super(painting);
		this.p = (Painting) painting;
	}

	@Override
	public MCArt getArt() {
		return MCArt.valueOf(p.getArt().getKey().getKey().toUpperCase(Locale.ROOT));
	}

	@Override
	public boolean setArt(MCArt art) {
		return setArt(art, false);
	}

	@Override
	public boolean setArt(MCArt art, boolean force) {
		Art concreteArt = Registry.ART.get(NamespacedKey.minecraft(art.name().toLowerCase(Locale.ROOT)));
		if(concreteArt != null) {
			return p.setArt(concreteArt, force);
		}
		return false;
	}

}
