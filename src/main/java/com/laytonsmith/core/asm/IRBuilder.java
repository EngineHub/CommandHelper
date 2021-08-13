package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class IRBuilder {
	Map<LLVMFunction, StartupCodeLineGetter> startupCode = new HashMap<>();

	List<String> lines = new ArrayList<>();
	List<Target> targets = new ArrayList<>();

	List<String> metadata = new ArrayList<>();

	public IRBuilder() {

	}

	public static interface StartupCodeLineGetter {
		String[] getLines(Environment env);
	}

	/**
	 * Adds code that will run at the beginning of main, and is intended for activities such as seeding rand, etc,
	 * which should only be called once ever per program run, and only if this function is actually used. Note that
	 * this code will only be called once per function, so feel free to run environment modifying code.
	 * @param function this
	 * @param lines The lines to append
	 */
	public void appendStartupCode(LLVMFunction function, StartupCodeLineGetter lines) {
		if(!startupCode.containsKey(function)) {
			startupCode.put(function, lines);
		}
	}

	public void appendLine(Target t, String line) {
		// This is a great place to put a breakpoint if you aren't sure where a line of IR is coming from.
		lines.add(line);
		targets.add(t);
	}

	public void appendLines(Target t, String... lines) {
		for(String line : lines) {
			appendLine(t, line);
		}
	}

	public void appendLines(Target t, List<String> lines) {
		for(String line : lines) {
			appendLine(t, line);
		}
	}

	public String renderStartupCode(Environment env) {
		StringBuilder b = new StringBuilder();
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);

		for(Map.Entry<LLVMFunction, StartupCodeLineGetter> entry : startupCode.entrySet()) {
			LLVMFunction function = entry.getKey();
			StartupCodeLineGetter lines = entry.getValue();
			String glue;
			if(llvmenv.isOutputIRCodeTargetLoggingEnabled()) {
				glue = " ; " + function.getName() + " startup code " + OSUtils.GetLineEnding() + "  ";
			} else {
				glue = OSUtils.GetLineEnding();
			}
			b.append("  " + StringUtils.Join(lines.getLines(env), glue) + glue);
		}
		b.append(OSUtils.GetLineEnding());
		return b.toString();
	}

	public String renderIR(Environment env) {
		StringBuilder b = new StringBuilder();
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		int padding = 0;
		if(llvmenv.isOutputIRCodeTargetLoggingEnabled()) {
			// Figure up the longest line, to determine padding.
			for(String line : lines) {
				padding = Math.max(padding, line.length());
			}
		}

		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			Target t = targets.get(i);
			b.append(AsmUtil.formatLine(t, llvmenv, line, padding + 2));
		}
		return b.toString();
	}

	public void setFinalMetadata(Environment env) {
		Set<IRMetadata> metadataRefs = env.getEnv(LLVMEnvironment.class).getMetadataRegistry().getAllMetadata();
		for(IRMetadata d : metadataRefs) {
			metadata.add(d.getDefinition());
		}
	}

	public Gen generator() {
		return this.new Gen();
	}

	public class Gen {

		public void alloca(int id, IRType type, Target t) {
			// TODO: Look into the alignment
			IRBuilder.this.appendLine(t, "%" + id + " = alloca " + type.getIRType());
		}

	}
}
