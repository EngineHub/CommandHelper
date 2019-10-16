package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCCommandMinecart;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CResource;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.jar.JarFile;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

/**
 *
 */
public class Meta {

	public static String docs() {
		return "These functions provide a way to run other commands, and otherwise interact with the system in a meta"
				+ " way.";
	}

	/*
	@api
	public static class first_load extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return false;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
			return MSVersion.V3_3_1;
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
		public Mixed exec(Target t, final Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(Construct.nval(args[1]) == null || args[1].val().length() <= 0 || args[1].val().charAt(0) != '/') {
				throw new CREFormatException("The first character of the command must be a forward slash (i.e. '/give')", t);
			}
			String cmd = args[1].val().substring(1);
			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray u = (CArray) args[0];
				for(int i = 0; i < u.size(); i++) {
					exec(t, env, new Mixed[]{new CString(u.get(i, t).val(), t), args[1]});
				}
				return CVoid.VOID;
			}
			if(args[0].val().equals(Static.getConsoleName())) {
				MSLog.GetLogger().Log(MSLog.Tags.META, LogLevel.INFO, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as console): " + args[1].val().trim(), t);
				if(Prefs.DebugMode()) {
					Static.getLogger().log(Level.INFO, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as : " + args[1].val().trim());
				}
				if(cmd.equalsIgnoreCase("interpreter-on")) {
					//This isn't allowed for security reasons.
					throw new CREFormatException("/interpreter-on cannot be run from runas for security reasons.", t);
				}
				Static.getServer().runasConsole(cmd);
			} else {
				MCPlayer m = Static.GetPlayer(args[0], t);
				if(m != null && m.isOnline()) {
					MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
					String name;

					if(p != null) {
						name = p.getName();
					} else {
						name = "Unknown player";
					}

					MSLog.GetLogger().Log(MSLog.Tags.META, LogLevel.INFO, "Executing command on " + name + " (running as " + args[0].val() + "): " + args[1].val().trim(), t);
					if(Prefs.DebugMode()) {
						Static.getLogger().log(Level.INFO, "Executing command on " + name + " (running as " + args[0].val() + "): " + args[1].val().trim());
					}
					//m.chat(cmd);
					Static.getServer().dispatchCommand(m, cmd);
				} else {
					throw new CREPlayerOfflineException("The player " + args[0].val() + " is not online", t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREPlayerOfflineException.class};
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
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class sudo extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			Mixed command;
			MCPlayer sender;
			if(args.length == 2) {
				sender = Static.GetPlayer(args[0], t);
				command = args[1];
			} else {
				sender = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				command = args[0];
			}

			//If the command sender is null, this is not a player, so just try to run() this.
			if(sender == null) {
				return new run().exec(t, env, args);
			}

			if(Construct.nval(command) == null || command.val().isEmpty() || command.val().charAt(0) != '/') {
				throw new CREFormatException("The first character of a command must be a forward slash (eg. /give)", t);
			}

			String cmd = command.val().substring(1);

			//Store their current op status
			Boolean isOp = sender.isOp();

			MSLog.GetLogger().Log(MSLog.Tags.META, LogLevel.INFO, "Executing command on " + sender
					+ " (as op): " + command.val(), t);
			if(Prefs.DebugMode()) {
				Static.getLogger().log(Level.INFO, "Executing command on " + sender + " (as op): " + command.val());
			}

			//If they aren't op, op them now
			if(!isOp) {
				this.setOp(sender, true);
			}

			try {
				Static.getServer().dispatchCommand(this.getOPCommandSender(sender), cmd);
			} finally {
				//If they just opped themselves, or deopped themselves in the command
				//don't undo what they just did. Otherwise, set their op status back
				//to their original status
				if(!cmd.equalsIgnoreCase("op " + sender.getName())
						&& !cmd.equalsIgnoreCase("deop " + sender.getName())) {
					this.setOp(sender, isOp);
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
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], command} Runs a single command for the current or provided player, as op."
					+ " ---- This is guaranteed to not allow the player to stay op, even if a fatal error occurs during"
					+ " the command. If this guarantee cannot be met, the function will simply fail. Some server types"
					+ " may find that this function does not work at all. If that's the case and you are ok with losing"
					+ " the deop guarantee, you can set use-sudo-fallback to true in your preferences."
					+ " Then if the normal sudo functionality fails on your server, then it will actually fully op the"
					+ " player, run the command, and finally deop the player. However, this is less reliable than"
					+ " the normal sudo mechanism, and could potentially fail, leaving the player as op."
					+ " So, this is not recommended. Enable that setting at your own risk.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		/**
		 * Set OP status for player without saving to ops.txt
		 *
		 * @param player
		 * @param value
		 */
		private void setOp(MCPlayer player, Boolean value) {
			if(player.isOp() == value) {
				return;
			}
			try {
				player.setTempOp(value);
			} catch (Exception e) {
				if(Prefs.UseSudoFallback()) {
					player.setOp(value);
				} else {
					Static.getLogger().log(Level.WARNING, "Failed to OP player " + player.getName() + "."
							+ " Check that your server jar ends with \".jar\"."
							+ " You can choose to enable \"use-sudo-fallback\" in preferences.ini.");
					StreamUtils.GetSystemErr().println("Extra information about the error: ");
					e.printStackTrace();
				}
			}
		}

		private MCPlayer getOPCommandSender(final MCPlayer sender) {
			if(sender.isOp()) {
				return sender;
			}

			return (MCPlayer) Proxy.newProxyInstance(sender.getClass().getClassLoader(),
					new Class[]{MCPlayer.class}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					String methodName = method.getName();
					if("isOp".equals(methodName) || "hasPermission".equals(methodName) || "isPermissionSet".equals(methodName)) {
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if(Construct.nval(args[0]) == null || args[0].val().length() <= 0 || args[0].val().charAt(0) != '/') {
				throw new CREFormatException("The first character of the command must be a forward slash (i.e. '/give')", t);
			}
			String cmd = args[0].val().substring(1);
			if(Prefs.DebugMode()) {
				if(env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					Static.getLogger().log(Level.INFO, "Executing command on " + env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() + ": " + args[0].val().trim());
				} else {
					Static.getLogger().log(Level.INFO, "Executing command from console equivalent: " + args[0].val().trim());
				}
			}
			if(cmd.equalsIgnoreCase("interpreter-on")) {
				throw new CREFormatException("/interpreter-on cannot be run as apart of an alias for security reasons.", t);
			}
			try {
				Static.getServer().dispatchCommand(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), cmd);
			} catch (Exception ex) {
				throw new CREPluginInternalException("While running the command: \"" + cmd + "\""
						+ " the plugin threw an unexpected exception (turn on debug mode to see the full"
						+ " stacktrace): " + ex.getMessage() + "\n\nThis is not a bug in " + Implementation.GetServerType().getBranding()
						+ " but in the plugin that provides the command.", t, ex);
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {string command} Runs a command as the current player. Useful for running commands in a loop."
					+ " Note that this accepts commands like from the "
					+ "chat; with a forward slash in front.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREPluginInternalException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_cmd_completions extends AbstractFunction {

		@Override
		public String getName() {
			return "get_cmd_completions";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender sender;
			String commandString;
			List<Mixed> argList;
			if(args.length == 2) {
				sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				commandString = args[0].val();
				argList = Static.getArray(args[1], t).asList();
			} else {
				sender = Static.GetCommandSender(args[0].val(), t);
				commandString = args[1].val();
				argList = Static.getArray(args[2], t).asList();
			}

			if(commandString.length() < 1 || commandString.charAt(0) != '/') {
				throw new CREFormatException("The first character of the command must be a forward slash (i.e. '/give')", t);
			}
			commandString = commandString.substring(1);
			MCCommand command = Static.getServer().getCommandMap().getCommand(commandString);
			if(command == null) {
				throw new CRENotFoundException("Command does not exist: " + commandString, t);
			}

			String[] arguments = new String[argList.size()];
			for(int i = 0; i < argList.size(); i++) {
				arguments[i] = argList.get(i).val();
			}

			List<String> completions = command.tabComplete(sender, commandString, arguments);
			CArray ret = new CArray(t);
			for(String s : completions) {
				ret.push(new CString(s, t), t);
			}
			return ret;
		}

		@Override
		public String docs() {
			return "array {[player], command, args} Runs a plugin command's tab completer and returns an array of"
					+ " possible completions for the final argument. ----"
					+ " The args parameter must be an array of strings."
					+ " A command prefix can be used to specify a specific plugin. (eg. \"/worldedit:remove\")"
					+ " Throws a NotFoundException if the command does not exist.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREPluginInternalException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class is_alias extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
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
		public CBoolean exec(Target t, Environment environment, Mixed... args)
				throws ConfigRuntimeException {
			AliasCore ac = Static.getAliasCore();

			for(Script s : ac.getScripts()) {
				if(s.match(args[0].val())) {
					return CBoolean.TRUE;
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
					+ " simply indicates if the command matches an alias.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			boolean doRemoval = true;
			if(!Static.getAliasCore().hasPlayerReference(env.getEnv(CommandHelperEnvironment.class).GetCommandSender())) {
				doRemoval = false;
			}
			if(doRemoval) {
				Static.getAliasCore().removePlayerReference(env.getEnv(CommandHelperEnvironment.class).GetCommandSender());
			}
			boolean ret = Static.getAliasCore().alias(args[0].val(), env.getEnv(CommandHelperEnvironment.class).GetCommandSender());
			if(doRemoval) {
				Static.getAliasCore().addPlayerReference(env.getEnv(CommandHelperEnvironment.class).GetCommandSender());
			}
			return CBoolean.get(ret);
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class scriptas extends AbstractFunction implements VariableScope {

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
			return "void {player, [label], script} Runs the specified script in the context of a given player or "
					+ Static.getConsoleName() + ". A script that runs player(), for instance,"
					+ " would return the specified player's name, not the player running the command."
					+ " Setting the label allows you to dynamically set the label"
					+ " this script is run under as well (in regards to permission checking)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
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
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) {
			return null;
		}

		@Override
		public Mixed execs(Target t, Environment environment, Script parent, ParseTree... nodes) throws ConfigRuntimeException {
			String senderName = parent.seval(nodes[0], environment).val();
			MCCommandSender sender;
			if(senderName.equals(Static.getConsoleName())) {
				sender = Static.getServer().getConsole();
			} else {
				sender = Static.GetPlayer(senderName, t);
			}
			MCCommandSender originalSender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			int offset = 0;
			String originalLabel = environment.getEnv(GlobalEnv.class).GetLabel();
			if(nodes.length == 3) {
				offset++;
				String label = parent.seval(nodes[1], environment).val();
				environment.getEnv(GlobalEnv.class).SetLabel(label);
			} else {
				environment.getEnv(GlobalEnv.class).SetLabel(parent.getLabel());
			}
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(sender);
			parent.enforceLabelPermissions();
			ParseTree tree = nodes[1 + offset];
			parent.eval(tree, environment);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(originalSender);
			environment.getEnv(GlobalEnv.class).SetLabel(originalLabel);
			return CVoid.VOID;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			ret.add(false);
			if(children.size() == 3) {
				ret.add(false);
			}
			ret.add(true);
			return ret;
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
			return "string {} Gets the command (as a string) that ended up triggering this script, exactly"
					+ " how it was entered by the player. This could be null, if for instance"
					+ " it is called from within an event.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(environment.getEnv(CommandHelperEnvironment.class).GetCommand() == null) {
				return CNull.NULL;
			} else {
				return new CString(environment.getEnv(CommandHelperEnvironment.class).GetCommand(), t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

	@api(environments = {CommandHelperEnvironment.class, GlobalEnv.class})
	public static class capture_runas extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class,
				CREPluginInternalException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String player = args[0].val();
			String cmd = args[1].val();
			if(!cmd.startsWith("/")) {
				throw new CREFormatException("Command must begin with a /", t);
			}
			cmd = cmd.substring(1);

			MCCommandSender operator = Static.GetCommandSender(player, t);

			String ret;
			try {
				ret = Static.getServer().dispatchAndCaptureCommand(operator, cmd);
			} catch (Exception ex) {
				throw new CREPluginInternalException(ex.getMessage(), t, ex);
			}

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
					+ " a problem in the other plugin either, but the other plugin will have to make changes for it to work properly."
					+ " A PluginInternalException is thrown if something goes wrong. Any number of things may go wrong that aren't necessarily"
					+ " this function's fault, and in those cases, this exception is thrown.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_command_block extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			MCLocation loc;
			CArray ret;
			if(sender instanceof MCBlockCommandSender) {
				loc = ((MCBlockCommandSender) sender).getBlock().getLocation();
				ret = ObjectGenerator.GetGenerator().location(loc, false); // Do not include pitch/yaw.
			} else if(sender instanceof MCCommandMinecart) {
				loc = ((MCCommandMinecart) sender).getLocation();
				ret = ObjectGenerator.GetGenerator().location(loc, true); // Include pitch/yaw.
			} else {
				return CNull.NULL;
			}
			return ret;
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
			return "locationArray {} If this command was being run from a command block block or minecart, this will"
					+ " return the location of the block or minecart."
					+ " The yaw and pitch will only be included in the locationArray for minecart command blocks."
					+ " If a player or console ran this command (or any other command sender), this will return null.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class psetop extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			boolean state;
			if(args.length == 1) {
				player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(player, t);
				state = ArgumentValidation.getBoolean(args[0], t);
			} else {
				player = Static.GetPlayer(args[0].val(), t);
				state = ArgumentValidation.getBoolean(args[1], t);
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class run_cmd extends AbstractFunction {

		// Variable is more clear when named after the function it represents.
		@SuppressWarnings("checkstyle:constantname")
		private static final run run = new run();

		// Variable is more clear when named after the function it represents.
		@SuppressWarnings("checkstyle:constantname")
		private static final call_alias call_alias = new call_alias();

		// Variable is more clear when named after the function it represents.
		@SuppressWarnings("checkstyle:constantname")
		private static final is_alias is_alias = new is_alias();

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CString s;
			if(args[0].isInstanceOf(CString.TYPE)) {
				s = (CString) args[0];
			} else {
				s = new CString(args[0].val(), t);
			}
			if(is_alias.exec(t, environment, s).getBoolean()) {
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
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class noop extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
			return "void {[...]} An operation that does nothing. Any arguments passed in are ignored entirely, though"
					+ " they will be evaluated first.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate // A boilerplate test on this function is relatively expensive and not necessary.
	public static class get_locales extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public CArray exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray c = new CArray(t);
			for(Locale l : Locale.getAvailableLocales()) {
				if(!l.getCountry().isEmpty()) {
					c.push(new CString(l.toString(), t), t);
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
			return MSVersion.V3_3_1;
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
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			JarFile jf;
			try {
				String jar = ClassDiscovery.GetClassContainer(Meta.class).toString();
				jar = jar.replaceFirst("file:", "");
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
			return MSVersion.V3_3_1;
		}
	}

	@api
	@noboilerplate
	public static class build_date extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class get_script_environment extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CResource<>(environment, t);
		}

		@Override
		public String getName() {
			return "get_script_environment";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "resource {} Returns a copy of the underlying engine's environment object. This is only useful to embedded scripting"
					+ " engines that are attempting to call back into " + Implementation.GetServerType().getBranding() + ". The object returned"
					+ " is a CResource.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class get_compiler_options extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(FileOptions.CompilerOption s : FileOptions.CompilerOption.values()) {
				ret.push(new CString(s.getName(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_compiler_options";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns a list of all defined compiler options, which can be set using the"
					+ " compilerOptions file option";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class get_compiler_warnings extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			for(FileOptions.SuppressWarning s : FileOptions.SuppressWarning.values()) {
				ret.push(new CString(s.getName(), t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_compiler_warnings";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns a list of all defined compiler warnings, which can be suppressed using the"
					+ " suppressWarnings file option";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	@seealso({has_runtime_setting.class, remove_runtime_setting.class, DataHandling._import.class,
		DataHandling._export.class})
	public static class set_runtime_setting extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String name = ArgumentValidation.getString(args[0], t);
			Mixed setting = args[1];
			environment.getEnv(GlobalEnv.class).SetRuntimeSetting(name, setting);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_runtime_setting";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {name, setting} Sets the value of a particular runtime setting. Various system components can"
					+ " define these differently, so see the documentation for a particular component to see if it has"
					+ " a runtime setting that can be changed, and what the name and setting should be. Note that there"
					+ " is intentionally no mechanism provided to get the value of a setting, as this is not meant to"
					+ " be used for user settings, just system level settings. To set your own user based settings,"
					+ " use {{function|import}}/{{function|export}}.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	@seealso({set_runtime_setting.class, has_runtime_setting.class})
	public static class remove_runtime_setting extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String name = ArgumentValidation.getString(args[0], t);
			GlobalEnv env = environment.getEnv(GlobalEnv.class);
			if(env.GetRuntimeSetting(name) != null) {
				env.SetRuntimeSetting(name, null);
			} else {
				if(!ArgumentValidation.getBooleanish(
						env.GetRuntimeSetting("function.remove_runtime_setting.no_warn_on_removing_blank",
								CBoolean.FALSE), t)) {
					MSLog.GetLogger().e(MSLog.Tags.META, "Attempting to remove a runtime setting that doesn't exist,"
							+ " '" + name + "'", t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "remove_runtime_setting";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {name} Removes the previously set runtime setting. If the setting wasn't already set, then"
					+ " a warning is issued, unless"
					+ " 'function.remove_runtime_setting.no_warn_on_removing_blank' is set to true.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	@seealso({set_runtime_setting.class, remove_runtime_setting.class})
	public static class has_runtime_setting extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String name = ArgumentValidation.getString(args[0], t);
			return CBoolean.get(environment.getEnv(GlobalEnv.class).GetRuntimeSetting(name) != null);
		}

		@Override
		public String getName() {
			return "has_runtime_setting";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {name} Returns true if the runtime setting is set. This will also return true if the value"
					+ " of the setting is null.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	public static class nameof extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new Error();
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException("Invalid number of arguments passed to " + getName(), t);
			}
			ParseTree d = children.get(0);
			Mixed m = d.getData();
			String ret = null;
			if(m instanceof IVariable) {
				ret = ((IVariable) m).getVariableName();
			} else if(m instanceof Variable) {
				ret = ((Variable) m).getVariableName();
			}
			if(ret == null) {
				throw new ConfigCompileException("Invalid type passed to " + getName(), t);
			}
			return new ParseTree(new CString(ret, t), fileOptions);
		}

		@Override
		public String getName() {
			return "nameof";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {component} Returns the name of the item. For now, only works with variables."
					+ " For instance, nameof(@var)"
					+ " returns the string \"@var\". This is useful for avoiding hardcoding of strings of items"
					+ " that are refactorable. This allows tools to properly refactor, without needing to manually"
					+ " update strings that contain the names of variables or other refactorable items. This is"
					+ " a meta function, and is fully resolved at compile time.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api
	public static class engine_location extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CString(ClassDiscovery.GetClassContainer(Meta.class).toString(), t);
		}

		@Override
		public String getName() {
			return "engine_location";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "string {} Returns the location of the currently running engine binary.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

	}
}
