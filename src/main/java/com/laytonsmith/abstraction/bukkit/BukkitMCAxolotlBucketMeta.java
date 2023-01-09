package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCAxolotlBucketMeta;
import com.laytonsmith.abstraction.enums.MCAxolotlType;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.meta.AxolotlBucketMeta;

public class BukkitMCAxolotlBucketMeta extends BukkitMCItemMeta implements MCAxolotlBucketMeta {

	AxolotlBucketMeta sm;

	public BukkitMCAxolotlBucketMeta(AxolotlBucketMeta im) {
		super(im);
		this.sm = im;
	}

	@Override
	public MCAxolotlType getAxolotlType() {
		if(!sm.hasVariant()) {
			return MCAxolotlType.LUCY;
		}
		return MCAxolotlType.valueOf(sm.getVariant().name());
	}

	@Override
	public void setAxolotlType(MCAxolotlType type) {
		sm.setVariant(Axolotl.Variant.valueOf(type.name()));
	}
}
