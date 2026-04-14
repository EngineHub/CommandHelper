package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;

/**
 *
 */
@InterfaceRunnerFor(Matrix.class)
public class MatrixRunner extends AbstractMixedInterfaceRunner {
	@Override
	public String docs() {
		return "A matrix is a multidimensional collection of values, similar to a multidimensional array."
				+ " Unlike a multidimensional array, a matrix requires the values in each dimension to have"
				+ " a fixed size. For instance, in a 2 x 5 2d matrix, each row must have exactly 5 elements, and there"
				+ " must be exactly 2 rows. Various specialized subclasses of matrices exist, such as a square"
				+ " matrix, the identity matrix, and more. This interface defines basic operations that are common"
				+ " to all matrices.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}
}
