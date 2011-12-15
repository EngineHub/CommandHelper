/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.ServerConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.security.pkcs11.P11TlsKeyMaterialGenerator;

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

        public Construct exec(int line_num, File f, final Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[1].val() == null || args[1].val().length() <= 0 || args[1].val().charAt(0) != '/') {
                throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
                        ExceptionType.FormatException, line_num, f);
            }
            String cmd = args[1].val().substring(1);
            if (args[0] instanceof CArray) {
                CArray u = (CArray) args[0];
                for (int i = 0; i < u.size(); i++) {
                    exec(line_num, f, env, new Construct[]{new CString(u.get(i, line_num).val(), line_num, f), args[1]});
                }
                return new CVoid(line_num, f);
            }
            if (args[0].val().equals("~op")) {
                //Store their current op status
                Boolean isOp = env.GetCommandSender().isOp();

                if ((Boolean) Static.getPreferences().getPreference("debug-mode")) {
                    if (env.GetCommandSender() instanceof Player) {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + env.GetPlayer().getName() + ": " + args[1].val().trim());
                    } else {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + args[1].val().trim());
                    }
                }

                //If they aren't op, op them now
                if (!isOp) {
                    this.setOp(env.GetCommandSender(), true);
                }

                try {
                    Static.getServer().dispatchCommand(this.getOPCommandSender(env.GetCommandSender()), cmd);
                } finally {
                    //If they just opped themselves, or deopped themselves in the command
                    //don't undo what they just did. Otherwise, set their op status back
                    //to their original status
                    if(env.GetPlayer() != null && !cmd.equalsIgnoreCase("op " + env.GetPlayer().getName()) && !cmd.equalsIgnoreCase("deop " + env.GetPlayer().getName())){
                        this.setOp(env.GetCommandSender(), isOp);
                    }
                }
            } else {
                Player m = Static.getServer().getPlayer(args[0].val());
                if (m != null && m.isOnline()) {
                    if (env.GetCommandSender() instanceof Player) {
                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + env.GetPlayer().getName() + ": " + args[0].val().trim());
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
                Server server = Bukkit.getServer();

                Class serverClass = Class.forName("org.bukkit.craftbukkit.CraftServer", true, server.getClass().getClassLoader());

                if (!server.getClass().isAssignableFrom(serverClass)) {
                    throw new IllegalStateException("Running server isn't CraftBukkit");
                }

                Field opSetField;

                try {
                    opSetField = ServerConfigurationManager.class.getDeclaredField("operators");
                } catch (NoSuchFieldException e){
                    opSetField = ServerConfigurationManager.class.getDeclaredField("h");
                }

                opSetField.setAccessible(true); // make field accessible for reflection 

                // Reflection magic
                Set opSet = (Set) opSetField.get((ServerConfigurationManager) serverClass.getMethod("getHandle").invoke(server));

                // since all Java objects pass by reference, we don't need to set field back to object
                if (value) {
                    opSet.add(player.getName().toLowerCase());
                } else {
                    opSet.remove(player.getName().toLowerCase());
                }

                player.recalculatePermissions();
                
            } catch (ClassNotFoundException e) {
            } catch (IllegalStateException e) {
            } catch (Throwable e) {
                Static.getLogger().log(Level.WARNING, "[CommandHelper]: Failed to OP player " + player.getName());
            }
        }

        protected CommandSender getOPCommandSender(final CommandSender sender) {
            if (sender.isOp()) {
                return sender;
            }

            return (CommandSender) Proxy.newProxyInstance(sender.getClass().getClassLoader(),
                    new Class[] { (sender instanceof Player) ? Player.class : CommandSender.class },
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String methodName = method.getName();
                            if ("isOp".equals(methodName) || "hasPermission".equals(methodName) || "isPermissionSet".equals(methodName)) {
                                return true;
                            } else {
                                return method.invoke(sender, args);
                            }
                        }
                    });            
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[0].val() == null || args[0].val().length() <= 0 || args[0].val().charAt(0) != '/') {
                throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
                        ExceptionType.FormatException, line_num, f);
            }
            String cmd = args[0].val().substring(1);
            if ((Boolean) Static.getPreferences().getPreference("debug-mode")) {
                if (env.GetCommandSender() instanceof Player) {
                    Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + env.GetPlayer().getName() + ": " + args[0].val().trim());
                } else {
                    Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + args[0].val().trim());
                }
            }
            //p.chat(cmd);
            Static.getServer().dispatchCommand(env.GetCommandSender(), cmd);
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            boolean doRemoval = true;
            if(!Static.getAliasCore().hasPlayerReference(env.GetCommandSender())){
                doRemoval = false;
            }
            if(doRemoval){
                Static.getAliasCore().removePlayerReference(env.GetCommandSender());
            }
            Static.getAliasCore().alias(args[0].val(), env.GetCommandSender(), null);
            if(doRemoval){
                Static.getAliasCore().addPlayerReference(env.GetCommandSender());
            }
            return new CVoid(line_num, f);
        }
    }
    
    @api public static class scriptas implements Function{

        public String getName() {
            return "scriptas";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {player, [label], script} Runs the specified script in the context of a given player."
                    + " A script that runs player() for instance, would return the specified player's name,"
                    + " not the player running the command. Setting the label allows you to dynamically set the label"
                    + " this script is run under as well (in regards to permission checking)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return false;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }
        
        public Construct exec(int line_num, File f, Env environment, Construct... args){
            return null;
        }

        public Construct execs(int line_num, File f, Env environment, List<GenericTreeNode<Construct>> args) throws ConfigRuntimeException {
            Player p = Static.GetPlayer(environment.GetScript().seval(args.get(0), environment).val(), line_num, f);
            CommandSender originalPlayer = environment.GetCommandSender();
            int offset = 0;
            String originalLabel = environment.GetLabel();
            if(args.size() == 3){
                offset++;
                String label = environment.GetScript().seval(args.get(1), environment).val();
                environment.SetLabel(label);
            }
            environment.SetPlayer(p);
            GenericTreeNode<Construct> tree = args.get(1 + offset);
            environment.GetScript().eval(tree, environment);
            environment.SetCommandSender(originalPlayer);
            environment.SetLabel(originalLabel);
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class has_permission implements Function{

        public String getName() {
            return "has_permission";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "boolean {[player], permissionName} Using the built in permissions system, checks to see if the player has a particular permission."
                    + " This is simply passed through to the permissions system. This function does not throw a PlayerOfflineException, because"
                    + " it works with offline players, but that means that names must be an exact match. If you notice, this function isn't"
                    + " restricted. However, it IS restricted if the player attempts to check another player's permissions.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            String player = null;
            String permission = null;
            if(args.length == 1){
                player = environment.GetPlayer().getName();
                permission = args[0].val();
            } else {
                player = args[0].val();
                permission = args[1].val();
            }
            if(environment.GetPlayer() != null && !environment.GetPlayer().getName().equals(player)){
                if(!Static.hasCHPermission(this.getName(), environment)){
                    throw new ConfigRuntimeException("You do not have permission to use the " + f.getName() + " function.",
                                ExceptionType.InsufficientPermissionException, line_num, f);
                }
            }
            PermissionsResolverManager perms = Static.getPermissionsResolverManager();
            return new CBoolean(perms.hasPermission(player, permission), line_num, f);
        }
        
    }
}
