package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCGameMode;

/**
 *
 * @author layton
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
