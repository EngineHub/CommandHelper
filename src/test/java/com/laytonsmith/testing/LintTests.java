
package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.testing.AbstractConstructor;
import com.laytonsmith.annotations.testing.MustOverride;
import com.laytonsmith.annotations.testing.SubclassesMustHaveAnnotation;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class contains all lint based or code structure tests. Failure of any of these tests
 * doesn't necessarily imply that the code is functionally non-working, but merely that there are potential
 * code smells, or incomplete code structures. See also the annotations linked below for various annotations
 * that may be used in code to trigger these tests.
 * 
 * @see AbstractConstructor
 * @see MustOverride
 * @see SubclassesMustHaveAnnotation
 */
public class LintTests {
	@BeforeClass
	public static void setup(){
		StaticTest.InstallFakeServerFrontend();
	}
	
	@Test
	public void testAbstractConstructors() throws Exception {
		Map<Class, Set<Constructor>> toTest = new HashMap<Class, Set<Constructor>>();
		for(Constructor constructor : ClassDiscovery.GetConstructorsWithAnnotation(AbstractConstructor.class)){
			if(!toTest.containsKey(constructor.getDeclaringClass())){
				toTest.put(constructor.getDeclaringClass(), new HashSet<Constructor>());
			}
			toTest.get(constructor.getDeclaringClass()).add(constructor);
		}
		SortedSet<String> problems = new TreeSet<String>();
		for(Class c : toTest.keySet()){
			for(Class sub : ClassDiscovery.GetAllClassesOfSubtype(c, null)){
				for(Constructor con : toTest.get(c)){
					try{
						sub.getDeclaredConstructor(con.getParameterTypes());
					} catch(NoSuchMethodException e){
						problems.add("In " + sub.toString() + ", there is no constructor with signature " + sub.getSimpleName() + "(" + StringUtils.Join(con.getParameterTypes(), ", ") + "). Please add it.");
					}
				}
			}
		}
		if(!problems.isEmpty()){
			fail(StringUtils.Join(problems, "\n"));
		}
	}
	
	@Test
	public void testMustOverride() throws Exception {
		Map<Class, Set<Method>> toTest = new HashMap<Class, Set<Method>>();
		for(Method method : ClassDiscovery.GetMethodsWithAnnotation(MustOverride.class)){
			if(!toTest.containsKey(method.getDeclaringClass())){
				toTest.put(method.getDeclaringClass(), new HashSet<Method>());
			}
			toTest.get(method.getDeclaringClass()).add(method);
		}
		SortedSet<String> problems = new TreeSet<String>();
		for(Class<?> c : toTest.keySet()){
			for(Class sub : ClassDiscovery.GetAllClassesOfSubtype(c, null)){
				for(Method m : toTest.get(c)){
					boolean directOnly = m.getAnnotation(MustOverride.class).directOnly();
					if(directOnly && sub.getSuperclass() != c){
						//Don't need to check this one.
						continue;
					}
					try{
						sub.getDeclaredMethod(m.getName(), m.getParameterTypes());
					} catch(NoSuchMethodException e){
						problems.add("In " + sub.toString() + ", there is no method with signature " 
								+ m.getReturnType() + " " + m.getName() + "(" + StringUtils.Join(m.getParameterTypes(), ", ") + ")."
								+ " Please override this method in the class.");
					}
				}
			}
		}
		if(!problems.isEmpty()){
			fail(StringUtils.Join(problems, "\n"));
		}
	}
	
	@Test
	public void testSubclassMustHaveAnnotation() throws Exception {
		Set<Class> toTest = new HashSet<Class>();
		SortedSet<String> problems = new TreeSet<String>();
		for(Class<?> c : ClassDiscovery.GetClassesWithAnnotation(SubclassesMustHaveAnnotation.class)){
			//Check to make sure that the values in the SubclassesMustHaveAnnotation are themselves runtime,
			//else we can't check that.
			SubclassesMustHaveAnnotation ann = c.getAnnotation(SubclassesMustHaveAnnotation.class);
			for(Class<? extends Annotation> a : ann.value()){
				Retention r = a.getAnnotation(Retention.class);
				if(r == null || r.value() != RetentionPolicy.RUNTIME){
					problems.add("The annotation @" + a.getName() + " is not itself annotated with @Retention(RetentionPolicy.RUNTIME), and"
							+ " therefore cannot actually be tested properly. Please either add runtime retention to " + a.getName() + ", or"
							+ " remove that class from the checked types on " + c.getName());
				}
				Target t = a.getAnnotation(Target.class);
				if(t != null && Arrays.binarySearch(t.value(), ElementType.TYPE) < 0){
					problems.add("The annotation @" + a.getName() + " is not allowed to be tagged to subclasses, so it is pointless to have it"
							+ " tested for on subclasses, because it would be a compile error if they were added to the class anyways. Please"
							+ " add ElementType.TYPE to the " + a.getName() + " annotation, or remove that class from the checked types on " + c.getName());
				}
			}
			toTest.add(c);
		}
		for(Class<?> c : toTest){
			SubclassesMustHaveAnnotation ann = c.getAnnotation(SubclassesMustHaveAnnotation.class);
			for(Class sub : ClassDiscovery.GetAllClassesOfSubtype(c, null)){
				for(Class<? extends Annotation> a : ann.value()){
					if(sub.getAnnotation(a) == null){
						problems.add("The subclass " + sub.getName() + " does not have the annotation @" + a.getName() + " tagged to it. Please add this"
								+ " annotation.");
					}
				}
			}
		}
		if(!problems.isEmpty()){
			fail(StringUtils.Join(problems, "\n"));
		}
	}
	
	@Test
	public void testForAbstractClassesHavingProtectedConstructors(){
		Set<Class> problems = new HashSet<Class>();
		for(Class c : ClassDiscovery.GetAllClasses()){
			if(Modifier.isAbstract(c.getModifiers())){
				for(Constructor con : c.getDeclaredConstructors()){
					if(!Modifier.isProtected(con.getModifiers()) && !Modifier.isPrivate(con.getModifiers())){
						problems.add(con.getDeclaringClass());
					}
				}
			}
		}
		if(!problems.isEmpty()){
			StringBuilder b = new StringBuilder();
			b.append("The following abstract classes have public constructors. They should be changed to protected (or private).\n"
					+ "This can also be caused if there is no declared constructor, and the default constructor was provided for you.\n");
			for(Class c : problems){
				b.append(c.getName()).append("\n");
			}
			fail(b.toString());
		}
	}
}
