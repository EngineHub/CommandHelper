package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.environments.Environment;

/**
 *
 */
public class IRMetadataDIBasicType extends IRMetadata {
	/**
	 * Creates a new basic type reference.
	 * @param env
	 * @param type
	 */
	public IRMetadataDIBasicType(Environment env, CClassType type) {
		super(env, new IRMetadata.PrototypeBuilder()
			.put("name", DataType.STRING)
			// TODO: Add more of the fields. We won't always use them, but it's helpful to have if we know.
			.build(), "DIBasicType");
		this.putAttribute("name", type.getFQCN().toString());
	}
}
