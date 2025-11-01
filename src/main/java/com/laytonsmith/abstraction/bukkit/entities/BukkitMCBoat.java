package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
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

	@Override
	public MCEntity getLeashHolder() {
		return new BukkitMCEntity(b.getLeashHolder());
	}

	@Override
	public boolean isLeashed() {
		if(((BukkitMCServer) Static.getServer()).isPaper()
				&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_3)
				|| Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_10)) {
			return b.isLeashed();
		}
		return false;
	}

	@Override
	public void setLeashHolder(MCEntity holder) {
		if(((BukkitMCServer) Static.getServer()).isPaper()
				&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_3)
				|| Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_10)) {
			if(holder == null) {
				b.setLeashHolder(null);
			} else {
				b.setLeashHolder((Entity) holder.getHandle());
			}
		}
	}
}
