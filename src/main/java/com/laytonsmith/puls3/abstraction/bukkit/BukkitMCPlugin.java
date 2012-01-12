/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.MCPlugin;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author layton
 */
class BukkitMCPlugin implements MCPlugin {

    Plugin p;
    public BukkitMCPlugin(Plugin plugin) {
        this.p = plugin;
    }

    public boolean isEnabled() {
        return p.isEnabled();
    }
    
}
