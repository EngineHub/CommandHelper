package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCEndGateway;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import org.bukkit.Location;
import org.bukkit.block.EndGateway;

public class BukkitMCEndGateway extends BukkitMCBlockState implements MCEndGateway {

	EndGateway eg;

	public BukkitMCEndGateway(EndGateway block) {
		super(block);
		this.eg = block;
	}

	@Override
	public MCLocation getExitLocation() {
		Location location = this.eg.getExitLocation();
		if(location == null) {
			return null;
		}
		return new BukkitMCLocation(location);
	}

	@Override
	public void setExitLocation(MCLocation location) {
		if(location == null) {
			this.eg.setExitLocation(null);
		} else {
			this.eg.setExitLocation((Location) location.getHandle());
		}
	}

	@Override
	public boolean isExactTeleport() {
		return this.eg.isExactTeleport();
	}

	@Override
	public void setExactTeleport(boolean isExact) {
		this.eg.setExactTeleport(isExact);
	}

	@Override
	public long getAge() {
		return this.eg.getAge();
	}

	@Override
	public void setAge(long age) {
		this.eg.setAge(age);
	}
}
