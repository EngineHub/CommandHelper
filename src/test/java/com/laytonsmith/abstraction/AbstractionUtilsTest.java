package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.WrappedItem;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lsmith
 */
public class AbstractionUtilsTest {
	
	public AbstractionUtilsTest() {
	}
	
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
	public void testAllWrappedItemClassesImplementAbstractionObject() throws Exception {
		SortedSet<String> problems = new TreeSet<String>();
		for(Field f : ClassDiscovery.GetFieldsWithAnnotation(WrappedItem.class)){
			if(!AbstractionObject.class.isAssignableFrom(f.getDeclaringClass())){
				problems.add(f.getDeclaringClass().getName() + " doesn't ultimately implement " + AbstractionObject.class.getName());
			}
		}
		if(!problems.isEmpty()){
			System.out.println(StringUtils.Join(problems, "\n"));
			fail(StringUtils.Join(problems, "\n"));
		}
	}
	
	@Test
	public void testAllWrappedItemClassesHaveDefaultConstructor() throws Exception {
		SortedSet<String> problems = new TreeSet<String>();
		for(Field f : ClassDiscovery.GetFieldsWithAnnotation(WrappedItem.class)){
			boolean found = false;
			for(Constructor c : f.getDeclaringClass().getDeclaredConstructors()){
				if(c.getParameterTypes().length == 0){
					found = true;
					break;
				}
			}
			if(!found){
				problems.add(f.getDeclaringClass().getName() + " does not have a no-arg constructor.");
			}
		}
		if(!problems.isEmpty()){
			System.out.println(problems.size() + " issue(s):\n" + StringUtils.Join(problems, "\n"));
			fail(StringUtils.Join(problems, "\n"));
		}
	}
	
	@Test
	public void testAllWrappedItemClassesAreUnique() throws Exception {
		Set<Class> wrappers = new HashSet<Class>();
		for(Field f : ClassDiscovery.GetFieldsWithAnnotation(WrappedItem.class)){
			if(wrappers.contains(f.getDeclaringClass())){
				fail(f.getDeclaringClass() + " is wrapped in more than one class");
			} else {
				wrappers.add(f.getDeclaringClass());
			}
		}
	}
	
	@Test
	public void testAllWrappedClassesAreCompatibleWithTheirParentClassSuperClassChain() throws Exception {
		SortedSet<String> problems = new TreeSet<String>();
		for(Field f : ClassDiscovery.GetFieldsWithAnnotation(WrappedItem.class)){
			Class c = f.getDeclaringClass();
			while((c = c.getSuperclass()) != null){
				for(Field ff : c.getDeclaredFields()){
					if(ff.getAnnotation(WrappedItem.class) != null){
						if(!ff.getType().isAssignableFrom(f.getType())){
							problems.add("The type " + f.getType().getName() + " contained in " + f.getDeclaringClass().getName() + " isn't castable to " 
									+ ff.getType().getName() + " which is declared in " + ff.getDeclaringClass().getName());
						}
						break;
					}
				}
			}
		}
		if(!problems.isEmpty()){
			System.out.println(problems.size() + " issue(s):\n" + StringUtils.Join(problems, "\n"));
			fail(StringUtils.Join(problems, "\n"));
		}
	}
}