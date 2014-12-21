package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.entities.MCGuardian;
import org.bukkit.entity.Guardian;

public class BukkitMCGuardian extends BukkitMCLivingEntity
		implements MCGuardian {

	Guardian e;
	public BukkitMCGuardian(Guardian ent) {
		super(ent);
		e = ent;
	}

	@Override
	public boolean isElder() {
		return e.isElder();
	}

	@Override
	public void setElder(boolean shouldBeElder) {
		e.setElder(shouldBeElder);
	}
}
