package com.laytonsmith.PureUtilities;

import com.laytonsmith.annotations.NonInheritImplements;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.Test;

/**
 *
 * @author cailin
 */
public class NonInheritImplementsTest {

	public static class A1 implements B1 {
		@Override
		public void method(){}
	}
	public static interface B1 {
		void method();
	}

	@Test
	public void testNormalWorks() throws Exception {
		assertThat(NonInheritImplements.Helper.Instanceof(new A1(), B1.class), is(true));
		A1 a1 = new A1();
		B1 b1 = NonInheritImplements.Helper.Cast(B1.class, a1);
		assertTrue(a1 == b1);
	}

	@Test(expected=ClassCastException.class)
	public void testNormalFailsCorrectly1() throws Exception {
		assertThat(NonInheritImplements.Helper.Instanceof(new A1(), NonInheritImplements.class), is(false));
		NonInheritImplements.Helper.Cast(NonInheritImplements.class, this);
	}

	@NonInheritImplements(B2.class)
	public static class A2 {
		public String method2(int i) {
			return Integer.toString(i);
		}
	}

	public static interface B2 {
		String method2(int i);
	}

	@Test
	public void testProxyWorks() throws Exception {
		assertThat(NonInheritImplements.Helper.Instanceof(new A2(), B2.class), is(true));
		A2 a2 = new A2();
		B2 b2 = NonInheritImplements.Helper.Cast(B2.class, a2);
		assertThat(b2.method2(12), is("12"));
	}

}
