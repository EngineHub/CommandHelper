package com.laytonsmith.PureUtilities.ClassLoading;

import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 *
 */
@GeneralTest.TestAnnotation("value")
public class GeneralTest {

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface TestAnnotation {

		String value();
	}

	@TestAnnotation("field")
	private final String field = "field";

	@TestAnnotation("method")
	private String method() {
		return "";
	}

	public GeneralTest() {
	}

	@BeforeClass
	public static void setUpClass() {
		ClassDiscovery.getDefaultInstance().addThisJar();
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testEquals() throws Exception {
		// Test that the same class, loaded two different ways, are still equal
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertTrue(c1.equals(c2));
	}

	@Test
	public void testHashCode() throws Exception {
		// Test that the same class, loaded two different ways, have the same hash code
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.hashCode(), c2.hashCode());
	}

	@Test
	public void testClassReferenceMirrorName() throws Exception {
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.getClassReference(), c2.getClassReference());
	}

	@Test
	public void testClassReferenceAnnotation() throws Exception {
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.getAnnotations(), c2.getAnnotations());
	}

	@Test
	public void testClassFieldReferences() throws Exception {
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		List<FieldMirror> c1l = new ArrayList<>(Arrays.asList(c1.getFields()));
		List<FieldMirror> c2l = new ArrayList<>(Arrays.asList(c2.getFields()));
		for(List<FieldMirror> f : new List[]{c1l, c2l}) {
			Iterator<FieldMirror> it = f.iterator();
			while(it.hasNext()) {
				// Jacoco adds synthetic fields to this class (as could other instrumentation suites)
				// so we want to ignore any synthetic fields for the purposes of this test.
				if(it.next().getName().startsWith("$")) {
					it.remove();
				}
			}
		}
		assertEquals(c1l, c2l);
	}

	@Test
	public void testClassMethodReferences() throws Exception {
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		List<MethodMirror> m1 = Arrays.asList(c1.getMethods());
		List<MethodMirror> m2 = Arrays.asList(c1.getMethods());
		Comparator<MethodMirror> c = new Comparator<MethodMirror>() {

			@Override
			public int compare(MethodMirror o1, MethodMirror o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};
		Collections.sort(m1, c);
		Collections.sort(m2, c);
		assertEquals(m1, m2);
	}

	@Test
	public void testPackageReferences() throws Exception {
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.getPackage(), c2.getPackage());
	}

	@Test
	public void testAnnotationValue() throws Exception {
		ClassMirror<?> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		FieldMirror f1 = c1.getField("field");
		FieldMirror f2 = c1.getField("field");
		MethodMirror m1 = c1.getMethod("method", new Class[]{});
		MethodMirror m2 = c2.getMethod("method", new Class[]{});
		assertEquals(c1.loadAnnotation(TestAnnotation.class).value(), "value");
		assertEquals(c2.loadAnnotation(TestAnnotation.class).value(), "value");
		assertEquals(f1.loadAnnotation(TestAnnotation.class).value(), "field");
		assertEquals(f2.loadAnnotation(TestAnnotation.class).value(), "field");
		assertEquals(m1.loadAnnotation(TestAnnotation.class).value(), "method");
		assertEquals(m2.loadAnnotation(TestAnnotation.class).value(), "method");
	}

	/*
	 * A -> B
	 * |
	 * | -> C -> D -> F
	 *      |
	 *      | -> E
	 */
	public static interface A extends B, C {
	}

	public static interface B {
	}

	public static interface C extends D, E {
	}

	public static interface D extends F {
	}

	public static interface E {
	}

	public static interface F {
	}

	@Test
	public void testExtendsInterfacesWorks() {
		// We are testing that interfaces (with complicated inheritance schemes, as seen above)
		// work. Particularly, does A extend F? (Yes, it does.)
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<A>(A.class), F.class));
		assertFalse(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<E>(E.class), F.class));

		// It also extends itself
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<A>(A.class), A.class));

		// Just check all of them too
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<A>(A.class), B.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<A>(A.class), C.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<A>(A.class), D.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<A>(A.class), E.class));

		// Just in case, check to make sure that if it isn't a "root" class, it still extends ok
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<C>(C.class), F.class));
	}

	public static class AConcrete implements A {
	}

	@Test
	public void testClassThatImplementsInterfaceExtendsProperly() {
		// Same thing, but this time make sure the concrete class works.
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<AConcrete>(AConcrete.class), A.class));
		// just... check all of them.
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<AConcrete>(AConcrete.class), B.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<AConcrete>(AConcrete.class), C.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<AConcrete>(AConcrete.class), D.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<AConcrete>(AConcrete.class), E.class));
		assertTrue(ClassDiscovery.getDefaultInstance().doesClassExtend(new ClassMirror<AConcrete>(AConcrete.class), F.class));
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Tag {
	}

	@Tag
	public static interface TestMe {
	}

	@Test
	public void testInterfaceWithAnnotationIsReturned() {
		// Test that the interface specified is *also* returned
		Set<ClassMirror<? extends TestMe>> t = ClassDiscovery.getDefaultInstance().getClassesWithAnnotationThatExtend(Tag.class, TestMe.class);
		assertTrue(t.size() == 1);
	}

	class A2 {
	}

	@TestAnnotation("")
	class B2 extends A2 {
	}

	@Test
	public void testThatSuperclassesWithoutAnnotationArentReturned() {
		Set s = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(TestAnnotation.class, A2.class);
		assertThat(s.size(), is(1));
	}

	@TestAnnotation("")
	class A3 {
	}

	@TestAnnotation("")
	class B3 extends A3 {
	}

	@Test
	public void testThatSuperclassesWithAnnotationAreReturned() {
		Set s = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(TestAnnotation.class, A3.class);
		assertThat(s.size(), is(2));
	}
}
