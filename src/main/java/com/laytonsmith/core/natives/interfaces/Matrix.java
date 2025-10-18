package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @param <T> The underlying type in the matrix.
 */
@typeof("ms.lang.Matrix")
public interface Matrix<T> extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Matrix.class);

	@Override
	public String docs();

	@Override
	public Version since();

	/**
	 * Returns true if the matrix is isotropic, that is, all dimensions are equal.
	 * For higher dimension matrices, this terminology is slightly
	 * inaccurate, it would rather be isCubic, isTesseractic, is5Cubic, etc, but for simplicity sake we simplify
	 * the name as if every matrix were 2d.
	 * @return
	 */
	public boolean isSquare();

	/**
	 * Returns the size of the given dimension. This is the same as {@code getDimensions()[dimension]}. Subclasses
	 * may provide named dimensions.
	 * @param dimension
	 * @param t
	 * @return
	 */
	public int getDimensionSize(int dimension, Target t);

	/**
	 * Returns true if the matrix has a dimension of the given value. That is, if this is a 2d matrix, then
	 * {@code hasDimension(2)} would return true, but {@code hasDimension(3)} would return false. If the input
	 * is 0 or negative, this will always return false, but will not otherwise error.
	 * @param dimension
	 * @return
	 */
	public boolean hasDimension(int dimension);

	/**
	 * Returns the dimension sizes.
	 * @return
	 */
	public int[] getDimensions();

	/**
	 * Returns true if the given matrix can be added to this matrix. This also implies that it could be subtracted
	 * instead.
	 * @param other
	 * @return
	 */
	public boolean canAdd(Matrix<T> other);

	/**
	 * Returns true if the given matrix can be multiplied with this matrix.
	 * @param other
	 * @return
	 */
	public boolean canMultiply(Matrix<T> other);

	/**
	 * Returns the underlying data type of this matrix.
	 * @return
	 */
	public Class<T> getDataType();
}
