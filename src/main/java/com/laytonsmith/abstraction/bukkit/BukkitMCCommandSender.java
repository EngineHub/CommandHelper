

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class BukkitMCCommandSender implements MCCommandSender {

    CommandSender c;
    public BukkitMCCommandSender(CommandSender c){
        this.c = c;
    }
    
    public BukkitMCCommandSender(AbstractionObject a){
        this((CommandSender)null);
        if(a instanceof MCCommandSender){
            this.c = ((CommandSender)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
	@Override
    public Object getHandle(){
        return c;
    }        
    
	@Override
    public void sendMessage(String string) {
        c.sendMessage(string);
    }

    
	@Override
    public MCServer getServer() {
        return new BukkitMCServer();
    }

    
	@Override
    public String getName() {
        return c.getName();
    }

	@Override
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

	@Override
	public boolean hasPermission(String perm) {
		return c.hasPermission(perm);
	}

	@Override
	public boolean isPermissionSet(String perm) {
		return c.isPermissionSet(perm);
	}

	@Override
	public List<String> getGroups() {
		// As in https://github.com/sk89q/WorldEdit/blob/master/
		// worldedit-bukkit/src/main/java/com/sk89q/wepif/DinnerPermsResolver.java#L112-L126
		List<String> groupNames = new ArrayList<String>();
		for (PermissionAttachmentInfo permAttach : c.getEffectivePermissions()) {
			String perm = permAttach.getPermission();
			if (!(perm.startsWith(Static.groupPrefix) && permAttach.getValue())) {
				continue;
			}
			groupNames.add(perm.substring(Static.groupPrefix.length(), perm.length()));
		}
		return groupNames;
	}

	@Override
	public boolean inGroup(String groupName) {
		return getGroups().contains(groupName);
	}
}
