

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
    public BukkitMCPluginManager(PluginManager pluginManager) {
        this.p = pluginManager;
    }
    
    public BukkitMCPluginManager(AbstractionObject a){
        this((PluginManager)null);
        if(a instanceof MCPluginManager){
            this.p = ((PluginManager)a.getHandle());
        } else {
            throw new ClassCastException();
        }
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
    
    public PluginManager __PluginManager(){
        return p;
    }
    
}
