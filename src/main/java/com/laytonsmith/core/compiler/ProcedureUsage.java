
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author Layton
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

	public String typeName() {
		return "$procusage";
	}
	
}
