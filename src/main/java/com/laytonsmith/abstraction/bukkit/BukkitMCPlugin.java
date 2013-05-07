

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author layton
 */
public class BukkitMCPlugin implements MCPlugin {

    @WrappedItem Plugin p;
    
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
