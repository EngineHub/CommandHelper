package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lsmith
 */
public class Cmdline {

    public static String docs() {
        return "This class contains functions that are mostly only useful for command line scripts, but in general may be used by any script. For"
                + " more information on running MethodScript from the command line, see [[CommandHelper/Command_Line_Scripting|this wiki page]].";
    }

    @api
    @noboilerplate
    public static class sys_out extends AbstractFunction {

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String msg = Static.MCToANSIColors(args[0].val());
            System.out.print(msg);
            if (msg.matches("(?m).*\033.*")) {
                //We have color codes in it, we need to reset them
                System.out.print(TermColors.reset());
            }
            System.out.println();
            return new CVoid(t);
        }

        public String getName() {
            return "sys_out";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {text} Writes the text to the system's std out. Unlike console(), this does not use anything else to format the output, though in many"
                    + " cases they will behave the same. However, colors and other formatting characters will not \"bleed\" through, so"
                    + " sys_out(color(RED) . 'This is red') will not cause the next line to also be red, so if you need to print multiple lines out, you should"
                    + " manually add \\n to create your linebreaks, and only make one call to sys_out.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
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
    public static class sys_err extends AbstractFunction {

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String msg = Static.MCToANSIColors(args[0].val());
            System.err.print(msg);
            if (msg.matches("(?m).*\033.*")) {
                //We have color codes in it, we need to reset them
                System.err.print(TermColors.reset());
            }
            System.err.println();
            return new CVoid(t);
        }

        public String getName() {
            return "sys_err";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {text} Writes the text to the system's std err. Unlike console(), this does not use anything else to format the output, though in many"
                    + " cases they will behave nearly the same. However, colors and other formatting characters will not \"bleed\" through, so"
                    + " sys_err(color(RED) . 'This is red') will not cause the next line to also be red, so if you need to print multiple lines out, you should"
                    + " manually add \\n to create your linebreaks, and only make one call to sys_err.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "#Note this is guaranteed to print to standard err\nsys_out('Hello World!')", ":Hello World!")
			};
		}
    }

    @api(environments={GlobalEnv.class})
    @noboilerplate
    public static class exit extends AbstractFunction implements Optimizable {

        public Exceptions.ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            int exit_code = 0;
            if (args.length == 1) {
                exit_code = Static.getInt32(args[0], t);
            }
            if (inCmdLine(environment)) {
                System.exit(exit_code);
            }
            return new Echoes.die().exec(t, environment, args);
        }

        public String getName() {
            return "exit";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[int]} Exits the program. If this is being run from the command line, works by exiting the interpreter, with "
                    + " the specified exit code (defaulting to 0). If this is being run from in-game, works just like die().";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
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
				new ExampleScript("Basic usage", "#Causes the JVM to exit with an exit code of 1\nexit(1)", ""),
			};
		}
    }

    @api
    public static class sys_properties extends AbstractFunction {

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            if (args.length == 1) {
                String propName = args[0].val();
                String prop = System.getProperty(propName);
                return new CString(prop, t);
            } else {
                CArray ca = new CArray(t);
                for (String key : System.getProperties().stringPropertyNames()) {
                    ca.set(key, System.getProperty(key));
                }
                return ca;
            }

        }

        public String getName() {
            return "sys_properties";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "mixed {[propertyName]} If propertyName is set, that single property is returned, or null if that property doesn't exist. If propertyName is not set, an"
                    + " associative array with all the system properties is returned. This mechanism hooks into Java's system property mechanism, and is just a wrapper for"
                    + " that. System properties are more reliable than environmental variables, and so are preferred in cases where they exist. For more information about system"
                    + " properties, see http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
		
		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Gets all properties", "array_size(sys_properties())"),
				new ExampleScript("Gets a single property", "sys_properties('java.specification.vendor')"),
			};
		}
    }

    @api
    public static class get_env extends AbstractFunction {

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            if (args.length == 1) {
                return new CString(System.getenv(args[0].val()), t);
            } else {
                CArray ca = new CArray(t);
                for (String key : System.getenv().keySet()) {
                    ca.set(key, System.getenv(key));
                }
                return ca;
            }
        }

        public String getName() {
            return "get_env";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "mixed {[variableName]} Returns the environment variable specified, if variableName is set. Otherwise, returns an associative array"
                    + " of all the environment variables.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }

    @api
    public static class set_env extends AbstractFunction {

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            //TODO: Make this more robust by having a local cache of the environment which we modify, and get_env returns from.
            Map<String, String> newenv = new HashMap<String, String>(System.getenv());
            newenv.put(args[0].val(), args[1].val());
            boolean ret = false;
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
            }
            catch (NoSuchFieldException e) {
                try {
                    Class[] classes = Collections.class.getDeclaredClasses();
                    Map<String, String> env = System.getenv();
                    for (Class cl : classes) {
                        if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                            Field field = cl.getDeclaredField("m");
                            field.setAccessible(true);
                            Object obj = field.get(env);
                            Map<String, String> map = (Map<String, String>) obj;
                            map.clear();
                            map.putAll(newenv);
                        }
                    }
                    ret = true;
                }
                catch (Exception e2) {
                    ret = false;
                    if(Prefs.DebugMode()){
                        e2.printStackTrace();
                    }
                }
            }
            catch (Exception e1) {
                ret = false;
                if(Prefs.DebugMode()){
                    e1.printStackTrace();
                }
            }
            return new CBoolean(ret, t);
        }

        public String getName() {
            return "set_env";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {variableName, value} Sets the value of an environment variable. This only changes the environment value in this process, not system-wide."
                    + " This uses some hackery to work, and may not be 100% reliable in all cases, and shouldn't be relied on heavily. It will"
                    + " always work with get_env, however, so you can rely on that mechanism. The value will always be interpreted as a string, so if you are expecting"
                    + " a particular data type on a call to get_env, you will need to manually cast the variable. Arrays will be toString'd as well, but will be accepted.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
    }
	
	@api
	public static class prompt_pass extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientPermissionException, ExceptionType.IOException};
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
			if(!inCmdLine(environment)){
				throw new ConfigRuntimeException(getName() + " cannot be used outside of cmdline mode.", ExceptionType.InsufficientPermissionException, t);
			}
			boolean mask = true;
			if(args.length > 1){
				mask = Static.getBoolean(args[1]);
			}
			
			String prompt = args[0].val();
			Character cha = new Character((char)0);
			if(mask){
				cha = new Character('*');
			}
			jline.console.ConsoleReader reader = null;
			try {
				reader = new jline.console.ConsoleReader();
				return new CString(reader.readLine(prompt, cha), t);
			} catch (IOException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t);
			} finally {
				if(reader != null){
					reader.shutdown();
				}
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
			return "string {prompt, [mask]} Prompts the user for a password. This only works in cmdline mode. If mask is true (default),"
					+ " then the password displays * characters for each password character they type. If mask is false, the field"
					+ " stays blank as they type. What they type is returned once they hit enter.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class prompt_char extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientPermissionException, ExceptionType.IOException};
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
			if(!inCmdLine(environment)){
				throw new ConfigRuntimeException(getName() + " cannot be used outside of cmdline mode.", ExceptionType.InsufficientPermissionException, t);
			}
			
			String prompt = args[0].val();
			System.out.print(prompt);
			System.out.flush();
			jline.console.ConsoleReader reader = null;
			try {
				reader = new jline.console.ConsoleReader();
				char c = (char)reader.readCharacter();
				System.out.println(c);
				return new CString(c, t);
			} catch (IOException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t);
			} finally {
				if(reader != null){
					reader.shutdown();
				}
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
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class prompt_line extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientPermissionException, ExceptionType.IOException};
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
			if(!inCmdLine(environment)){
				throw new ConfigRuntimeException(getName() + " cannot be used outside of cmdline mode.", ExceptionType.InsufficientPermissionException, t);
			}
			
			String prompt = args[0].val();
			jline.console.ConsoleReader reader = null;
			try {
				reader = new jline.console.ConsoleReader();
				String line = reader.readLine(prompt);
				return new CString(line, t);
			} catch (IOException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t);
			} finally {
				if(reader != null){
					reader.shutdown();
				}
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
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class sys_beep extends AbstractFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InsufficientPermissionException, ExceptionType.PluginInternalException};
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
			java.awt.Toolkit.getDefaultToolkit().beep();
			return new CVoid(t);
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
			return CHVersion.V3_3_1;
		}
		
	}
	
	public static boolean inCmdLine(Environment environment){
		return environment.getEnv(GlobalEnv.class).GetCustom("cmdline") instanceof Boolean 
					&& (Boolean) environment.getEnv(GlobalEnv.class).GetCustom("cmdline");
	}
}
