

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.plugin.Plugin;

/**
 *
 */
public class BukkitMCPlugin implements MCPlugin {

    Plugin p;
    public BukkitMCPlugin(Plugin plugin) {
        this.p = plugin;
    }
    
    public BukkitMCPlugin(AbstractionObject a){
        this((Plugin)null);
        if(a instanceof MCPlugin){
            this.p = ((Plugin)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public Object getHandle(){
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
    
    public Plugin getPlugin() {
        return p;
    }
	
	@Override
	public String toString() {
		return p.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCPlugin?p.equals(((BukkitMCPlugin)obj).p):false);
	}

	@Override
	public int hashCode() {
		return p.hashCode();
	}

	public String getName() {
		return p.getName();
	}
    
}
