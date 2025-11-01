package com.laytonsmith.abstraction;
public interface MCLeashable {
	MCEntity getLeashHolder();
	boolean isLeashed();
	void setLeashHolder(MCEntity holder);
}
