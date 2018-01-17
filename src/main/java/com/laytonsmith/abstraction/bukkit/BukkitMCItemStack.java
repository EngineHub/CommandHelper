

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

	@Override
	public Object getHandle(){
		return is;
	}

	@Override
	public MCMaterialData getData(){
		if(is == null){
			return null;
		}
		MaterialData md = is.getData();
		if(md == null){
			return null;
		}
		return new BukkitMCMaterialData(md);
	}

	@Override
	public short getDurability(){
		if(is == null){
			return 0;
		}
		return is.getDurability();
	}

	@Override
	public int getTypeId(){
		if(is == null){
			return 0;
		}
		return is.getTypeId();
	}

	@Override
	public void setDurability(short data) {
		if(is == null){
			return;
		}
		is.setDurability(data);
	}

	@Override
	public void addEnchantment(MCEnchantment e, int level) {
		if(is == null){
			return;
		}
		is.addEnchantment(((BukkitMCEnchantment)e).__Enchantment(), level);
	}

	@Override
	public void addUnsafeEnchantment(MCEnchantment e, int level){
		if(is == null){
			return;
		}
		is.addUnsafeEnchantment(((BukkitMCEnchantment)e).__Enchantment(), level);
	}

	@Override
	public Map<MCEnchantment, Integer> getEnchantments(){
		Map<MCEnchantment, Integer> map = new HashMap<>();
		try {
			for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()){
				map.put(new BukkitMCEnchantment(entry.getKey()), entry.getValue());
			}
		} catch(NullPointerException npe) {
			// Probably invalid enchantment, always return map
		}
		return map;
	}

	@Override
	public void removeEnchantment(MCEnchantment e){
		if(is == null){
			return;
		}
		is.removeEnchantment(((BukkitMCEnchantment)e).__Enchantment());
	}

	@Override
	public MCMaterial getType() {
		if(is == null){
			return null;
		}
		return new BukkitMCMaterial(is.getType());
	}

	@Override
	public void setTypeId(int type) {
		if(is == null){
			return;
		}
		is.setTypeId(type);
	}

	@Override
	public int getAmount() {
		if(is == null){
			return 0;
		}
		return is.getAmount();
	}

	@Override
	public void setAmount(int amt) {
		if(is == null) {
			return;
		}
		is.setAmount(amt);
	}

	public ItemStack __ItemStack() {
		return is;
	}

	@Override
	public void setData(int data) {
		if(is == null){
			return;
		}
		is.setData(new MaterialData(is.getTypeId(), (byte)data));
	}

	@Override
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
		return obj instanceof BukkitMCItemStack && is.equals(((BukkitMCItemStack) obj).asItemStack());
	}

	@Override
	public int hashCode() {
		return is.hashCode();
	}

	@Override
	public boolean hasItemMeta() {
		return is.hasItemMeta();
	}
	
	@Override
	public MCItemMeta getItemMeta() {
		return BukkitConvertor.BukkitGetCorrectMeta(is.getItemMeta());
	}

	@Override
	public void setItemMeta(MCItemMeta im) {
		if (is == null) {
			return;
		}
		if (im == null) {
			is.setItemMeta(null);
			return;
		}
		is.setItemMeta(((BukkitMCItemMeta)im).asItemMeta());
	}
}
