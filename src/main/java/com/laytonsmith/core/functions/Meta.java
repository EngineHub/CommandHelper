package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
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
import java.lang.reflect.Constructor;
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

	@api(environments={CommandHelperEnvironment.class})
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
				//Store their current op status
				Boolean isOp = env.getEnv(CommandHelperEnvironment.class).GetCommandSender().isOp();

				CHLog.GetLogger().Log(CHLog.Tags.META, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as op): " + args[1].val().trim(), t);
				if (Prefs.DebugMode()) {
					Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as op): " + args[1].val().trim());
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
			} else if (args[0].val().equals("~console")) {
				CHLog.GetLogger().Log(CHLog.Tags.META, "Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as console): " + args[1].val().trim(), t);
				if (Prefs.DebugMode()) {
					Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + (env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null ? env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName() : "console") + " (as : " + args[1].val().trim());
				}
				Static.getServer().runasConsole(cmd);
			} else {
				MCPlayer m = Static.GetPlayer(args[0]);
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
			return "void {player, command} Runs a command as a particular user. The special user '~op' is a user that runs as op. Be careful with this very powerful function."
					+ " Commands cannot be run as an offline player. Returns void. If the first argument is an array of usernames, the command"
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

	@api(environments={CommandHelperEnvironment.class})
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
			//p.chat(cmd);
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
			ParseTree node = nodes[0];
			try {
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

	@api(environments={CommandHelperEnvironment.class, GlobalEnv.class})
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

		public Construct exec(Target t, Environment environment, Construct... args)
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

	@api(environments={CommandHelperEnvironment.class})
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

	@api(environments={CommandHelperEnvironment.class, GlobalEnv.class})
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
			String originalLabel = environment.getEnv(CommandHelperEnvironment.class).GetLabel();
			if (nodes.length == 3) {
				offset++;
				String label = environment.getEnv(GlobalEnv.class).GetScript().seval(nodes[1], environment).val();
				environment.getEnv(CommandHelperEnvironment.class).SetLabel(label);
				environment.getEnv(GlobalEnv.class).GetScript().setLabel(label);
			}
			environment.getEnv(CommandHelperEnvironment.class).SetPlayer(p);
			ParseTree tree = nodes[1 + offset];
			environment.getEnv(GlobalEnv.class).GetScript().eval(tree, environment);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(originalPlayer);
			environment.getEnv(CommandHelperEnvironment.class).SetLabel(originalLabel);
			environment.getEnv(GlobalEnv.class).GetScript().setLabel(originalLabel);
			return new CVoid(t);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
	}

	@api(environments={CommandHelperEnvironment.class})
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
	
	public static class CommandSenderIntercepter implements InvocationHandler{
		MCCommandSender sender;
		StringBuilder buffer;
		public CommandSenderIntercepter(MCCommandSender sender){
			this.sender = sender;
			buffer = new StringBuilder();
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {	
			System.out.println("---------------> Invoking proxy");
			if("sendMessage".equals(method.getName())){
				System.out.println("---------------> Intercepting sendMessage()");
				buffer.append(args[0].toString());
				return Void.TYPE;
			} else {
				System.out.println("---------------> Bypassing intercepter, and calling real's " + method.getName());
				return method.invoke(sender, args);
			}
		}
		
		public String getBuffer(){
			return buffer.toString();
		}
	}
	@api(environments={CommandHelperEnvironment.class, GlobalEnv.class})
	public static class capture_runas extends AbstractFunction{

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
			if("~op".equals(args[0].val()) || "~console".equals(args[0].val())){
				System.out.println("---------------> Using ~op or ~console, so retrieving operator from environment");
				operator = oldCommandSender;
			} else {
				System.out.println("---------------> Using player, so retrieving operator from args: " + args[0].val());
				operator = Static.GetPlayer(args[0]);
			}
			if(operator instanceof MCPlayer){
				Static.UninjectPlayer(((MCPlayer)operator));
			}
			CommandSenderIntercepter intercepter = new CommandSenderIntercepter(operator);
			MCCommandSender newCommandSender = (MCCommandSender) Proxy.newProxyInstance(Meta.class.getClassLoader(), new Class[]{MCCommandSender.class, MCPlayer.class}, intercepter);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(newCommandSender);
			if(operator instanceof MCPlayer){
				Static.InjectPlayer(((MCPlayer)newCommandSender));
			}
			new runas().exec(t, environment, args);
			environment.getEnv(CommandHelperEnvironment.class).SetCommandSender(oldCommandSender);
			if(operator instanceof MCPlayer){
				Static.UninjectPlayer(((MCPlayer)newCommandSender));
				Static.InjectPlayer(((MCPlayer)operator));
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
}
