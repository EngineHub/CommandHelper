

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

    public short getMaxDurability() {
        return this.m.getMaxDurability();
    }

    public int getType() {
        return m.getId();
    }

	public MCMaterialData getData() {
		return new BukkitMCMaterialData(new MaterialData(m));
	}

    public int getMaxStackSize() {
        return m.getMaxStackSize();
    }

	public boolean hasGravity() {
		return m.hasGravity();
	}

	public boolean isBlock() {
		return m.isBlock();
	}

	public boolean isBurnable() {
		return m.isBurnable();
	}

	public boolean isEdible() {
		return m.isEdible();
	}

	public boolean isFlammable() {
		return m.isFlammable();
	}

	public boolean isOccluding() {
		return m.isOccluding();
	}

	public boolean isRecord() {
		return m.isRecord();
	}

	public boolean isSolid() {
		return m.isSolid();
	}

	public boolean isTransparent() {
		return m.isTransparent();
	}
}
