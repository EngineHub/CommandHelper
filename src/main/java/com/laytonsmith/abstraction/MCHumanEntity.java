package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCGameMode;

/**
 *
 */
public interface MCHumanEntity extends MCInventoryHolder, MCLivingEntity, MCAnimalTamer{
	public void closeInventory();
	public MCGameMode getGameMode();
	public MCItemStack getItemInHand();
	public MCItemStack getItemOnCursor();
	public String getName();
	public int getSleepTicks();
	public boolean isBlocking();
	public boolean isSleeping();
	public MCInventoryView openEnchanting(MCLocation location, boolean force);
	public MCInventoryView openInventory(MCInventory inventory);
	public MCInventoryView getOpenInventory();
	public MCInventory getEnderChest();
	public MCInventoryView openWorkbench(MCLocation loc, boolean force);
	//public MCInventoryView	openWorkbench(MCLocation location, boolean force);
	void	setGameMode(MCGameMode mode);
	void	setItemInHand(MCItemStack item);
	void	setItemOnCursor(MCItemStack item);
	
	//public boolean setWindowProperty(MCInventoryView.Property prop, int value);
}
