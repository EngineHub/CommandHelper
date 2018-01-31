package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorldBorder;
import org.bukkit.Location;
import org.bukkit.WorldBorder;

public class BukkitMCWorldBorder implements AbstractionObject, MCWorldBorder {

	private WorldBorder wb;

	BukkitMCWorldBorder(WorldBorder wb) {
		this.wb = wb;
	}

	@Override
	public void reset() {
		wb.reset();
	}

	@Override
	public double getSize() {
		return wb.getSize();
	}

	@Override
	public void setSize(double size) {
		wb.setSize(size);
	}

	@Override
	public void setSize(double size, int seconds) {
		wb.setSize(size, seconds);
	}

	@Override
	public MCLocation getCenter() {
		return new BukkitMCLocation(wb.getCenter());
	}

	@Override
	public void setCenter(MCLocation location) {
		wb.setCenter((Location) location.getHandle());
	}

	@Override
	public double getDamageBuffer() {
		return wb.getDamageBuffer();
	}

	@Override
	public void setDamageBuffer(double blocks) {
		wb.setDamageBuffer(blocks);
	}

	@Override
	public double getDamageAmount() {
		return wb.getDamageAmount();
	}

	@Override
	public void setDamageAmount(double damage) {
		wb.setDamageAmount(damage);
	}

	@Override
	public int getWarningTime() {
		return wb.getWarningTime();
	}

	@Override
	public void setWarningTime(int seconds) {
		wb.setWarningTime(seconds);
	}

	@Override
	public int getWarningDistance() {
		return wb.getWarningDistance();
	}

	@Override
	public void setWarningDistance(int distance) {
		wb.setWarningDistance(distance);
	}

	@Override
	public Object getHandle() {
		return wb;
	}
}

