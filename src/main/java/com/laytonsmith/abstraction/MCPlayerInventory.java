
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCPlayerInventory extends MCInventory {
    public void setHelmet(MCItemStack stack);
    public void setChestplate(MCItemStack stack);
    public void setLeggings(MCItemStack stack);
    public void setBoots(MCItemStack stack);
    public MCItemStack getHelmet();
    public MCItemStack getChestplate();
    public MCItemStack getLeggings();
    public MCItemStack getBoots();
	public int getHeldItemSlot();
	public void setHeldItemSlot(int slot);
}
