package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCBoat;
import com.laytonsmith.abstraction.enums.MCTreeSpecies;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeSpecies;
import com.laytonsmith.core.Static;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;

public class BukkitMCBoat extends BukkitMCVehicle implements MCBoat {

	Boat b;

	public BukkitMCBoat(Entity e) {
		super(e);
		this.b = (Boat) e;
	}

	@Override
	public MCTreeSpecies getWoodType() {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
			return MCTreeSpecies.valueOf(b.getBoatType().name());
		}
		return BukkitMCTreeSpecies.getConvertor().getAbstractedEnum(b.getWoodType());
	}

	@Override
	public void setWoodType(MCTreeSpecies type) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19)) {
			b.setBoatType(Boat.Type.valueOf(type.name()));
		} else {
			b.setWoodType(BukkitMCTreeSpecies.getConvertor().getConcreteEnum(type));
		}
	}

}
