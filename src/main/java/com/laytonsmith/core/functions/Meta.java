package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.UserManager;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.persistence.DataSourceException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.jar.JarFile;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * 
 */
public class Meta {

	public static String docs() {
		return "These functions provide a way to run other commands";
	}
/*
	@api
	public static class first_load extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CBoolean.get(CommandHelperPlugin.isFirstLoad());
		}

		public String getName() {
			return "first_load";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "boolean {} Returns true if the scripts have not been reloaded since the plugin was enabled."
					+ " In otherwords, using this in main.ms will return false when you do /reloadaliases.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
*/
	@api(environments = {CommandHelperEnvironment.class})
	public static class runas extends AbstractFunction {

		@Override
		public String getName() {
			return "runas";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public Construct exec(Target t, final Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[1].nval() == null || args[1].val().length() <= 0 || args[1].val().charAt(0) != '/') {
				throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
						ExceptionType.FormatException, t);
			}
			String cmd = args[1].val().substring(1);
			if (args[0] instanceof CArray) {
				CArray u = (CArray) args[0];
				for (int i = 0; i < u.size(); i++) {
					exec(t, env, new Construct[]{new CString(u.get(i, t).val(), t), args[1]});
				}
				return CVoid.VOID;
			}
			if (args[0].val().equals("~op")) {
				//TODO: Remove this after next release (3.3.1)
				CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.WARNING, "Using runas(~op, " + args[1].asString().getQuote() 
						+ ") is deprecated. Use sudo(" + args[1].asString().getQuote() + ") instead.", t);
				new sudo().exec(t, env, args[1]);
			} else if (args[0].val().equals(Static.getConsoleName())) {
				CHLog.GetLogger().Log(CHLog.Tags.META, LogLevel.INFO, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as console): " + args[1].val().trim(), t);
				if (Prefs.DebugMode()) {
					Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as : " + args[1].val().trim());
				}
				if(cmd.equalsIgnoreCase("interpreter-on")){
					//This isn't allowed for security reasons.
					throw new ConfigRuntimeException("/interpreter-on cannot be run from runas for security reasons.", ExceptionType.FormatException, t);
				}
				Static.getServer().runasConsole(cmd);
			} else {
				MCPlayer m = Static.GetPlayer(args[0], t);
				if (m != null && m.isOnline()) {
					MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
					String name;

					if (p != null) {
						name = p.getName();
					} else {
						name = "Unknown player";
					}

					CHLog.GetLogger().Log(CHLog.Tags.META, "Executing command on " + name + " (running as " + args[0].val() + "): " + args[1].val().trim(), t);
					if (Prefs.DebugMode()) {
						Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + name + " (running as " + args[0].val() + "): " + args[1].val().trim());
					}
					//m.chat(cmd);
					Static.getServer().dispatchCommand(m, cmd);
				} else {
					throw new ConfigRuntimeException("The player " + args[0].val() + " is not online",
							ExceptionType.PlayerOfflineException, t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
		}

		@Override
		public String docs() {
			return "void {player, command} Runs a command as a particular user. The special user '" + Static.getConsoleName() + "' can be used to run it as a console"
					+ " user. Using '~op' is deprecated, and will be removed after the next release, use sudo() instead."
					+ " Commands cannot be run as an offline player. If the first argument is an array of usernames, the command"
					+ " will be run in the context of each user in the array.";
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class sudo extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if (args[0].nval() == null || args[0].val().length() <= 0 || args[0].val().charAt(0) != '/') {
				throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
						ExceptionType.FormatException, t);
			}
			String cmd = args[0].val().substring(1);
			//If the command sender is null, then just try to run() this. It's unclear to me what
			//would cause this for sure, but just in case. Regardless, this allows us to consolidate the error checking
			//into the run function
			if(env.getEnv(CommandHelperEnvironment.class).GetCommandSender() == null){
				return new run().exec(t, env, args);
			}
			//Store their current op status
			Boolean isOp = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().isOp();

			CHLog.GetLogger().Log(CHLog.Tags.META, LogLevel.INFO, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as op): " + args[0].val().trim(), t);
			if (Prefs.DebugMode()) {
				Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as op): " + args[0].val().trim());
			}

			//If they aren't op, op them now
			if (!isOp) {
				this.setOp(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), true);
			}

			try {
				Static.getServer().dispatchCommand(this.getOPCommandSender(env.getEnv(CommandHelperEnvironment.class).GetCommandSender()), cmd);
			} finally {
				//If they just opped themselves, or deopped themselves in the command
				//don't undo what they just did. Otherwise, set their op status back
				//to their original status
				if (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null
						&& !cmd.equalsIgnoreCase("op " + env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName())
						&& !cmd.equalsIgnoreCase("deop " + env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName())) {
					this.setOp(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), isOp);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "sudo";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {command} Runs a single command for this user, as op. Works like runas(~op, '/command') used to work,"
					+ " before it was deprecated. ---- This is guaranteed to not allow the player to stay op, even if a fatal"
					+ " error occurs during the command. If this guarantee cannot be met, the function will simply fail. This"
					+ " guarantee only exists in CraftBukkit. Other server types may find that this function does not work at"
					+ " all, if that's the case, and you are ok with losing the deop guarantee, you can set use-sudo-fallback"
					+ " to true in your preferences. If the normal sudo functionality fails on your server then, it will"
					+ " actually fully op the player, run the command, then deop the player, however, this is less reliable than"
					+ " the normal sudo mechanism, and could potentially fail, leaving the player as op, so is not recommended."
					+ " Enable that setting at your own risk.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		/**
		 * Set OP status for player without saving to ops.txt
		 *
		 * @param player
		 * @param value
		 */
		protected void setOp(MCCommandSender player, Boolean value) {
			if (!(player instanceof MCPlayer) || player.isOp() == value) {
				return;
			}
			MCPlayer p = (MCPlayer) player;
			try {
				p.setTempOp(value);
			} catch (Exception e) {
				if(Prefs.UseSudoFallback()){
					p.setOp(value);
				} else {
					Static.getLogger().log(Level.WARNING, "[CommandHelper]: Failed to OP player " + player.getName());
					StreamUtils.GetSystemErr().println("Extra information about the error: ");
					e.printStackTrace();
				}
			}
		}

		protected MCCommandSender getOPCommandSender(final MCCommandSender sender) {
			if (sender.isOp()) {
				return sender;
			}

			return (MCCommandSender) Proxy.newProxyInstance(sender.getClass().getClassLoader(),
					new Class[]{(sender instanceof MCPlayer) ? MCPlayer.class : MCCommandSender.class},
					new InvocationHandler() {
						@Override
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class run extends AbstractFunction {

		@Override
		public String getName() {
			return "run";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0].nval() == null || args[0].val().length() <= 0 || args[0].val().charAt(0) != '/') {
				throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
						ExceptionType.FormatException, t);
			}
			String cmd = args[0].val().substring(1);
			if (Prefs.DebugMode()) {
				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() + ": " + args[0].val().trim());
				} else {
					Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + args[0].val().trim());
				}
			}
			if(cmd.equalsIgnoreCase("interpreter-on")){
				throw new Exceptions.FormatException("/interpreter-on cannot be run as apart of an alias for security reasons.", t);
			}
			try{
				Static.getServer().dispatchCommand(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), cmd);
			} catch(Exception ex){
				throw new ConfigRuntimeException("While running the command: \"" + cmd + "\""
						+ " the plugin threw an unexpected exception (turn on debug mode to see the full"
						+ " stacktrace): " + ex.getMessage() + "\n\nThis is not a bug in " + Implementation.GetServerType().getBranding()
						+ " but in the plugin that provides the command.", 
						ExceptionType.PluginInternalException, t, ex);
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {var1} Runs a command as the current player. Useful for running commands in a loop. Note that this accepts commands like from the "
					+ "chat; with a forward slash in front.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.PluginInternalException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class is_alias extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IOException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CBoolean exec(Target t, Environment environment, Construct... args)
				throws ConfigRuntimeException {
			AliasCore ac = Static.getAliasCore();

			for (Script s : ac.getScripts()) {
				if (s.match(args[0].val())) {
					return CBoolean.TRUE;
				}
			}

			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (p instanceof MCPlayer) {
				try {
					// p might be null
					for (Script s : UserManager.GetUserManager(p.getName()).getAllScripts(environment.getEnv(GlobalEnv.class).GetPersistenceNetwork())) {
						if (s.match(args[0].val())) {
							return CBoolean.TRUE;
						}
					}
				} catch (DataSourceException ex) {
					throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t);
				}
			}

			return CBoolean.FALSE;
		}

		@Override
		public String getName() {
			return "is_alias";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {cmd} Returns true if using call_alias with this cmd would trigger an alias.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class call_alias extends AbstractFunction {

		@Override
		public String getName() {
			return "call_alias";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {cmd} Allows a CommandHelper alias to be called from within another alias. Typically this is not possible, as"
					+ " a script that runs \"/jail = /jail\" for instance, would simply be calling whatever plugin that actually"
					+ " provides the jail functionality's /jail command. However, using this function makes the command loop back"
					+ " to CommandHelper only. ---- Returns true if the command was run, or false otherwise. Note however that if an alias"
					+ " ends up throwing an exception to the top level, it will not bubble up to this script, it will be caught and dealt"
					+ " with already; if this happens, this function will still return true, because essentially the return value"
					+ " simply indicates if the command matches an alias. Also, it is worth noting that this will trigger a player's"
					+ " personal alias possibly.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			boolean doRemoval = true;
			if (!Static.getAliasCore().hasPlayerReference(env.getEnv(CommandHelperEnvironment.class).GetCommandSender())) {
				doRemoval = false;
			}
			if (doRemoval) {
				Static.getAliasCore().removePlayerReference(env.getEnv(CommandHelperEnvironment.class).GetCommandSender());
			}
			boolean ret = Static.getAliasCore().alias(args[0].val(), env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), null);
			if (doRemoval) {
				Static.getAliasCore().addPlayerReference(env.getEnv(CommandHelperEnvironment.class).GetCommandSender());
			}
			return CBoolean.get(ret);
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class scriptas extends AbstractFunction {

		@Override
		public String getName() {
			return "scriptas";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {player, [label], script} Runs the specified script in the context of a given player."
					+ " A script that runs player() for instance, would return the specified player's name,"
					+ " not the player running the command. Setting the label allows you to dynamically set the label"
					+ " this script is run under as well (in regards to permission checking)";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) {
			return null;
		}

		@Override
		public Construct execs(Target t, Environment environment, Script parent, ParseTree... nodes) throws ConfigRuntimeException {
			MCPlayer p = Static.GetPlayer(parent.seval(nodes[0], environment).val(), t);
			MCCommandSender originalPlayer = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			int offset = 0;
			String originalLabel = environment.getEnv(GlobalEnv.class).GetLabel();
			if (nodes.length == 3) {
				offset++;
				String label = environment.getEnv(GlobalEnv.class).GetScript().seval(nodes[1], environment).val();
				environment.getEnv(GlobalEnv.class).SetLabel(label);
				environment.getEnv(GlobalEnv.class).GetScript().setLabel(label);
			}
			environment.getEnv(CommandHelperEnvironment.class).SetPlayer(p);
			ParseTree tree = nodes[1 + offset];
			environment.getEnv(GlobalEnv.class).GetScript().eval(tree, environment);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(originalPlayer);
			environment.getEnv(GlobalEnv.class).SetLabel(originalLabel);
			environment.getEnv(GlobalEnv.class).GetScript().setLabel(originalLabel);
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_cmd extends AbstractFunction {

		@Override
		public String getName() {
			return "get_cmd";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "mixed {} Gets the command (as a string) that ended up triggering this script, exactly"
					+ " how it was entered by the player. This could be null, if for instance"
					+ " it is called from within an event.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetCommand() == null) {
				return CNull.NULL;
			} else {
				return new CString(environment.getEnv(CommandHelperEnvironment.class).GetCommand(), t);
			}
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class capture_runas extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String player = args[0].val();
			String cmd = args[1].val();
			if(!cmd.startsWith("/")){
				throw new Exceptions.FormatException("Command must begin with a /", t);
			}
			cmd = cmd.substring(1);
			
			MCCommandSender operator = Static.GetCommandSender(player, t);
			
			String ret = Static.getServer().dispatchAndCaptureCommand(operator, cmd);
			
			return new CString(ret, t);
		}

		@Override
		public String getName() {
			return "capture_runas";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "string {player, command} Works like runas, except any messages sent to the command sender during command execution are attempted to be"
					+ " intercepted, and are then returned as a string, instead of being sent to the command sender. Note that this is VERY easy"
					+ " for plugins to get around in such a way that this function will not work, this is NOT a bug in CommandHelper, nor is it necessarily"
					+ " a problem in the other plugin either, but the other plugin will have to make changes for it to work properly.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_command_block extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCBlockCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetBlockCommandSender();
			if(cs != null){
				MCLocation l = (cs.getBlock().getLocation());
				return ObjectGenerator.GetGenerator().location(l);
			}
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "get_command_block";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "locationArray {} If this command was being run from a command block, this will return the location of"
					+ " the block. If a player or console ran this command, (or any other command sender) this will return null.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
		
	@api(environments = {CommandHelperEnvironment.class})
	public static class psetop extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer player;
			boolean state;
			if (args.length == 1) {
				player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(player, t);
				state = Static.getBoolean(args[0]);
			} else {
				player = Static.GetPlayer(args[0].val(), t);
				state = Static.getBoolean(args[1]);
			}
			player.setOp(state);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "psetop";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {[player], status} Sets whether or not a player has operator status. If no player is specified the player running the script is given.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api(environments = CommandHelperEnvironment.class)
	public static class run_cmd extends AbstractFunction {
		
		private final static run run = new run();
		private final static call_alias call_alias = new call_alias();
		private final static is_alias is_alias = new is_alias();

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CString s;
			if(args[0] instanceof CString){
				s = (CString)args[0];
			} else {
				s = new CString(args[0].val(), t);
			}
			if(is_alias.exec(t, environment, s).getBoolean()){
				call_alias.exec(t, environment, s);
			} else {
				run.exec(t, environment, s);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "run_cmd";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {cmd} Runs a command regardless of whether or not it is an alias or a builtin command. Essentially,"
					+ " this works like checking if(is_alias(@cmd)){ call_alias(@cmd) } else { run(@cmd) }. Be careful with"
					+ " this command, as like call_alias(), you could accidentally create infinite loops. The command must"
					+ " start with a / or this will throw a FormatException.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}

	@api
	public static class noop extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "noop";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {[...]} An operation that does nothing. Any arguments passed in are ignored entirely.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}
	
	@api
	public static class get_locales extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CArray exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray c = new CArray(t);
			for(Locale l : Locale.getAvailableLocales()){
				if(!l.getCountry().equals("")){
					c.push(new CString(l.toString(), t));
				}
			}
			new ArrayHandling.array_sort().exec(t, environment, c);
			c = new ArrayHandling.array_unique().exec(t, environment, c);
			return c;


		}

		@Override
		public String getName() {
			return "get_locales";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns a list of locales on this system.";

		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "get_locales()")
			};
		}

	}
	
	@api
	public static class engine_build_date extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			JarFile jf;
			try {
				String jar = ClassDiscovery.GetClassContainer(Meta.class).toString();
				jar = jar.replaceFirst("file:[\\|/]", "");
				jf = new JarFile(jar);
			} catch (IOException ex) {
				return CNull.NULL;
			}
			ZipEntry manifest = jf.getEntry("META-INF/MANIFEST.MF");
			long manifestTime = manifest.getTime();
			return new CInt(manifestTime, t);
		}

		@Override
		public String getName() {
			return "engine_build_date";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "int {} Returns the compile date, in a millisecond unit time stamp, of when " + Implementation.GetServerType().getBranding() + " was compiled,"
					+ " or null, if that can't be computed for various reasons.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class build_date extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CInt(environment.getEnv(GlobalEnv.class).GetScript().getCompileTime(), t);
		}

		@Override
		public String getName() {
			return "build_date";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "int {} Returns the compile date of the current script, as a unix time stamp in milliseconds.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
