package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.ZipMaker;
import com.laytonsmith.core.LocalPackages;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Profiles.InvalidProfileException;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.persistence.DataSourceException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.laytonsmith.PureUtilities.TermColors.GREEN;
import static com.laytonsmith.PureUtilities.TermColors.RED;
import static com.laytonsmith.PureUtilities.TermColors.pl;
import static com.laytonsmith.PureUtilities.TermColors.prompt;
import static com.laytonsmith.PureUtilities.TermColors.reset;

/**
 *
 *
 */
public class MSLPMaker {

	public static void start(String path, Set<Class<? extends Environment.EnvironmentImpl>> envs) throws IOException {
		File start = new File(path);
		if(!start.exists()) {
			StreamUtils.GetSystemErr().println("The specified file does not exist!");
			return;
		}

		File output = new File(start.getParentFile(), start.getName() + ".mslp");
		if(output.exists()) {
			pl("The file " + output.getName() + " already exists, would you like to overwrite? (Y/N)");
			String overwrite = prompt();
			if(!overwrite.equalsIgnoreCase("y")) {
				return;
			}
		}
		//First attempt to compile it, and make sure it doesn't fail
		LocalPackages localPackages = new LocalPackages();
		localPackages.search(start);
		boolean error = false;
		for(LocalPackages.FileInfo fi : localPackages.getMSFiles()) {
			Environment fakeEnv = null;
			try {
				fakeEnv = Static.GenerateStandaloneEnvironment();
			} catch (DataSourceException | URISyntaxException | InvalidProfileException e) {
				throw new RuntimeException(e);
			}
			try {
				MethodScriptCompiler.compile(
						MethodScriptCompiler.lex(fi.contents(), null, fi.file(), true), null, envs);
			} catch (ConfigCompileException e) {
				error = true;
				ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script. Compilation will attempt to continue, however.", null, fakeEnv);
			} catch (ConfigCompileGroupException ex) {
				error = true;
				ConfigRuntimeException.HandleUncaughtException(ex, null, fakeEnv);
			}
		}
		List<Script> allScripts = new ArrayList<>();
		for(LocalPackages.FileInfo fi : localPackages.getMSAFiles()) {
			List<Script> tempScripts;
			Environment env;
			try {
				env = Static.GenerateStandaloneEnvironment();
			} catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
				throw new RuntimeException(ex);
			}
			try {
				tempScripts = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(fi.contents(), null, fi.file(), false), envs);
				for(Script s : tempScripts) {
					try {
						s.compile(env.clone());
						s.checkAmbiguous(allScripts);
						allScripts.add(s);
					} catch (ConfigCompileException e) {
						error = true;
						ConfigRuntimeException.HandleUncaughtException(e, "Compile error in script. Compilation will attempt to continue, however.", null, env);
					} catch (ConfigCompileGroupException e) {
						error = true;
						ConfigRuntimeException.HandleUncaughtException(e, "Compile errors in script. Compilation will attempt to continue, however.", null, env);
					} catch (CloneNotSupportedException e) {
						throw new Error("Environment wasn't clonable, while it should be.", e);
					}
				}
			} catch (ConfigCompileException e) {
				error = true;
				ConfigRuntimeException.HandleUncaughtException(e, "Could not compile file " + fi.file() + " compilation will halt.", null, env);
			}
		}

		if(!error) {
			ZipMaker.MakeZip(start, output.getName());

			pl(GREEN + "The MSLP file has been created at " + output.getAbsolutePath() + reset());
		} else {
			pl(RED + "MSLP file has not been created due to compile errors. Correct the errors, and try again." + reset());
		}
	}
}
