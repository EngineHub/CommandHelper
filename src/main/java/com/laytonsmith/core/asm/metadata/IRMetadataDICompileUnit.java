package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.environments.Environment;

/**
 *
 */
public class IRMetadataDICompileUnit extends IRMetadata {
	/**
	 * Creates a new DICompileUnit reference.
	 * @param env
	 * @param file The file this compile unit is based on.
	 * @param producer The same data that's in !llvm.ident
	 * @param isOptimized
	 * @param enums A reference to a Metadata Tuple
	 * @param retainedTypes A reference to a Metadata Tuple
	 * @param globals A reference to a Metadata Tuple
	 * @param imports A reference to a Metadata Tuple
	 */
	public IRMetadataDICompileUnit(Environment env, IRMetadataDIFile file, String producer, boolean isOptimized,
			IRMetadata enums, IRMetadata retainedTypes, IRMetadata globals, IRMetadata imports) {
		super(env, new IRMetadata.PrototypeBuilder()
				.put("language", DataType.NUMBER)
				.put("file", DataType.REFERENCE)
				.put("producer", DataType.STRING)
				.put("isOptimized", DataType.BOOLEAN)
				.put("runtimeVersion", DataType.NUMBER)
				.put("emissionKind", DataType.CONST)
				.put("enums", DataType.REFERENCE)
				.put("retainedTypes", DataType.REFERENCE)
				.put("globals", DataType.REFERENCE)
				.put("imports", DataType.REFERENCE)
				.build(), "DICompileUnit");
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		// Since MethodScript doesn't exist in the DWARF standard yet, for now, this is just randomly selected.
		this.putNumber("language", 0x7777);
		this.putMetadataReference("file", file);
		this.putAttribute("producer", producer);
		this.putBoolean("isOptimized", isOptimized);
		this.putNumber("runtimeVersion", 0);
		this.putConst("emissionKind", "FullDebug");
		this.putMetadataReference("enums", enums);
		this.putMetadataReference("retainedTypes", retainedTypes);
		this.putMetadataReference("globals", globals);
		this.putMetadataReference("imports", imports);
	}
}
