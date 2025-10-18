package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.AbstractMixedClass;
import com.laytonsmith.core.natives.interfaces.Matrix;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@typeof("ms.lang.Real2dMatrix")
public class CReal2dMatrix extends AbstractMixedClass implements Matrix<Double>,
		com.laytonsmith.core.natives.interfaces.Iterable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CReal2dMatrix.class);

	int rows;
	int columns;
	double[] data;

	@Override
	public String docs() {
		return "This object represents a matrix.";
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
		return new CClassType[]{Matrix.TYPE, com.laytonsmith.core.natives.interfaces.Iterable.TYPE};
	}

	/**
	 * Constructs a new Real2dMatrix. This should only be used in case the data originates from within MethodScript.
	 * When a matrix object is passed in via user code, it should be constructed with {@link #FromConstruct}.
	 *
	 * @param dimensions The dimension sizes. As only 2d matrixes are supported, the 0th index is the row length, and
	 * the 1st index is the column length.
	 * @param data The data, as a flat array.
	 */
	public CReal2dMatrix(int[] dimensions, double[] data) {
		this(dimensions[0], dimensions[1], data);
		if(dimensions.length != 2) {
			throw new IllegalArgumentException("Dimensions can currently only be 2.");
		}
	}

	/**
	 * Constructs a new Real2dMatrix. This should only be used in case the data originates from within MethodScript.
	 * When a matrix object is passed in via user code, it should be constructed with {@link #FromConstruct}.
	 *
	 * @param rows
	 * @param columns
	 * @param data
	 */
	public CReal2dMatrix(int rows, int columns, double[] data) {
		this.rows = rows;
		this.columns = columns;
		this.data = data;
	}

	@Override
	public String toString() {
		return "Real2dMatrix[" + rows + "x" + columns + "]";
	}

	/**
	 * Creates a CArray from this object.
	 *
	 * @return
	 */
	public CArray toArray() {
		CArray construct = CArray.GetAssociativeArray(Target.UNKNOWN);
		CArray dimensionsC = new CArray(Target.UNKNOWN, 2);
		construct.set("dimensions", dimensionsC, Target.UNKNOWN);
		CArray dataC = new CArray(Target.UNKNOWN, data.length);
		construct.set("data", dataC, Target.UNKNOWN);
		for(int i : getDimensions()) {
			dimensionsC.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN);
		}
		for(double d : data) {
			dataC.push(new CDouble(d, Target.UNKNOWN), Target.UNKNOWN);
		}
		return construct;
	}

	/**
	 * Creates a Matrix object from the underlying CArray.
	 *
	 * @param construct
	 * @param t
	 * @return
	 */
	public static CReal2dMatrix FromConstruct(CArray construct, Target t) {
		CArray dimensions = ArgumentValidation.getArray(construct.get("dimensions", t), t);
		if(dimensions.size() != 2) {
			throw new CREIllegalArgumentException("Currently, only 2 dimensional matrices are supported.", t);
		}
		if(dimensions.isAssociative()) {
			throw new CREIllegalArgumentException("Dimensions array must be normal, not associative.", t);
		}
		long expectedLength = 1;
		int[] dimensionsJ = new int[(int) dimensions.size()];
		for(int j = 0; j < dimensions.size(); j++) {
			Mixed m = dimensions.get(j, t);
			int i = ArgumentValidation.getInt32(m, t);
			if(i < 1) {
				throw new CRERangeException("All matrix dimensions must be at least 1.", t);
			}
			dimensionsJ[j] = i;
			expectedLength *= i;
		}
		CArray data = ArgumentValidation.getArray(construct.get("data", t), t);
		if(data.size() != expectedLength) {
			throw new CRELengthException("Matrix data is of unexpected length, it must be the product of all dimensions.", t);
		}

		double[] dataJ = new double[(int) data.size()];

		for(int j = 0; j < data.size(); j++) {
			dataJ[j] = ArgumentValidation.getDouble(data.get(j, t), t);
		}

		return new CReal2dMatrix(dimensionsJ, dataJ);
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public boolean isAssociative() {
		return false;
	}

	@Override
	public CArray get(Mixed index, Target t) throws ConfigRuntimeException {
		return get(ArgumentValidation.getInt32(index, t), t);
	}

	@Override
	public Mixed get(String index, Target t) throws ConfigRuntimeException {
		throw new CREIllegalArgumentException("Matrices cannot be indexed into with non-numeric values.", t);
	}

	@Override
	public CArray get(int index, Target t) throws ConfigRuntimeException {
		if(index >= getRowCount() || index < 0) {
			throw new CRERangeException("Matrix range out of bounds.", t);
		}
		double[] d = getRow(index, t);
		CArray ret = new CArray(t);
		for(int i = 0; i < d.length; i++) {
			ret.push(new CDouble(d[i], t), t);
		}
		return ret;
	}

	@Override
	public Set<Mixed> keySet() {
		Set<Mixed> set = new HashSet<>();
		for(int i = 0; i < getRowCount(); i++) {
			set.add(new CInt(i, Target.UNKNOWN));
		}
		return set;
	}

	@Override
	public boolean getBooleanValue(Target t) {
		return data.length != 0;
	}

	@Override
	public CArray slice(int begin, int end, Target t) {
		CArray ret = new CArray(t);
		int step = (begin <= end) ? 1 : -1;

		// Note: loop includes 'begin', excludes 'end', just like typical slice semantics
		for(int i = begin; i != end; i += step) {
			CArray row = new CArray(t);
			double[] d = getRow(i, t);
			for(int k = 0; k < d.length; k++) {
				row.push(new CDouble(d[k], t), t);
			}
			ret.push(row, t);
		}

		return ret;
	}

	@Override
	public long size() {
		return getRowCount();
	}

	private int getPosition(int row, int column) {
		return row * columns + column;
	}

	/**
	 * Returns the dimensions of the matrix. This will be an array of 2 values, representing the row and column count.
	 *
	 * @return
	 */
	@Override
	public int[] getDimensions() {
		return new int[]{this.rows, this.columns};
	}

	/**
	 * Returns the underlying data, as a flat array.
	 *
	 * @return
	 */
	public double[] getData() {
		return this.data;
	}

	/**
	 * Returns the values in the specified row. Note that this will be a {@code columns} length array.
	 *
	 * @param row
	 * @return
	 */
	public double[] getRow(int row, Target t) {
		if(row < 0 || row >= this.rows) {
			throw new CREIndexOverflowException("Row index out of range: " + row, t);
		}
		double[] rowData = new double[this.columns];
		int start = row * this.columns;
		System.arraycopy(data, start, rowData, 0, this.columns);
		return rowData;
	}

	/**
	 * Returns the values in the specified column. Note that this will be a {@code rows} length array.
	 *
	 * @param column
	 * @return
	 */
	public double[] getColumn(int column) {
		double[] columnData = new double[this.rows];
		int k = 0;
		for(int i = column; i < this.data.length; i += this.columns) {
			columnData[k++] = this.data[i];
		}
		return columnData;
	}

	/**
	 * Returns the number of rows.
	 *
	 * @return
	 */
	public int getRowCount() {
		return this.rows;
	}

	/**
	 * Returns the number of columns.
	 *
	 * @return
	 */
	public int getColumnCount() {
		return this.columns;
	}

	@Override
	public int getDimensionSize(int dimension, Target t) {
		if(dimension == 0) {
			return rows;
		} else if(dimension == 1) {
			return columns;
		} else {
			throw new CRERangeException("Dimension count too high", t);
		}
	}

	/**
	 * Returns an independent copy of this matrix.
	 *
	 * @return
	 */
	public CReal2dMatrix copyMatrix() {
		double[] newData = new double[this.data.length];
		System.arraycopy(this.data, 0, newData, 0, newData.length);
		return new CReal2dMatrix(rows, columns, newData);
	}

	@Override
	public boolean canAdd(Matrix<Double> other) {
		return Arrays.equals(this.getDimensions(), other.getDimensions());
	}

	@Override
	public boolean canMultiply(Matrix<Double> other) {
		return other.hasDimension(1) && this.columns == other.getDimensions()[1];
	}

	@Override
	public boolean hasDimension(int dimension) {
		return dimension == 1 || dimension == 2;
	}

	@Override
	public boolean isSquare() {
		return rows == columns;
	}

	@Override
	public Class<Double> getDataType() {
		return Double.class;
	}

	/**
	 * Returns true if the two matrices are equal, within the given tolerance, and the dimensions are the same.
	 *
	 * @param right
	 * @param tolerance
	 * @return
	 */
	public boolean equals(CReal2dMatrix right, double tolerance) {
		if(this.rows != right.rows || this.columns != right.columns) {
			return false;
		}
		for(int i = 0; i < this.data.length; i++) {
			if(Math.abs(this.data[i] - right.data[i]) > tolerance) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds two matrices together. They must be equal dimensions. The addition is in place, done in this matrix.
	 * {@code this} is returned.
	 *
	 * @param b
	 * @param t
	 * @return
	 */
	public CReal2dMatrix add(CReal2dMatrix b, Target t) {
		if(!Arrays.equals(this.getDimensions(), b.getDimensions())) {
			throw new CREIllegalArgumentException("Matrix addition can only occur on matrices of the same size.", t);
		}
		for(int i = 0; i < this.data.length; i++) {
			this.data[i] = this.data[i] + b.data[i];
		}
		return this;
	}

	/**
	 * Subtracts two matrices together. They must be equal dimensions. The subtraction is in place, done in this matrix.
	 * {@code this} is returned.
	 *
	 * @param b
	 * @param t
	 * @return
	 */
	public CReal2dMatrix subtract(CReal2dMatrix b, Target t) {
		if(!Arrays.equals(this.getDimensions(), b.getDimensions())) {
			throw new CREIllegalArgumentException("Matrix addition can only occur on matrices of the same size.", t);
		}
		for(int i = 0; i < this.data.length; i++) {
			this.data[i] = this.data[i] - b.data[i];
		}
		return this;
	}

	/**
	 * Performs scalar multiplication on the matrix. The multiplication is in place, done in this matrix. {@code this}
	 * is returned.
	 *
	 * @see #multiply
	 * @param scalar
	 * @return
	 */
	public CReal2dMatrix scalarMultiply(double scalar) {
		for(int i = 0; i < this.data.length; i++) {
			this.data[i] *= scalar;
		}
		return this;
	}

	/**
	 * Transposes the matrix in place.
	 *
	 * @return
	 */
	public CReal2dMatrix transpose() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		int rows = this.rows;
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		int columns = this.columns;
		double[] newData = new double[this.data.length];
		int i = 0;
		for(int column = 0; column < columns; column++) {
			for(int row = 0; row < rows; row++) {
				int src = row * columns + column;
				newData[i++] = this.data[src];
			}
		}
		if(rows != columns) {
			this.columns = rows;
			this.rows = columns;
		}
		this.data = newData;
		return this;
	}

	/**
	 * Creates a submatrix by deleting the given row and column. A new matrix is returned. If either row or column are
	 * negative, only the other will be deleted. If both are negative, or either is greater than the dimensions of the
	 * matrix, a range exception is thrown.
	 *
	 * @param row
	 * @param column
	 * @param t
	 * @return
	 */
	@SuppressWarnings("UnnecessaryLabelOnContinueStatement")
	public CReal2dMatrix submatrix(int row, int column, Target t) {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		int rows = this.rows;
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		int columns = this.columns;
		if(row >= rows) {
			throw new CRERangeException("Row parameter beyond range.", t);
		}
		if(column >= columns) {
			throw new CRERangeException("Column parameter beyond range.", t);
		}
		if(row < 0 && column < 0) {
			throw new CRERangeException("Both row and column parameters cannot be negative.", t);
		}
		int newRows = rows - 1;
		int newColumns = columns - 1;
		if(row < 0) {
			newRows = rows;
		} else if(column < 0) {
			newColumns = columns;
		}

		double[] newData = new double[newRows * newColumns];
		int i = 0;
		rows:
		for(int r = 0; r < rows; r++) {
			columns:
			for(int c = 0; c < columns; c++) {
				if(c == column) {
					continue columns;
				}
				if(r == row) {
					continue rows;
				}
				int src = r * columns + c;
				newData[i++] = this.data[src];
			}
		}
		int[] newDimensions = new int[]{newRows, newColumns};
		return new CReal2dMatrix(newDimensions, newData);
	}

	/**
	 * Performs matrix multiplication between this matrix and the specified matrix. A new matrix is returned. Note that
	 * this matrix is considered the "left" matrix, and the passed in matrix is considered the "right" matrix.
	 * Additionally, the column count of the left matrix must match the row count of the right matrix, or a
	 * CREIllegalArgumentException is thrown.
	 *
	 * @see #scalarMultiply
	 * @param b
	 * @param t
	 * @return
	 */
	public CReal2dMatrix multiply(CReal2dMatrix b, Target t) {
		if(this.columns != b.rows) {
			throw new CREIllegalArgumentException("Invalid matrix multiplication. The column count of the left matrix"
					+ " must be equal to the row count of the right matrix.", t);
		}

		double[] newData = new double[this.rows * b.columns];
		int k = 0;
		int multiplicandSize = this.columns;
		for(int r = 0; r < getRowCount(); r++) {
			for(int c = 0; c < b.getColumnCount(); c++) {
				double value = 0;
				for(int i = 0; i < multiplicandSize; i++) {
					value += this.data[this.getPosition(r, i)] * b.data[b.getPosition(i, c)];
				}
				newData[k++] = value;
			}
		}
		return new CReal2dMatrix(this.rows, b.columns, newData);
	}

	/**
	 * Returns the trace of the matrix, that is, the sum of the values on the diagonal. Throws a CRERangeException if
	 * the matrix is not square.
	 *
	 * @return
	 */
	public double trace(Target t) {
		if(!isSquare()) {
			throw new CRERangeException("To get a matrix trace, the matrix must be square.", t);
		}
		double sum = 0;
		for(int i = 0; i < rows; i++) {
			sum += data[i * columns + i];
		}
		return sum;
	}

	/**
	 * Returns the determinant of the matrix, that is the product of the main diagonal, minus the product of the other
	 * diagonal. Throws a CRERangeException if the matrix is not square.
	 *
	 * @param t
	 * @return
	 */
	public double determinant(Target t) {
		if(!isSquare()) {
			throw new CRERangeException("To get a matrix determinant, the matrix must be square.", t);
		}

		// This is O(n!), where n is the dimension, so anything > 6 starts to get ugly fast. Need to implement
		// LU decomposition to make this performant for any size matrix. This is good enough for most realistic
		// purposes for now.
		return determinantRecursive(this);
	}

	private double determinantRecursive(CReal2dMatrix m) {

		int n = m.rows;
		if(n == 1) {
			return m.data[0];
		} else if(n == 2) {
			return m.data[0] * m.data[3] - m.data[1] * m.data[2];
		} else {
			double det = 0;
			for(int col = 0; col < n; col++) {
				double sign = (col % 2 == 0) ? 1 : -1;
				CReal2dMatrix sub = m.submatrix(0, col, Target.UNKNOWN); // remove row 0, col 'col'
				double elem = m.data[col];
				det += sign * elem * determinantRecursive(sub);
			}
			return det;
		}
	}

	/**
	 * Returns the Frobenius norm of the matrix.
	 *
	 * @return
	 */
	public double norm() {
		double sumSq = 0;
		for(double v : data) {
			sumSq += v * v;
		}
		return Math.sqrt(sumSq);
	}
}
