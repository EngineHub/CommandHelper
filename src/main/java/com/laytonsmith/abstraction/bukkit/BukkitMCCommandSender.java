

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.annotations.WrappedItem;
import com.laytonsmith.annotations.testing.AbstractConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCCommandSender implements MCCommandSender{

    @WrappedItem CommandSender c;
    public BukkitMCCommandSender(CommandSender c){
        this.c = c;
    }
    
	@AbstractConstructor
    public BukkitMCCommandSender(AbstractionObject a){
        this.c = a.getHandle();
    }
    
    public CommandSender getHandle(){
        return c;
    }        
    
    public void sendMessage(String string) {
        c.sendMessage(string);
    }

    
    public MCServer getServer() {
        return new BukkitMCServer();
    }

    
    public String getName() {
        return c.getName();
    }

    public boolean isOp() {
        return c.isOp();
    }

    public CommandSender _CommandSender() {
        return c;
    }

    public boolean instanceofPlayer() {
        return c instanceof Player;
    }

    public boolean instanceofMCConsoleCommandSender() {
        return c instanceof ConsoleCommandSender;
    }
	
	@Override
	public String toString() {
		return c.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCCommandSender?c.equals(((BukkitMCCommandSender)obj).c):false);
	}

	@Override
	public int hashCode() {
		return c.hashCode();
	}
    
}
