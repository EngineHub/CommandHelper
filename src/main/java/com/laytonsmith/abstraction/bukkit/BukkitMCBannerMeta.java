package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCBannerMeta;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCBannerMeta extends BukkitMCItemMeta implements MCBannerMeta {

	BannerMeta bm;
	public BukkitMCBannerMeta(BannerMeta meta) {
		super(meta);
		bm = meta;
	}

	@Override
	public Object getHandle() {
		return bm;
	}

	@Override
	public void addPattern(MCPattern pattern) {
		bm.addPattern((Pattern) pattern.getHandle());
	}

	@Override
	public MCDyeColor getBaseColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(bm.getBaseColor());
	}

	@Override
	public MCPattern getPattern(int i) {
		return new BukkitMCPattern(bm.getPattern(i));
	}

	@Override
	public List<MCPattern> getPatterns() {
		List<Pattern> bukkitPatterns = bm.getPatterns();
		List<MCPattern> patterns = new ArrayList<>(bukkitPatterns.size());
		for(Pattern p : bukkitPatterns) {
			patterns.add(new BukkitMCPattern(p));
		}
		return patterns;
	}

	@Override
	public int numberOfPatterns() {
		return bm.numberOfPatterns();
	}

	@Override
	public void removePattern(int i) {
		bm.removePattern(i);
	}

	@Override
	public void setBaseColor(MCDyeColor color) {
		bm.setBaseColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public void setPattern(int i, MCPattern pattern) {
		bm.setPattern(i, (Pattern) pattern.getHandle());
	}

	@Override
	public void setPatterns(List<MCPattern> patterns) {
		List<Pattern> bukkitPatterns = new ArrayList<>(patterns.size());
		for(MCPattern pattern : patterns) {
			bukkitPatterns.add((Pattern) pattern.getHandle());
		}
		bm.setPatterns(bukkitPatterns);
	}

}
