

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.MCMetadatable;

/**
 *
 * 
 */
public interface MCBlockState extends MCMetadatable {

    public MCMaterialData getData();
    
    public int getTypeId();
    
}
