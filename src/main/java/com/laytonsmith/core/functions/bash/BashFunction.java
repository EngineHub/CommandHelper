package com.laytonsmith.core.functions.bash;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.core;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.functions.CompiledFunction;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.snapins.PackagePermission;
import java.net.URL;

/**
 * This is a marker interface to make Bash functions separate.
 *
 */
public abstract class BashFunction implements FunctionBase, CompiledFunction, Documentation {

	@Override
	public boolean appearInDocumentation() {
		return true;
	}

	@Override
	public PackagePermission getPermission() {
		return PackagePermission.NO_PERMISSIONS_NEEDED;
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	private static final Class[] EMPTY_CLASS = new Class[0];

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return EMPTY_CLASS;
	}

	@Override
	public final boolean isCore() {
		Class c = this.getClass();
		do {
			if(c.getAnnotation(core.class) != null) {
				return true;
			}
			c = c.getDeclaringClass();
		} while(c != null);
		return false;
	}

}
