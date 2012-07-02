package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCHumanEntity extends MCLivingEntity, MCAnimalTamer{
	public void closeInventory();
	public MCGameMode getGameMode();
	//public MCPlayerInventory getInventory();
	public MCItemStack getItemInHand();
	public MCItemStack getItemOnCursor();
	public String getName();
	//public MCInventoryView getOpenInventory();
	public int getSleepTicks();
	public boolean isBlocking();
	public boolean isSleeping();
	//public MCInventoryView openEnchanting(MCLocation location, boolean force);
	//public MCInventoryView openInventory(MCInventory inventory);
	//public void openInventory(MCInventoryView inventory);
	//public MCInventoryView	openWorkbench(MCLocation location, boolean force);
	void	setGameMode(MCGameMode mode);
	void	setItemInHand(MCItemStack item);
	void	setItemOnCursor(MCItemStack item);
	//public boolean setWindowProperty(MCInventoryView.Property prop, int value);
}
