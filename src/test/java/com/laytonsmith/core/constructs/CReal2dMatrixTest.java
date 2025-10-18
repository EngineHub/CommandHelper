package com.laytonsmith.core.constructs;

import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class CReal2dMatrixTest {

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGetRow() {
		int[] dimensions = new int[]{3, 4};
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		CReal2dMatrix m = new CReal2dMatrix(dimensions, data1);
		double[] expected = new double[]{
			4, 5, 6, 7
		};
		double[] row = m.getRow(1, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, row, 0.1);
	}

	@Test
	public void testGetColumn() {
		int[] dimensions = new int[]{3, 4};
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		CReal2dMatrix m = new CReal2dMatrix(dimensions, data1);
		double[] expected = new double[]{
			2, 6, 10
		};
		double[] column = m.getColumn(2);
		Assert.assertArrayEquals(expected, column, 0.1);
	}

	@Test
	public void testForwardSlice() {
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		CReal2dMatrix m = new CReal2dMatrix(3, 4, data1);
		CArray slice = m.slice(1, 2, Target.UNKNOWN);
		Assert.assertEquals(5, ((CDouble) ((ArrayAccess) slice.get(0, Target.UNKNOWN)).get(1, Target.UNKNOWN)).val, 0.1);
	}

	@Test
	public void testReverseSlice() {
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		CReal2dMatrix m = new CReal2dMatrix(3, 4, data1);
		CArray slice = m.slice(2, 1, Target.UNKNOWN);
		Assert.assertEquals(9, ((CDouble) ((ArrayAccess) slice.get(0, Target.UNKNOWN)).get(1, Target.UNKNOWN)).val, 0.1);
	}

	@Test
	public void testSquareTranspose() {
		int[] dimensions = new int[]{3, 3};
		double[] data = new double[]{
			1, 2, 3,
			4, 5, 6,
			7, 8, 9
		};
		CReal2dMatrix m = new CReal2dMatrix(dimensions, data);
		m.transpose();
		double[] expected = new double[]{
			1, 4, 7,
			2, 5, 8,
			3, 6, 9
		};
		Assert.assertArrayEquals(expected, m.getData(), 0.1);
	}

	@Test
	public void testRectangleTranspose1() {
		int[] dimensions = new int[]{4, 3};
		double[] data = new double[]{
			0, 1, 2,
			3, 4, 5,
			6, 7, 8,
			9, 10, 11
		};
		CReal2dMatrix m = new CReal2dMatrix(dimensions, data);
		m.transpose();
		double[] expected = new double[]{
			0, 3, 6, 9,
			1, 4, 7, 10,
			2, 5, 8, 11
		};
		Assert.assertArrayEquals(expected, m.getData(), 0.1);
		Assert.assertArrayEquals(new int[]{3, 4}, m.getDimensions());
	}

	@Test
	public void testRectangleTranspose2() {
		int[] dimensions = new int[]{3, 4};
		double[] data = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		CReal2dMatrix m = new CReal2dMatrix(dimensions, data);
		m.transpose();
		double[] expected = new double[]{
			0, 4, 8,
			1, 5, 9,
			2, 6, 10,
			3, 7, 11
		};
		Assert.assertArrayEquals(expected, m.getData(), 0.1);
		Assert.assertArrayEquals(new int[]{4, 3}, m.getDimensions());
	}

	@Test
	public void testMatrixAdd() {
		int[] dimensions = new int[]{3, 4};
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		double[] data2 = new double[]{
			2, 4, 6, 8,
			10, 12, 14, 16,
			18, 20, 22, 24
		};
		double[] expected = new double[]{
			2, 5, 8, 11,
			14, 17, 20, 23,
			26, 29, 32, 35
		};
		CReal2dMatrix original = new CReal2dMatrix(dimensions, data1);
		CReal2dMatrix add = new CReal2dMatrix(dimensions, data2);
		original.add(add, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, original.getData(), 0.1);
	}

	@Test
	public void testMatrixSubtract() {
		int[] dimensions = new int[]{3, 4};
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		double[] data2 = new double[]{
			1, 1, 1, 1,
			1, 1, 1, 1,
			1, 1, 1, 1
		};
		double[] expected = new double[]{
			-1, 0, 1, 2,
			3, 4, 5, 6,
			7, 8, 9, 10
		};
		CReal2dMatrix original = new CReal2dMatrix(dimensions, data1);
		CReal2dMatrix sub = new CReal2dMatrix(dimensions, data2);
		original.subtract(sub, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, original.getData(), 0.1);
	}

	@Test
	public void testMatrixScalarMultiply() {
		double[] data1 = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		double[] expected = new double[]{
			0, 2, 4, 6,
			8, 10, 12, 14,
			16, 18, 20, 22
		};
		CReal2dMatrix original = new CReal2dMatrix(3, 4, data1);
		original.scalarMultiply(2);
		Assert.assertArrayEquals(expected, original.getData(), 0.1);
	}

	@Test
	public void testSubmatrix1() {
		double[] data = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		double[] expected = new double[]{
			0, 1, 3,
			8, 9, 11
		};
		CReal2dMatrix original = new CReal2dMatrix(3, 4, data);
		CReal2dMatrix submatrix = original.submatrix(1, 2, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, submatrix.getData(), 0.1);
		Assert.assertArrayEquals(new int[]{2, 3}, submatrix.getDimensions());
	}

	@Test
	public void testSubmatrix2() {
		double[] data = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		double[] expected = new double[]{
			0, 1, 2, 3,
			8, 9, 10, 11
		};
		CReal2dMatrix original = new CReal2dMatrix(3, 4, data);
		CReal2dMatrix submatrix = original.submatrix(1, -1, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, submatrix.getData(), 0.1);
		Assert.assertArrayEquals(new int[]{2, 4}, submatrix.getDimensions());
	}

	@Test
	public void testSubmatrix3() {
		double[] data = new double[]{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11
		};
		double[] expected = new double[]{
			0, 1, 3,
			4, 5, 7,
			8, 9, 11
		};
		CReal2dMatrix original = new CReal2dMatrix(3, 4, data);
		CReal2dMatrix submatrix = original.submatrix(-1, 2, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, submatrix.getData(), 0.1);
		Assert.assertArrayEquals(new int[]{3, 3}, submatrix.getDimensions());
	}

	@Test
	public void testMultiply1() {
		double[] data1 = new double[]{
			0, 1,
			2, 3,
			4, 5,
			6, 7
		};
		double[] data2 = new double[]{
			0, 1, 2,
			3, 4, 5
		};
		double[] expected = new double[]{
			3, 4, 5,
			9, 14, 19,
			15, 24, 33,
			21, 34, 47
		};
		CReal2dMatrix left = new CReal2dMatrix(4, 2, data1);
		CReal2dMatrix right = new CReal2dMatrix(2, 3, data2);
		CReal2dMatrix result = left.multiply(right, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, result.data, 0.1);
		Assert.assertEquals(4, result.rows);
		Assert.assertEquals(3, result.columns);
	}

	@Test
	public void testMultiply2() {
		double[] data = new double[]{
			0, 1, 2,
			3, 4, 5,
			6, 7, 8
		};
		double[] expected = new double[]{
			15, 18, 21,
			42, 54, 66,
			69, 90, 111
		};
		CReal2dMatrix left = new CReal2dMatrix(3, 3, data);
		CReal2dMatrix right = new CReal2dMatrix(3, 3, data);
		CReal2dMatrix result = left.multiply(right, Target.UNKNOWN);
		Assert.assertArrayEquals(expected, result.data, 0.1);
		Assert.assertEquals(3, result.rows);
		Assert.assertEquals(3, result.columns);
	}

	@Test
	public void testEquals() {
		double[] leftData = new double[]{
			0, 1, 2,
			3, 4, 5,
			6, 7, 8
		};
		double[] rightData = new double[]{
			0.01, 1.01, 2.01,
			3.01, 4.01, 5.01,
			6.01, 7.01, 8.01
		};

		CReal2dMatrix left = new CReal2dMatrix(3, 3, leftData);
		CReal2dMatrix right = new CReal2dMatrix(3, 3, rightData);
		Assert.assertTrue(left.equals(right, 0.1));
	}

	@Test
	public void testTrace() {
		double[] data = new double[]{
			1, 2, 3,
			4, 5, 6,
			7, 8, 9
		};

		CReal2dMatrix matrix = new CReal2dMatrix(3, 3, data);
		Assert.assertEquals(15, matrix.trace(Target.UNKNOWN), 0.000001);
	}

	@Test
	public void testDeterminant() {
		double[] data1 = new double[]{
			10, 2, 3,
			4, 5, 6,
			7, 8, 9
		};

		CReal2dMatrix matrix1 = new CReal2dMatrix(3, 3, data1);
		// 10*5*9 + 2*6*7 + 3*4*8 - 3*5*7 - 2*4*9 - 10*6*8 = -27
		Assert.assertEquals(-27, matrix1.determinant(Target.UNKNOWN), 0.0000001);

		double[] data2 = new double[]{
			1, 2, 3,
			4, 5, 6,
			7, 8, 9
		};
		CReal2dMatrix matrix2 = new CReal2dMatrix(3, 3, data2);
		// 1*5*9 + 2*6*7 + 3*4*8 - 3*5*7 - 2*4*9 - 1*6*8 = 0
		Assert.assertEquals(0, matrix2.determinant(Target.UNKNOWN), 0.0000001);
	}

	@Test
	public void testNorm() {
		double[] data = new double[]{
			1, 2, 3,
			4, 5, 6,
			7, 8, 9
		};

		CReal2dMatrix matrix = new CReal2dMatrix(3, 3, data);
		Assert.assertEquals(16.882, matrix.norm(), 0.01);
	}
}
