package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.blocks.MCBanner;
import com.laytonsmith.abstraction.bukkit.BukkitMCPattern;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCBanner extends BukkitMCBlockState implements MCBanner {

	Banner b;

	public BukkitMCBanner(Banner block) {
		super(block);
		b = block;
	}

	@Override
	public MCDyeColor getBaseColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(b.getBaseColor());
	}

	@Override
	public void setBaseColor(MCDyeColor color) {
		b.setBaseColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public int numberOfPatterns() {
		return b.numberOfPatterns();
	}

	@Override
	public List<MCPattern> getPatterns() {
		List<Pattern> bukkitPatterns = b.getPatterns();
		List<MCPattern> patterns = new ArrayList<>(bukkitPatterns.size());
		for(Pattern p : bukkitPatterns) {
			patterns.add(new BukkitMCPattern(p));
		}
		return patterns;
	}

	@Override
	public void addPattern(MCPattern pattern) {
		b.addPattern((Pattern) pattern.getHandle());
	}
}
