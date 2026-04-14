package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.compiler.signature.SignatureBuilder;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CReal2dMatrix;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class MatrixHandling {

	public static String docs() {
		return "Contains functions related to matrices. The general format of a matrix object is either a Real2dMatrix object,"
				+ " or an array of two parameters,"
				+ " dimensions, and data. Dimensions is an array of dimensions, indicating the size of each dimension."
				+ " The data parameter contains the actual matrix data, stored in row-major order. Currently, only"
				+ " 2 dimensional arrays of real numbers are supported. Dimension sizes must be 32 bit integers. The data array"
				+ " contains doubles, and the size of this array must be equal to the dimension values multiplied together."
				+ " Matrix operations done via a Real2dMatrix object will be faster than equivalent operations directly"
				+ " on an array. If you have a flat array, use matrix_create() to convert it into a matrix object.";
	}

	/**
	 * Gets the CReal2dMatrix from the user supplied value.
	 *
	 * @param value
	 * @param t
	 * @param argNumber
	 * @return
	 */
	public static CReal2dMatrix getMatrix(Mixed value, Target t, int argNumber) {
		if(value instanceof CReal2dMatrix m) {
			return m;
		} else if(value instanceof CArray m) {
			return CReal2dMatrix.FromConstruct(m, t);
		} else {
			throw new CRECastException("Expected argument " + argNumber + " to be a Real2dMatrix or an array.", t);
		}
	}

	/**
	 * Gets the CReal2dMatrix from the user supplied value, but does not accept CArray, only an actual CReal2dMatrix
	 * object.
	 *
	 * @param value
	 * @param t
	 * @param argNumber
	 * @return
	 */
	public static CReal2dMatrix getMatrixOnly(Mixed value, Target t, int argNumber) {
		if(value instanceof CReal2dMatrix m) {
			return m;
		} else {
			throw new CRECastException("Expected argument " + argNumber + " to be a Real2dMatrix or an array.", t);
		}
	}

	@api
	public static class matrix_create extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 1) {
				return CReal2dMatrix.FromConstruct(ArgumentValidation.getArray(args[0], t), t);
			} else {
				if(CNull.NULL.equals(args[0])) {
					// Fast creation
					int rows = ArgumentValidation.getInt32(args[1], t);
					int columns = ArgumentValidation.getInt32(args[2], t);
					double[] data = new double[rows * columns];
					return new CReal2dMatrix(rows, columns, data);
				} else {
					CArray construct = CArray.GetAssociativeArray(t);
					CArray dimensions = new CArray(t, 2);
					dimensions.set(0, args[1], t);
					dimensions.set(1, args[2], t);
					construct.set("dimensions", dimensions, t);
					construct.set("data", args[0], t);
					return CReal2dMatrix.FromConstruct(construct, t);
				}
			}
		}

		@Override
		public String getName() {
			return "matrix_create";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 3};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {array matrixObject | array matrix, int rows, int columns} Creates a Real2dMatrix object"
					+ " from a flat array of doubles. The matrix may instead be null, and a matrix"
					+ " of all zeros will be created with the specified row and column count."
					+ " If an array is passed in, the length of the array must be equal to"
					+ " rows * columns, or a RangeException is thrown. Alternatively, if the underlying"
					+ " array is a matrix object (an associative array with a dimensions and data property)"
					+ " it is converted to a Real2dMatrix object.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "matrix", "The array to convert to a matrix.")
					.param(CInt.TYPE, "rows", "The row count for the matrix.")
					.param(CInt.TYPE, "columns", "The column count for the matrix.")
					.build();
		}
	}

	@api
	public static class matrix_get_column extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = getMatrix(args[0], t, 1);
			int column = ArgumentValidation.getInt32(args[1], t);
			double[] data = matrix.getColumn(column);
			CArray ret = new CArray(t, data.length);
			for(int i = 0; i < data.length; i++) {
				ret.set(i, new CDouble(data[i], t), t);
			}
			return ret;
		}

		@Override
		public String getName() {
			return "matrix_get_column";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {Real2dArray|array matrix, int column} Gets the specified zero indexed column of the matrix,"
					+ " as an array. A RangeException is thrown if the specified value exceeds the ";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CArray.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.param(CInt.TYPE, "columns", "The column to grab from the matrix.")
					.newSignature(CArray.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.param(CInt.TYPE, "columns", "The column to grab from the matrix.")
					.build();
		}
	}

	@api
	public static class matrix_clone extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = getMatrixOnly(args[0], t, 1);
			return matrix.deepClone();
		}

		@Override
		public String getName() {
			return "matrix_clone";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {Real2dMatrix matrix} Clones the matrix. This clone is a deep copy, fully independent"
					+ " from the source matrix.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CString.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix to clone.")
					.build();
		}
	}

	@api
	@seealso(StringHandling.sprintf.class)
	public static class matrix_format extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = getMatrix(args[0], t, 1);
			double[] data = matrix.getData();
			String format = "%.3f";
			if(args.length > 1) {
				format = args[1].val();
			}
			String[] formattedData = new String[data.length];
			int maxSize = 0;
			for(int i = 0; i < data.length; i++) {
				formattedData[i] = String.format(format, data[i]);
				maxSize = java.lang.Math.max(maxSize, formattedData[i].length());
			}
			StringBuilder output = new StringBuilder();
			for(int i = 0; i < data.length; i++) {
				if(i != 0 && i % matrix.getColumnCount() == 0) {
					output.append("\n");
				}
				String s = formattedData[i];
				int paddingLength = maxSize - s.length();
				output.append(StringUtils.stringMultiply(paddingLength, " "));
				if(i % matrix.getColumnCount() != 0) {
					output.append(" ");
				}
				output.append(formattedData[i]);
			}
			output.append("\n");
			return new CString(output.toString(), Target.UNKNOWN);
		}

		@Override
		public String getName() {
			return "matrix_format";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {Real2dMatrix|array matrix, string format = '%.3f'} Returns a human readable representation of the matrix,"
					+ " formatting the numbers according to the format, by default '%.3f'. See sprintf for full"
					+ " formatting rules.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage with the identity matrix", "matrix_format(matrix_identity(4));"),
				new ExampleScript("Basic usage with a general matrix", "matrix_format(array(dimensions: array(4, 4),\n"
				+ "\tdata: array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)));")
			};
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CString.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix to format.")
					.param(CString.TYPE, "format", "The format string, by default '%.3f'", true)
					.newSignature(CString.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix to format.")
					.param(CString.TYPE, "format", "The format string, by default '%.3f'", true)
					.build();
		}
	}

	@api
	public static class matrix_to_array extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CArray exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix m = MatrixHandling.getMatrixOnly(args[0], t, 1);
			return m.toArray();
		}

		@Override
		public String getName() {
			return "matrix_to_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {Real2dMatrix matrix} Converts a Real2dMatrix object into an array. Note that matrices"
					+ " implement ArrayAccess, so this step is not generally necessary just to access elements of"
					+ " the matrix. The returned array will have two keys, dimensions, and data. Dimensions will"
					+ " be a length 2 array of integers, containing the row and column count, and data is a flat"
					+ " array containing the doubles.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CArray.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix to convert to an array.")
					.build();
		}
	}

	@api
	public static class matrix_identity extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CReal2dMatrix exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			int dimension = ArgumentValidation.getInt32(args[0], t);
			double[] data = new double[dimension * dimension];
			for(int i = 0; i < (dimension * dimension); i++) {
				data[i] = 0;
				if(i == 0 || i % (dimension + 1) == 0) {
					data[i] = 1;
				}
			}
			return new CReal2dMatrix(dimension, dimension, data);
		}

		@Override
		public String getName() {
			return "matrix_identity";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {int size} Returns a new identity matrix of the given size. The size value is a single"
					+ " dimension, the other dimension will be the same size, as all identity matrices are square.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CInt.TYPE, "size", "The dimensions of the matrix (both width and height).")
					.build();
		}
	}

	@api
	public static class matrix_transpose extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return MatrixHandling.getMatrix(args[0], t, 1).transpose();
		}

		@Override
		public String getName() {
			return "matrix_transpose";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {array|Real2dMatrix matrix} Transposes a matrix. Transposing a matrix inverts the matrix about the diagonal,"
					+ " changing the length of dimensions (if they are different). The underlying matrix object is"
					+ " modified in place if it is a Real2dMatrix, but a reference is always returned.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix to transpose")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix to transpose")
					.build();
		}
	}

	@api
	public static class matrix_add extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix left = MatrixHandling.getMatrix(args[0], t, 1);
			CReal2dMatrix right = MatrixHandling.getMatrix(args[1], t, 2);
			left.add(right, t);
			return left;
		}

		@Override
		public String getName() {
			return "matrix_add";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {Real2dMatrix|array left, Real2dMatrix|array right} Adds the right matrix to the left. The underlying left matrix object is"
					+ " modified in place if it is a Real2dMatrix, but a reference is always returned.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.build();
		}
	}

	@api
	public static class matrix_subtract extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix left = MatrixHandling.getMatrix(args[0], t, 1);
			CReal2dMatrix right = MatrixHandling.getMatrix(args[1], t, 2);
			left.subtract(right, t);
			return left;
		}

		@Override
		public String getName() {
			return "matrix_subtract";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {Real2dMatrix|array left, Real2dMatrix|array right} Subtracts the right matrix from the left. The underlying left matrix object is"
					+ " modified in place if it is a Real2dMatrix, but a reference is always returned.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.build();
		}
	}

	@api
	public static class matrix_scalar_multiply extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = MatrixHandling.getMatrix(args[0], t, 1);
			double scalar = ArgumentValidation.getDouble(args[1], t);
			matrix.scalarMultiply(scalar);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "matrix_scalar_multiply";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {Real2dMatrix|array matrix, double scalar} Does scalar multiplication on the matrix. The underlying left matrix object is"
					+ " modified in place if it is a Real2dMatrix, but a reference is always returned.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.param(CDouble.TYPE, "scalar", "The scalar to multiply by.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.param(CDouble.TYPE, "scalar", "The scalar to multiply by.")
					.build();
		}
	}

	@api
	public static class matrix_submatrix extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRERangeException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = MatrixHandling.getMatrix(args[0], t, 1);
			int row = ArgumentValidation.getInt32(args[1], t);
			int column = ArgumentValidation.getInt32(args[2], t);
			return matrix.submatrix(row, column, t);
		}

		@Override
		public String getName() {
			return "matrix_submatrix";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {Real2dMatrix|array matrix, int row, int column} Creates a submatrix by deleting the given row and column."
					+ " A new matrix is returned. If either row or column are"
					+ " negative, only the other will be deleted. If both are negative,"
					+ " or either is greater than the dimensions of the"
					+ " matrix, a range exception is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.param(CInt.TYPE, "row", "The row to delete.")
					.param(CInt.TYPE, "column", "The column to delete.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.param(CInt.TYPE, "row", "The row to delete.")
					.param(CInt.TYPE, "column", "The column to delete.")
					.build();
		}
	}

	@api
	public static class matrix_multiply extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix left = MatrixHandling.getMatrix(args[0], t, 1);
			CReal2dMatrix right = MatrixHandling.getMatrix(args[1], t, 2);
			return left.multiply(right, t);
		}

		@Override
		public String getName() {
			return "matrix_multiply";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "Real2dMatrix {Real2dMatrix|array left, Real2dMatrix|array right} Performs matrix multiplication between"
					+ " this matrix and the specified matrix. A new matrix is returned. "
					+ " The column count of the left matrix must match the row count of the right matrix, or a"
					+ " IllegalArgumentException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.newSignature(CReal2dMatrix.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.build();
		}
	}

	@api
	public static class matrix_can_multiply extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix left = MatrixHandling.getMatrix(args[0], t, 1);
			CReal2dMatrix right = MatrixHandling.getMatrix(args[1], t, 2);
			return CBoolean.get(left.canMultiply(right));
		}

		@Override
		public String getName() {
			return "matrix_can_multiply";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {Real2dMatrix|array left, Real2dMatrix|array right} Returns true if matrix multiplication"
					+ " would succed for the two matrices. That is, true if the column count of the left matrix"
					+ " matches the row count of the right matrix";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.build();
		}
	}

	@api
	public static class matrix_can_add extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix left = MatrixHandling.getMatrix(args[0], t, 1);
			CReal2dMatrix right = MatrixHandling.getMatrix(args[1], t, 2);
			return CBoolean.get(left.canAdd(right));
		}

		@Override
		public String getName() {
			return "matrix_can_add";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {Real2dMatrix|array left, Real2dMatrix|array right} Returns true if the"
					+ " two matrices can be added (or subtracted). That is, are the dimensions of both"
					+ " matrices equal.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.build();
		}
	}

	@api
	public static class matrix_is_square extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = MatrixHandling.getMatrix(args[0], t, 1);
			return CBoolean.get(matrix.isSquare());
		}

		@Override
		public String getName() {
			return "matrix_is_square";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {Real2dMatrix|array matrix} Returns true if the"
					+ " matrix is square, that is the row and column count are the same.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.build();
		}
	}

	@api
	public static class matrix_equals extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix left = MatrixHandling.getMatrix(args[0], t, 1);
			CReal2dMatrix right = MatrixHandling.getMatrix(args[1], t, 2);
			double tolerance = ArgumentValidation.getDouble(args[2], t);
			return CBoolean.get(left.equals(right, tolerance));
		}

		@Override
		public String getName() {
			return "matrix_equals";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "boolean {Real2dMatrix|array left, Real2dMatrix|array right, double tolerance} Returns true if the"
					+ " two matrices are equal to each other, within the given tolerance.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.param(CDouble.TYPE, "tolerance", "The floating point difference tolerance.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CReal2dMatrix.TYPE, "right", "The right side matrix.")
					.param(CDouble.TYPE, "tolerance", "The floating point difference tolerance.")
					.newSignature(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.param(CDouble.TYPE, "tolerance", "The floating point difference tolerance.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "left", "The left side matrix.")
					.param(CArray.TYPE, "right", "The right side matrix.")
					.param(CDouble.TYPE, "tolerance", "The floating point difference tolerance.")
					.build();
		}
	}

	@api
	public static class matrix_trace extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = MatrixHandling.getMatrix(args[0], t, 1);
			return new CDouble(matrix.trace(t), t);
		}

		@Override
		public String getName() {
			return "matrix_trace";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {Real2dMatrix|array matrix} Returns the trace of the matrix, that is, the sum"
					+ " of the values on the diagonal. This throws a RangeException if the matrix is not"
					+ " square.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.build();
		}
	}

	@api
	public static class matrix_determinant extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = MatrixHandling.getMatrix(args[0], t, 1);
			return new CDouble(matrix.determinant(t), t);
		}

		@Override
		public String getName() {
			return "matrix_determinant";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {Real2dMatrix|array matrix} Returns the determinant of the matrix, that is, the product of"
					+ " the main diagonal, minus the product of the other"
					+ " diagonal. This throws a RangeException if the matrix is not"
					+ " square.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.build();
		}
	}

	@api
	public static class matrix_norm extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CReal2dMatrix matrix = MatrixHandling.getMatrix(args[0], t, 1);
			return new CDouble(matrix.norm(), t);
		}

		@Override
		public String getName() {
			return "matrix_norm";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {Real2dMatrix|array matrix} Returns the Frobinius norm of the matrix, that is, the square"
					+ " root of the sum of the squares of all elements.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(CBoolean.TYPE)
					.param(CReal2dMatrix.TYPE, "matrix", "The matrix.")
					.newSignature(CBoolean.TYPE)
					.param(CArray.TYPE, "matrix", "The matrix.")
					.build();
		}
	}
}
