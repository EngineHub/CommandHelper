package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.persistance.DataSourceException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;

/**
 * I'm So Meta, Even This Acronym
 *
 * @author Layton
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
			return new CBoolean(CommandHelperPlugin.isFirstLoad(), t);
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

		public String getName() {
			return "runas";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

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
				return new CVoid(t);
			}
			if (args[0].val().equals("~op")) {
				//TODO: Remove this after next release (3.3.1)
				CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.WARNING, "Using runas(~op, " + args[1].asString().getQuote() 
						+ ") is deprecated. Use sudo(" + args[1].asString().getQuote() + ") instead.", t);
				new sudo().exec(t, env, args[1]);
			} else if (args[0].val().equals(Static.getConsoleName())) {
				CHLog.GetLogger().Log(CHLog.Tags.META, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as console): " + args[1].val().trim(), t);
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
			return new CVoid(t);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
		}

		public String docs() {
			return "void {player, command} Runs a command as a particular user. The special user '" + Static.getConsoleName() + "' can be used to run it as a console"
					+ " user. Using '~op' is deprecated, and will be removed after the next release, use sudo() instead."
					+ " Commands cannot be run as an offline player. If the first argument is an array of usernames, the command"
					+ " will be run in the context of each user in the array.";
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class sudo extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

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
			return new CVoid(t);
		}

		public String getName() {
			return "sudo";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

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
					System.err.println("Extra information about the error: ");
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

		public String getName() {
			return "run";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

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
			Static.getServer().dispatchCommand(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), cmd);
			return new CVoid(t);
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

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return false;
		}
	}

	@api
	@noprofile
	@hide("This will eventually be replaced by ; statements.")
	public static class g extends AbstractFunction {

		public String getName() {
			return "g";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			for (int i = 0; i < args.length; i++) {
				args[i].val();
			}
			return new CVoid(t);
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

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			return null;
		}
	}

	@api
	public static class eval extends AbstractFunction {

		public String getName() {
			return "eval";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {script_string} Executes arbitrary MethodScript. Note that this function is very experimental, and is subject to changing or "
					+ "removal.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_0;
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			boolean oldDynamicScriptMode = env.getEnv(GlobalEnv.class).GetDynamicScriptingMode();
			ParseTree node = nodes[0];
			try {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(true);
				Construct script = parent.seval(node, env);
				ParseTree root = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script.val(), t.file(), true));
				StringBuilder b = new StringBuilder();
				int count = 0;
				for (ParseTree child : root.getChildren()) {
					Construct s = parent.seval(child, env);
					if (!s.val().trim().isEmpty()) {
						if (count > 0) {
							b.append(" ");
						}
						b.append(s.val());
					}
					count++;
				}
				return new CString(b.toString(), t);
			} catch (ConfigCompileException e) {
				throw new ConfigRuntimeException("Could not compile eval'd code: " + e.getMessage(), ExceptionType.FormatException, t);
			} finally {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(oldDynamicScriptMode);
			}
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			return new CVoid(t);
		}
		//Doesn't matter, run out of state anyways

		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class is_alias extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IOException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public CBoolean exec(Target t, Environment environment, Construct... args)
				throws ConfigRuntimeException {
			AliasCore ac = Static.getAliasCore();

			for (Script s : ac.getScripts()) {
				if (s.match(args[0].val())) {
					return new CBoolean(true, t);
				}
			}

			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if (p instanceof MCPlayer) {
				try {
					// p might be null
					for (Script s : UserManager.GetUserManager(p.getName()).getAllScripts(environment.getEnv(GlobalEnv.class).GetPersistanceNetwork())) {
						if (s.match(args[0].val())) {
							return new CBoolean(true, t);
						}
					}
				} catch (DataSourceException ex) {
					throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t);
				}
			}

			return new CBoolean(false, t);
		}

		public String getName() {
			return "is_alias";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {cmd} Returns true if using call_alias with this cmd would trigger an alias.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class call_alias extends AbstractFunction {

		public String getName() {
			return "call_alias";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

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

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_2_0;
		}

		public Boolean runAsync() {
			return null;
		}

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
			return new CBoolean(ret, t);
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class scriptas extends AbstractFunction {

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

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

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
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_cmd extends AbstractFunction {

		public String getName() {
			return "get_cmd";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "mixed {} Gets the command (as a string) that ended up triggering this script, exactly"
					+ " how it was entered by the player. This could be null, if for instance"
					+ " it is called from within an event.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetCommand() == null) {
				return new CNull(t);
			} else {
				return new CString(environment.getEnv(CommandHelperEnvironment.class).GetCommand(), t);
			}
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	public static class CommandSenderIntercepter implements InvocationHandler {

		MCCommandSender sender;
		StringBuilder buffer;

		public CommandSenderIntercepter(MCCommandSender sender) {
			this.sender = sender;
			buffer = new StringBuilder();
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			System.out.println("---------------> Invoking proxy");
			if ("sendMessage".equals(method.getName())) {
				System.out.println("---------------> Intercepting sendMessage()");
				buffer.append(args[0].toString());
				return Void.TYPE;
			} else {
				System.out.println("---------------> Bypassing intercepter, and calling real's " + method.getName());
				return method.invoke(sender, args);
			}
		}

		public String getBuffer() {
			return buffer.toString();
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class capture_runas extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			System.out.println("---------------> Executing capture_runas(" + args[0].val() + ", " + args[1].val() + ")");
			MCCommandSender oldCommandSender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCCommandSender operator;
			if ("~op".equals(args[0].val()) || "~console".equals(args[0].val())) {
				System.out.println("---------------> Using ~op or ~console, so retrieving operator from environment");
				operator = oldCommandSender;
			} else {
				System.out.println("---------------> Using player, so retrieving operator from args: " + args[0].val());
				operator = Static.GetPlayer(args[0], t);
			}
			if (operator instanceof MCPlayer) {
				Static.UninjectPlayer(((MCPlayer) operator));
			}
			CommandSenderIntercepter intercepter = new CommandSenderIntercepter(operator);
			MCCommandSender newCommandSender = (MCCommandSender) Proxy.newProxyInstance(Meta.class.getClassLoader(), new Class[]{MCCommandSender.class, MCPlayer.class}, intercepter);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(newCommandSender);
			if (operator instanceof MCPlayer) {
				Static.InjectPlayer(((MCPlayer) newCommandSender));
			}
			new runas().exec(t, environment, args);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(oldCommandSender);
			if (operator instanceof MCPlayer) {
				Static.UninjectPlayer(((MCPlayer) newCommandSender));
				Static.InjectPlayer(((MCPlayer) operator));
			}
			return new CString(intercepter.getBuffer(), t);
		}

		public String getName() {
			return "capture_runas";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "string {player, command} Works like runas, except any messages sent to the command sender during command execution are attempted to be"
					+ " intercepted, and are then returned as a string, instead of being sent to the command sender. Note that this is VERY easy"
					+ " for plugins to get around in such a way that this function will not work, this is NOT a bug in CommandHelper, nor is it necessarily"
					+ " a problem in the other plugin either, but the other plugin will have to make changes for it to work properly.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class get_command_block extends AbstractFunction {

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCBlockCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetBlockCommandSender();
			if(cs != null){
				MCLocation l = (cs.getBlock().getLocation());
				return ObjectGenerator.GetGenerator().location(l);
			}
			return new CNull(t);
		}

		public String getName() {
			return "get_command_block";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "locationArray {} If this command was being run from a command block, this will return the location of"
					+ " the block. If a player or console ran this command, (or any other command sender) this will return null.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
		
	@api(environments = {CommandHelperEnvironment.class})
	public static class psetop extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

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
			return new CVoid(t);
		}

		public String getName() {
			return "psetop";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "string {[player], status} Sets whether or not a player has operator status. If no player is specified the player running the script is given.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api(environments = CommandHelperEnvironment.class)
	public static class run_cmd extends AbstractFunction {
		
		private final static run run = new run();
		private final static call_alias call_alias = new call_alias();
		private final static is_alias is_alias = new is_alias();

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

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
			return new CVoid(t);
		}

		public String getName() {
			return "run_cmd";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {cmd} Runs a command regardless of whether or not it is an alias or a builtin command. Essentially,"
					+ " this works like checking if(is_alias(@cmd)){ call_alias(@cmd) } else { run(@cmd) }. Be careful with"
					+ " this command, as like call_alias(), you could accidentally create infinite loops. The command must"
					+ " start with a / or this will throw a FormatException.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
