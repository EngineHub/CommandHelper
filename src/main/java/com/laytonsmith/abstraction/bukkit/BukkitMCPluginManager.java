/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginManager;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author layton
 */
public class BukkitMCPluginManager implements MCPluginManager {

    PluginManager p;
    public BukkitMCPluginManager(AbstractionObject a){
        this((PluginManager)null);
        if(a instanceof MCPluginManager){
            this.p = ((PluginManager)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCPluginManager(PluginManager pluginManager) {
        this.p = pluginManager;
    }
    
    public PluginManager __PluginManager(){
        return p;
    }

    public Object getHandle(){
        return p;
    }
    
    public MCPlugin getPlugin(String name) {
        if(p.getPlugin(name) == null){
            return null;
        }
        return new BukkitMCPlugin(p.getPlugin(name));
    }
    
}
