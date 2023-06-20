package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCSignText;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.block.sign.SignSide;

public class BukkitMCSignText implements MCSignText {

	SignSide ss;

	public BukkitMCSignText(SignSide sign) {
		this.ss = sign;
	}

	@Override
	public SignSide getHandle() {
		return ss;
	}

	@Override
	public String[] getLines() {
		return ss.getLines();
	}

	@Override
	public void setLine(int i, String line1) {
		ss.setLine(i, line1);
	}

	@Override
	public String getLine(int i) {
		return ss.getLine(i);
	}

	@Override
	public boolean isGlowingText() {
		return ss.isGlowingText();
	}

	@Override
	public void setGlowingText(boolean glowing) {
		ss.setGlowingText(glowing);
	}

	@Override
	public MCDyeColor getDyeColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(ss.getColor());
	}

	@Override
	public void setDyeColor(MCDyeColor color) {
		ss.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public String toString() {
		return ss.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCSignText && ss.equals(((BukkitMCSignText) obj).ss);
	}

	@Override
	public int hashCode() {
		return ss.hashCode();
	}

}
