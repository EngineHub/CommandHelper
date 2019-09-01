package com.laytonsmith.core.compiler;

import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
import com.laytonsmith.core.objects.ObjectDefinitionTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * The compiler environment provides compilation settings, or other controller specific values. This allows for
 * separation of the compiler from the general layout of configuration and other files. The compiler environment is
 * available to the runtime environment as well, but contains values that the compiler (or function optimizations) might
 * need, and are usually considered "static". The settings are all passed in to the constructor, but you can use the
 * various factory methods to create an environment from other sources.
 *
 */
public class CompilerEnvironment implements Environment.EnvironmentImpl {

	/**
	 * A constant is a construct that is defined in source like ${this}. The value must be passed in at compile time.
	 */
	private final Map<String, Construct> constants = new HashMap<>();

	/**
	 * A list of included parse trees. These likely will have come from other files, but the compilation result should
	 * have been cached.
	 */
	private final List<ParseTree> includes = new ArrayList<>();

	/**
	 * A list of assigned vars are kept here, so when in strict mode, if a variable hasn't been declared yet, it will be
	 * a compiler error.
	 */
	private final Stack<Set<String>> knownVars = new Stack<>();

	/**
	 * Classes are generally defined at compile time, and anyways, the object definitions that are available are
	 * compiled into the ObjectDefinitionTable. Methods and internal pieces may not be fully defined yet, however,
	 * but at runtime these will be fully useable mechanisms. During compile time, care must be taken not to use
	 * a potentially partially defined class.
	 */
	private final ObjectDefinitionTable objectDefinitionTable = ObjectDefinitionTable.GetBlankInstance();

	private final List<CompilerWarning> compilerWarnings = new ArrayList<>();

	private boolean logCompilerWarnings = true;

	//TODO: Need to figure out how to do known procs.
	public void setConstant(String name, Construct value) {
		constants.put(name, value);
	}

	public void setConstant(String name, String value) {
		setConstant(name, new CString(value, Target.UNKNOWN));
	}

	public Construct getConstant(String name) {
		return constants.get(name);
	}

	public void pushVariableStack() {
		knownVars.push(new HashSet<>());
	}

	public void popVariableStack() {
		knownVars.pop();
	}

	public void addKnownVar(String name) {
		knownVars.peek().add(name);
	}

	public boolean isVarKnown(String name) {
		for(Set<String> scope : knownVars) {
			if(scope.contains(name)) {
				return true;
			}
		}
		return false;
	}

	public void addInclude(ParseTree tree) {
		includes.add(tree);
	}

	public List<ParseTree> getIncludes() {
		return new ArrayList<>(includes);
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public EnvironmentImpl clone() throws CloneNotSupportedException {
		// There should always only be one CompilerEnvironment.
		return this;
	}

	public ObjectDefinitionTable getObjectDefinitionTable() {
		return this.objectDefinitionTable;
	}

	/**
	 * Turns on or off logging of compiler warnings. By default, this is on.
	 * @param logCompilerWarnings
	 */
	public void setLogCompilerWarnings(boolean logCompilerWarnings) {
		this.logCompilerWarnings = logCompilerWarnings;
	}

	/**
	 * Adds the compiler warning object to the environment, which can be used later by tools and such in a tool
	 * specific way. Additionally, unless set in the environment otherwise, also logs the warning to the console.
	 * @param fileOptions The file options. May be null if not available, but then that means this warning is not
	 * suppressable.
	 * @param warning The compiler warning itself. Note that if the suppression category is null, this will neither
	 * be added to the list, nor logged.
	 */
	public void addCompilerWarning(FileOptions fileOptions, CompilerWarning warning) {
		boolean isSuppressed;
		if(fileOptions == null || warning.getSuppressCategory() == null) {
			isSuppressed = false;
		} else {
			isSuppressed = fileOptions.isWarningSuppressed(warning.getSuppressCategory());
		}
		if(isSuppressed) {
			return;
		}
		this.compilerWarnings.add(warning);
		if(logCompilerWarnings) {
			MSLog.GetLogger().Log(MSLog.Tags.COMPILER, LogLevel.WARNING, warning.getMessage(), warning.getTarget());
		}
	}

	/**
	 * Code upgrade notices are similar to compiler warnings in all ways except the logic for deciding when to display
	 * them. They only display when strict mode is enabled, and they can even still be suppressed. The CompilerWarning
	 * object should have the {@link FileOptions.SuppressWarning#CodeUpgradeNotices} type, but it is ignored either way.
	 * <p>
	 * Warnings added in this way show up in {@link #getCompilerWarnings()}.
	 * @param fileOptions
	 * @param warning
	 */
	public void addCodeUpgradeNotice(FileOptions fileOptions, CompilerWarning warning) {
		if(fileOptions == null) {
			return;
		}
		if(!fileOptions.isStrict()) {
			return;
		}
		if(fileOptions.isWarningSuppressed(FileOptions.SuppressWarning.CodeUpgradeNotices)) {
			return;
		}
		this.compilerWarnings.add(warning);
		if(logCompilerWarnings) {
			MSLog.GetLogger().Log(MSLog.Tags.COMPILER, LogLevel.WARNING, warning.getMessage(), warning.getTarget());
		}
	}

	/**
	 * Returns a list of compiler warnings that were logged during compilation.
	 * @return
	 */
	public List<CompilerWarning> getCompilerWarnings() {
		return new ArrayList<>(compilerWarnings);
	}

}
