/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author layton
 */
public class BukkitMCPlugin implements MCPlugin {

    Plugin p;
    public BukkitMCPlugin(Plugin plugin) {
        this.p = plugin;
    }

    public boolean isEnabled() {
        return p.isEnabled();
    }
    
    public boolean isInstanceOf(Class c) {
        if (c.isInstance(p)) {
            return true;
        }
        
        return false;
    }
    
    public Plugin getPlugin() {
        return p;
    }
    
}
