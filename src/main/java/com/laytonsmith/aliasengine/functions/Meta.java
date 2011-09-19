/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.ServerConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;

/**
 * I'm So Meta, Even This Acronym
 * @author Layton
 */
public class Meta {

    public static String docs() {
        return "These functions provide a way to run other commands";
    }

    @api
    public static class runas implements Function {

        public String getName() {
            return "runas";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, final CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[1].val() == null || args[1].val().length() <= 0 || args[1].val().charAt(0) != '/') {
                throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
                        ExceptionType.FormatException, line_num, f);
            }
            String cmd = args[1].val().substring(1);
            if (args[0] instanceof CArray) {
                CArray u = (CArray) args[0];
                for (int i = 0; i < u.size(); i++) {
                    exec(line_num, f, p, new Construct[]{new CString(u.get(i, line_num).val(), line_num, f), args[1]});
                }
                return new CVoid(line_num, f);
            }
            if (args[0].val().equals("~op")) {
                Boolean isOp = p.isOp();

                if (!isOp) {
                    this.setOp(p, true);
                }

                if ((Boolean) Static.getPreferences().getPreference("debug-mode")) {
                    if (p instanceof Player) {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((Player) p).getName() + ": " + args[1].val().trim());
                    } else {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + args[1].val().trim());
                    }
                }
                //m.chat(cmd);
                try {
                    Static.getServer().dispatchCommand(p, cmd);
                } finally {
                    this.setOp(p, isOp);
                }
            } else {
                Player m = Static.getServer().getPlayer(args[0].val());
                if (m != null && m.isOnline()) {
                    if (p instanceof Player) {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((Player) p).getName() + ": " + args[0].val().trim());
                    } else {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + args[0].val().trim());
                    }
                    //m.chat(cmd);
                    Static.getServer().dispatchCommand(m, cmd);
                } else {
                    throw new ConfigRuntimeException("The player " + args[0].val() + " is not online",
                            ExceptionType.PlayerOfflineException, line_num, f);
                }
            }
            return new CVoid(line_num, f);
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
        }

        public String docs() {
            return "void {player, command} Runs a command as a particular user. The special user '~op' is a user that runs as op. Be careful with this very powerful function."
                    + " Commands cannot be run as an offline player. Returns void. If the first argument is an array of usernames, the command"
                    + " will be run in the context of each user in the array.";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
        
        /**
         * Set OP status for player without saving to ops.txt
         * 
         * @param player
         * @param value 
         */
        protected void setOp(CommandSender player, Boolean value) {            
            if (!(player instanceof Player) || player.isOp() == value) {
                return;
            }

            try {
                CraftServer server = (CraftServer) Bukkit.getServer();

                Field opSetField = ServerConfigurationManager.class.getDeclaredField("h");
                
                opSetField.setAccessible(true); // make field mutable for reflection 
                
                Set opSet = (Set)opSetField.get(server.getHandle()); 
                
                // since all Java objects pass by reference, we don't need to set field back to object
                if(value){
                    opSet.add(player.getName().toLowerCase());
                } else {
                    opSet.remove(player.getName().toLowerCase());
                }
                
                player.recalculatePermissions();
                
            } catch (Exception e) {
                Static.getLogger().log(Level.WARNING, "[CommandHelper]: Failed to OP player " + player.getName());
            }

        }
    }

    @api
    public static class run implements Function {

        public String getName() {
            return "run";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[0].val() == null || args[0].val().length() <= 0 || args[0].val().charAt(0) != '/') {
                throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
                        ExceptionType.FormatException, line_num, f);
            }
            String cmd = args[0].val().substring(1);
            if ((Boolean) Static.getPreferences().getPreference("debug-mode")) {
                if (p instanceof Player) {
                    Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((Player) p).getName() + ": " + args[0].val().trim());
                } else {
                    Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + args[0].val().trim());
                }
            }
            //p.chat(cmd);
            Static.getServer().dispatchCommand(p, cmd);
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "void {var1} Runs a command as the current player. Useful for running commands in a loop. Note that this accepts commands like from the "
                    + "chat; with a forward slash in front.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class g implements Function {

        public String getName() {
            return "g";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            for (int i = 0; i < args.length; i++) {
                args[i].val();
            }
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "string {func1, [func2...]} Groups any number of functions together, and returns void. ";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Boolean runAsync() {
            return null;
        }
    }

    @api
    public static class p implements Function {

        public String getName() {
            return "p";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "mixed {c} Used internally by the compiler.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            return Static.resolveConstruct(args[0].val(), line_num, f);
        }
    }

    @api
    public static class eval implements Function {

        public String getName() {
            return "eval";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {script_string} Executes arbitrary MScript. Note that this function is very experimental, and is subject to changing or "
                    + "removal.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CVoid(line_num, f);
        }
        //Doesn't matter, run out of state anyways

        public Boolean runAsync() {
            return null;
        }
    }

    @api
    public static class call_alias implements Function {

        public String getName() {
            return "call_alias";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {cmd} Allows a CommandHelper alias to be called from within another alias. Typically this is not possible, as"
                    + " a script that runs \"/jail = /jail\" for instance, would simply be calling whatever plugin that actually"
                    + " provides the jail functionality's /jail command. However, using this function makes the command loop back"
                    + " to CommandHelper only.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            Static.getAliasCore().removePlayerReference(p);
            Static.getAliasCore().alias(args[0].val(), p, null);
            Static.getAliasCore().addPlayerReference(p);
            return new CVoid(line_num, f);
        }
    }
}
