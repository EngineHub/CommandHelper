package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import java.io.IOException;

/**
 * A reference to a function.
 */
public class IRMetadataDISubprogram extends IRMetadata {
	/**
	 * Creates a subprogram (function) refernece.
	 * @param env
	 * @param name The name of the function.
	 * @param file
	 * @param t The code target where this function is defined.
	 * @param type A reference to a IRMetadataDISubroutineType object.
	 */
	public IRMetadataDISubprogram(Environment env, String name, IRMetadataDIFile file, Target t, IRMetadataDISubroutineType type, IRMetadataDICompileUnit compileUnit) throws IOException {
		super(env, new IRMetadata.PrototypeBuilder()
			.put("name", DataType.STRING)
			.put("scope", DataType.REFERENCE) //DIFile
			.put("file", DataType.REFERENCE) //DIFile
			.put("line", DataType.NUMBER)
			.put("type", DataType.REFERENCE) // DISubroutineType
			.put("unit", DataType.REFERENCE)
			.build(), "DISubprogram");
		this.putAttribute("name", name);
		this.putMetadataReference("scope", file);
		this.putMetadataReference("file", file);
		this.putNumber("line", t.line());
		this.putMetadataReference("type", type);
		this.putMetadataReference("unit", compileUnit);
	}
}
