/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCServer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCCommandSender implements MCCommandSender{

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
    
    public Object getHandle(){
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
    
}
