
package com.laytonsmith.abstraction;

/**
 *
 * @author Layton
 */
public interface MCTNT extends MCEntity {
	MCEntity getSource();
	int getFuseTicks();
	void setFuseTicks(int ticks);
}
