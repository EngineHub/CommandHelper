

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
public class BukkitMCMaterial implements MCMaterial {
    Material m;

    public BukkitMCMaterial(Material type) {
        this.m = type;
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
	public MCMaterialData getData() {
		return new BukkitMCMaterialData(new MaterialData(m));
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
}
