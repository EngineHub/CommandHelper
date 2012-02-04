/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
public class BukkitMCItemStack implements MCItemStack{
    ItemStack is;
    public BukkitMCItemStack(ItemStack is){
        this.is = is;
    }
    
    public MCMaterialData getData(){
        if(is == null || is.getData() == null){
            return null;
        }
        return new BukkitMCMaterialData(is.getData());
    }
    
    public short getDurability(){
        if(is == null){
            return 0;
        }
        return is.getDurability();
    }
    
    public int getTypeId(){
        if(is == null){
            return 0;
        }
        return is.getTypeId();
    }

    public void setDurability(short data) {
        is.setDurability(data);
    }

    public void addEnchantment(MCEnchantment e, int level) {
        is.addEnchantment(((BukkitMCEnchantment)e).__Enchantment(), level);
    }
    
    public Map<MCEnchantment, Integer> getEnchantments(){
        if(is == null || is.getEnchantments() == null){
            return null;
        }
        Map<MCEnchantment, Integer> map = new HashMap<MCEnchantment, Integer>();
        for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()){
            map.put(new BukkitMCEnchantment(entry.getKey()), entry.getValue());
        }
        return map;
    }
    
    public void removeEnchantment(MCEnchantment e){
        is.removeEnchantment(((BukkitMCEnchantment)e).__Enchantment());
    }

    public MCMaterial getType() {
        return new BukkitMCMaterial(is.getType());
    }

    public void setTypeId(int type) {
        is.setTypeId(type);
    }

    public int getAmount() {
        return is.getAmount();
    }

    public ItemStack __ItemStack() {
        return is;
    }

    public void setData(int data) {
        is.setData(new MaterialData(is.getTypeId(), (byte)data));
    }
}
