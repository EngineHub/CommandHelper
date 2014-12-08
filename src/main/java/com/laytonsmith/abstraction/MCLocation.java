
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * 
 */
public interface MCLocation extends AbstractionObject{
    public double getX();
    public double getY();
    public double getZ();
	public double distance(MCLocation o);
	public double distanceSquared(MCLocation o);
    public MCWorld getWorld();
    public float getYaw();
    public float getPitch();
    public int getBlockX();
    public int getBlockY();
    public int getBlockZ();
	public MCChunk getChunk();
    public MCBlock getBlock();
	public MCLocation add(MCLocation vec);

	public MCLocation add(MVector3D vec);
	public MCLocation add(double x, double y, double z);
	public MCLocation multiply(double m);

	public MVector3D toVector();
	public MCLocation subtract(MCLocation vec);

	public MCLocation subtract(MVector3D vec);
	public MCLocation subtract(double x, double y, double z);

	public void setX(double x);
	public void setY(double y);
	public void setZ(double z);
    public void setPitch(float p);
    public void setYaw(float y);
	public void breakBlock();

	public MVector3D getDirection();

    public MCLocation clone();
}
