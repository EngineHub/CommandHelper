package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.persistence.DataSourceException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     */
    public ExampleScript(String description, String script) throws ConfigCompileException {
	this(description, script, null, false);
    }

    public ExampleScript(String description, String script, boolean intentionalCompileError) throws ConfigCompileException {
	this(description, script, null, intentionalCompileError);
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
	this(description, script, output, false);
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
    private ExampleScript(String description, String script, String output, boolean intentionalCompileError) throws ConfigCompileException {
	Class<?> c = StackTraceUtils.getCallingClass();
	if (Function.class.isAssignableFrom(c)) {
	    functionName = ReflectionUtils.instantiateUnsafe((Class<? extends Function>) c).getName();
	}
	this.description = description;
	this.originalScript = script;
	try {
	    this.script = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, new File((OSUtils.GetOS() == OSUtils.OS.WINDOWS ? "C:\\" : "/") + "Examples.ms"), true));
	    this.output = output;
	} catch (ConfigCompileException e) {
	    if (intentionalCompileError) {
		this.output = "Causes compile error: " + e.getMessage();
	    }
	} catch (ConfigCompileGroupException ex) {
	    if (intentionalCompileError) {
		StringBuilder b = new StringBuilder();
		b.append("Causes compile errors:\n");
		for (ConfigCompileException e : ex.getList()) {
		    b.append(e.getMessage()).append("\n");
		}
		this.output = b.toString();
	    }
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
	if (r.isPrimitive()) {
	    if (r == int.class) {
		return 0;
	    } else if (r == byte.class) {
		return (byte) 0;
	    } else if (r == double.class) {
		return 0.0;
	    } else if (r == float.class) {
		return 0.0f;
	    } else if (r == char.class) {
		return '\0';
	    } else if (r == short.class) {
		return (short) 0;
	    } else if (r == boolean.class) {
		return false;
	    } else { //long
		return 0L;
	    }
	} else {
	    if (r == String.class) {
		return "";
	    }
	    return null;
	}
    }

    public String getScript() {
	return originalScript;
    }

    public String getOutput() throws IOException, DataSourceException, URISyntaxException {
	if (output != null) {
	    return output;
	}
	Script s = Script.GenerateScript(script, Static.GLOBAL_PERMISSION);
	Environment env;
	try {
	    env = Static.GenerateStandaloneEnvironment();
	} catch (Profiles.InvalidProfileException ex) {
	    throw new RuntimeException(ex);
	}
	Class[] interfaces = new Class[]{
	    MCPlayer.class
	};
	MCPlayer p = (MCPlayer) Proxy.newProxyInstance(ExampleScript.class.getClassLoader(), interfaces, new InvocationHandler() {
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("getName") || method.getName().equals("getDisplayName")) {
		    return "Player";
		}
		if (method.getName().equals("sendMessage")) {
		    playerOutput.append(args[0].toString()).append("\n");
		}
		if (method.getName().equals("isOnline")) {
		    return true;
		}
		return genericReturn(method.getReturnType());
	    }
	});
	// TODO: Remove this dependency. Make MCPlayer implement a generic "User" and make that
	// part of the GlobalEnv.
	env.getEnv(CommandHelperEnvironment.class).SetPlayer(p);
	final StringBuilder finalOutput = new StringBuilder();
	String thrown = null;
	try {
	    List<Variable> vars = new ArrayList<>();
	    try {
		MethodScriptCompiler.execute(originalScript, new File("/" + functionName + ".ms"), true, env, new MethodScriptComplete() {

		    @Override
		    public void done(String output) {
			if (output != null) {
			    finalOutput.append(output);
			}
		    }
		}, null, vars);
	    } catch (ConfigCompileException | ConfigCompileGroupException ex) {
		// We already checked for compile errors, so this won't happen
	    }
	} catch (ConfigRuntimeException e) {
	    String name = e.getClass().getName();
	    if (e instanceof AbstractCREException) {
		name = ((AbstractCREException) e).getName();
	    }
	    thrown = "\n(Throws " + name + ": " + e.getMessage() + ")";
	}
	String playerOut = playerOutput.toString().trim();
	String finalOut = finalOutput.toString().trim();

	String out = (playerOut.equals("") ? "" : playerOut) + (finalOut.equals("") || !playerOut.trim().equals("") ? "" : ":" + finalOut);
	if (thrown != null) {
	    out += thrown;
	}
	return out;
    }

}
