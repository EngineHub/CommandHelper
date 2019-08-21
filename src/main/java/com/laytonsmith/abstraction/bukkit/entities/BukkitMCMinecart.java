package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.entities.MCMinecart;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class BukkitMCMinecart extends BukkitMCVehicle implements MCMinecart {

	Minecart m;

	public BukkitMCMinecart(Entity e) {
		super(e);
		this.m = (Minecart) e;
	}

	@Override
	public void setDamage(double damage) {
		m.setDamage(damage);
	}

	@Override
	public double getDamage() {
		return m.getDamage();
	}

	@Override
	public double getMaxSpeed() {
		return m.getMaxSpeed();
	}

	@Override
	public void setMaxSpeed(double speed) {
		m.setMaxSpeed(speed);
	}

	@Override
	public boolean isSlowWhenEmpty() {
		return m.isSlowWhenEmpty();
	}

	@Override
	public void setSlowWhenEmpty(boolean slow) {
		m.setSlowWhenEmpty(slow);
	}

	@Override
	public void setDisplayBlock(MCBlockData data) {
		m.setDisplayBlockData((BlockData) data.getHandle());
	}

	@Override
	public MCBlockData getDisplayBlock() {
		return new BukkitMCBlockData(m.getDisplayBlockData());
	}

	@Override
	public void setDisplayBlockOffset(int offset) {
		m.setDisplayBlockOffset(offset);
	}

	@Override
	public int getDisplayBlockOffset() {
		return m.getDisplayBlockOffset();
	}
}
