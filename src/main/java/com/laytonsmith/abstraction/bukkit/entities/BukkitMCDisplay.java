package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.bukkit.BukkitMCColor;
import com.laytonsmith.abstraction.entities.MCDisplay;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

public class BukkitMCDisplay extends BukkitMCEntity implements MCDisplay {

	Display d;

	public BukkitMCDisplay(Entity e) {
		super(e);
		this.d = (Display) e;
	}

	@Override
	public MCDisplay.Billboard getBillboard() {
		return MCDisplay.Billboard.valueOf(this.d.getBillboard().name());
	}

	@Override
	public void setBillboard(MCDisplay.Billboard billboard) {
		this.d.setBillboard(Display.Billboard.valueOf(billboard.name()));
	}

	@Override
	public MCDisplay.Brightness getBrightness() {
		Display.Brightness brightness = this.d.getBrightness();
		if(brightness == null) {
			return null;
		}
		return new MCDisplay.Brightness(brightness.getBlockLight(), brightness.getSkyLight());
	}

	@Override
	public void setBrightness(MCDisplay.Brightness brightness) {
		if(brightness == null) {
			this.d.setBrightness(null);
		} else {
			this.d.setBrightness(new Display.Brightness(brightness.block(), brightness.sky()));
		}
	}

	@Override
	public MCColor getGlowColorOverride() {
		Color color = this.d.getGlowColorOverride();
		if(color == null) {
			return null;
		}
		return BukkitMCColor.GetMCColor(color);
	}

	@Override
	public void setGlowColorOverride(MCColor color) {
		if(color == null) {
			this.d.setGlowColorOverride(null);
		} else {
			this.d.setGlowColorOverride(BukkitMCColor.GetColor(color));
		}
	}

	@Override
	public float getDisplayHeight() {
		return this.d.getDisplayHeight();
	}

	@Override
	public void setDisplayHeight(float height) {
		this.d.setDisplayHeight(height);
	}

	@Override
	public float getDisplayWidth() {
		return this.d.getDisplayWidth();
	}

	@Override
	public void setDisplayWidth(float width) {
		this.d.setDisplayWidth(width);
	}

	@Override
	public int getInterpolationDurationTicks() {
		return this.d.getInterpolationDuration();
	}

	@Override
	public void setInterpolationDurationTicks(int ticks) {
		this.d.setInterpolationDuration(ticks);
	}

	@Override
	public int getInterpolationDelayTicks() {
		return this.d.getInterpolationDelay();
	}

	@Override
	public void setInterpolationDelayTicks(int ticks) {
		this.d.setInterpolationDuration(ticks);
	}

	@Override
	public int getTeleportDuration() {
		return this.d.getTeleportDuration();
	}

	@Override
	public void setTeleportDuration(int ticks) {
		this.d.setTeleportDuration(ticks);
	}

	@Override
	public float getShadowRadius() {
		return this.d.getShadowRadius();
	}

	@Override
	public void setShadowRadius(float radius) {
		this.d.setShadowRadius(radius);
	}

	@Override
	public float getShadowStrength() {
		return this.d.getShadowStrength();
	}

	@Override
	public void setShadowStrength(float strength) {
		this.d.setShadowStrength(strength);
	}

	@Override
	public float getViewRange() {
		return this.d.getViewRange();
	}

	@Override
	public void setViewRange(float range) {
		this.d.setViewRange(range);
	}
}
