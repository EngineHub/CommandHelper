
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
import org.junit.After;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	private String method(){
		return "";
	}

	public GeneralTest() {
    }

    @BeforeClass
    public static void setUpClass(){
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(GeneralTest.class));
    }
    @Before
    public void setUp() throws Exception {        
        
    }
    @After
    public void tearDown(){
        
    }
	
	@Test
	public void testEquals() throws Exception {
		// Test that the same class, loaded two different ways, are still equal
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertTrue(c1.equals(c2));
	}
	
	@Test
	public void testHashCode() throws Exception {
		// Test that the same class, loaded two different ways, have the same hash code
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.hashCode(), c2.hashCode());
	}
	
	@Test
	public void testClassReferenceMirrorName() throws Exception {
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.getClassReference(), c2.getClassReference());
	}
	
	@Test
	public void testClassReferenceAnnotation() throws Exception {
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.getAnnotations(), c2.getAnnotations());
	}
	
	@Test
	public void testClassFieldReferences() throws Exception {
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		List<FieldMirror> c1l = new ArrayList<>(Arrays.asList(c1.getFields()));
		List<FieldMirror> c2l = new ArrayList<>(Arrays.asList(c2.getFields()));
		for(List<FieldMirror> f : new List[]{c1l, c2l}){
			Iterator<FieldMirror> it = f.iterator();
			while(it.hasNext()){
				// Jacoco adds synthetic fields to this class (as could other instrumentation suites)
				// so we want to ignore any synthetic fields for the purposes of this test.
				if(it.next().getName().startsWith("$")){
					it.remove();
				}
			}
		}
		assertEquals(c1l, c2l);
	}
	
	@Test
	public void testClassMethodReferences() throws Exception {
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
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
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
		ClassMirror<GeneralTest> c2 = new ClassMirror<>(GeneralTest.class);
		assertEquals(c1.getPackage(), c2.getPackage());
	}
	
	@Test
	public void testAnnotationValue() throws Exception {
		ClassMirror<GeneralTest> c1 = ClassDiscovery.getDefaultInstance().forName(GeneralTest.class.getName());
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
}
