package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandMap;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jb_aero
 */
public class Commands {

	public static String docs() {
		return "A series of functions for creating and managing custom commands.";
	}

	public static Map<String, CClosure> onCommand = new HashMap<String, CClosure>();
	public static Map<String, CClosure> onTabComplete = new HashMap<String, CClosure>();

	@api
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
			if(arg.isInstanceOf(CClosure.TYPE)) {
				onTabComplete.remove(cmd.getName());
				onTabComplete.put(cmd.getName(), (CClosure) arg);
			} else {
				throw new CREFormatException("At this time, only closures are accepted as tabcompleters", t);
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
							+ "\treturn(array_filter(@completions, closure(@index, @string) {\n"
							+ "\t\treturn(length(@input) <= length(@string) \n"
							+ "\t\t\t\t&& equals_ic(@input, substr(@string, 0, length(@input))));\n"
							+ "\t}));\n"
							+ "});",
					"Will only suggest 'orange' if given 'o' for the second argument for /cmd.")
			};
		}
	}

	@api
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
			MCCommand cmd = map.getCommand(args[0].val());
			if(cmd == null) {
				throw new CRENotFoundException("Command not found, did you forget to register it?", t);
			}
			return CBoolean.get(map.unregister(cmd));
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
			return "boolean {commandname} unregisters a command from the server's command list";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
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
				throw new CRENotFoundException(this.getName() + " is not supported in this mode (CommandMap not found).", t);
			}
			MCCommand cmd = map.getCommand(args[0].val().toLowerCase());
			boolean isnew = false;
			if(cmd == null) {
				isnew = true;
				cmd = StaticLayer.GetConvertor().getNewCommand(args[0].val().toLowerCase());
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
				if(ops.containsKey("aliases")) {
					if(ops.get("aliases", t).isInstanceOf(CArray.TYPE)) {
						List<Mixed> ca = ((CArray) ops.get("aliases", t)).asList();
						List<String> aliases = new ArrayList<String>();
						for(Mixed c : ca) {
							aliases.add(c.val().toLowerCase());
						}
						cmd.setAliases(aliases);
					}
				}
				if(ops.containsKey("executor")) {
					set_executor.customExec(t, environment, cmd, ops.get("executor", t));
				}
				if(ops.containsKey("tabcompleter")) {
					set_tabcompleter.customExec(t, environment, cmd, ops.get("tabcompleter", t));
				}
				boolean success = true;
				if(isnew) {
					success = map.register(Implementation.GetServerType().getBranding(), cmd);
				}
				return CBoolean.get(success);
			} else {
				throw new CREFormatException("Arg 2 was expected to be an array.", t);
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
					+ " description, usage, permission, noPermMsg, aliases, tabcompleter, and/or executor. Everything"
					+ " is optional and can be modified later, except for 'aliases' which can only be changed by first"
					+ " unregistering the command. 'noPermMsg' is the message displayed when the user doesn't"
					+ " have the permission specified in 'permission'. 'Usage' is the message shown when the"
					+ " 'executor' returns false. 'Executor' is the closure run when the command is executed,"
					+ " and can return true or false (by default is treated as true). 'tabcompleter' is the closure"
					+ " run when a user hits tab while the command is entered and ready for args."
					+ " It is meant to return an array of completions, but if not the tab_complete_command event"
					+ " will be fired, and the completions of that event will be sent to the user. Both executor"
					+ " and tabcompleter closures are passed the following information in this order:"
					+ " alias used, name of the sender, array of arguments used, array of command info.";
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
				+ "\t'noPermMsg': 'You do not have permission to give hugs to players (Sorry :o).',\n"
				+ "\t'tabcompleter': closure(@alias, @sender, @args) {\n"
				+ "\t\t@input = @args[-1];\n"
				+ "\t\treturn(array_filter(all_players(), closure(@index, @player) {\n"
				+ "\t\t\treturn(length(@input) <= length(@string)\n"
				+ "\t\t\t\t\t&& equals_ic(@input, substr(@player, 0, length(@input))));\n"
				+ "\t\t\t}));\n"
				+ "\t\t},\n"
				+ "\t'aliases':array('hugg', 'hugs'),\n"
				+ "\t'executor': closure(@alias, @sender, @args) {\n"
				+ "\t\tif(array_size(@args) == 1) {\n"
				+ "\t\t\tif(ponline(@args[0])) {\n"
				+ "\t\t\t\tbroadcast(colorize('&4'.@sender.' &6hugs &4'.@args[0]));\n"
				+ "\t\t\t} else {\n"
				+ "\t\t\t\ttmsg(@sender, colorize('&cThe given player is not online.'));\n"
				+ "\t\t\t}\n"
				+ "\t\t\treturn(true);\n"
				+ "\t\t}\n"
				+ "\t\treturn(false);\n"
				+ "\t}\n"
				+ "));",
				"Registers the /hug command.")
			};
		}
	}

	@api
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
		public static void customExec(Target t, Environment environment, MCCommand cmd, Mixed arg) {
			if(arg.isInstanceOf(CClosure.TYPE)) {
				onCommand.remove(cmd.getName());
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

	@api
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
			return "array {} Returns an array of command arrays in the format register_command expects or null if no commands could be found."
					+ " This does not include " + Implementation.GetServerType().getBranding() + " aliases, as they are not registered commands.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
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
			return "void {} Attempts to clear all registered commands on the server. Note that this probably has some special"
					+ " limitations, but they are a bit unclear as to what commands can and cannot be unregistered.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
}
