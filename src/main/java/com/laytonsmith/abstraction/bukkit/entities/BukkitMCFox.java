package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCFox;
import com.laytonsmith.abstraction.enums.MCFoxType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;

public class BukkitMCFox extends BukkitMCLivingEntity implements MCFox {

	Fox f;

	public BukkitMCFox(Entity fox) {
		super(fox);
		this.f = (Fox) fox;
	}

	@Override
	public Fox getHandle() {
		return f;
	}

	@Override
	public MCFoxType getVariant() {
		return MCFoxType.valueOf(f.getFoxType().name());
	}

	@Override
	public void setVariant(MCFoxType type) {
		f.setFoxType(Fox.Type.valueOf(type.name()));
	}

	@Override
	public boolean isCrouching() {
		return f.isCrouching();
	}

	@Override
	public void setCrouching(boolean crouching) {
		f.setCrouching(crouching);
	}

	@Override
	public boolean isSitting() {
		return f.isSitting();
	}

	@Override
	public void setSitting(boolean sitting) {
		f.setSitting(sitting);
	}
}
