

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import java.util.Collection;

/**
 *
 * @author layton
 */
public interface MCBlock {

    public int getTypeId();

    public byte getData();

    public void setTypeId(int idata);

    public void setData(byte imeta);

	public void setTypeAndData(int type, byte data, boolean physics);

    public MCBlockState getState();

    public MCMaterial getType();

    public MCWorld getWorld();

    public int getX();

    public int getY();

    public int getZ();

    public MCSign getSign();
	
	public MCLocation getLocation();

    public boolean isSign();

    public boolean isNull();
	
	public boolean isSolid();
	
	public boolean isFlammable();
	
	public boolean isTransparent();
	
	public boolean isOccluding();
	
	public boolean isBurnable();

    public Collection<MCItemStack> getDrops();

	public Collection<MCItemStack> getDrops(MCItemStack tool);

	public int getLightLevel();

	public int getBlockPower();
	
	public boolean isBlockPowered();

	public MCBlock getRelative(MCBlockFace face);
}
