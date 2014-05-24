
package com.laytonsmith.abstraction;

/**
 *
 * 
 */
public interface MCTNT extends MCEntity {
	MCEntity getSource();
	int getFuseTicks();
	void setFuseTicks(int ticks);
}
