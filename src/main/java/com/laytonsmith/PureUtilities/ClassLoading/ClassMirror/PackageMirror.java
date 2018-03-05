package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import java.io.Serializable;
import java.util.Objects;

/**
 * A package mirror provides information about the package a class is in.
 */
public class PackageMirror implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String name;

	public PackageMirror(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the package
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Attempts to load the underlying {@link java.lang.Package} object. If the package doesn't exist, null is returned.
	 *
	 * @return
	 */
	public Package loadPackage() {
		return Package.getPackage(name);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final PackageMirror other = (PackageMirror) obj;
		if(!Objects.equals(this.name, other.name)) {
			return false;
		}
		return true;
	}

}
