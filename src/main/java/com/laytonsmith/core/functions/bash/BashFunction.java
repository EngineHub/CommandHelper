package com.laytonsmith.core.functions.bash;

import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.functions.CompiledFunction;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.snapins.PackagePermission;

/**
 * This is a marker interface to make Bash functions separate.
 * @author layton
 */
public abstract class BashFunction implements FunctionBase, CompiledFunction, Documentation {

    public boolean appearInDocumentation() {
        return true;
    }    

	public PackagePermission getPermission() {
		return PackagePermission.NO_PERMISSIONS_NEEDED;
	}
}
