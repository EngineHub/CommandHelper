package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPainting;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt;
import org.bukkit.entity.Painting;

/**
 * 
 */
public class BukkitMCPainting extends BukkitMCHanging implements MCPainting {

	public BukkitMCPainting(Painting painting) {
		super(painting);
	}

	public BukkitMCPainting(AbstractionObject ao) {
		this((Painting) ao.getHandle());
	}

	@Override
	public Painting getHandle() {
		return (Painting) metadatable;
	}

	public MCArt getArt() {
		return BukkitMCArt.getConvertor().getAbstractedEnum(getHandle().getArt());
	}

	public boolean setArt(MCArt art) {
		return setArt(art, false);
	}

	public boolean setArt(MCArt art, boolean force) {
		return getHandle().setArt(BukkitMCArt.getConvertor().getConcreteEnum(art), force);
	}
}