package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Set;

/**
 * A Field is a representation of a concrete field within an object definition.
 */
@typeof("ms.lang.Field")
public class Field extends ElementDefinition {


	public Field(
			AccessModifier accessModifier,
			Set<ElementModifier> elementModifiers,
			UnqualifiedClassName definedIn,
			UnqualifiedClassName type,
			String name,
			ParseTree code,
			String signature,
			Construct.ConstructType constructType,
			Target t
	) {
		super(
			accessModifier,
			elementModifiers,
			definedIn,
			type,
			name,
			code,
			signature,
			constructType,
			t
		);
	}

	/**
	 * Calls the initialization code on the field, and returns the initial value for the field.
	 * @param parent
	 * @param env
	 * @return
	 */
	public Mixed initialize(Script parent, Environment env) {
		return parent.eval(getTree(), env);
	}
}
