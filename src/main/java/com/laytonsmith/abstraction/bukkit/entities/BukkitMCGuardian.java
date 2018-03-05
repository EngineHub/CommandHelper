package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCGuardian;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;

public class BukkitMCGuardian extends BukkitMCLivingEntity implements MCGuardian {

	Guardian e;

	public BukkitMCGuardian(Entity ent) {
		super(ent);
		e = (Guardian) ent;
	}

	@Override
	public boolean isElder() {
		return e.isElder();
	}

	@Override
	public void setElder(boolean shouldBeElder) {
		try {
			e.setElder(shouldBeElder);
		} catch (UnsupportedOperationException ex) {
			// 1.11 or later
			CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.ERROR,
					"Cannot change Guardian to ElderGuardian in Minecraft 1.11+", Target.UNKNOWN);
		}
	}
}
