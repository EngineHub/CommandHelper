package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.blocks.MCBeacon;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLivingEntity;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.block.Beacon;
import org.bukkit.entity.LivingEntity;

public class BukkitMCBeacon extends BukkitMCBlockState implements MCBeacon {

	private Beacon beacon;

	public BukkitMCBeacon(Beacon block) {
		super(block);
		this.beacon = block;
	}

	@Override
	public Collection<MCLivingEntity> getEntitiesInRange() {
		HashSet<MCLivingEntity> ret = new HashSet<>();
		for(LivingEntity entity : this.beacon.getEntitiesInRange()) {
			ret.add(new BukkitMCLivingEntity(entity));
		}
		return ret;
	}

//	@Override
//	public MCPotionEffect getPrimaryEffect() {
//		// TODO Implement.
//		return null;
//	}
//
//	@Override
//	public MCPotionEffect getSecondaryEffect() {
//		// TODO Implement.
//		return null;
//	}

	@Override
	public int getTier() {
		return this.beacon.getTier();
	}

//	@Override
//	public void setPrimaryEffect(MCPotionEffect effect) {
//		this.beacon.setPrimaryEffect(effect.getHandle());
//	}
//
//	@Override
//	public void setSecondaryEffect(MCPotionEffect effect) {
//		this.beacon.setSecondaryEffect(effect.getHandle());
//	}
}
