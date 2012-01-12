/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

import com.laytonsmith.puls3.abstraction.blocks.MCMaterial;
import java.util.Map;

/**
 *
 * @author layton
 */
public interface MCItemStack {
    public MCMaterialData getData();
    public short getDurability();
    public int getTypeId();
    public void setDurability(short data);
    public void addEnchantment(MCEnchantment e, int level);
    public Map<MCEnchantment, Integer> getEnchantments();
    public void removeEnchantment(MCEnchantment e);
    public MCMaterial getType();
    public void setTypeId(int type);
    
    public int getAmount();
}
