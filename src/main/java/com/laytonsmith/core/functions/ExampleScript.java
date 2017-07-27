package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.persistence.DataSourceException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * An example script is a self contained script that runs itself, and returns
 * the output. This is used in documentation to show examples, and furthermore, since
 * the code can be ACTUALLY run, examples are more guaranteed to show actual output
 * (bugs and all). These somewhat function as unit tests as well, but are not necessarily
 * meant to cover all possible use cases. The environment is mocked up in several cases,
 * but there is no guarantee that functions will work. Since this is the case,
 * output can be mocked, by using the appropriate constructor. This will bypass actually
 * running the code, but will still output the information in the standard format.
 */
public class ExampleScript {

	String description;
	String originalScript;
	ParseTree script;
	String output;
	StringBuilder playerOutput = null;

	static AliasCore fakeCore;
	static boolean init = false;


	/**
	 * Creates a new example script, where the output will come from the
	 * script itself.
	 * @param description
	 * @param script
	 */
	public ExampleScript(String description, String script) throws ConfigCompileException{
		this(description, script, null, false);
	}

	public ExampleScript(String description, String script, boolean intentionalCompileError) throws ConfigCompileException{
		this(description, script, null, intentionalCompileError);
	}

	/**
	 * Creates a new example script, but the output is also specified. Use
	 * this in cases where the script cannot be run.
	 * @param description
	 * @param script
	 * @param output
	 * @throws ConfigCompileException
	 */
	public ExampleScript(String description, String script, String output) throws ConfigCompileException{
		this(description, script, output, false);
	}

	/**
	 * Works like {@link #ExampleScript(java.lang.String, java.lang.String, java.lang.String)} but prints
	 * a message in the normal output when a compile error is encountered, instead of triggering the exceptional
	 * handling.
	 * @param description
	 * @param script
	 * @param output
	 * @param intentionalCompileError
	 * @throws ConfigCompileException
	 */
	private ExampleScript(String description, String script, String output, boolean intentionalCompileError) throws ConfigCompileException{
		this.description = description;
		this.originalScript = script;
		try{
			this.script = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, new File((OSUtils.GetOS() == OSUtils.OS.WINDOWS ? "C:\\" : "/") + "Examples.ms"), true));
			this.output = output;
		} catch(ConfigCompileException e){
			if(intentionalCompileError){
				this.output = "Causes compile error: " + e.getMessage();
			}
		} catch(ConfigCompileGroupException ex){
			if(intentionalCompileError){
				StringBuilder b = new StringBuilder();
				b.append("Causes compile errors:\n");
				for(ConfigCompileException e : ex.getList()){
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

	public boolean isAutomatic(){
		return output == null;
	}

	private class FakeCore extends AliasCore{
		public FakeCore(){
			super(null, null, null, null, null);
			this.autoIncludes = new ArrayList<>();
		}
	}

	private Object genericReturn(Class r){
			if(r.isPrimitive()){
				if(r == int.class){
					return 0;
				} else if(r == byte.class){
					return (byte)0;
				} else if(r == double.class){
					return 0.0;
				} else if(r == float.class){
					return 0.0f;
				} else if(r == char.class){
					return '\0';
				} else if(r == short.class){
					return (short)0;
				} else if(r == boolean.class){
					return false;
				} else { //long
					return 0L;
				}
			} else {
				if(r == String.class){
					return "";
				}
				return null;
			}
	}

	public String getScript(){
		return originalScript;
	}

	public String getOutput() throws IOException, DataSourceException, URISyntaxException{
		if(output != null){
			return output;
		}
		Script s = Script.GenerateScript(script, Static.GLOBAL_PERMISSION);
		Environment env;
		try {
			env = Static.GenerateStandaloneEnvironment();
		} catch (Profiles.InvalidProfileException ex) {
			throw new RuntimeException(ex);
		}
		final StringBuilder finalOutput = new StringBuilder();
		String thrown = null;
		try{
			s.run(new ArrayList<Variable>(), env, new MethodScriptComplete() {

				@Override
				public void done(String output) {
					if(output != null){
						finalOutput.append(output);
					}
				}
			});
		} catch(ConfigRuntimeException e){
			String name = e.getClass().getName();
			if(e instanceof AbstractCREException){
				name = ((AbstractCREException)e).getName();
			}
			thrown = "\n(Throws " + name + ": " + e.getMessage() + ")";
		}
		String playerOut = playerOutput.toString().trim();
		String finalOut = finalOutput.toString().trim();

		String out = (playerOut.equals("")?"":playerOut) + (finalOut.equals("")||!playerOut.trim().equals("") ?"":":" + finalOut);
		if(thrown != null){
			out += thrown;
		}
		return out;
	}


}
