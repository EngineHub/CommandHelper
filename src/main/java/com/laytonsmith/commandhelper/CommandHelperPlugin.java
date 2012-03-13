// $Id$
/*
 * CommandHelper
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.laytonsmith.commandhelper;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.core.*;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the plugin.
 *
 * @author sk89q
 */
public class CommandHelperPlugin extends JavaPlugin {
    public static final Logger logger = Logger.getLogger("Minecraft.CommandHelper");
    //Do not rename this field, it is changed reflectively in unit tests.
    private static AliasCore ac;
    public static MCServer myServer;
    public static SerializedPersistance persist;
    public static PermissionsResolverManager perms;
    public static Version version;
    public static Preferences prefs;
    public static CommandHelperPlugin self;
    public static WorldEditPlugin wep;
    public static ExecutorService hostnameLookupThreadPool;
    public static ConcurrentHashMap<String, String> hostnameLookupCache;
    private static int hostnameThreadPoolID = 0;
    /**
     * Listener for the plugin system.
     */
    final CommandHelperListener playerListener =
            new CommandHelperListener(this);
    
    /**
     * Interpreter listener
     */
    public final CommandHelperInterpreterListener interpreterListener = 
            new CommandHelperInterpreterListener();
    /**
     * Server Command Listener, for console commands
     */
    final CommandHelperServerListener serverListener =
            new CommandHelperServerListener();

    final Set<MCPlayer> commandRunning = new HashSet<MCPlayer>();
    
    
    @Override
    public void onLoad(){
        Installer.Install();
    }
    /**
     * Called on plugin enable.
     */
    public void onEnable() {       
        self = this;
        myServer = StaticLayer.GetServer();
        persist = new SerializedPersistance(new File("plugins/CommandHelper/persistance.ser"), this);
        logger.info("CommandHelper/CommandHelper " + getDescription().getVersion() + " enabled");
        version = new Version(getDescription().getVersion());
        PermissionsResolverManager.initialize(this);
        perms = PermissionsResolverManager.getInstance();
        Plugin pwep = getServer().getPluginManager().getPlugin("WorldEdit");
        if(pwep != null && pwep.isEnabled() && pwep instanceof WorldEditPlugin){
            wep = (WorldEditPlugin)pwep;
        }
        try {
            File prefsFile = new File("plugins/CommandHelper/preferences.txt");
            Static.getPreferences().init(prefsFile);
            if(Prefs.UseColors()){
                TermColors.EnableColors();
            } else {
                TermColors.DisableColors();
            }
            String script_name = Prefs.ScriptName();
            String main_file = Prefs.MainFile();
            boolean showSplashScreen = Prefs.ShowSplashScreen();
            if(showSplashScreen){
                System.out.println(TermColors.reset());
                //System.out.flush();
                System.out.println("\n\n\n" + Static.Logo());
            }
            ac = new AliasCore(new File("plugins/CommandHelper/" + script_name), new File("plugins/CommandHelper/LocalPackages"), prefsFile, new File("plugins/CommandHelper/" + main_file), perms, this);
            ac.reload(null);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ConfigCompileException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        //Clear out our hostname cache
        hostnameLookupCache = new ConcurrentHashMap<String, String>();
        //Create a new thread pool, with a custom ThreadFactory,
        //so we can more clearly name our threads.
        hostnameLookupThreadPool = Executors.newFixedThreadPool(3, new ThreadFactory() {
            
            public Thread newThread(Runnable r) {
                return new Thread(r, "CommandHelperHostnameLookup-" + (++hostnameThreadPoolID));
            }
        });
        for(MCPlayer p : Static.getServer().getOnlinePlayers()){
            //Repopulate our cache for currently online players.
            //New players that join later will get a lookup done
            //on them at that time.
            Static.HostnameCache(p);
        }
        
        Static.PlayDirty();
        registerEvent(playerListener);
        
        //interpreter events
        registerEvent(interpreterListener);
        registerEvent(serverListener);
        
        //Script events
        EventList.Startup(this);
        
        playerListener.loadGlobalAliases();
        interpreterListener.reload();
    }

    public static AliasCore getCore(){
        return ac;
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void onDisable() {
        //free up some memory
        ac = null;
        wep = null;
    }
    
    /**
     * Register an event.
     * 
     * @param type
     * @param listener
     * @param priority
     */
    public void registerEvent(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    /**
     * Called when a command registered by this plugin is received.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if((sender.isOp() || (sender instanceof Player && (perms.hasPermission(((Player)sender).getName(), "commandhelper.reloadaliases") 
                || perms.hasPermission(((Player)sender).getName(), "ch.reloadaliases"))))
                && (cmd.getName().equals("reloadaliases") || cmd.getName().equals("reloadalias"))){
            MCPlayer player = null;
            if(sender instanceof Player){
                player = new BukkitMCPlayer((Player)sender);
            }
            ac.reload(player);
//            if(ac.reload(player)){
//                if(sender instanceof Player){
//                    Static.SendMessage(player, MCChatColor.GOLD + "Command Helper scripts sucessfully recompiled.");
//                }
//                System.out.println(TermColors.YELLOW + "Command Helper scripts sucessfully recompiled." + TermColors.reset());
//            } else{
//                if(sender instanceof Player){
//                    Static.SendMessage(player, MCChatColor.RED + "An error occured when trying to compile the script. Check the console for more information.");
//                }
//                System.out.println(TermColors.RED + "An error occured when trying to compile the script. Check the console for more information." + TermColors.reset());
//            }
            return true;
        } else if(cmd.getName().equals("commandhelper") && args.length >= 1 && args[0].equalsIgnoreCase("null")){
            return true;
        } else if(cmd.getName().equals("runalias")){
            //Hardcoded alias rebroadcast
            if(sender instanceof Player){
                PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent((Player)sender, Static.strJoin(args, " "));
                playerListener.onPlayerCommandPreprocess(pcpe);
            } else if(sender instanceof ConsoleCommandSender){
                String cmd2 = Static.strJoin(args, " ");
                if(cmd2.startsWith("/")){
                    cmd2 = cmd2.substring(1);
                }
                ServerCommandEvent sce = new ServerCommandEvent((ConsoleCommandSender)sender, cmd2);
                serverListener.onServerCommand(sce);                
            }
            return true;
        } else if (sender instanceof Player) {
                return runCommand(new BukkitMCPlayer((Player)sender), cmd.getName(), args);
        } else {
            return false;
        }
    }
    
    /**
     * Runs commands.
     *
     * @param player
     * @param split
     * @return
     */
    private boolean runCommand(final MCPlayer player, String cmd, String[] args) {
        if(commandRunning.contains(player)){
            return true;
        }

        commandRunning.add(player);
        UserManager um = UserManager.GetUserManager(player.getName());
        // Repeat command
        if (cmd.equals("repeat")) {
            if(perms.hasPermission(player.getName(), "commandhelper.repeat") ||
                    perms.hasPermission(player.getName(), "ch.repeat")){
                //Go ahead and remove them, so that they can repeat aliases. They can't get stuck in
                //an infinite loop though, because the preprocessor won't try to fire off a repeat command
                commandRunning.remove(player);
                if (um.getLastCommand() != null) {
                    Static.SendMessage(player, MCChatColor.GRAY + um.getLastCommand());
                    execCommand(player, um.getLastCommand());
                } else {
                    Static.SendMessage(player, MCChatColor.RED + "No previous command.");
                }
                return true;
            } else {
                Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the repeat command");
                commandRunning.remove(player);
                return true;
            }
    
        // Save alias
        } else if (cmd.equalsIgnoreCase("alias") || cmd.equalsIgnoreCase("commandhelper")
                /*&& player.canUseCommand("/alias")*/) {
            if(!perms.hasPermission(player.getName(), "commandhelper.useralias") && !perms.hasPermission(player.getName(), "ch.useralias")){
                Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the alias command");
                commandRunning.remove(player);
                return true;
            }
            if(args.length > 0){

                String alias = CommandHelperPlugin.joinString(args, " ");
                try {
                    int id = um.addAlias(alias);
                    if(id > -1){
                        Static.SendMessage(player, MCChatColor.YELLOW + "Alias added with id '" + id + "'");
                    }
                } catch (ConfigCompileException ex) {
                    Static.SendMessage(player, "Your alias could not be added due to a compile error:\n" + MCChatColor.RED + ex.getMessage());
                }
            } else{
                //Display a help message
                Static.SendMessage(player, MCChatColor.GREEN + "Command usage: \n"
                        + MCChatColor.GREEN + "/alias <alias> - adds an alias to your user defined list\n"
                        + MCChatColor.GREEN + "/delalias <id> - deletes alias with id <id> from your user defined list\n"
                        + MCChatColor.GREEN + "/viewalias - shows you all of your aliases");
            }

            commandRunning.remove(player);
            return true;
        //View all aliases for this user
        } else if(cmd.equalsIgnoreCase("viewalias")){
            if(!perms.hasPermission(player.getName(), "commandhelper.useralias") && !perms.hasPermission(player.getName(), "ch.useralias")){
                Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the viewalias command");
                commandRunning.remove(player);
                return true;
            }
            int page = 0;
            try{
                page = Integer.parseInt(args[0]);
            } catch(Exception e){
                //Meh. Index out of bounds, or number format exception. Whatever, show page 1
            }
            Static.SendMessage(player, um.getAllAliases(page));
            commandRunning.remove(player);
            return true;
        // Delete alias
        } else if (cmd.equalsIgnoreCase("delalias")) {
            if(!perms.hasPermission(player.getName(), "commandhelper.useralias") && !perms.hasPermission(player.getName(), "ch.useralias")){
                Static.SendMessage(player, MCChatColor.RED + "You do not have permission to access the delalias command");
                commandRunning.remove(player);
                return true;
            }
            try{
                ArrayList<String> deleted = new ArrayList<String>();
                for(int i = 0; i < args.length; i++){
                    um.delAlias(Integer.parseInt(args[i]));
                    deleted.add("#" + args[i]);
                }
                if(args.length > 1){
                    String s = MCChatColor.YELLOW + "Aliases " + deleted.toString() + " were deleted";
                    Static.SendMessage(player, s);

                } else{
                    Static.SendMessage(player, MCChatColor.YELLOW + "Alias #" + args[0] + " was deleted");
                }
            } catch(NumberFormatException e){
                Static.SendMessage(player, MCChatColor.RED + "The id must be a number");
            } catch(ArrayIndexOutOfBoundsException e){
                Static.SendMessage(player, MCChatColor.RED + "Usage: /delalias <id> <id> ...");
            }
            commandRunning.remove(player);
            return true;
    
        } else if(cmd.equalsIgnoreCase("interpreter")){
            if(perms.hasPermission(player.getName(), "commandhelper.interpreter")){
                if(Prefs.EnableInterpreter()){
                    interpreterListener.startInterpret(player.getName());
                    Static.SendMessage(player, MCChatColor.YELLOW + "You are now in interpreter mode. Type a dash (-) on a line by itself to exit, and >>> to enter"
                            + " multiline mode.");
                } else {
                    Static.SendMessage(player, MCChatColor.RED + "The interpreter is currently disabled. Check your preferences file.");
                }
            } else {
                Static.SendMessage(player, MCChatColor.RED + "You do not have permission to run that command");
            }
            commandRunning.remove(player);
            return true;
        }
        commandRunning.remove(player);
        return false;
    }

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    public static String joinString(String[] str, String delimiter) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }
    
    /**
     * Execute a command.
     * @param player 
     *
     * @param cmd
     */
    public static void execCommand(MCPlayer player, String cmd) {
        player.chat(cmd);
    }
}
