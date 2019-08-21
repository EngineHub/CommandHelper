package com.laytonsmith.PureUtilities.ClassLoading.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryURLCache;
import com.laytonsmith.PureUtilities.Common.Annotations.AnnotationChecks;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.functions.DummyFunction;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.core.natives.interfaces.MixedInterfaceRunner;

/**
 *
 */
public class CacheAnnotations {

	public static void main(String[] args) throws Exception {
		File outputDir = new File(args[0]);
		File scanDir = new File(args[1]);
		if(outputDir.toString().startsWith("-classpath") || outputDir.toString().startsWith("-Xdebug")) {
			//This happens when running locally. I dunno what that is, but we
			//can skip this step.
			StreamUtils.GetSystemOut().println("Skipping annotation caching, running locally.");
			return;
		}
		StreamUtils.GetSystemOut().println("-- Caching annotations --");
		StreamUtils.GetSystemOut().println("Scanning for classes in " + scanDir.getAbsolutePath());
		StreamUtils.GetSystemOut().println("Outputting file to directory " + outputDir.getAbsolutePath());
		long start = System.currentTimeMillis();
		URL cacheFile = new URL("file:" + scanDir.getCanonicalPath());
		ClassDiscoveryURLCache cache = new ClassDiscoveryURLCache(cacheFile);
		cache.writeDescriptor(new FileOutputStream(new File(outputDir, ClassDiscoveryCache.OUTPUT_FILENAME)));
		StreamUtils.GetSystemOut().println("Done writing " + ClassDiscoveryCache.OUTPUT_FILENAME + ", which took " + (System.currentTimeMillis() - start) + " ms.");
		ClassDiscovery.getDefaultInstance().addPreCache(cacheFile, cache);
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(cacheFile);
		StreamUtils.GetSystemOut().println("-- Checking for custom compile errors --");
		AnnotationChecks.checkForceImplementation();
		AnnotationChecks.checkForTypeInTypeofClasses();
		AnnotationChecks.verifyExhaustiveVisitors();
		AnnotationChecks.verifyNonInheritImplements();

		Implementation.setServerType(Implementation.Type.SHELL);
		List<String> uhohs = new ArrayList<>();
		Set<Class> apiClasses = new HashSet<>();
		apiClasses.addAll(ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class));
		apiClasses.addAll(ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(typeof.class));
		for(Class c : apiClasses) {
			boolean isGetNameExempt = false;
			if(c.isInterface()) {
				for(Class r : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(InterfaceRunnerFor.class)) {
					InterfaceRunnerFor f = (InterfaceRunnerFor) r.getAnnotation(InterfaceRunnerFor.class);
					if(f.value() == c) {
						isGetNameExempt = ClassDiscovery.GetClassAnnotation(c, typeof.class) != null;
						c = r;
						break;
					}
				}
			}
			// Verify that all classes that are @api classes have the valid functions required for proper documentation
			// generation, as well as ultimately extend at minimum SimpleDocumentation.
			if(DummyFunction.class.isAssignableFrom(c)) {
				// Skip this one. These are excused from the normal reporting requirements.
				continue;
			}
			if(!SimpleDocumentation.class.isAssignableFrom(c) && !MixedInterfaceRunner.class.isAssignableFrom(c)) {
				uhohs.add(c.getName() + " must implement SimpleDocumentation");
				continue;
			}
			for(Method m : SimpleDocumentation.class.getDeclaredMethods()) {
				try {
					c.getDeclaredMethod(m.getName(), m.getParameterTypes());
				} catch (NoSuchMethodException ex) {
					// typeof is exempt from having getName in each individual class, because the
					// typeof value is that information.
					if(!m.getName().equals("getName")) {
						if(ClassDiscovery.GetClassAnnotation(c, typeof.class) != null && !isGetNameExempt) {
							uhohs.add(c.getName() + " must implement " + m.getName() + "().");
						}
					}
				} catch (SecurityException ex) {
					throw new Error(ex);
				}
			}
		}
		if(!uhohs.isEmpty()) {
			Collections.sort(uhohs);
			throw new Exception("There " + StringUtils.PluralHelper(uhohs.size(), "compile error") + ":\n" + StringUtils.Join(uhohs, "\n"));
		}
		StreamUtils.GetSystemOut().println("-- Finished with custom compiler checks --");
	}
}
