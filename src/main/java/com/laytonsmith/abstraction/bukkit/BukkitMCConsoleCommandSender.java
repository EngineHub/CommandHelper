

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 */
public class BukkitMCConsoleCommandSender extends BukkitMCCommandSender implements MCConsoleCommandSender{

    ConsoleCommandSender ccs;
    public BukkitMCConsoleCommandSender(ConsoleCommandSender ccs){
        super(ccs);
        this.ccs = ccs;
    }
    
}
