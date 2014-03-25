

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.MCMetadatable;

/**
 *
 * @author layton
 */
public interface MCBlockState extends MCMetadatable {

    public MCMaterialData getData();
    
    public int getTypeId();
    
}
