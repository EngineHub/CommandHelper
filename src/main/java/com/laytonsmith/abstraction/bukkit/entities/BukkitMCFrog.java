package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCFrog;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;

import java.util.Locale;

public class BukkitMCFrog extends BukkitMCAnimal implements MCFrog {

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
		// changed from enum to interface in 1.21
		NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, f.getVariant(), "getKey");
		return MCFrogType.valueOf(key.getKey().toUpperCase(Locale.ROOT));
	}

	@Override
	public void setFrogType(MCFrogType type) {
		Frog.Variant v = Registry.FROG_VARIANT.get(NamespacedKey.minecraft(type.name().toLowerCase(Locale.ROOT)));
		if(v != null) {
			f.setVariant(v);
		}
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
