package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.persistence.DataSourceException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An example script is a self contained script that runs itself, and returns the output. This is used in documentation
 * to show examples, and furthermore, since the code can be ACTUALLY run, examples are more guaranteed to show actual
 * output (bugs and all). These somewhat function as unit tests as well, but are not necessarily meant to cover all
 * possible use cases. The environment is mocked up in several cases, but there is no guarantee that functions will
 * work. Since this is the case, output can be mocked, by using the appropriate constructor. This will bypass actually
 * running the code, but will still output the information in the standard format.
 */
public class ExampleScript {

	String description;
	String originalScript;
	ParseTree script;
	String output;
	StringBuilder playerOutput = null;
	String functionName = "Example";

	static AliasCore fakeCore;
	static boolean init = false;

	/**
	 * Creates a new example script, where the output will come from the script itself.
	 *
	 * @param description
	 * @param script
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public ExampleScript(String description, String script) throws ConfigCompileException {
		this(StackTraceUtils.getCallingClass(), description, script, null, false);
	}

	/**
	 * Creates a new example script, where the output will come from the script itself. If the code is meant to
	 * demonstrate that it causes a compile error, then {@code intentionalCompileError} should be set to true.
	 * Otherwise, it will display blank, and print an error to console.
	 *
	 * @param description
	 * @param script
	 * @param intentionalCompileError
	 * @throws ConfigCompileException
	 */
	public ExampleScript(String description, String script, boolean intentionalCompileError)
			throws ConfigCompileException {
		this(StackTraceUtils.getCallingClass(), description, script, null, intentionalCompileError);
	}

	/**
	 * Creates a new example script, but the output is also specified. Use this in cases where the script cannot be run.
	 *
	 * @param description
	 * @param script
	 * @param output
	 * @throws ConfigCompileException
	 */
	public ExampleScript(String description, String script, String output) throws ConfigCompileException {
		this(StackTraceUtils.getCallingClass(), description, script, output, false);
	}

	/**
	 * Works like {@link #ExampleScript(java.lang.String, java.lang.String, java.lang.String)} but prints a message in
	 * the normal output when a compile error is encountered, instead of triggering the exceptional handling.
	 *
	 * @param description
	 * @param script
	 * @param output
	 * @param intentionalCompileError
	 * @throws ConfigCompileException
	 */
	@SuppressWarnings("unchecked")
	private ExampleScript(Class source, String description, String script, String output,
			boolean intentionalCompileError)
			throws ConfigCompileException {
		Class<?> c = source;
		if(Function.class.isAssignableFrom(c)) {
			functionName = ReflectionUtils.instantiateUnsafe((Class<? extends Function>) c).getName();
		}
		this.description = description;
		this.originalScript = script;
		String errorOutput = "Oops, something went wrong with this example.";
		String consoleErrorOutput = Static.MCToANSIColors(MCChatColor.RED.toString() + "Unintentional compile error in "
				+ c.getEnclosingClass().getSimpleName() + ":" + functionName + "(): \"" + description + "\"\n"
				+ MCChatColor.PLAIN_WHITE);
		try {
			Environment env = Static.GenerateStandaloneEnvironment();
			this.script = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null,
					new File((OSUtils.GetOS() == OSUtils.OS.WINDOWS ? "C:\\" : "/") + "Examples/" + functionName
							+ ".ms"), true), env,
					// We can't send null here, or it errors out, but we really do want to bypass the linking in
					// this instance. Therefore, we just return a list of all environments. We can't run the examples
					// that require non-default environments, but we can at least ensure that the script will generally
					// compile in *some* environment.
					new HashSet<>(ClassDiscovery.getDefaultInstance()
									.loadClassesThatExtend(Environment.EnvironmentImpl.class)));
			this.output = output;
		} catch (ConfigCompileException e) {
			if(intentionalCompileError) {
				this.output = "Causes compile error: " + e.getMessage();
			} else {
				this.output = errorOutput;
				System.out.println(consoleErrorOutput);
				System.out.println(e.getMessage());
			}
		} catch (ConfigCompileGroupException ex) {
			if(intentionalCompileError) {
				StringBuilder b = new StringBuilder();
				b.append("Causes compile errors:\n");
				for(ConfigCompileException e : ex.getList()) {
					b.append(e.getMessage()).append("\n");
				}
				this.output = b.toString();
			} else {
				this.output = errorOutput;
				System.out.println(consoleErrorOutput);
				for(ConfigCompileException e : ex.getList()) {
					System.out.println(e.getMessage());
				}
			}
		} catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
			ex.printStackTrace(System.err);
		}
		playerOutput = new StringBuilder();

	}

	public String getDescription() {
		return description;
	}

	public boolean isAutomatic() {
		return output == null;
	}

	private class FakeCore extends AliasCore {

		public FakeCore() {
			super(null, null, null, null, null);
			this.autoIncludes = new ArrayList<>();
		}
	}

	private Object genericReturn(Class r) {
		if(r.isPrimitive()) {
			if(r == int.class) {
				return 0;
			} else if(r == byte.class) {
				return (byte) 0;
			} else if(r == double.class) {
				return 0.0;
			} else if(r == float.class) {
				return 0.0f;
			} else if(r == char.class) {
				return '\0';
			} else if(r == short.class) {
				return (short) 0;
			} else if(r == boolean.class) {
				return false;
			} else { //long
				return 0L;
			}
		} else {
			if(r == String.class) {
				return "";
			}
			return null;
		}
	}

	public String getScript() {
		return originalScript;
	}

	public String getOutput() throws IOException, DataSourceException, URISyntaxException {
		if(output != null) {
			return output;
		}
		Script s = Script.GenerateScript(script, Static.GLOBAL_PERMISSION);
		Environment env;
		try {
			env = Static.GenerateStandaloneEnvironment();
			// We must have CommandHelperEnvironment to generate the docs. This should be removed
			// before CH/MS can be split, but that means a lot of examples probably need to change
			// first. We use GenerateStandaloneEnvironment to use the standardized creation of
			// GlobalEnv and CompilerEnv, but then add CHEnv as well.
			env = env.cloneAndAdd(new CommandHelperEnvironment());
		} catch (Profiles.InvalidProfileException ex) {
			throw new RuntimeException(ex);
		}
		Class[] interfaces = new Class[]{
			MCPlayer.class
		};
		MCPlayer p = (MCPlayer) Proxy.newProxyInstance(ExampleScript.class.getClassLoader(), interfaces,
				(Object proxy, Method method, Object[] args) -> {
			if(method.getName().equals("getName") || method.getName().equals("getDisplayName")) {
				return "Player";
			}
			if(method.getName().equals("sendMessage")) {
				playerOutput.append(args[0].toString()).append("\n");
			}
			if(method.getName().equals("isOnline")) {
				return true;
			}
			return genericReturn(method.getReturnType());
		});
		// TODO: Remove this dependency. Make MCPlayer implement a generic "User" and make that
		// part of the GlobalEnv.
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(p);
		final StringBuilder finalOutput = new StringBuilder();
		String thrown = null;
		try {
			List<Variable> vars = new ArrayList<>();
			try {
				MethodScriptCompiler.execute(originalScript, new File("/" + functionName + ".ms"), true, env,
						env.getEnvClasses(), (String output1) -> {
							if(output1 != null) {
								finalOutput.append(output1);
							}
						},
						null, vars);
			} catch (ConfigCompileException | ConfigCompileGroupException ex) {
				// We already checked for compile errors, so this won't happen
			}
		} catch (ConfigRuntimeException e) {
			String name = e.getClass().getName();
			if(e instanceof AbstractCREException) {
				name = ((AbstractCREException) e).getName();
			}
			thrown = "\n(Throws " + name + ": " + e.getMessage() + ")";
		}
		String playerOut = playerOutput.toString().trim();
		String finalOut = finalOutput.toString().trim();

		String out = (playerOut.isEmpty() ? "" : playerOut) + (finalOut.isEmpty() || !playerOut.trim().isEmpty()
				? "" : ":" + finalOut);
		if(thrown != null) {
			out += thrown;
		}
		return out;
	}

}
