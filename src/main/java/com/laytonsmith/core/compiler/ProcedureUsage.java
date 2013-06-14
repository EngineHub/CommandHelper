
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 */
public class ProcedureUsage extends Construct{
	ProcedureDefinition definition;
	public ProcedureUsage(String name, Target t){
		super(name, t);
	}

	@Override
	public boolean isDynamic() {
		return definition.isDynamic();
	}

	/**
	 * This returns whatever type the underlying procedure defines.
	 * @return 
	 */
	public String typeName() {
		return definition.returnType().toString();
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
