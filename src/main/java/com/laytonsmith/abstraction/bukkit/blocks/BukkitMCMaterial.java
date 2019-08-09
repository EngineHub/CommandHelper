package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import org.bukkit.Material;

public class BukkitMCMaterial implements MCMaterial {

	Material m;

	public BukkitMCMaterial(Material type) {
		this.m = type;
	}

	@Override
	public MCBlockData createBlockData() {
		return new BukkitMCBlockData(m.createBlockData());
	}

	@Override
	public short getMaxDurability() {
		return this.m.getMaxDurability();
	}

	@Override
	public int getType() {
		return m.getId();
	}

	@Override
	public String getName() {
		return m.name();
	}

	@Override
	public int getMaxStackSize() {
		return m.getMaxStackSize();
	}

	@Override
	public boolean hasGravity() {
		return m.hasGravity();
	}

	@Override
	public boolean isBlock() {
		return m.isBlock();
	}

	@Override
	public boolean isBurnable() {
		return m.isBurnable();
	}

	@Override
	public boolean isEdible() {
		return m.isEdible();
	}

	@Override
	public boolean isFlammable() {
		return m.isFlammable();
	}

	@Override
	public boolean isOccluding() {
		return m.isOccluding();
	}

	@Override
	public boolean isRecord() {
		return m.isRecord();
	}

	@Override
	public boolean isSolid() {
		return m.isSolid();
	}

	@Override
	public boolean isTransparent() {
		return m.isTransparent();
	}

	@Override
	public boolean isInteractable() {
		return m.isInteractable();
	}

	@Override
	public boolean isLegacy() {
		return m.isLegacy();
	}

	@Override
	public float getHardness() {
		return m.getHardness();
	}

	@Override
	public float getBlastResistance() {
		return m.getBlastResistance();
	}

	@Override
	public Material getHandle() {
		return m;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MCMaterial && m.equals(((MCMaterial) obj).getHandle());
	}

	@Override
	public int hashCode() {
		return m.hashCode();
	}

	@Override
	public String toString() {
		return m.toString();
	}

}
