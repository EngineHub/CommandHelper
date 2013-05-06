

package com.laytonsmith.abstraction.blocks;

/**
 *
 * @author layton
 */
public interface MCMaterial {
    short getMaxDurability();

    public int getType();

    public int getMaxStackSize();

	public boolean hasGravity();
	public boolean isBlock();
	public boolean isBurnable();
	public boolean isEdible();
	public boolean isFlammable();
	public boolean isOccluding();
	public boolean isRecord();
	public boolean isSolid();
	public boolean isTransparent();
}
