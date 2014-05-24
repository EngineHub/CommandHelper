

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import org.bukkit.material.MaterialData;

/**
 *
 * 
 */
public class BukkitMCMaterialData implements MCMaterialData{
    MaterialData md;
    public BukkitMCMaterialData(MaterialData md){
        this.md = md;
    }
    
    public BukkitMCMaterialData(AbstractionObject a){
        this((MaterialData)null);
        if(a instanceof MCMaterialData){
            this.md = ((MaterialData)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
	@Override
    public Object getHandle(){
        return md;
    }

	@Override
    public int getData() {
        return md.getData();
    }

	@Override
	public MCMaterial getMaterial() {
		return new BukkitMCMaterial(md.getItemType());
	}
    
	@Override
	public String toString() {
		return md.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCMaterialData?md.equals(((BukkitMCMaterialData)obj).md):false);
	}

	@Override
	public int hashCode() {
		return md.hashCode();
	}
    
}
