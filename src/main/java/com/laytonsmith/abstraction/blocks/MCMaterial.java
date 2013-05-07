

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.AbstractionObject;

/**
 *
 * @author layton
 */
public interface MCMaterial extends AbstractionObject {
    short getMaxDurability();

    public int getType();

    public int getMaxStackSize();
}
