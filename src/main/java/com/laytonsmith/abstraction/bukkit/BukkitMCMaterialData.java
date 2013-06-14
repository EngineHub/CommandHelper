

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMaterialData;
import org.bukkit.material.MaterialData;

/**
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
    
    public Object getHandle(){
        return md;
    }

    public int getData() {
        return md.getData();
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
