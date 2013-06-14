package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MLocation;
import com.laytonsmith.core.natives.annotations.FormatString;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.persistance.DataSourceException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * I'm So Meta, Even This Acronym
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

		public Mixed exec(Target t, final Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[1].val() == null || args[1].val().length() <= 0 || args[1].val().charAt(0) != '/') {
				throw new ConfigRuntimeException("The first character of the command must be a forward slash (i.e. '/give')",
						ExceptionType.FormatException, t);
			}
			String cmd = args[1].val().substring(1);
			if (args[0] instanceof CArray) {
				CArray u = (CArray) args[0];
				for (int i = 0; i < u.size(); i++) {
					exec(t, env, new Mixed[]{new CString(u.get(i, t).val(), t), args[1]});
				}
				return new CVoid(t);
			}
			if (args[0].val().equals("~op")) {
				//TODO: Remove this after next release (3.3.1)
				CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.WARNING, "Using runas(~op, " + CString.asString(args[1]).getQuote() 
						+ ") is deprecated. Use sudo(" + CString.asString(args[1]).getQuote() + ") instead.", t);
				new sudo().exec(t, env, args[1]);
			} else if (args[0].val().equals(Static.getConsoleName())) {
				CHLog.GetLogger().Log(CHLog.Tags.META, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as console): " + args[1].val().trim(), t);
				if (Prefs.DebugMode()) {
					Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as : " + args[1].val().trim());
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
			return "Runs a command as a particular user. The special user '~console' can be used to run it as a console"
					+ " user. Using '~op' is deprecated, and will be removed after the next release, use sudo() instead."
					+ " Commands cannot be run as an offline player. If the first argument is an array of usernames, the command"
					+ " will be run in the context of each user in the array.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The player (or players if an array) to run the command as", CString.class, CArray.class, "users"),
						new Argument("The command to run. It should start with a '/'", CString.class, "command").addAnnotation(new FormatString("/.*"))
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
			return "Runs a single command for this user, as if they were op. ---- Often times this function is used to allow a user"
					+ " to run an otherwise restricted command, but that only works if the plugin running the command allows operators"
					+ " all privileges on the server, or your permissions plugin grants all permissions to operators, though all possible attempts"
					+ " have been made to make this work regardless. If the command"
					+ " still doesn't work, check to see if the player can run the command if you give them op, and if not, the problem"
					+ " is not with your usage of this function, but rather your server configuration, or the other plugin. Essentially,"
					+ " \"sudo('/command')\" works the same as if you chained together a '/op player' '/command' '/deop player', however"
					+ " the operator status is maintained in memory only, and after the command is run, (or even if an error occurs) they"
					+ " will ALWAYS be deopped after the command is run. There is special handling however, if the command is either"
					+ " \"sudo('/op player')\" or \"sudo('/deop player')\", that is, you're using this function to permanently op or deop"
					+ " a player, then the status will remain permanent, though it is better to use the psetop function directly.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The command to run. It should start with a '/'", CString.class, "command").addAnnotation(new FormatString("/.*"))
					);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Runs the command \"command\" as op", "sudo('/command')", ""),
				new ExampleScript("Permanently ops the player", "sudo('/op '.player())", "")
			};
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

			try {
				((MCPlayer) player).setTempOp(value);
			} catch (ClassNotFoundException e) {
			} catch (IllegalStateException e) {
			} catch (Throwable e) {
				Static.getLogger().log(Level.WARNING, "[CommandHelper]: Failed to OP player " + player.getName());
				System.err.println("Extra information about the error: ");
				e.printStackTrace();
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if (args[0].val() == null || args[0].val().length() <= 0 || args[0].val().charAt(0) != '/') {
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
			//p.chat(cmd);
			Static.getServer().dispatchCommand(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), cmd);
			return new CVoid(t);
		}

		public String docs() {
			return "Runs a command as the current player. Useful for running commands in a loop. Note that this accepts commands like from the "
					+ "chat; with a forward slash in front.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The command to run. It must start with a '/'", CString.class, "command").addAnnotation(new FormatString("/.*"))
					);
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
	public static class g extends AbstractFunction implements Optimizable {

		public String getName() {
			return "g";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			for (int i = 0; i < args.length; i++) {
				args[i].val();
			}
			return new CVoid(t);
		}

		public String docs() {
			return "Groups any number of functions together, and returns void. This method is deprecated, and should not be used anymore."
					+ " It will be removed in a future release, as it is not needed anymore.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", Mixed.class, "code").setVarargs()
					);
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

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			CHLog.GetLogger().CompilerWarning(CompilerWarning.Deprecated, "g() is deprecated,"
					+ " and will be removed in a future release. You do not need this function anymore."
					+ " If your code were g(run('/cmd') run('/cmd')) you can simply remove the g(), and"
					+ " it will function the same.", t, env.getEnv(CompilerEnvironment.class).getFileOptions());
			return null;
		}
		
	}

	@api
	public static class eval extends AbstractFunction implements Optimizable {

		public String getName() {
			return "eval";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Executes arbitrary MethodScript. Note that this function is very experimental, and is subject to changing or "
					+ "removal. The function returns a string, which is by design, so it encourages better design patterns than executing dynamic code."
					+ " This does allow for embedding an interpreter however, and can be used in a very few cases to allow for things like a built in"
					+ " debug console, or other meta programming tasks. The environment is passed along to the evaluated script, and the working file"
					+ " of the script is set to this file, even if the script is read() from elsewhere. Ideally, however, you should simply use"
					+ " procedures and includes to accomplish your tasks.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The script to execute.", CString.class, "script")
					);
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
			ParseTree node = nodes[0];
			try {
				Mixed script = parent.seval(node, env);
				ParseTree root = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script.val(), t.file(), true), env);
				StringBuilder b = new StringBuilder();
				int count = 0;
				for (ParseTree child : root.getChildren()) {
					Mixed s = parent.seval(child, env);
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
			}
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
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

		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			if(children.get(0).isConst()){
				CHLog.GetLogger().Log(CHLog.Tags.META, LogLevel.WARNING, "Hardcoded string in eval. This is inefficient."
						+ " You should instead simply run the code.", t);
			}
			CHLog.GetLogger().CompilerWarning(CompilerWarning.UseOfEval, "Use of eval is strongly discouraged,"
					+ " and only meant for extremely experimental or temporary features. If you truly do need"
					+ " eval, consider filing a feature request for support for what you're actually doing, instead"
					+ " of continuing to rely on eval in the future.", t, children.get(0).getFileOptions());
			return null;
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

		public Mixed exec(Target t, Environment environment, Mixed... args)
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
			return "Returns true if using call_alias with this cmd would trigger an alias.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "command")
					);
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
			return "Allows a CommandHelper alias to be called from within another alias. Typically this is not possible, as"
					+ " a script that runs \"/jail = /jail\" for instance, would simply be calling whatever plugin that actually"
					+ " provides the jail functionality's /jail command. However, using this function makes the command loop back"
					+ " to CommandHelper only. ---- Returns true if the command was run, or false otherwise. Note however that if an alias"
					+ " ends up throwing an exception to the top level, it will not bubble up to this script, it will be caught and dealt"
					+ " with already; if this happens, this function will still return true, because essentially the return value"
					+ " simply indicates if the command matches an alias. Also, it is worth noting that this will trigger a player's"
					+ " personal alias possibly.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The alias to call", CString.class, "alias").addAnnotation(new FormatString("/.*"))
					);
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

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
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
			return "Runs the specified script in the context of a given player."
					+ " A script that runs player() for instance, would return the specified player's name,"
					+ " not the player running the command. Setting the label allows you to dynamically set the label"
					+ " this script is run under as well (in regards to permission checking)";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player"),
						new Argument("", CString.class, "label").setOptionalDefaultNull(),
						new Argument("", CString.class, "script")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) {
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
			return "Gets the command (as a string) that ended up triggering this script, exactly"
					+ " how it was entered by the player. This could be null, if for instance"
					+ " it is called from within an event.";
		}
		
		public Argument returnType() {
			return new Argument("", Mixed.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetCommand() == null) {
				return null;
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
			return "This function is still experimental, but may work in some cases. Works like runas,"
					+ " except any messages sent to the command sender during command execution are attempted to be"
					+ " intercepted, and are then returned as a string, instead of being sent to the command sender. Note that this is VERY easy"
					+ " for plugins to get around in such a way that this function will not work, this is NOT a bug in CommandHelper, nor is it necessarily"
					+ " a problem in the other plugin either, but the other plugin will have to make changes for it to work properly.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player"),
						new Argument("", CString.class, "command")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCBlockCommandSender cs = environment.getEnv(CommandHelperEnvironment.class).GetBlockCommandSender();
			if(cs != null){
				MCLocation l = (cs.getBlock().getLocation());
				return ObjectGenerator.GetGenerator().location(l);
			}
			return null;
		}

		public String getName() {
			return "get_command_block";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "If this command was being run from a command block, this will return the location of"
					+ " the block. If a player or console ran this command, (or any other command sender) this will return null.";
		}
		
		public Argument returnType() {
			return new Argument("", MLocation.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			Static.AssertPlayerNonNull(p, t);
			boolean state = false;
			if(args.length == 2) {
				p = Static.GetPlayer(args[0].val(), t);
				state = args[1].primitive(t).castToBoolean();
			} else if(args.length == 1) {
				state = args[0].primitive(t).castToBoolean();
			}
			
			p.setOp(state);
			return new CVoid(t);
		}

		public String getName() {
			return "psetop";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Sets whether or not a player has operator status. If no player is specified the player running the script is given.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CString.class, "player").setOptionalDefaultNull(),
					new Argument("", CBoolean.class, "status")
				);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
