package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author jb_aero
 */
public class Commands {

	public static String docs() {
		return "A series of functions for creating and managing custom commands.";
	}

	public static Map<String, Callable> onCommand = new HashMap<>();
	public static Map<String, Callable> onTabComplete = new HashMap<>();

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({register_command.class})
	public static class set_tabcompleter extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRENotFoundException.class};
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
			MCServer s = Static.getServer();
			MCCommandMap map = s.getCommandMap();
			if(map == null) {
				throw new CRENotFoundException(this.getName() + " is not supported in this mode (CommandMap not found).", t);
			}
			MCCommand cmd = map.getCommand(args[0].val());
			if(cmd == null) {
				throw new CRENotFoundException("Command not found, did you forget to register it?", t);
			}
			customExec(t, environment, cmd, args[1]);
			return CVoid.VOID;
		}

		/**
		 * For setting the completion code of a command that exists but might not be registered yet
		 *
		 * @param t
		 * @param environment
		 * @param cmd
		 * @param arg
		 */
		public static void customExec(Target t, Environment environment, MCCommand cmd, Mixed arg) {
			if(arg.isInstanceOf(Callable.TYPE)) {
				onTabComplete.put(cmd.getName(), (Callable) arg);
			} else {
				throw new CREFormatException("Only Callables are accepted as tabcompleters", t);
			}
		}

		@Override
		public String getName() {
			return "set_tabcompleter";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {commandname, closure} Sets the code that will be run when a user attempts"
					+ " to tabcomplete a command. The closure is expected to return an array of completions,"
					+ " otherwise the tab_complete_command event will be fired and used to send completions."
					+ " The closure is passed the following information in this order:"
					+ " alias used, name of the sender, array of arguments used, array of command info.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates completion suggestions for multiple arguments.",
					"set_tabcompleter('cmd', closure(@alias, @sender, @args, @info) {\n"
							+ "\t@input = @args[-1];\n"
							+ "\t@completions = array();\n"
							+ "\tif(array_size(@args) == 1) {\n"
							+ "\t\t@completions = array('one', 'two', 'three');\n"
							+ "\t} else if(array_size(@args) == 2) {\n"
							+ "\t\t@completions = array('apple', 'orange', 'banana');\n"
							+ "\t}\n"
							+ "\treturn(array_filter(@completions, closure(@key, @value) {\n"
							+ "\t\treturn(length(@input) <= length(@value) \n"
							+ "\t\t\t\t&& equals_ic(@input, substr(@value, 0, length(@input))));\n"
							+ "\t}));\n"
							+ "});",
					"Will only suggest 'orange' if given 'o' for the second argument for /cmd.")
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({register_command.class})
	public static class unregister_command extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
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
			MCCommandMap map = Static.getServer().getCommandMap();
			if(map == null) {
				throw new CRENotFoundException(this.getName() + " is not supported in this mode (CommandMap not found).", t);
			}
			String name = args[0].val();
			MCCommand cmd = map.getCommand(name);
			if(cmd == null) {
				throw new CRENotFoundException("Command not found, did you forget to register it?", t);
			}
			boolean success = map.unregister(cmd);
			if(success) {
				onCommand.remove(name);
				onTabComplete.remove(name);
			}
			return CBoolean.get(success);
		}

		@Override
		public String getName() {
			return "unregister_command";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {commandname} Unregisters a command from the server's command list."
					+ " Commands from other plugins can be unregistered using this function.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({set_tabcompleter.class, set_executor.class, unregister_command.class, get_tabcomplete_prototype.class})
	public static class register_command extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class,
				CRENotFoundException.class};
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
			MCCommandMap map = Static.getServer().getCommandMap();
			if(map == null) {
				throw new CRENotFoundException(
						this.getName() + " is not supported in this mode (CommandMap not found).", t);
			}
			String cmdStr = args[0].val().toLowerCase();
			if(cmdStr.indexOf(' ') != -1) {
				throw new CREFormatException("Command passed to " + this.getName() + " must not contain whitespaces."
						+ " Received: \"" + cmdStr + "\".", t);
			}
			MCCommand cmd = map.getCommand(cmdStr);
			String prefix = Implementation.GetServerType().getBranding().toLowerCase(Locale.ENGLISH);
			boolean register = false;
			if(cmd == null) {
				register = true;
				cmd = StaticLayer.GetConvertor().getNewCommand(cmdStr);
			}
			if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray ops = (CArray) args[1];
				if(ops.containsKey("permission")) {
					cmd.setPermission(ops.get("permission", t).val());
				}
				if(ops.containsKey("description")) {
					cmd.setDescription(ops.get("description", t).val());
				}
				if(ops.containsKey("usage")) {
					cmd.setUsage(ops.get("usage", t).val());
				}
				if(ops.containsKey("noPermMsg")) {
					cmd.setPermissionMessage(ops.get("noPermMsg", t).val());
				}
				List<String> oldAliases = new ArrayList<>(cmd.getAliases());
				if(ops.containsKey("aliases")) {
					if(ops.get("aliases", t).isInstanceOf(CArray.TYPE)) {
						List<Mixed> ca = ((CArray) ops.get("aliases", t)).asList();
						List<String> aliases = new ArrayList<>();
						for(Mixed c : ca) {
							String alias = c.val().toLowerCase().trim();
							if(!oldAliases.remove(alias)) {
								register = true;
							}
							aliases.add(alias);
						}
						cmd.setAliases(aliases);
					}
				}
				if(oldAliases.size() > 0) {
					// we need to remove these
					for(String alias : oldAliases) {
						map.unregister(prefix + ":" + alias);
						map.unregister(alias);
					}
				}
				if(ops.containsKey("executor")) {
					set_executor.customExec(t, environment, cmd, ops.get("executor", t));
				}
				if(ops.containsKey("tabcompleter")) {
					set_tabcompleter.customExec(t, environment, cmd, ops.get("tabcompleter", t));
				}
				boolean success = true;
				if(register) {
					cmd.unregister(map);
					success = map.register(prefix, cmd);
				}
				return CBoolean.get(success);
			} else {
				throw new CREFormatException(
						"The second argument passed to " + this.getName() + " was expected to be an array.", t);
			}
		}

		@Override
		public String getName() {
			return "register_command";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {commandname, optionsArray} Registers a command to the server's command list,"
					+ " or updates an existing one. Options is an associative array that can have the following keys:"
					+ " description, usage, permission, noPermMsg, aliases, tabcompleter, and/or executor. ---- "
					+ " The 'noPermMsg' argument is the message displayed when the user doesn't have the permission"
					+ " specified in 'permission' (unused as of Spigot 1.19)."
					+ " The 'usage' is the message shown when the 'executor' returns false."
					+ " The 'executor' is the closure run when the command is executed,"
					+ " and can return true or false (by default is treated as true). The 'tabcompleter' is the closure"
					+ " run when a user hits tab while the command is entered and ready for args."
					+ " It is meant to return an array of completions, but if not the tab_complete_command event"
					+ " will be fired, and the completions of that event will be sent to the user. Both executor"
					+ " and tabcompleter closures are passed the following information in this order:"
					+ " alias used, name of the sender, array of arguments used, array of command info.\n"
					+ "When this function is not supported on the used platform, a NotFoundException is thrown."
					+ " When the command name contains whitespaces, a FormatException is thrown.\n\n"
					+ "In most simple cases, the tabcompleter can be easily created using the"
					+ " {{function|get_tabcompleter_prototype}} function, though for complex completion scenarios,"
					+ " you may prefer writing a custom closure anyways.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Register the /hug <player> command.",
				"register_command('hug', array(\n"
				+ "\t'description': 'Spread the love!',\n"
				+ "\t'usage': '/hug <player>',\n"
				+ "\t'permission': 'perms.hugs',\n"
				+ "\t'tabcompleter':\n"
				+ "\t\tclosure(@alias, @sender, @args) {\n"
				+ "\t\t\t// This replicates the default tabcompleter for registered commands.\n"
				+ "\t\t\t// If no tabcompleter is set, this behavior is used.\n"
				+ "\t\t\t@input = @args[-1];\n"
				+ "\t\t\treturn(array_filter(all_players(), closure(@key, @value) {\n"
				+ "\t\t\t\treturn(length(@input) <= length(@value)\n"
				+ "\t\t\t\t\t\t&& equals_ic(@input, substr(@value, 0, length(@input))));\n"
				+ "\t\t\t}));\n"
				+ "\t\t},\n"
				+ "\t'aliases': array('hugg', 'hugs'),\n"
				+ "\t'executor':\n"
				+ "\t\tclosure(@alias, @sender, @args) {\n"
				+ "\t\t\tif(array_size(@args) == 1) {\n"
				+ "\t\t\t\t@target = @args[0];\n"
				+ "\t\t\t\tif(ponline(@target)) {\n"
				+ "\t\t\t\t\tbroadcast(colorize('&4'.@sender.' &6hugs &4'.@target));\n"
				+ "\t\t\t\t} else {\n"
				+ "\t\t\t\t\tmsg(colorize('&cThe given player is not online.'));\n"
				+ "\t\t\t\t}\n"
				+ "\t\t\t\treturn(true);\n"
				+ "\t\t\t}\n"
				+ "\t\t\treturn(false); // prints usage\n"
				+ "\t\t}\n"
				+ "));",
				"Registers the /hug command.")
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso(register_command.class)
	public static class get_tabcomplete_prototype extends CompositeFunction {

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
		protected String script() {
			return getBundledCode();
		}

		@Override
		public String getName() {
			return "get_tabcomplete_prototype";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return getBundledDocs();
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple example, using only pre-baked classes (documentation above under 'Strings')."
						+ " Assume our command looks like this: \"/cmd $player\". Note that here we're using"
						+ " register_command, which is the most likely use of this function, but in the remaining"
						+ " examples this is not shown.",
						"register_command('cmd', array(\n"
								+ "\t'tabcompleter': get_tabcomplete_prototype('Player'),\n"
								+ "\t'executor': closure(@alias, @sender, @args){\n\t\t/* ... */}"
								+ "));",
						"Provides a tabcomplete for the described scenario. Once the player types \"/cmd \" they will be presented with a list of currently online players to tabcomplete from."),
				new ExampleScript("Using both player names and an array of completions. Assume our command looks like"
						+ "\"/cmd $action $player\" where $action can be one of \"add\" or \"remove\".",
						"get_tabcomplete_prototype(array('add', 'remove'), 'OfflinePlayer')",
						"Provides a tabcomplete for the described scenario. The first parameter will tabcomplete as either add/remove, and the second parameter will tabcomplete with a list of all known players - both online and offline."),
				new ExampleScript("Using a built in enum type. Assume the command is \"/cmd $WorldEnvironment\" and we"
						+ " expect this to be completed with one of the com.commandhelper.WorldEnvironment enum values.",
						"get_tabcomplete_prototype(WorldEnvironment)",
						"Provides a tabcomplete for the described scenario."),
				new ExampleScript("Using a closure to return dynamic input based on the current user. This could"
						+ " be based on the parameters passed in the closure, but could just as easily be any other"
						+ " dynamic input.",
						"get_tabcomplete_prototype(closure(@alias, @sender, @args) {\n"
								+ "\tif(_is_admin(@sender)) {\n"
								+ "\t\t/* Admin gets extra options */\n"
								+ "\t\treturn(array(1, 2, 3));\n"
								+ "\t} else { \n"
								+ "\t\treturn(array(1, 2));\n"
								+ "\t}\n"
								+ "});",
						"Provides a tabcomplete for the described scenario."),
				new ExampleScript("Using the associative array value. Using this method, we can change the autocomplete"
						+ " of later arguments based on what the user has typed so far.\n"
						+ "\n"
						+ "Assume that the command looks"
						+ " like \"/cmd $action $Player $group\". The actions are \"add\" and \"remove\", and the"
						+ " total list of groups is array(\"a\", \"b\", \"c\"), of which the player is already in"
						+ " group \"a\". Also, assume that the procedure _get_user_groups() returns a list of groups the"
						+ " player is already in, and _get_groups() returns a list of all groups.",
						"get_tabcomplete_prototype(\n"
								+ "\tarray('add', 'remove'), // first argument\n"
								+ "\t'Player', // second argument\n"
								+ "\tarray( // third argument\n"
								+ "\t\t// Note that the << symbols mean \"look at the value of the user input two arguments prior\"\n"
								+ "\t\t// so in this case, we will look at the value already provided by the user in the first argument.\n"
								+ "\t\t// If they provided \"add\", then we will run the first closure, \"remove\" will run the second closure,\n"
								+ "\t\t// and if they typed something else, no tab completion will occur. We could also provide a default\n"
								+ "\t\t// set of completions by using the key \"<\", which is specially defined.\n"
								+ "\t\t'<<add': closure(@alias, @sender, @args) { return(array_subtract(_get_groups(), _get_user_groups(@sender))); },\n"
								+ "\t\t'<<remove': closure(@alias, @sender, @args) { return(_get_user_groups(@sender)); }\n"
								+ "));",
						"Provides a tabcomplete for the described scenario. Since the player is already in group \"a\","
								+ " if the command typed so far were \"/cmd add MyPlayer\" then only \"b\" and \"c\""
								+ " would be provided for the next completion.")
			};
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	@seealso({register_command.class})
	public static class set_executor extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRENotFoundException.class};
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
			MCCommandMap map = Static.getServer().getCommandMap();
			if(map == null) {
				throw new CRENotFoundException(this.getName() + " is not supported in this mode (CommandMap not found).", t);
			}
			MCCommand cmd = map.getCommand(args[0].val());
			if(cmd == null) {
				throw new CRENotFoundException("Command not found did you forget to register it?", t);
			}
			customExec(t, environment, cmd, args[1]);
			return CVoid.VOID;
		}

		/**
		 * For setting the execution code of a command that exists but might not be registered yet
		 *
		 * @param t
		 * @param environment
		 * @param cmd
		 * @param arg
		 */
		static void customExec(Target t, Environment environment, MCCommand cmd, Mixed arg) {
			if(arg.isInstanceOf(CClosure.TYPE)) {
				onCommand.put(cmd.getName(), (CClosure) arg);
			} else {
				throw new CREFormatException("At this time, only closures are accepted as command executors.", t);
			}
		}

		@Override
		public String getName() {
			return "set_executor";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {commandname, closure} Sets the code that will be run when a user attempts to execute a command."
					+ " The closure can return true false (treated as true by default). Returning false will display"
					+ " The usage message if it is set. The closure is passed the following information in this order:"
					+ " alias used, name of the sender, array of arguments used, array of command info.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class get_commands extends AbstractFunction {

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
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandMap map = Static.getServer().getCommandMap();
			if(map == null) {
				return CNull.NULL;
			}
			Collection<MCCommand> commands = map.getCommands();
			CArray ret = CArray.GetAssociativeArray(t);
			for(MCCommand command : commands) {
				CArray ca = CArray.GetAssociativeArray(t);
				ca.set("name", new CString(command.getName(), t), t);
				ca.set("description", new CString(command.getDescription(), t), t);
				Mixed permission;
				if(command.getPermission() == null) {
					permission = CNull.NULL;
				} else {
					permission = new CString(command.getPermission(), t);
				}
				ca.set("permission", permission, t);
				ca.set("nopermmsg", new CString(command.getPermissionMessage(), t), t);
				ca.set("usage", new CString(command.getUsage(), t), t);
				CArray aliases = new CArray(t);
				for(String a : command.getAliases()) {
					aliases.push(new CString(a, t), t);
				}
				ca.set("aliases", aliases, t);
				ret.set(command.getName(), ca, t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "get_commands";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of command arrays in the format register_command expects or null if no"
					+ " commands could be found. The command arrays will not include executors or tabcompleters."
					+ " This does not include " + Implementation.GetServerType().getBranding()
					+ " aliases, as they are not registered commands.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class clear_commands extends AbstractFunction {

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
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandMap map = Static.getServer().getCommandMap();
			if(map != null) {
				map.clearCommands();
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "clear_commands";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Attempts to clear all registered commands on the server. Vanilla and default Spigot"
					+ " functions are not affected, but all plugins commands are.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
}
