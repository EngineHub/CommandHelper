package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.environments.Environment;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class represents a subroutine (function) signature.
 */
public class IRMetadataDISubroutineType extends IRMetadata {
	public IRMetadataDISubroutineType(Environment env, CClassType returnType, CClassType[] args) throws FileNotFoundException, IOException {
		super(env, new IRMetadata.PrototypeBuilder()
			.put("types", DataType.TUPLE)
			.build(), "DISubroutineType");
		String[] tuple = new String[args.length + 1];
		String ret;
		if(CVoid.TYPE.equals(returnType)) {
			ret = "null";
		} else {
			ret = env.getEnv(LLVMEnvironment.class).getMetadataRegistry().getBasicType(env, returnType).getReference();
		}
		tuple[0] = ret;
		for(int i = 0; i < args.length; i++) {
			CClassType type = args[i];
			tuple[i + 1] = env.getEnv(LLVMEnvironment.class).getMetadataRegistry().getBasicType(env, type).getReference();
		}
		this.putTuple("types", tuple);
	}
}
