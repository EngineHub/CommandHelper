
package com.laytonsmith.abstraction;

/**
 *
 * @author import
 */
public interface MCChunk extends AbstractionObject {
	public int getX();
	public int getZ();
	public MCWorld getWorld();
	public MCEntity[] getEntities();
}
