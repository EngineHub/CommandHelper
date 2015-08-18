package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPatternShape;
import org.bukkit.block.banner.Pattern;

public class BukkitMCPattern implements MCPattern {

	Pattern pattern;
	public BukkitMCPattern(Pattern p) {
		pattern = p;
	}

	@Override
	public Object getHandle() {
		return pattern;
	}

	@Override
	public MCDyeColor getColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(pattern.getColor());
	}

	@Override
	public MCPatternShape getShape() {
		return BukkitMCPatternShape.getConvertor().getAbstractedEnum(pattern.getPattern());
	}

	@Override
	public String toString() {
		return pattern.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCPattern && pattern.equals(((BukkitMCPattern)obj).pattern);
	}

	@Override
	public int hashCode() {
		return pattern.hashCode();
	}

}
