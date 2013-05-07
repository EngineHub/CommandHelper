

package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMaterialData;

/**
 *
 * @author layton
 */
public interface MCBlockState extends AbstractionObject {

    public MCMaterialData getData();
    
    public int getTypeId();
    
}
