

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCWorld;
import java.util.Collection;

/**
 *
 * 
 */
public interface MCBlock extends MCMetadatable {

    public int getTypeId();

    public byte getData();

    public void setTypeId(int idata);

    public void setData(byte imeta);

	public void setTypeAndData(int type, byte data, boolean physics);

	public double getTemperature();

    public MCBlockState getState();

    public MCMaterial getType();

    public MCWorld getWorld();

    public int getX();

    public int getY();

    public int getZ();

    public MCSign getSign();
	
	public MCLocation getLocation();

    public boolean isSign();
	
	public MCCommandBlock getCommandBlock();
	
	public boolean isCommandBlock();

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

	public MCBlockFace getFace(MCBlock get);
}
