/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
