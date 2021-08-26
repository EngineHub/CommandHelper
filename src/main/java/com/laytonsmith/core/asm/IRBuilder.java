package com.laytonsmith.core.asm;

import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class IRBuilder {
	Set<LLVMFunction> functionsUsed = new HashSet<>();

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

	public String renderStartupCode(Environment env) {
		StringBuilder b = new StringBuilder();
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);

		IRBuilder newBuilder = new IRBuilder();

		for(LLVMFunction f : functionsUsed) {
			Target t = new Target(0, new File("/" + f.getName() + " startup code"), 0);
			f.addStartupCode(newBuilder, env, t);
		}

		return newBuilder.renderIR(env);
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

	public Gen generator(Target t, Environment env) {
		return this.new Gen(t, env);
	}

	public class Gen {

		Target t;
		Environment env;
		LLVMEnvironment llvmenv;

		Gen(Target t, Environment env) {
			this.t = t;
			this.env = env;
			this.llvmenv = env.getEnv(LLVMEnvironment.class);
		}

		/**
		 * Creates an alloca statement.
		 * @param id The id
		 * @param type
		 */
		public void alloca(int id, IRType type) {
			// TODO: Look into the alignment
			IRBuilder.this.appendLine(t, "%" + id + " = alloca " + type.getIRType());
		}

		/**
		 * Used to store variables into an alloca. {@code into} is the identifier for the alloca statement,
		 * and type and id are what you're storing.
		 * @param type
		 * @param id
		 * @param into
		 */
		public void store(IRType type, int id, int into) {
			store(type, type.getIRType() + " %" + id, into);
		}

		/**
		 * Used to store a constant value into an alloca. {@code into} is the identifier for the alloca statement,
		 * and type and value are what you're storing. Note that the value can be anything, and is added as is
		 * to the statement.
		 * @param type
		 * @param value
		 * @param into
		 */
		public void store(IRType type, String value, int into) {
			IRBuilder.this.appendLine(t, "store " + value
					+ ", " + type.getIRType() + "* %" + into);
		}

		/**
		 * Combines an alloca and store into one call.
		 * @param allocaId The reference to the alloca call
		 * @param type The type of value
		 * @param storeId The value that will be stored in memory.
		 */
		public void allocaAndStore(int allocaId, IRType type, int storeId) {
			alloca(allocaId, type);
			store(type, storeId, allocaId);
		}

		/**
		 * Combines an alloca and store into one call, storing an arbitrary value.
		 * @param allocaId The reference to the alloca call
		 * @param type The type of value
		 * @param storeValue
		 */
		public void allocaAndStore(int allocaId, IRType type, String storeValue) {
			alloca(allocaId, type);
			store(type, storeValue, allocaId);
		}

		/**
		 * Creates a load statement.
		 * @param loadInto The reference to load into (That is, {@code x} in {@code %x = load...}).
		 * @param type The type of value
		 * @param allocaId The reference to the alloca call
		 */
		public void load(int loadInto, IRType type, int allocaId) {
			IRBuilder.this.appendLine(t, "%" + loadInto + " = load " + type.getIRType() + ", " + type.getIRType() + "* %" + allocaId);
		}

		/**
		 * Allocas, stores, and re-loads a value. The loaded id is returned, to make construction easier.
		 * @param allocaId The reference to use for the alloca call
		 * @param type The type of value
		 * @param storeId The reference to store.
		 * @param loadId The reference to use for the load call.
		 * @return
		 */
		public int allocaStoreAndLoad(int allocaId, IRType type, int storeId, int loadId) {
			allocaAndStore(allocaId, type, storeId);
			load(loadId, type, allocaId);
			return loadId;
		}

		/**
		 * Allocas, stores, and re-loads an arbitrary. The loaded id is returned, to make construction easier.
		 * @param allocaId The reference to use for the alloca call
		 * @param type The type of value
		 * @param storeValue The value to store.
		 * @param loadId The reference to use for the load call
		 * @return
		 */
		public int allocaStoreAndLoad(int allocaId, IRType type, String storeValue, int loadId) {
			allocaAndStore(allocaId, type, storeValue);
			load(loadId, type, allocaId);
			return loadId;
		}

	}
}
