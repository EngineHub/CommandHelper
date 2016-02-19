
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCTone;

/**
 *
 * 
 */
public interface MCNote extends AbstractionObject {

	public MCTone getTone();

	public int getOctave();

	public boolean isSharped();

}
