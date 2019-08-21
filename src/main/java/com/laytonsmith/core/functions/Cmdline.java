package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSecureString;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CREShellException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@core
public class Cmdline {

	public static String docs() {
		return "This class contains functions that are mostly only useful for command line scripts, but in general may be used by any script. For"
				+ " more information on running MethodScript from the command line, see [[Command_Line_Scripting|this wiki page]].";
	}

	@api
	@noboilerplate
	@seealso(print_out.class)
	public static class sys_out extends AbstractFunction {

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
		public CVoid exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String msg = Static.MCToANSIColors(args[0].val());
			StreamUtils.GetSystemOut().print(msg);
			if(msg.contains("\033")) {
				//We have color codes in it, we need to reset them
				StreamUtils.GetSystemOut().print(TermColors.reset());
			}
			StreamUtils.GetSystemOut().println();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "sys_out";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {text} Writes the text to the system's std out. Unlike console(), this does not use anything else to format the output, though in many"
					+ " cases they will behave the same. However, colors and other formatting characters will not \"bleed\" through, so"
					+ " sys_out(color(RED) . 'This is red') will not cause the next line to also be red, so if you need to print multiple lines out, you should"
					+ " manually add \\n to create your linebreaks, and only make one call to sys_out.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "#Note, this is guaranteed to print to standard out\nsys_out('Hello World!')", ":Hello World!")
			};
		}

	}

	@api
	@noboilerplate
	@seealso(print_err.class)
	public static class sys_err extends AbstractFunction {

		@Override
		public Class[] thrown() {
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
		public CVoid exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String msg = Static.MCToANSIColors(args[0].val());
			PrintStream se = StreamUtils.GetSystemErr();
			se.print(msg);
			if(msg.contains("\033")) {
				//We have color codes in it, we need to reset them
				se.print(TermColors.reset());
			}
			se.println();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "sys_err";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {text} Writes the text to the system's std err. Unlike console(), this does not use anything else to format the output, though in many"
					+ " cases they will behave nearly the same. However, colors and other formatting characters will not \"bleed\" through, so"
					+ " sys_err(color(RED) . 'This is red') will not cause the next line to also be red, so if you need to print multiple lines out, you should"
					+ " manually add \\n to create your linebreaks, and only make one call to sys_err.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "#Note this is guaranteed to print to standard err\nsys_out('Hello World!')", ":Hello World!")
			};
		}
	}

	@api
	@noboilerplate
	@seealso(sys_out.class)
	public static class print_out extends AbstractFunction {

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
		public CVoid exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String msg = Static.MCToANSIColors(args[0].val());
			PrintStream so = StreamUtils.GetSystemOut();
			so.print(msg);
			so.flush();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "print_out";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {text} Writes the text to the system's std out, but does not automatically add a newline at the end."
					+ " Unlike console(), this does not use anything else to format the output, though in many"
					+ " cases they will behave the same. Unlike other print methdods, colors and other formatting characters WILL"
					+ " \"bleed\" through, so"
					+ " print_out(color(RED) . 'This is red') will also cause the next line to also be red,"
					+ " so if you need to print multiple lines out, you should manually reset the color with print_out(color(RESET)),"
					+ " or use sys_out..";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso(sys_err.class)
	public static class print_err extends AbstractFunction {

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
		public CVoid exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String msg = Static.MCToANSIColors(args[0].val());
			StreamUtils.GetSystemErr().print(msg);
			StreamUtils.GetSystemErr().flush();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "print_err";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {text} Writes the text to the system's std err, but does not automatically add a newline at the end."
					+ " Unlike console(), this does not use anything else to format the output, though in many"
					+ " cases they will behave the same. Unlike other print methdods, colors and other formatting characters WILL"
					+ " \"bleed\" through, so"
					+ " print_err(color(RED) . 'This is red') will also cause the next line to also be red,"
					+ " so if you need to print multiple lines out, you should manually reset the color with print_out(color(RESET)),"
					+ " or use sys_err.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api(environments = {GlobalEnv.class})
	@noboilerplate
	public static class exit extends AbstractFunction implements Optimizable {

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
			int exit_code = 0;
			if(args.length == 1) {
				exit_code = Static.getInt32(args[0], t);
			}
			if(Static.InCmdLine(environment, true)) {
				System.exit(exit_code);
			}
			return new ControlFlow.die().exec(t, environment, args);
		}

		@Override
		public String getName() {
			return "exit";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[int]} Exits the program. If this is being run from the command line, works by exiting the interpreter, with "
					+ " the specified exit code (defaulting to 0). If this is being run from in-game, works just like die().";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.TERMINAL
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "#Causes the JVM to exit with an exit code of 0\nexit(0)", ""),
				new ExampleScript("Basic usage", "#Causes the JVM to exit with an exit code of 1\nexit(1)", "")};
		}
	}

	@api
	public static class sys_properties extends AbstractFunction {

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
			if(args.length == 1) {
				String propName = args[0].val();
				String prop;
				if(propName.startsWith("methodscript.")) {
					prop = getMethodScriptProperties().get(propName);
				} else {
					prop = System.getProperty(propName);
				}
				if(prop == null) {
					return CNull.NULL;
				}
				return new CString(prop, t);
			} else {
				CArray ca = CArray.GetAssociativeArray(t);
				for(String key : System.getProperties().stringPropertyNames()) {
					ca.set(key, System.getProperty(key));
				}
				Map<String, String> msProps = getMethodScriptProperties();
				for(String key : msProps.keySet()) {
					ca.set(key, msProps.get(key));
				}
				return ca;
			}

		}

		private Map<String, String> getMethodScriptProperties() {
			Map<String, String> map = new HashMap<>();
			for(Prefs.PNames name : Prefs.PNames.values()) {
				Object o;
				switch(name.type()) {
					case BOOLEAN:
						o = Prefs.prefB(name);
						break;
					case DOUBLE:
					case NUMBER:
						o = Prefs.prefD(name);
						break;
					case FILE:
					case STRING:
						o = Prefs.prefS(name);
						break;
					case INT:
						o = Prefs.prefI(name);
						break;
					default:
						throw new Error("switch is not exhaustive");
				}
				map.put("methodscript.preference." + name.config(), o.toString());
			}
			return map;
		}

		@Override
		public String getName() {
			return "sys_properties";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mixed {[propertyName]} If propertyName is set, that single property is returned, or null if that property doesn't exist. If propertyName is not set, an"
					+ " associative array with all the system properties is returned. This mechanism hooks into Java's system property mechanism, and is just a wrapper for"
					+ " that. System properties are more reliable than environmental variables, and so are preferred in cases where they exist. For more information about system"
					+ " properties, see http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html. In addition, known preferences listed in preferences.ini"
					+ " are also included, starting with the prefix \"methodscript.preference.\"";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Gets all properties", "array_size(sys_properties())"),
				new ExampleScript("Gets a single property", "sys_properties('java.specification.vendor')"),
				new ExampleScript("Gets a single property", "sys_properties('methodscript.preference.debug-mode')")};
		}
	}

	@api
	public static class get_env extends AbstractFunction {

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
			if(args.length == 1) {
				return new CString(System.getenv(args[0].val()), t);
			} else {
				CArray ca = CArray.GetAssociativeArray(t);
				for(String key : System.getenv().keySet()) {
					ca.set(key, System.getenv(key));
				}
				return ca;
			}
		}

		@Override
		public String getName() {
			return "get_env";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mixed {[variableName]} Returns the environment variable specified, if variableName is set. Otherwise, returns an associative array"
					+ " of all the environment variables.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class set_env extends AbstractFunction {

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

		@SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			//TODO: Make this more robust by having a local cache of the environment which we modify, and get_env returns from.
			Map<String, String> newenv = new HashMap<>(System.getenv());
			newenv.put(args[0].val(), args[1].val());
			boolean ret;
			try {
				Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
				Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
				theEnvironmentField.setAccessible(true);
				Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
				env.putAll(newenv);
				Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
				theCaseInsensitiveEnvironmentField.setAccessible(true);
				Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
				cienv.putAll(newenv);
				ret = true;
			} catch (NoSuchFieldException e) {
				try {
					Class[] classes = Collections.class.getDeclaredClasses();
					Map<String, String> env = System.getenv();
					for(Class cl : classes) {
						if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
							Field field = cl.getDeclaredField("m");
							field.setAccessible(true);
							Object obj = field.get(env);
							Map<String, String> map = (Map<String, String>) obj;
							map.clear();
							map.putAll(newenv);
						}
					}
					ret = true;
				} catch (Exception e2) {
					ret = false;
					if(Prefs.DebugMode()) {
						MSLog.GetLogger().e(MSLog.Tags.GENERAL, e2, t);
					}
				}
			} catch (Exception e1) {
				ret = false;
				if(Prefs.DebugMode()) {
					MSLog.GetLogger().e(MSLog.Tags.GENERAL, e1, t);
				}
			}
			return CBoolean.get(ret);
		}

		@Override
		public String getName() {
			return "set_env";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {variableName, value} Sets the value of an environment variable. This only changes the environment value in this process, not system-wide."
					+ " This uses some hackery to work, and may not be 100% reliable in all cases, and shouldn't be relied on heavily. It will"
					+ " always work with get_env, however, so you can rely on that mechanism. The value will always be interpreted as a string, so if you are expecting"
					+ " a particular data type on a call to get_env, you will need to manually cast the variable. Arrays will be toString'd as well, but will be accepted.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	@noboilerplate
	@seealso(StringHandling.decrypt_secure_string.class)
	public static class prompt_pass extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREIOException.class};
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
			if(!Static.InCmdLine(environment, true)) {
				throw new CREInsufficientPermissionException(getName() + " cannot be used outside of cmdline mode.", t);
			}
			boolean mask = true;
			if(args.length > 1) {
				mask = ArgumentValidation.getBoolean(args[1], t);
			}

			String prompt = args[0].val();
			Character cha = (char) 0;
			if(mask) {
				cha = '*';
			}
			try(jline.console.ConsoleReader reader = new jline.console.ConsoleReader()) {
				reader.setExpandEvents(false);
				return new CSecureString(reader.readLine(Static.MCToANSIColors(prompt), cha).toCharArray(), t);
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t);
			}

		}

		@Override
		public String getName() {
			return "prompt_pass";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "secure_string {prompt, [mask]} Prompts the user for a password. This only works in cmdline mode. If mask is true (default),"
					+ " then the password displays * characters for each password character they type. If mask is false, the field"
					+ " stays blank as they type. What they type is returned once they hit enter. The value returned is a secure_string,"
					+ " so to get the actual password, you must use decrypt_secure_string.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "prompt_pass('password: ');", "Would wait for the user to type in a password"),
				new ExampleScript("Decrypting the password", "@password = prompt_pass('password: ');\n"
				+ "msg(@password);\n"
				+ "msg(decrypt_secure_string(@password));", "password: ****\n**secure string**\n{p,a,s,s}")
			};
		}

	}

	@api
	@noboilerplate
	public static class prompt_char extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREIOException.class};
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
			requireCmdlineMode(environment, this, t);

			String prompt = args[0].val();
			try {
				char c = promptChar(Static.MCToANSIColors(prompt));
				return new CString(c, t);
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t);
			}

		}

		/**
		 * Prompts for a single char from the console. The character typed is returned.
		 * @param prompt
		 * @return
		 * @throws IOException
		 */
		public static char promptChar(String prompt) throws IOException {
			StreamUtils.GetSystemOut().print(prompt);
			StreamUtils.GetSystemOut().flush();
			try(jline.console.ConsoleReader reader = new jline.console.ConsoleReader()) {
				reader.setExpandEvents(false);
				char c = (char) reader.readCharacter();
				StreamUtils.GetSystemOut().println(c);
				return c;
			}
		}

		@Override
		public String getName() {
			return "prompt_char";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {prompt} Prompts the user for a single character. They do not need to hit enter first. This only works"
					+ " in cmdline mode.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	public static class prompt_line extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREIOException.class};
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
			if(!Static.InCmdLine(environment, true)) {
				throw new CREInsufficientPermissionException(getName() + " cannot be used outside of cmdline mode.", t);
			}

			String prompt = args[0].val();
			try(jline.console.ConsoleReader reader = new jline.console.ConsoleReader()) {
				reader.setExpandEvents(false);
				String line = reader.readLine(Static.MCToANSIColors(prompt));
				return new CString(line, t);
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t);
			}

		}

		@Override
		public String getName() {
			return "prompt_line";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {prompt} Prompts the user for a line. The line typed is returned once the user presses enter. This"
					+ " only works in cmdline mode.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	public static class sys_beep extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREPluginInternalException.class};
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
			java.awt.Toolkit.getDefaultToolkit().beep();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "sys_beep";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Emits a system beep, on the system itself, not in game. This is only useful from cmdline.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	public static class clear_screen extends AbstractFunction {

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
			if(Static.InCmdLine(environment, true)) {
				try {
					new jline.console.ConsoleReader().clearScreen();
				} catch (IOException ex) {
					throw new CREIOException(ex.getMessage(), t);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "clear_screen";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Clears the screen. This only works from cmdline mode, nothing happens otherwise.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso({shell.class, shell_on_adv.class, shell_on.class})
	public static class shell_adv extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREIOException.class};
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
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(!Static.InCmdLine(environment, true)) {
				if(!Prefs.AllowShellCommands()) {
					throw new CREInsufficientPermissionException("Shell commands are not allowed. Enable them in preferences.ini.", t);
				}
				if(environment.getEnv(GlobalEnv.class).GetDynamicScriptingMode() && !Prefs.AllowDynamicShell()) {
					throw new CREInsufficientPermissionException("Shell commands are disabled from dynamic sources.", t);
				}
			}
			String[] command;
			File workingDir = null;
			final CClosure stdout;
			final CClosure stderr;
			final CClosure exit;
			final boolean subshell;
			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray array = (CArray) args[0];
				command = new String[(int) array.size()];
				for(int i = 0; i < array.size(); i++) {
					command[i] = array.get(i, t).val();
				}
			} else {
				command = StringUtils.ArgParser(args[0].val()).toArray(new String[0]);
			}
			if(args.length > 1) {
				CArray options = Static.getArray(args[1], t);
				if(options.containsKey("workingDir") && !(options.get("workingDir", t) instanceof CNull)) {
					workingDir = new File(options.get("workingDir", t).val());
					if(!workingDir.isAbsolute()) {
						workingDir = new File(t.file().getParentFile(), workingDir.getPath());
					}
				}
				stdout = (options.containsKey("stdout") && !(options.get("stdout", t) instanceof CNull)
						? Static.getObject(options.get("stdout", t), t, CClosure.class) : null);
				stderr = (options.containsKey("stderr") && !(options.get("stderr", t) instanceof CNull)
						? Static.getObject(options.get("stderr", t), t, CClosure.class) : null);
				exit = (options.containsKey("exit") && !(options.get("exit", t) instanceof CNull)
						? Static.getObject(options.get("exit", t), t, CClosure.class) : null);
				subshell = (options.containsKey("subshell")
						? ArgumentValidation.getBoolean(options.get("subshell", t), t) : false);
			} else {
				stdout = null;
				stderr = null;
				exit = null;
				subshell = false;
			}
			final CommandExecutor cmd = new CommandExecutor(command);
			cmd.setWorkingDir(workingDir);
			final MutableObject<StringBuilder> sbout = new MutableObject(new StringBuilder());
			final MutableObject<StringBuilder> sberr = new MutableObject(new StringBuilder());
			cmd.setSystemOut(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					if(stdout == null) {
						return;
					}
					char c = (char) b;
					if(c == '\n' || b == -1) {
						try {
							StaticLayer.GetConvertor().runOnMainThreadAndWait(() -> {
								stdout.executeCallable(new CString(sbout.getObject(), t));
								return null;
							});
						} catch (Exception ex) {
							Logger.getLogger(Cmdline.class.getName()).log(Level.SEVERE, null, ex);
						}
						sbout.setObject(new StringBuilder());
					} else {
						sbout.getObject().append(c);
					}
				}
			});
			cmd.setSystemErr(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					if(stderr == null) {
						return;
					}
					char c = (char) b;
					if(c == '\n' || b == -1) {
						try {
							StaticLayer.GetConvertor().runOnMainThreadAndWait(() -> {
								stderr.executeCallable(new CString(sberr.getObject(), t));
								return null;
							});
						} catch (Exception ex) {
							Logger.getLogger(Cmdline.class.getName()).log(Level.SEVERE, null, ex);
						}
						sberr.setObject(new StringBuilder());
					} else {
						sberr.getObject().append(c);
					}
				}
			});
			try {
				cmd.start();
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t);
			}

			Runnable run = () -> {
				environment.getEnv(GlobalEnv.class).GetDaemonManager().activateThread(null);
				try {
					final int exitCode = cmd.waitFor();
					try {
						cmd.getSystemOut().flush();
						if(cmd.getSystemOut() != StreamUtils.GetSystemOut()) {
							cmd.getSystemOut().close();
						}
					} catch (IOException ex) {
						Logger.getLogger(Cmdline.class.getName()).log(Level.SEVERE, null, ex);
					}
					try {
						cmd.getSystemErr().flush();
						if(cmd.getSystemErr() != StreamUtils.GetSystemErr()) {
							cmd.getSystemErr().close();
						}
					} catch (IOException ex) {
						Logger.getLogger(Cmdline.class.getName()).log(Level.SEVERE, null, ex);
					}
					if(exit != null) {
						try {
							StaticLayer.GetConvertor().runOnMainThreadAndWait(() -> {
								exit.executeCallable(new CInt(exitCode, t));
								return null;
							});
						} catch (Exception ex) {
							Logger.getLogger(Cmdline.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				} catch (InterruptedException ex) {
					throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), t);
				} finally {
					environment.getEnv(GlobalEnv.class).GetDaemonManager().deactivateThread(null);
				}
			};
			if(subshell) {
				new Thread(run, "shell-adv-subshell (" + StringUtils.Join(command, " ") + ")").start();
			} else {
				run.run();
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "shell_adv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {command, [options]} Runs a shell command. <code>command</code> can either be a string or an array of string arguments,"
					+ " which are run as an external process. Requires the allow-shell-commands option to be enabled in preferences, or run from command line, otherwise"
					+ " an InsufficientPermissionException is thrown. ---- <code>options</code> is an associative array with zero or more"
					+ " of the following options:\n\n"
					+ "{| border=\"1\" class=\"wikitable\" cellspacing=\"1\" cellpadding=\"1\"\n"
					+ "|-\n| workingDir || Sets the working directory for"
					+ " the sub process. By default null, which represents the directory of this script."
					+ " If the path is relative, it is relative to the directory of this script.\n"
					+ "|-\n| stdout || A closure which receives the program"
					+ " output to stdout line by line. The closure should accept a single string, which will be a line.\n"
					+ "|-\n| stderr || A closure which receives the program output to stderr line by line. The closure should accept a single string,"
					+ " which should be a line.\n"
					+ "|-\n| exit || A closure which is triggered one time, and contains the process's exit code, once it terminates.\n"
					+ "|-\n| subshell || A boolean. If true, the process will not block, and script execution will continue. If false (default)"
					+ " script execution will halt until the process exits.\n"
					+ "|}";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso({shell_adv.class, shell_on.class, shell_adv.class})
	public static class shell extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREShellException.class, CREIOException.class};
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
			if(!Static.InCmdLine(environment, true)) {
				if(!Prefs.AllowShellCommands()) {
					throw new CREInsufficientPermissionException("Shell commands are not allowed. Enable them in preferences.ini.", t);
				}
				if(environment.getEnv(GlobalEnv.class).GetDynamicScriptingMode() && !Prefs.AllowDynamicShell()) {
					throw new CREInsufficientPermissionException("Shell commands are disabled from dynamic sources.", t);
				}
			}
			String[] command;
			int expectedExitCode = 0;
			File workingDir = null;
			if(args[0].isInstanceOf(CArray.TYPE)) {
				CArray array = (CArray) args[0];
				command = new String[(int) array.size()];
				for(int i = 0; i < array.size(); i++) {
					command[i] = array.get(i, t).val();
				}
			} else {
				command = StringUtils.ArgParser(args[0].val()).toArray(new String[0]);
			}
			if(args.length > 1) {
				CArray options = Static.getArray(args[1], t);
				if(options.containsKey("expectedExitCode")) {
					expectedExitCode = Static.getInt32(options.get("expectedExitCode", t), t);
				}
				if(options.containsKey("workingDir") && !(options.get("workingDir", t) instanceof CNull)) {
					workingDir = new File(options.get("workingDir", t).val());
					if(!workingDir.isAbsolute()) {
						workingDir = new File(t.file().getParentFile(), workingDir.getPath());
					}
				}
			}
			CommandExecutor cmd = new CommandExecutor(command);
			final StringBuilder sout = new StringBuilder();
			OutputStream out = new BufferedOutputStream(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					sout.append((char) b);
				}
			});
			final StringBuilder serr = new StringBuilder();
			OutputStream err = new BufferedOutputStream(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					serr.append((char) b);
				}
			});
			cmd.setSystemOut(out).setSystemErr(err).setWorkingDir(workingDir);
			try {
				int exitCode = cmd.start().waitFor();
				try {
					if(exitCode != expectedExitCode) {
						err.flush();
						throw new CREShellException(serr.toString(), t);
					} else {
						out.flush();
						return new CString(sout.toString(), t);
					}
				} finally {
					out.close();
					err.close();
				}
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t);
			} catch (InterruptedException ex) {
				throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "shell";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {command, [options]} Runs a shell command. <code>command</code> can be either a string, or array of string"
					+ " arguments. This works mostly like {{function|shell_adv}} however, it buffers then"
					+ " returns the output for sysout once the process is completed, and throws a ShellException with the exception"
					+ " message set to the syserr output if the"
					+ " process exits with an exit code that isn't the expectedExitCode, which defaults to 0. This is useful for simple commands"
					+ " that return output and don't need very complicated usage, and failures don't need to check the exact error code."
					+ " If the underlying command throws an IOException, it is"
					+ " passed through. Requires the allow-shell-commands option to be enabled in preferences, or run from command line, otherwise"
					+ " an InsufficientPermissionException is thrown. Options is an associative array which expects zero or more"
					+ " of the following options: expectedErrorCode - The expected error code indicating successful command completion. Defaults to 0."
					+ " workingDir - Sets the working directory for the sub process. By default null, which represents the directory of this script."
					+ " If the path is relative, it is relative to the directory of this script.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public com.laytonsmith.core.functions.ExampleScript[] examples() throws com.laytonsmith.core.exceptions.ConfigCompileException {
			return new com.laytonsmith.core.functions.ExampleScript[]{
				new com.laytonsmith.core.functions.ExampleScript("Basic usage with array", "shell(array('grep', '-r', 'search content', '*'))", "<output of command>"),
				new com.laytonsmith.core.functions.ExampleScript("Basic usage with string", "shell('grep -r \"search content\" *')", "<output of command>"),
				new com.laytonsmith.core.functions.ExampleScript("Changing the working directory", "shell('grep -r \"search content\" *', array(workingDir: '/'))", "<output of command>")};
		}

	}

	@api
	@noboilerplate
	@seealso({shell_adv.class, shell_on.class, shell.class, get_os.class})
	public static class shell_on_adv extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREShellException.class, CREIOException.class, CREFormatException.class};
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
			Set<OSUtils.OS> oses = new HashSet<>();
			String osS = args[0].val();
			for(String osSS : osS.split("\\|")) {
				OSUtils.OS os;
				try {
					os = OSUtils.OS.valueOf(osSS);
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Input OS must be one of " + StringUtils.Join(OSUtils.OS.values(), ", ", ", or "), t, ex);
				}
				oses.add(os);
			}
			if(oses.contains(OSUtils.GetOS())) {
				return new shell_adv().exec(t, environment, ArrayUtils.cast(ArrayUtils.slice(args, 1, args.length - 1), Construct[].class));
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "shell_on_adv";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {os, command, [options]} Executes the command if and only if on the given operating system (one of " + StringUtils.Join(OSUtils.OS.values(), ", ", ", or ")
					+ "). If not on the specified OS, this command does nothing. The os argument may be a pipe seperated list of OSes, for instance 'MAC|LINUX', which is"
					+ " useful given that both are unix based, and often times the same command will work for both."
					+ " Otherwise completely equivalent to {{function|shell_adv}}. This is useful, because usually a command is"
					+ " tailored to a specific OS, and simply won't run on other OSes. This allows you to create similar commands across various OSes, and ensure that they only"
					+ " run for the correct OS.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	@noboilerplate
	@seealso({shell.class, shell_adv.class, shell_on_adv.class, get_os.class})
	public static class shell_on extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREShellException.class, CREIOException.class, CREFormatException.class};
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
			Set<OSUtils.OS> oses = new HashSet<>();
			String osS = args[0].val();
			for(String osSS : osS.split("\\|")) {
				OSUtils.OS os;
				try {
					os = OSUtils.OS.valueOf(osSS);
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Input OS must be one of " + StringUtils.Join(OSUtils.OS.values(), ", ", ", or "), t, ex);
				}
				oses.add(os);
			}
			if(oses.contains(OSUtils.GetOS())) {
				return new shell().exec(t, environment, ArrayUtils.cast(ArrayUtils.slice(args, 1, args.length - 1), Construct[].class));
			}
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "shell_on";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "string {os, command, [options]} Executes the command if and only if on the given operating system (one of " + StringUtils.Join(OSUtils.OS.values(), ", ", ", or ")
					+ "). If not on the specified OS, this command returns null. The os argument may be a pipe seperated list of OSes, for instance 'MAC|LINUX', which is"
					+ " useful given that both are unix based, and often times the same command will work for both."
					+ " Otherwise completely equivalent to {{function|shell}}. This is useful, because usually a command is"
					+ " tailored to a specific OS, and simply won't run on other OSes. This allows you to create similar commands across various OSes, and ensure that they only"
					+ " run for the correct OS.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Mac OS usage", "shell_on('MAC', 'diskutil secureErase freespace /Volumes/*')", "<Would run the mac specific command>"),
				new ExampleScript("Unix usage", "shell_on('MAC|LINUX|SOLARIS', 'whoami')", "<Prints the current user name on Unix based systems>"),
				new ExampleScript("Windows batch file", "shell_on('WINDOWS', 'ERASE file.txt')", "Erases the file, but only on windows"),
				new ExampleScript("Delete a file on all OSes", "// Note I don't need any conditionals here\n"
				+ "shell_on('WINDOWS', 'ERASE file.txt');\n"
				+ "shell_on('MAC|LINUX|SOLARIS', 'rm file.txt');", "<Would delete file.txt on all OSes>")
			};
		}

	}

	@api
	public static class get_os extends AbstractFunction {

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
			return new CString(OSUtils.GetOS().name(), t);
		}

		@Override
		public String getName() {
			return "get_os";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "string {} Returns the name of the current OS. Will be one of " + StringUtils.Join(OSUtils.OS.values(), ", ", ", or ") + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	public static class read_pipe_input extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREInsufficientPermissionException.class};
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
			if(!Static.InCmdLine(environment, true)) {
				throw new CREInsufficientPermissionException(getName() + " cannot be used outside of cmdline mode.", t);
			}
			if(System.console() != null) {
				throw new CREIOException(getName() + " can only be used in TTY mode.", t);
			}
			boolean binary = false;
			if(args.length > 0) {
				binary = ArgumentValidation.getBoolean(args[0], t);
			}
			try {
				if(binary) {
					CByteArray ba = new CByteArray(t);
					while(true) {
						int b = System.in.read();
						if(b < 0) {
							break;
						}
						ba.putByte((byte) b, null);
					}
					return ba;
				} else {
					Reader r = new InputStreamReader(System.in);
					StringBuilder b = new StringBuilder();
					while(true) {
						int ch;
						ch = r.read();
						if(ch < 0) {
							break;
						}
						b.append((char) ch);
					}
					return new CString(b.toString(), t);
				}
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			}
		}

		@Override
		public String getName() {
			return "read_pipe_input";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mixed {[binary]} Reads the input from a process that is piped to this script. It is assumed that the"
					+ " data piped to the script will come all at once, and it will be returned as a string (or byte_array if binary is true)."
					+ " This can only be used in cmdline mode, and binary defaults to false. If the script isn't started in TTY mode,"
					+ " an IOException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class pwd extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREShellException.class};
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
			File root;
			if(Static.InCmdLine(environment, true)) {
				root = environment.getEnv(GlobalEnv.class).GetRootFolder();
			} else if(t.file() == null) {
				root = null;
			} else {
				root = t.file().getParentFile();
			}
			if(root == null) {
				throw new CREShellException("Running in interpreted mode. pwd() is not available.", t);
			} else {
				try {
					String ret = root.getCanonicalPath();
					return new CString(ret, t);
				} catch (IOException ex) {
					//This shouldn't happen, because the current working directory will only be
					//set programmatically.
					throw new RuntimeException(ex);
				}
			}
		}

		@Override
		public String getName() {
			return "pwd";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "string {} Returns the path to the current working directory. This is available outside cmdline mode, but"
					+ " is probably only useful for debugging, meta, or informational purposes when not in cmdline interpreter mode,"
					+ " as the current working directory is known simply by knowing what file this is running from. When run from"
					+ " a context where there is no working directory, a ShellException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class cd extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREInsufficientPermissionException.class};
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
			requireCmdlineMode(environment, this, t);
			File cd = Static.GetFileFromArgument(args.length == 0 ? null : args[0].val(), environment, t, new File(System.getProperty("user.home")));
			if(!cd.exists()) {
				throw new CREIOException("No such file or directory: " + cd.getPath(), t);
			}
			environment.getEnv(GlobalEnv.class).SetRootFolder(cd);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "cd";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[dir]} Changes the current working directory to the path specified, or the user's home"
					+ " directory if omitted. This only works from cmdline mode.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class ls extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class};
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
			requireCmdlineMode(environment, this, t);
			CArray ca = new CArray(t);
			File cwd = Static.GetFileFromArgument(args.length > 0 ? args[0].val() : null, environment, t, environment.getEnv(GlobalEnv.class).GetRootFolder());
			if(cwd.exists()) {
				for(File f : cwd.listFiles()) {
					ca.push(new CString(f.getName(), t), t);
				}
			} else {
				throw new CREIOException("No such file or directory: " + cwd.getPath(), t);
			}
			return ca;
		}

		@Override
		public String getName() {
			return "ls";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "array {[directory]} Returns an array of files in the current working directory, including \"hidden\" files, or"
					+ "if directory is specified, the files in that directory. This is only available in cmdline mode.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class set_cmdline_prompt extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientPermissionException.class};
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
			requireCmdlineMode(environment, this, t);
			if(!(args[0].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Expecting a closure for argument 1 of " + getName(), t);
			}
			environment.getEnv(GlobalEnv.class).SetCustom("cmdline_prompt", args[0]);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_cmdline_prompt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {closure} Sets the cmdline prompt. This is only usable or useful in cmdline interpreter mode. The closure should"
					+ " return a string, that string will be used as the prompt. The closure is called each time a prompt needs generating,"
					+ " thereby allowing for dynamic prompts. A boolean is sent to the closure, if true, the shell is in shellMode, meaning"
					+ " the command is interpreted as a shell command. If false, it is in normal mscript mode.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class get_terminal_width extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class};
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
			requireCmdlineMode(environment, this, t);
			try {
				int i = new jline.console.ConsoleReader().getTerminal().getWidth();
				return new CInt(i, t);
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			}
		}

		@Override
		public String getName() {
			return "get_terminal_width";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "int {} Returns the current width of the terminal, measured in characters. This is"
					+ " useful for determining proper layout for dynamic output. This only works in cmdline mode.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class user extends AbstractFunction {

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
			String name = StaticLayer.GetConvertor().GetUser(environment);
			if(name == null) {
				return CNull.NULL;
			} else {
				return new CString(name, t);
			}
		}

		@Override
		public String getName() {
			return "user";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "string {} Returns the name of the current user. This is retrieved in a platform specific manner, and should"
					+ " be cross compatible in all scripts. Null is returned if this function call is non-sensical in the"
					+ " current platform";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class in_cmdline_mode extends AbstractFunction {

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
			return CBoolean.GenerateCBoolean(Static.InCmdLine(environment, true), t);
		}

		@Override
		public String getName() {
			return "in_cmdline_mode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "boolean {} Returns true if the environment is in cmdline mode. False otherwise.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	public static class x_find extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREInsufficientPermissionException.class};
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
			requireCmdlineMode(environment, this, t);
			final CArray a = new CArray(t);
			String regex = ArgumentValidation.getString(args[0], t);
			String startFrom = environment.getEnv(GlobalEnv.class).GetRootFolder().getAbsolutePath();
			Set<FindType> findTypes = EnumSet.allOf(FindType.class);
			if(args.length == 2) {
				if(args[1].isInstanceOf(CArray.TYPE)) {
					findTypes = ArgumentValidation.getEnumSet(args[1], FindType.class, t);
				} else {
					startFrom = ArgumentValidation.getString(args[1], t);
				}
			} else if(args.length > 2) {
				startFrom = ArgumentValidation.getString(args[1], t);
				findTypes = ArgumentValidation.getEnumSet(args[2], FindType.class, t);
			}
			if(findTypes.isEmpty()) {
				throw new CREIllegalArgumentException("types must be a non-empty array, and not null", t);
			}
			File startFromF = new File(startFrom);
			final Set<FindType> types = findTypes;
			try {
				FileUtil.recursiveFind(startFromF, (file) -> {
					// We are such a potentially long running process, that we want to specially provide support
					// for Ctrl-C, so we check for interruption here.
					if(environment.getEnv(GlobalEnv.class).IsInterrupted()) {
						throw new CancelCommandException("", Target.UNKNOWN);
					}
					if(!types.contains(FindType.FILE) && file.isHidden()) {
						return;
					}
					if(!types.contains(FindType.DIRECTORY) && file.isDirectory()) {
						return;
					}
					if(!types.contains(FindType.FILE) && file.isFile()) {
						return;
					}
					if(file.getName().matches(regex)) {
						a.push(new CString(file.getAbsolutePath(), t), t);
					}
				});
			} catch (IOException ex) {
				//
			}
			return a;
		}

		@Override
		public String getName() {
			return "x_find";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@MEnum("ms.lang.FindType")
		public static enum FindType implements SimpleDocumentation {
			DIRECTORY("Only processes directory names, not files.", MSVersion.V3_3_4),
			FILE("Only processes files, not directories.", MSVersion.V3_3_4),
			HIDDEN("Only processes files/folders that are hidden. Leave this off to ignore hidden files.",
				MSVersion.V3_3_4);

			String docs;
			Version since;
			private FindType(String docs, Version since) {
				this.docs = docs;
				this.since = since;
			}
			@Override
			public String getName() {
				return name();
			}

			@Override
			public String docs() {
				return docs;
			}

			@Override
			public Version since() {
				return since;
			}
		}

		@Override
		public String docs() {
			return "array<string> {regex, [startFrom], [type] | regex, [startFrom], types} Returns a list of absolute"
					+ " paths of files whose name"
					+ " matches the given regex, searching recursively. Only available in cmdline mode."
					+ " ---- Type may be a single value, or an array of values from the"
					+ " following list:\n" + createEnumTable(FindType.class) + "\n\nIf the type is not specified,"
					+ " this is the same as specifying all types. If an empty array is used, this causes an error. If"
					+ " you wish to use the current directory as the starting point, use null as startFrom. Note"
					+ " that if you only send two arguments to this function, the second one must be an array if it"
					+ " is the types (it may contain a single value).";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

	}

	/**
	 * Requires cmdline mode. If not currently in cmdline mode, a proper CRE is thrown.
	 *
	 * @param environment
	 * @param f The function this is being called from.
	 * @param t
	 * @throws ConfigRuntimeException If not in cmdline mode.
	 */
	public static void requireCmdlineMode(Environment environment, Function f, Target t) throws ConfigRuntimeException {
		if(!Static.InCmdLine(environment, true)) {
			throw new CREInsufficientPermissionException(f.getName() + " cannot be used outside of cmdline mode.", t);
		}
	}

}
