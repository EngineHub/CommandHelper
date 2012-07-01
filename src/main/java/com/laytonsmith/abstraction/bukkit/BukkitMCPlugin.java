/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author layton
 */
public class BukkitMCPlugin implements MCPlugin {

    Plugin p;
    public BukkitMCPlugin(AbstractionObject a){
        this((Plugin)null);
        if(a instanceof MCPlugin){
            this.p = ((Plugin)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCPlugin(Plugin plugin) {
        this.p = plugin;
    }
    
    public Object getHandle(){
        return p;
    }

    public Plugin getPlugin() {
        return p;
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
    
}
