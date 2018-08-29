package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCTropicalFishBucketMeta;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCTropicalFish;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;

public class BukkitMCTropicalFishBucketMeta extends BukkitMCItemMeta implements MCTropicalFishBucketMeta {

	TropicalFishBucketMeta meta;

	public BukkitMCTropicalFishBucketMeta(TropicalFishBucketMeta im) {
		super(im);
		meta = im;
	}

	@Override
	public MCDyeColor getPatternColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(meta.getPatternColor());
	}

	@Override
	public void setPatternColor(MCDyeColor color) {
		meta.setPatternColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public MCDyeColor getBodyColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(meta.getBodyColor());
	}

	@Override
	public void setBodyColor(MCDyeColor color) {
		meta.setBodyColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public MCTropicalFish.MCPattern getPattern() {
		return BukkitMCTropicalFish.BukkitMCPattern.getConvertor().getAbstractedEnum(meta.getPattern());
	}

	@Override
	public void setPattern(MCTropicalFish.MCPattern pattern) {
		meta.setPattern(BukkitMCTropicalFish.BukkitMCPattern.getConvertor().getConcreteEnum(pattern));
	}

	@Override
	public boolean hasVariant() {
		return meta.hasVariant();
	}
}
