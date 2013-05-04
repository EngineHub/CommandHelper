

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 * @author layton
 */
public class BukkitMCConsoleCommandSender extends BukkitMCCommandSender implements MCConsoleCommandSender{

    @WrappedItem ConsoleCommandSender ccs;
    public BukkitMCConsoleCommandSender(ConsoleCommandSender ccs){
        super(ccs);
        this.ccs = ccs;
    }
    
}
