package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCOcelot;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import org.bukkit.entity.Ocelot;

public class BukkitMCOcelot extends BukkitMCTameable implements MCOcelot {

	public BukkitMCOcelot(Ocelot ocelot) {
		super(ocelot);
	}

	public BukkitMCOcelot(AbstractionObject ao) {
		this((Ocelot) ao.getHandle());
	}

	@Override
	public Ocelot getHandle() {
		return (Ocelot) metadatable;
	}

	public MCOcelotType getCatType() {
		return MCOcelotType.valueOf(getHandle().getCatType().name());
	}

	public boolean isSitting() {
		return getHandle().isSitting();
	}

	public void setCatType(MCOcelotType type) {
		getHandle().setCatType(Ocelot.Type.valueOf(type.name()));
	}

	public void setSitting(boolean sitting) {
		getHandle().setSitting(sitting);
	}
}