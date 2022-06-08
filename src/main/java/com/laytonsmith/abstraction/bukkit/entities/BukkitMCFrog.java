package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCFrog;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;

public class BukkitMCFrog extends BukkitMCLivingEntity implements MCFrog {

	Frog f;

	public BukkitMCFrog(Entity frog) {
		super(frog);
		this.f = (Frog) frog;
	}

	@Override
	public Frog getHandle() {
		return f;
	}

	@Override
	public MCFrogType getFrogType() {
		return MCFrogType.valueOf(f.getVariant().name());
	}

	@Override
	public void setFrogType(MCFrogType type) {
		f.setVariant(Variant.valueOf(type.name()));
	}

	@Override
	public MCEntity getTongueTarget() {
		Entity target = f.getTongueTarget();
		if(target == null) {
			return null;
		}
		return BukkitConvertor.BukkitGetCorrectEntity(target);
	}

	@Override
	public void setTongueTarget(MCEntity target) {
		if(target == null) {
			f.setTongueTarget(null);
		} else {
			f.setTongueTarget((Entity) target.getHandle());
		}
	}
}
