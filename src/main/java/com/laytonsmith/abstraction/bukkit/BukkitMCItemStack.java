

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
public class BukkitMCItemStack implements MCItemStack {
    ItemStack is;
    public BukkitMCItemStack(ItemStack is){
        this.is = is;
    }
    
    public BukkitMCItemStack(AbstractionObject a){
        this((ItemStack)null);
        if(a instanceof MCItemStack){
            this.is = ((ItemStack)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public Object getHandle(){
        return is;
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
        if(is == null){
            return;
        }
        is.setDurability(data);
    }

    public void addEnchantment(MCEnchantment e, int level) {        
        if(is == null){
            return;
        }
        is.addEnchantment(((BukkitMCEnchantment)e).__Enchantment(), level);
    }
    
    public void addUnsafeEnchantment(MCEnchantment e, int level){
        if(is == null){
            return;
        }
        is.addUnsafeEnchantment(((BukkitMCEnchantment)e).__Enchantment(), level);
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
        if(is == null){
            return;
        }
        is.removeEnchantment(((BukkitMCEnchantment)e).__Enchantment());
    }

    public MCMaterial getType() {
        if(is == null){
            return null;
        }
        return new BukkitMCMaterial(is.getType());
    }

    public void setTypeId(int type) {
        if(is == null){
            return;
        }
        is.setTypeId(type);
    }

    public int getAmount() {
        if(is == null){
            return 0;
        }
        return is.getAmount();
    }

    public ItemStack __ItemStack() {
        return is;
    }

    public void setData(int data) {
        if(is == null){
            return;
        }
        is.setData(new MaterialData(is.getTypeId(), (byte)data));
    }

    public int maxStackSize() {
        if(is == null){
            return 0;
        }
        return is.getMaxStackSize();
    }

    public ItemStack asItemStack() {
        return is;
    }
	
	@Override
	public String toString() {
		return is.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCItemStack?is.equals(((BukkitMCItemStack)obj).is):false);
	}

	@Override
	public int hashCode() {
		return is.hashCode();
	}

	public boolean hasItemMeta() {
		return is.hasItemMeta();
	}
	
	public MCItemMeta getItemMeta() {
		return BukkitConvertor.BukkitGetCorrectMeta(is.getItemMeta());
	}

	public void setItemMeta(MCItemMeta im) {
		is.setItemMeta(((BukkitMCItemMeta)im).im);
	}
}
