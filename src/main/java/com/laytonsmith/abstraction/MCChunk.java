package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.entities.MCEntity;
import java.util.List;

/**
 *
 * @author import
 */
public interface MCChunk extends AbstractionObject {
	public int getX();
	public int getZ();
	public MCWorld getWorld();
	public List<MCEntity> getEntities();
}