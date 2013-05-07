

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.command.ConsoleCommandSender;

/**
 * TODO: Is this class even needed anymore?
 */
public class BukkitMCConsoleCommandSender extends BukkitMCCommandSender implements MCConsoleCommandSender{

    @WrappedItem ConsoleCommandSender ccs;
    
}
