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
    public BukkitMCCommandSender(AbstractionObject a){
        this((CommandSender)null);
        if(a instanceof MCCommandSender){
            this.c = ((CommandSender)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCCommandSender(CommandSender c){
        this.c = c;
    }
    
    public CommandSender _CommandSender() {
        return c;
    }        
    
    public Object getHandle(){
        return c;
    }

    
    public String getName() {
        return c.getName();
    }

    
    public MCServer getServer() {
        return new BukkitMCServer();
    }

    public boolean instanceofMCConsoleCommandSender() {
        return c instanceof ConsoleCommandSender;
    }

    public boolean instanceofPlayer() {
        return c instanceof Player;
    }

    public boolean isOp() {
        return c.isOp();
    }

    public void sendMessage(String string) {
        c.sendMessage(string);
    }
    
}
