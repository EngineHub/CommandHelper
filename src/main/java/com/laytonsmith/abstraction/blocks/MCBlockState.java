

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCMaterialData;

/**
 *
 * @author layton
 */
public interface MCBlockState {

    public MCMaterialData getData();
    
    public int getTypeId();
    
}
