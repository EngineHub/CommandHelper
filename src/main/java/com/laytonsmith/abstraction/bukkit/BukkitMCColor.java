package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import org.bukkit.Color;

public final class BukkitMCColor implements MCColor {

	private static final BukkitMCColor BUILDER = new BukkitMCColor();

	public static MCColor GetMCColor(Color c) {
		return BUILDER.build(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	public static Color GetColor(MCColor c) {
		return Color.fromARGB(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
	}

	private BukkitMCColor() {
	}

	private int red;
	private int green;
	private int blue;
	private int alpha;

	@Override
	public int getAlpha() {
		return alpha;
	}

	@Override
	public int getRed() {
		return red;
	}

	@Override
	public int getGreen() {
		return green;
	}

	@Override
	public int getBlue() {
		return blue;
	}

	@Override
	public MCColor build(int red, int green, int blue) {
		BukkitMCColor color = new BukkitMCColor();
		color.alpha = 255;
		color.red = red;
		color.green = green;
		color.blue = blue;
		return color;
	}

	@Override
	public MCColor build(int red, int green, int blue, int alpha) {
		BukkitMCColor color = new BukkitMCColor();
		color.alpha = alpha;
		color.red = red;
		color.green = green;
		color.blue = blue;
		return color;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 11 * hash + this.alpha;
		hash = 11 * hash + this.red;
		hash = 11 * hash + this.green;
		hash = 11 * hash + this.blue;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final BukkitMCColor other = (BukkitMCColor) obj;
		if(this.red != other.red) {
			return false;
		}
		if(this.green != other.green) {
			return false;
		}
		if(this.blue != other.blue) {
			return false;
		}
		if(this.alpha != other.alpha) {
			return false;
		}
		return true;
	}
}
