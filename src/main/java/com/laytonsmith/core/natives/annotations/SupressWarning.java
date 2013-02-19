package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.documentation;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.CompilerWarning;

/**
 *
 * @author lsmith
 */
public class SupressWarning extends MAnnotation {
	
	@documentation(docs="The name of the warning to supress")
	public CompilerWarning value;

	public String getName() {
		return "SupressWarning";
	}

	public String docs() {
		return "Supresses a warning on a single code component, which allows for more precise warning supression if there is a specific reason you are"
				+ " going against the compiler's suggestions, but do not wish to globally disable warnings, which itself will result in a warning. Using this"
				+ " annotation instead will not trigger its own warning, but will simply make that particular warning ignored. Ideally you wouldn't use this"
				+ " annotation anyways however, it is available should you want it.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
}
