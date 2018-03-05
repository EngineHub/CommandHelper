package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCGameMode;

public interface MCHumanEntity extends MCInventoryHolder, MCLivingEntity, MCAnimalTamer {

	void closeInventory();

	MCGameMode getGameMode();

	MCItemStack getItemInHand();

	MCItemStack getItemOnCursor();

	@Override
	String getName();

	int getSleepTicks();

	boolean isBlocking();

	boolean isSleeping();

	MCInventoryView openEnchanting(MCLocation location, boolean force);

	MCInventoryView openInventory(MCInventory inventory);

	MCInventoryView getOpenInventory();

	MCInventory getEnderChest();

	MCInventoryView openWorkbench(MCLocation loc, boolean force);

	//MCInventoryView	openWorkbench(MCLocation location, boolean force);
	void setGameMode(MCGameMode mode);

	void setItemInHand(MCItemStack item);

	void setItemOnCursor(MCItemStack item);

	int getCooldown(MCMaterial material);

	void setCooldown(MCMaterial material, int ticks);
	//boolean setWindowProperty(MCInventoryView.Property prop, int value);
}
