package com.laytonsmith.core.asm;


import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.asm.metadata.LLVMMetadataRegistry;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.environments.Environment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class LLVMEnvironment implements Environment.EnvironmentImpl {

	private final AtomicInteger stringIdCounter = new AtomicInteger();
	private final Map<String, String> strings = new HashMap<>();
	private final Stack<Map<String, Integer>> variableTable = new Stack<>();
	private final Stack<Map<String, LeftHandSideType>> variableTableType = new Stack<>();
	private final Stack<Map<Integer, IRType>> irVariableTableType = new Stack<>();
	private final Set<String> globalDeclarations = new HashSet<>();
	private final StaticAnalysis staticAnalysis = new StaticAnalysis(false);
	private int localVariableCounter = 0;
	// LinkedHashSet, to maintain insertion order, but only allow one entry per header.
	private final LinkedHashSet<Header> additionalHeaders = new LinkedHashSet<>();
	private int metadataId = 0;
	private final LLVMMetadataRegistry metadataRegistry = new LLVMMetadataRegistry();

	public static enum HeaderType {
		SYSTEM,
		LOCAL
	}
	public static class Header {
		String name;
		HeaderType type;
	}

	@Override
	public Environment.EnvironmentImpl clone() throws CloneNotSupportedException {
		return this;
	}

	/**
	 * Puts a new constant string in the registry, and returns the ID for it. The id will be something like
	 * ".strings.13", so when referencing it, you probably need the @ in front.
	 * @param string
	 * @return
	 */
	public synchronized String getOrPutStringConstant(String string) {
		if(strings.containsKey(string)) {
			return strings.get(string);
		}
		String id = ".strings." + stringIdCounter.getAndIncrement();
		strings.put(string, id);
		return id;
	}

	/**
	 * Returns the list of registered strings, mapping from string -> id
	 * @return
	 */
	public Map<String, String> getStrings() {
		return new HashMap<>(strings);
	}

	// Default to true during initial development, eventually false
	private boolean outputIRCodeTargetLogging = true;

	public boolean isOutputIRCodeTargetLoggingEnabled() {
		return outputIRCodeTargetLogging;
	}

	public void setOutputIRCodeTargetLogging(boolean enabled) {
		this.outputIRCodeTargetLogging = enabled;
	}

	/**
	 * Adds a standard C library header file. If this file has already been included, it won't be re-included, but
	 * insertion order is otherwise maintained. Each header file will be compiled with Clang, and the resultant LLVM IR
	 * integrated into the main program. This is roughly equivalent to doing <code>#include &lt;header.h&gt;</code> in
	 * C code.
	 * @param headerName Something like <code>stdio.h</code>. Do not include the angle brackets.
	 */
	public void addSystemHeader(String headerName) {
		Header h = new Header();
		h.name = headerName;
		h.type = HeaderType.SYSTEM;
		additionalHeaders.add(h);
	}

	/**
	 * Adds a local C header file. If this file has already been included, it won't be re-included, but
	 * insertion order is otherwise maintained. Each header file will be compiled with Clang, and the resultant LLVM IR
	 * integrated into the main program. This is roughly equivalent to doing <code>#include "header.h"</code> in
	 * C code. In MethodScript, doing this only searches the internal includes, not user code.
	 * @param headerName Something like <code>myFile.h</code>. Do not include the quotes.
	 */
	public void addLocalHeader(String headerName) {
		Header h = new Header();
		h.name = headerName;
		h.type = HeaderType.LOCAL;
		additionalHeaders.add(h);
	}

	/**
	 * Returns the list of included headers. Note that system headers start with &lt; (<code>&lt;stdio.h</code>) and
	 * local headers start with ", (<code>"myFile.h</code>)
	 * @return
	 */
	public LinkedHashSet<Header> getAdditionalHeaders() {
		return new LinkedHashSet<>(additionalHeaders);
	}

	public void addGlobalDeclaration(String global) {
		// TODO: Deduplicate includes
		globalDeclarations.add(global);
	}

	/**
	 * Convenience method for adding a template defined in AsmCommonLibTemplates
	 * @param template
	 * @param env
	 */
	public void addGlobalDeclaration(AsmCommonLibTemplates.Generator template, Environment env) {
		template.include(env);
	}

	public String getGlobalDeclarations() {
		StringBuilder b = new StringBuilder();
		for(String gd : globalDeclarations) {
			b.append(gd).append(OSUtils.GetLineEnding());
		}
		return b.toString();
	}

	/**
	 * When a new method is begun, this should be called, which resets counters and other method specific information.
	 * @param methodName The name of the new method.
	 */
	public void newMethodFrame(String methodName) {
		localVariableCounter = 0;
	}

	/**
	 * Returns a new local variable reference, which can be used for %vars, and is guaranteed to be unique in this
	 * function definition.
	 * @return
	 */
	public int getNewLocalVariableReference(IRType type) {
		int value = localVariableCounter++;
		irVariableTableType.peek().put(value, type);
		return value;
	}

	/**
	 * Returns a new, globally unique label, which can be used to uniquely identify a br point.
	 * @return
	 */
	public int getGotoLabel() {
		// This also uses localVariableCounter, but we want to hide that fact, so we have two separate
		// functions for this.
		return localVariableCounter++;
	}

	public static enum OptimizationLevel {
		NONE("-O0"),
		NORMAL("-O2"),
		EXTRA("-O3");

		private final String arg;
		private OptimizationLevel(String arg) {
			this.arg = arg;
		}

		/**
		 * Returns the LLVM optimization flag for the corresponding level. (The dash is included.)
		 */
		public String getArg() {
			return arg;
		}
	}

	private OptimizationLevel optimizationLevel = OptimizationLevel.NORMAL;

	public OptimizationLevel getOptimizationLevel() {
		return optimizationLevel;
	}

	public void setOptimizationLevel(OptimizationLevel optimizationLevel) {
		this.optimizationLevel = optimizationLevel;
	}

	public void pushVariableScope() {
		variableTable.push(new HashMap<>());
		variableTableType.push(new HashMap<>());
		irVariableTableType.push(new HashMap<>());
	}

	public void popVariableScope() {
		variableTable.pop();
		variableTableType.pop();
		irVariableTableType.pop();
	}

	public void addVariableMapping(String methodscriptVariableName, int llvmVariableName, LeftHandSideType type) {
		variableTable.peek().put(methodscriptVariableName, llvmVariableName);
		variableTableType.peek().put(methodscriptVariableName, type);
	}

	/**
	 * Returns the IR reference to this variable. All variables are allocaStoreAndLoaded, so this is the value of
	 * the load instruction.
	 * @param methodscriptVariableName
	 * @return
	 */
	public int getVariableMapping(String methodscriptVariableName) {
		return variableTable.peek().get(methodscriptVariableName);
	}

	public LeftHandSideType getVariableType(String methodscriptVariableName) {
		return variableTableType.peek().get(methodscriptVariableName);
	}

	public IRType getIRType(int variable) {
		return irVariableTableType.peek().get(variable);
	}

	public int getNewMetadataId() {
		return metadataId++;
	}

	public LLVMMetadataRegistry getMetadataRegistry() {
		return this.metadataRegistry;
	}

	public StaticAnalysis getStaticAnalysis() {
		return this.staticAnalysis;
	}
}
