package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCPainting;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt;
import org.bukkit.entity.Painting;

/**
 * 
 */
public class BukkitMCPainting extends BukkitMCHanging implements MCPainting {
	
	Painting p;
	
	public BukkitMCPainting(Painting painting){
		super(painting);
		this.p = painting;
	}

	@Override
	public MCArt getArt() {
		return BukkitMCArt.getConvertor().getAbstractedEnum(p.getArt());
	}

	@Override
	public boolean setArt(MCArt art) {
		return setArt(art, false);
	}

	@Override
	public boolean setArt(MCArt art, boolean force) {
		return p.setArt(BukkitMCArt.getConvertor().getConcreteEnum(art), force);
	}

}
