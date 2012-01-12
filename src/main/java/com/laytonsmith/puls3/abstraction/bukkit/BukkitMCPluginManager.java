/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.MCPlugin;
import com.laytonsmith.puls3.abstraction.MCPluginManager;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author layton
 */
public class BukkitMCPluginManager implements MCPluginManager {

    PluginManager p;
    public BukkitMCPluginManager(PluginManager pluginManager) {
        this.p = pluginManager;
    }

    public MCPlugin getPlugin(String name) {
        if(p.getPlugin(name) == null){
            return null;
        }
        return new BukkitMCPlugin(p.getPlugin(name));
    }
    
    public PluginManager __PluginManager(){
        return p;
    }
    
}
