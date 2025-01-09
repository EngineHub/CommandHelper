package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPainting;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt;
import org.bukkit.Art;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;

public class BukkitMCPainting extends BukkitMCHanging implements MCPainting {

	Painting p;

	public BukkitMCPainting(Entity painting) {
		super(painting);
		this.p = (Painting) painting;
	}

	@Override
	public MCArt getArt() {
		return BukkitMCArt.valueOfConcrete(p.getArt());
	}

	@Override
	public boolean setArt(MCArt art) {
		return setArt(art, false);
	}

	@Override
	public boolean setArt(MCArt art, boolean force) {
		return p.setArt((Art) art.getConcrete(), force);
	}

}
