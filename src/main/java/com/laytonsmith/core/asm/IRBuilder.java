package com.laytonsmith.core.asm;

import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class IRBuilder {
	List<String> lines = new ArrayList<>();
	List<Target> targets = new ArrayList<>();

	List<String> metadata = new ArrayList<>();

	public IRBuilder() {

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
}
