/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMaterialData;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
public class BukkitMCMaterialData implements MCMaterialData{
    MaterialData md;
    public BukkitMCMaterialData(AbstractionObject a){
        this((MaterialData)null);
        if(a instanceof MCMaterialData){
            this.md = ((MaterialData)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCMaterialData(MaterialData md){
        this.md = md;
    }
    
    public int getData() {
        return md.getData();
    }

    public Object getHandle(){
        return md;
    }
    
    
}
