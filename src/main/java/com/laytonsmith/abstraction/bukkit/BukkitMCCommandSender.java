/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCServer;
import org.bukkit.command.CommandSender;

/**
 *
 * @author layton
 */
public class BukkitMCCommandSender implements MCCommandSender{

    CommandSender c;
    public BukkitMCCommandSender(CommandSender c){
        this.c = c;
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
    
}
