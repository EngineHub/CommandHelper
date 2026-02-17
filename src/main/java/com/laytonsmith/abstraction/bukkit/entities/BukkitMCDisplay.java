package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.bukkit.BukkitMCColor;
import com.laytonsmith.abstraction.entities.MCDisplay;
import com.laytonsmith.abstraction.entities.MCTransformation;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.joml.Matrix4f;

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
		this.d.setInterpolationDelay(ticks);
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

	@Override
	public MCTransformation getTransformation() {
		return new BukkitMCTransformation(this.d.getTransformation());
	}

	@Override
	public void setTransformation(MCTransformation transformation) {
		this.d.setTransformation(((BukkitMCTransformation) transformation).transformation);
	}

	@Override
	public void setTransformationMatrix(float[] mtrxf) {
		// Note that the order of the matrix is flipped about the identity of the
		// matrix. This allows us to accept inputs that are the same order as the
		// /data command would accept.
		Matrix4f matrix = new Matrix4f(
				mtrxf[0], mtrxf[4], mtrxf[8], mtrxf[12],
				mtrxf[1], mtrxf[5], mtrxf[9], mtrxf[13],
				mtrxf[2], mtrxf[6], mtrxf[10], mtrxf[14],
				mtrxf[3], mtrxf[7], mtrxf[11], mtrxf[15]);
		this.d.setTransformationMatrix(matrix);
	}
}
