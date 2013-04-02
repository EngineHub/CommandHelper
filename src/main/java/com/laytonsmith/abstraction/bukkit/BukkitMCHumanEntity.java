package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCGameMode;
import org.bukkit.Location;
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
	
	public HumanEntity asHumanEntity() {
		return he;
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

	public MCInventoryView getOpenInventory() {
		return new BukkitMCInventoryView(he.getOpenInventory());
	}

	public MCInventory getInventory() {
		return new BukkitMCInventory(he.getInventory());
	}

	public MCInventory getEnderChest() {
		return new BukkitMCInventory(he.getEnderChest());
	}
	
	public MCInventoryView openWorkbench(MCLocation loc, boolean force) {
		return new BukkitMCInventoryView(he.openWorkbench((Location)loc.getHandle(), force));
	}
	
	public MCInventoryView openEnchanting(MCLocation loc, boolean force) {
		return new BukkitMCInventoryView(he.openEnchanting((Location)loc.getHandle(), force));
	}
}
