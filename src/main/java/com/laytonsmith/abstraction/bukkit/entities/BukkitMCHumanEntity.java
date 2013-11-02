package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventoryView;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCGameMode;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author layton
 */
public class BukkitMCHumanEntity extends BukkitMCLivingEntity implements MCHumanEntity {

	public BukkitMCHumanEntity(HumanEntity human) {
		super(human);
	}

	public BukkitMCHumanEntity(AbstractionObject ao) {
		this((HumanEntity) ao.getHandle());
	}

	@Override
	public HumanEntity getHandle() {
		return (HumanEntity) metadatable;
	}

	public String getName() {
		return getHandle().getName();
	}

	public void closeInventory() {
		getHandle().closeInventory();
	}

	public MCGameMode getGameMode() {
		return BukkitMCGameMode.getConvertor().getAbstractedEnum(getHandle().getGameMode());
	}

	public MCItemStack getItemInHand() {
		HumanEntity hu = getHandle();
        if (hu == null || hu.getItemInHand() == null) {
            return null;
        } else {
			return new BukkitMCItemStack(hu.getItemInHand());
		}
    }

	public MCItemStack getItemOnCursor() {
		return new BukkitMCItemStack(getHandle().getItemOnCursor());
	}

	public int getSleepTicks() {
		return getHandle().getSleepTicks();
	}

	public boolean isBlocking() {
		return getHandle().isBlocking();
	}

	public boolean isSleeping() {
		return getHandle().isSleeping();
	}

	public void setGameMode(MCGameMode mode) {
		getHandle().setGameMode(BukkitMCGameMode.getConvertor().getConcreteEnum(mode));
	}

	public void setItemInHand(MCItemStack item) {
		getHandle().setItemInHand(((BukkitMCItemStack)item).asItemStack());
	}

	public void setItemOnCursor(MCItemStack item) {
		getHandle().setItemOnCursor(((BukkitMCItemStack)item).asItemStack());
	}

	public MCInventoryView openInventory(MCInventory inventory) {
		return new BukkitMCInventoryView(getHandle().openInventory((Inventory)inventory.getHandle()));
	}

	public MCInventoryView getOpenInventory() {
		return new BukkitMCInventoryView(getHandle().getOpenInventory());
	}

	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}

	public MCInventory getEnderChest() {
		return new BukkitMCInventory(getHandle().getEnderChest());
	}
	
	public MCInventoryView openWorkbench(MCLocation loc, boolean force) {
		return new BukkitMCInventoryView(getHandle().openWorkbench((Location)loc.getHandle(), force));
	}
	
	public MCInventoryView openEnchanting(MCLocation loc, boolean force) {
		return new BukkitMCInventoryView(getHandle().openEnchanting((Location)loc.getHandle(), force));
	}
}
