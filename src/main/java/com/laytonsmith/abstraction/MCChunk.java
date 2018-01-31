package com.laytonsmith.abstraction;

public interface MCChunk extends AbstractionObject {
	int getX();
	int getZ();
	MCWorld getWorld();
	MCEntity[] getEntities();
}
