package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCAxolotl;
import com.laytonsmith.abstraction.enums.MCAxolotlType;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;

public class BukkitMCAxolotl extends BukkitMCLivingEntity implements MCAxolotl {

	Axolotl a;

	public BukkitMCAxolotl(Entity be) {
		super(be);
		this.a = (Axolotl) be;
	}

	@Override
	public boolean isPlayingDead() {
		return a.isPlayingDead();
	}

	@Override
	public void setPlayingDead(boolean playingDead) {
		a.setPlayingDead(playingDead);
	}

	@Override
	public MCAxolotlType getAxolotlType() {
		return MCAxolotlType.valueOf(a.getVariant().name());
	}

	@Override
	public void setAxolotlType(MCAxolotlType type) {
		a.setVariant(Axolotl.Variant.valueOf(type.name()));
	}
}
