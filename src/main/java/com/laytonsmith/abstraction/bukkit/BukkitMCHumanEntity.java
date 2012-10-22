package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCGameMode;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author layton
 */
public class BukkitMCHumanEntity extends BukkitMCLivingEntity implements MCHumanEntity {
    
    HumanEntity he;

    public BukkitMCHumanEntity(HumanEntity humanEntity) {
        super(humanEntity);
        he = humanEntity;
    }

    public String getName() {
        return he.getName();
    }

	public void closeInventory() {
		he.closeInventory();
	}

	public MCGameMode getGameMode() {
		return MCGameMode.valueOf(he.getGameMode().name());
	}

	public MCItemStack getItemInHand() {
        if (he == null || he.getItemInHand() == null) {
            return null;
        }
        
        return new BukkitMCItemStack(he.getItemInHand());
    }

	public MCItemStack getItemOnCursor() {
		return new BukkitMCItemStack(he.getItemOnCursor());
	}

	public int getSleepTicks() {
		return he.getSleepTicks();
	}

	public boolean isBlocking() {
		return he.isBlocking();
	}

	public boolean isSleeping() {
		return he.isSleeping();
	}

	public void setGameMode(MCGameMode mode) {
		he.setGameMode(org.bukkit.GameMode.valueOf(mode.name()));
	}

	public void setItemInHand(MCItemStack item) {
		he.setItemInHand(((BukkitMCItemStack)item).asItemStack());
	}

	public void setItemOnCursor(MCItemStack item) {
		he.setItemOnCursor(((BukkitMCItemStack)item).asItemStack());
	}

	public MCInventoryView openInventory(MCInventory inventory) {
		return new BukkitMCInventoryView(he.openInventory((Inventory)inventory.getHandle()));
	}
}
