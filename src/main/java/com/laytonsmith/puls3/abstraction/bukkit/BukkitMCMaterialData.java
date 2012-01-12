/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.MCMaterialData;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
public class BukkitMCMaterialData implements MCMaterialData{
    MaterialData md;
    public BukkitMCMaterialData(MaterialData md){
        this.md = md;
    }

    public byte getData() {
        return md.getData();
    }
    
    
}
