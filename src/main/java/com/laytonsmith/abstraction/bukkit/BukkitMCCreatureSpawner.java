package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.core.Static;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class BukkitMCCreatureSpawner extends BukkitMCBlockState implements MCCreatureSpawner {

	CreatureSpawner cs;

	public BukkitMCCreatureSpawner(CreatureSpawner cs) {
		super(cs);
		this.cs = cs;
	}

	@Override
	public MCEntityType getSpawnedType() {
		EntityType type = cs.getSpawnedType();
		if(type == null) {
			return null;
		}
		return BukkitMCEntityType.valueOfConcrete(type);
	}

	@Override
	public void setSpawnedType(MCEntityType type) {
		if(type == null) {
			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20)) {
				cs.setSpawnedType(null);
			} else {
				cs.setSpawnedType(EntityType.PIG); // null unsupported
			}
		} else {
			cs.setSpawnedType(((BukkitMCEntityType) type).getConcrete());
		}
		cs.update();
	}

	@Override
	public int getDelay() {
		return cs.getDelay();
	}

	@Override
	public void setDelay(int delay) {
		cs.setDelay(delay);
		cs.update();
	}

	@Override
	public int getMinDelay() {
		return cs.getMinSpawnDelay();
	}

	@Override
	public void setMinDelay(int delay) {
		cs.setMinSpawnDelay(delay);
	}

	@Override
	public int getMaxDelay() {
		return cs.getMaxSpawnDelay();
	}

	@Override
	public void setMaxDelay(int delay) {
		cs.setMaxSpawnDelay(delay);
	}

	@Override
	public int getSpawnCount() {
		return cs.getSpawnCount();
	}

	@Override
	public void setSpawnCount(int count) {
		cs.setSpawnCount(count);
	}

	@Override
	public int getMaxNearbyEntities() {
		return cs.getMaxNearbyEntities();
	}

	@Override
	public void setMaxNearbyEntities(int max) {
		cs.setMaxNearbyEntities(max);
	}

	@Override
	public int getPlayerRange() {
		return cs.getRequiredPlayerRange();
	}

	@Override
	public void setPlayerRange(int range) {
		cs.setRequiredPlayerRange(range);
	}

	@Override
	public int getSpawnRange() {
		return cs.getSpawnRange();
	}

	@Override
	public void setSpawnRange(int range) {
		cs.setSpawnRange(range);
	}
}
