package com.laytonsmith.PureUtilities.Common.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 */
public class AnnotationChecks {

	public static void checkForceImplementation() throws Exception{
		Set<String> uhohs = new HashSet<>();
		Set<Constructor> set = ClassDiscovery.getDefaultInstance().loadConstructorsWithAnnotation(ForceImplementation.class);
		for(Constructor cons : set){
			Class superClass = cons.getDeclaringClass();
			Set<Class> s = ClassDiscovery.getDefaultInstance().loadClassesThatExtend(superClass);
			checkImplements: for(Class c : s){
				// c is the class we want to check to make sure it implements cons
				for(Constructor cCons : c.getDeclaredConstructors()){
					if(Arrays.equals(cons.getParameterTypes(), cCons.getParameterTypes())){
						continue checkImplements;
					}
				}
				if(c.isMemberClass() && (c.getModifiers() & Modifier.STATIC) == 0){
					// Ok, so, an inner, non static class actually passes the super class's reference to the constructor as
					// the first parameter, at a byte code level. So this is a different type of error, or at least, a different
					// error message will be helpful.
					uhohs.add(c.getName() + " must be static.");
				} else {
					uhohs.add(c.getName() + " must implement the constructor with signature (" + getSignature(cons) + "), but doesn't.");
				}
			}
		}
		
		if(!uhohs.isEmpty()){
			List<String> uhohsList = new ArrayList<>(uhohs);
			Collections.sort(uhohsList);
			throw new Exception("There " + StringUtils.PluralHelper(uhohs.size(), "error") + ". The following classes need to implement various methods:\n" + StringUtils.Join(uhohs, "\n"));
		}
	}
	
	private static String getSignature(Executable executable){
		List<String> l = new ArrayList<>();
		for(Class cc : executable.getParameterTypes()){
			l.add(cc.getName());
		}
		return StringUtils.Join(l, ", ");
	}
	
}
