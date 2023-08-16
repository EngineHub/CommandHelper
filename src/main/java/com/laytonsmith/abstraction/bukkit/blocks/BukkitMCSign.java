package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.blocks.MCSignText;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.block.Sign;

public class BukkitMCSign extends BukkitMCBlockState implements MCSign {

	Sign s;

	public BukkitMCSign(Sign sign) {
		super(sign);
		this.s = sign;
	}

	@Override
	public Sign getHandle() {
		return s;
	}

	@Override
	public String[] getLines() {
		return s.getLines();
	}

	@Override
	public void setLine(int i, String line1) {
		s.setLine(i, line1);
	}

	@Override
	public String getLine(int i) {
		return s.getLine(i);
	}

	@Override
	public boolean isGlowingText() {
		try {
			return s.isGlowingText();
		} catch (NoSuchMethodError ex) {
			// probably before 1.17
		}
		return false;
	}

	@Override
	public void setGlowingText(boolean glowing) {
		try {
			s.setGlowingText(glowing);
		} catch (NoSuchMethodError ex) {
			// probably before 1.17
		}
	}

	@Override
	public MCDyeColor getDyeColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(s.getColor());
	}

	@Override
	public void setDyeColor(MCDyeColor color) {
		s.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public boolean isWaxed() {
		try {
			return s.isWaxed();
		} catch(NoSuchMethodError ex) {
			// probably before 1.20.1
			return false;
		}
	}

	@Override
	public void setWaxed(boolean waxed) {
		try {
			s.setWaxed(waxed);
		} catch(NoSuchMethodError ignore) {
			// probably before 1.20.1
		}
	}

	@Override
	public MCSignText getBackText() {
		try {
			return new BukkitMCSignText(s.getSide(org.bukkit.block.sign.Side.BACK));
		} catch(NoSuchMethodError | NoClassDefFoundError ex) {
			// probably before 1.20
			return null;
		}
	}

}
