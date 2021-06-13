package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCSign;
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
	public void setLine(int i, String line1) {
		s.setLine(i, line1);
		s.update();
	}

	@Override
	public String getLine(int i) {
		return s.getLine(i);
	}

	@Override
	public boolean isGlowingText() {
		return s.isGlowingText();
	}

	@Override
	public void setGlowingText(boolean glowing) {
		s.setGlowingText(glowing);
		s.update();
	}

}
