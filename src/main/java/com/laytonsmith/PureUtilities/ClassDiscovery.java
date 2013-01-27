
package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ClassDiscovery {

	private ClassDiscovery() {
	}

	/**
	 * Adds a jar or file path location to be scanned by the default call to
	 * GetClassesWithinPackageHierarchy. This is useful if an external library
	 * wishes to be considered by the scanner.
	 *
	 * @param url
	 */
	public static void InstallDiscoveryLocation(String url) {
		additionalURLs.add(url);
	}

	/**
	 * Clears the class cache. Upon the first call to the rather expensive
	 * GetClassesWithinPackageHierarchy(String) method, the returned classes are
	 * cached instead of being regenerated. This method is automatically called
	 * if a new discovery location is installed, but if new classes are being
	 * generated dynamically, this cache will become stale, and you should clear
	 * the cache for a particular url. If url is null, all caches are cleared.
	 */
	public static void InvalidateCache(String url) {
		if (url == null) {
			for (String u : new HashSet<String>(classCache.keySet())) {
				InvalidateCache(u);
			}
			fuzzyClassCache.clear();
		} else {
			classCache.remove(url);
		}
		classAnnotationCache.clear();
		fieldAnnotationCache.clear();
		methodAnnotationCache.clear();
	}

	/**
	 * Equivalent to InvalidateCache(null);
	 */
	public static void InvalidateCache() {
		InvalidateCache(null);
	}
	/**
	 * There's no need to rescan the project every time
	 * GetClassesWithinPackageHierarchy is called, unless we add a new discovery
	 * location, or code is being generated on the fly or something crazy like
	 * that, so let's cache unless told otherwise.
	 */
	private static Map<String, Class[]> classCache = new HashMap<String, Class[]>();
	private static Map<String, Class> fuzzyClassCache = new HashMap<String, Class>();
	private static Set<String> additionalURLs = new HashSet<String>();
	
	private static Map<Class<? extends Annotation>, Set<Class>> classAnnotationCache = new HashMap<Class<? extends Annotation>, Set<Class>>();
	private static Map<Class<? extends Annotation>, Set<Field>> fieldAnnotationCache = new HashMap<Class<? extends Annotation>, Set<Field>>();
	private static Map<Class<? extends Annotation>, Set<Method>> methodAnnotationCache = new HashMap<Class<? extends Annotation>, Set<Method>>();
	
	private static final Set<ClassLoader> defaultClassLoaders = new HashSet<ClassLoader>();
	static{ defaultClassLoaders.add(ClassDiscovery.class.getClassLoader()); }

	public static Class[] GetClassesWithinPackageHierarchy() {
		List<Class> classes = new ArrayList<Class>();
		classes.addAll(Arrays.asList(GetClassesWithinPackageHierarchy(null, null)));
		for (String url : additionalURLs) {
			classes.addAll(Arrays.asList(GetClassesWithinPackageHierarchy(url, null)));
		}
		return classes.toArray(new Class[classes.size()]);
	}
	
	public static void InstallClassLoader(ClassLoader cl){
		defaultClassLoaders.add(cl);
	}

	/**
	 * Gets all the classes in the specified location. The url can point to a
	 * jar, or a file system location. If null, the binary in which
	 * this particular class file is located is used.
	 *
	 * @param url The url to the jar/folder
	 * @param loader The classloader to use to load the classes. If null, the default classloader list is used, which
	 * can be added to with InstallClassLoader().
	 * @return
	 */
	public static Class[] GetClassesWithinPackageHierarchy(String url, Set<ClassLoader> loaders) {
		if (classCache.containsKey(url)) {
			return classCache.get(url);
		}
		if(loaders == null){
			loaders = defaultClassLoaders;
		}
		String originalURL = url;
		if (url == null) {
			url = GetClassPackageHierachy(ClassDiscovery.class);
		}
		List<String> classNameList = new ArrayList<String>();
		if (url.startsWith("file:")) {
			//We are running from the file system
			//First, get the "root" of the class structure. We assume it's
			//the root of this class
			String fileName = Pattern.quote(ClassDiscovery.class.getCanonicalName().replace(".", "/"));
			fileName = fileName/*.replaceAll("\\\\Q", "").replaceAll("\\\\E", "")*/ + ".class";
			String root = url.replaceAll("file:" + (TermColors.SYSTEM == TermColors.SYS.WINDOWS ? "/" : ""), "").replaceAll(fileName, "");
			//System.out.println(root);
			//Ok, now we have the "root" of the known class structure. Let's recursively
			//go through everything and pull out the files
			List<File> fileList = new ArrayList<File>();
			descend(new File(root), fileList);
			//Now, we have all the class files in the package. But, it's the absolute path
			//to all of them. We have to first remove the "front" part
			for (File f : fileList) {
				classNameList.add(f.getAbsolutePath().replaceFirst(Pattern.quote(new File(root).getAbsolutePath() + File.separator), ""));
			}

		} else if (url.startsWith("jar:")) {
			//We are running from a jar
			if (url.endsWith("!/")) {
				url = StringUtils.replaceLast(url, "!/", "");
			}
			url = url.replaceFirst("jar:", "");
			ZipInputStream zip = null;
			try {
				URL jar = new URL(url);
				zip = new ZipInputStream(jar.openStream());
				ZipEntry ze;
				while ((ze = zip.getNextEntry()) != null) {
					classNameList.add(ze.getName());
				}
			} catch (IOException ex) {
				Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				try {
					zip.close();
				} catch (IOException ex) {
					Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		//Ok, now we need to go through the list, and throw out entries
		//that are anonymously named (they end in $\d.class) because they
		//are inaccessible anyways
		List<Class> files = new ArrayList<Class>();
		for (String s : classNameList) {
			//Don't consider anonymous inner classes
			if (!s.matches(".*\\$(?:\\d)*\\.class") && s.endsWith(".class")) {
				//Now, replace any \ with / and replace / with ., and remove the .class,
				//and forName it.
				String className = s.replaceAll("\\.class", "").replaceAll("\\\\", "/").replaceAll("[/]", ".");
				for(ClassLoader loader : loaders){
					try {
						//Don't initialize it, so we don't have to deal with ExceptionInInitializer errors
						Class c = Class.forName(className, false, loader);
						files.add(c);
					} catch (ClassNotFoundException ex) {
						//It can't be loaded? O.o Oh well.
					} catch (NoClassDefFoundError ex) {
						//Must have been an external library
					}
				}
			}
		}
		//Put the results in the cache
		Class[] ret = files.toArray(new Class[files.size()]);
		classCache.put(originalURL, ret);
		return ret;
	}

	public static Class[] GetClassesWithAnnotation(Class<? extends Annotation> annotation) {
		if(classAnnotationCache.containsKey(annotation)){
			return new HashSet<Class>(classAnnotationCache.get(annotation))
					.toArray(new Class[classAnnotationCache.get(annotation).size()]);
		}
		Set<Class> classes = new HashSet<Class>();
		for (Class c : GetClassesWithinPackageHierarchy()) {
			if (c.getAnnotation(annotation) != null) {
				classes.add(c);
			}
		}
		classAnnotationCache.put(annotation, classes);
		return classes.toArray(new Class[classes.size()]);
	}
	
	public static Field[] GetFieldsWithAnnotation(Class<? extends Annotation> annotation){
		if(fieldAnnotationCache.containsKey(annotation)){
			return new HashSet<Field>(fieldAnnotationCache.get(annotation))
					.toArray(new Field[fieldAnnotationCache.get(annotation).size()]);
		}
		Set<Field> fields = new HashSet<Field>();
		for (Class c : GetClassesWithinPackageHierarchy()) {
			try{
				for(Field f : c.getDeclaredFields()){
					if (f.getAnnotation(annotation) != null) {
						fields.add(f);
					}
				}
			} catch(Throwable t){
				//This can happen in any number of cases, but we don't care, we just want to skip the class.
			}
		}
		fieldAnnotationCache.put(annotation, fields);
		return fields.toArray(new Field[fields.size()]);
	}
	
	public static Method[] GetMethodsWithAnnotation(Class<? extends Annotation> annotation){
		if(methodAnnotationCache.containsKey(annotation)){
			return new HashSet<Method>(methodAnnotationCache.get(annotation))
					.toArray(new Method[methodAnnotationCache.get(annotation).size()]);
		}
		Set<Method> methods = new HashSet<Method>();
		for (Class c : GetClassesWithinPackageHierarchy()) {
			try{
				for(Method m : c.getDeclaredMethods()){
					if (m.getAnnotation(annotation) != null) {
						methods.add(m);
					}
				}
			} catch(Throwable t){
				//This can happen in any number of cases, but we don't care, we just want to skip the class.
			}
		}
		methodAnnotationCache.put(annotation, methods);
		return methods.toArray(new Method[methods.size()]);
	}
	
	/**
	 * Returns a list of all known or discoverable classes in this class loader.
	 * @return 
	 */
	static Set<Class> GetAllClasses(){
		Set<Class> classes = new HashSet<Class>();
		Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
		classLoaders.add(ClassDiscovery.class.getClassLoader());
		for(String url : additionalURLs){
			classes.addAll(Arrays.asList(GetClassesWithinPackageHierarchy(url, classLoaders)));
		}
		return classes;
	}

	/**
	 * Gets all the package hierarchies for all currently known classes. This is
	 * a VERY expensive operation, and should be avoided as much as possible.
	 * The set of ClassLoaders to search should be sent, which will each be
	 * searched, including each ClassLoader's parent. If classLoaders is null,
	 * the calling class's ClassLoader is used, if possible, otherwise the
	 * current Thread's context class loader is used. This may not be a
	 * reasonable default for your purposes, so it is best to
	 *
	 * @return
	 */
	@SuppressWarnings("UseOfObsoleteCollectionType")
	public static Set<String> GetKnownPackageHierarchies(Set<ClassLoader> classLoaders) {
		Set<String> list = new HashSet<String>();
		if (classLoaders == null) {
			classLoaders = new HashSet<ClassLoader>();
			//0 is the thread, 1 is us, and 2 is our caller.
			StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
			try {
				Class c = Class.forName(ste.getClassName());
				classLoaders.add(c.getClassLoader());
			} catch (ClassNotFoundException ex) {
				classLoaders.add(Thread.currentThread().getContextClassLoader());
			}
		}
		//Go through and pull out all the parents, and we can just iterate through that, which allows
		//us to skip classloaders if multiple classloaders (which will ultimately all have at least one
		//classloader in common) are passed in, we aren't scanning them twice.
		Set<ClassLoader> classes = new HashSet<ClassLoader>();
		for (ClassLoader cl : classLoaders) {
			while (cl != null) {
				classes.add(cl);
				cl = cl.getParent();
			}
		}

		Set<String> foundClasses = new TreeSet<String>();
		for (ClassLoader l : classes) {
			List v = new ArrayList((Vector) ReflectionUtils.get(ClassLoader.class, l, "classes"));
			for (Object o : v) {
				Class c = (Class) o;
				if (!foundClasses.contains(c.getName())) {
					String url = GetClassPackageHierachy(c);
					if (url != null) {
						list.add(url);
					}
					foundClasses.add(c.getName());
				}
			}
		}
		return list;
	}

	/**
	 * Works like Class.forName, but tries all the given ClassLoaders before
	 * giving up. Eventually though, it will throw a ClassNotFoundException if
	 * none of the classloaders know about it.
	 *
	 * @param name
	 * @param initialize
	 * @param loaders
	 * @return
	 */
	public static Class<?> forName(String name, boolean initialize, Set<ClassLoader> loaders) throws ClassNotFoundException {
		Class c = null;
		for (ClassLoader loader : loaders) {
			try {
				c = Class.forName(name, initialize, loader);
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		if (c == null) {
			throw new ClassNotFoundException(name + " was not found in any ClassLoader!");
		}
		return c;
	}
	
	public static Class<?> forFuzzyName(String packageRegex, String className){
		return forFuzzyName(packageRegex, className, true, ClassDiscovery.class.getClassLoader());
	}
	
	/**
	 * Returns a class given a "fuzzy" package name, that is, the package name provided is a
	 * regex. The class name must match exactly, but the package name will be the closest match,
	 * or undefined if there is no clear candidate. If no matches are found, null is returned.
	 * @param packageRegex
	 * @param className
	 * @param initialize
	 * @param classLoader
	 * @return 
	 */
	public static Class<?> forFuzzyName(String packageRegex, String className, boolean initialize, ClassLoader classLoader){
		String index = packageRegex + className;
		if(fuzzyClassCache.containsKey(index)){
			return fuzzyClassCache.get(index);
		} else {
			Set<Class> found = new HashSet<Class>();
			Set<Class> searchSpace = GetAllClasses();
			for(Class c : searchSpace){
				if(c.getPackage().getName().matches(packageRegex) && c.getSimpleName().equals(className)){
					found.add(c);
				}
			}
			if(found.size() == 1){
				return found.iterator().next();
			} else if(found.isEmpty()){
				return null;
			} else {
				Class candidate = null;
				int max = Integer.MAX_VALUE;
				for(Class f : found){
					int distance = StringUtils.LevenshteinDistance(f.getPackage().getName(), packageRegex);
					if(distance < max){
						candidate = f;
						max = distance;
					}
				}
				fuzzyClassCache.put(index, candidate);
				return candidate;
			}
		}
	}
	
	/**
	 * Gets all concrete classes that either extend (in the case of a class) or implement
	 * (in the case of an interface) this superType. Additionally, if superType is a concrete
	 * class, it itself will be returned in the list. Note that by "concrete class" it is meant
	 * that the class is instantiatable, so abstract classes would not be returned.
	 * @param superType
	 * @return 
	 */
	public static Class[] GetAllClassesOfSubtype(Class superType, Set<ClassLoader> classLoaders){
		Set<Class> list = new HashSet<Class>();
		for(String url : additionalURLs){
			list.addAll(Arrays.asList(GetClassesWithinPackageHierarchy(url, classLoaders)));
		}
		Set<Class> ret = new HashSet<Class>();
		for(Class c : list){
			if(superType.isAssignableFrom(c) && !c.isInterface()){
				//Check to see if abstract
				if(!Modifier.isAbstract(c.getModifiers())){
					ret.add(c);
				}
			}
		}
		return ret.toArray(new Class[ret.size()]);
	}

	/**
	 * Returns the container url for this class. This varies based on whether or
	 * not the class files are in a zip/jar or not, so this method standardizes
	 * that. The method may return null, if the class is a dynamically generated
	 * class (perhaps with asm, or a proxy class)
	 *
	 * @param c
	 * @return
	 */
	public static String GetClassPackageHierachy(Class c) {
		if (c == null) {
			throw new NullPointerException("The Class passed to this method may not be null");
		}
		try {
			while(c.isMemberClass() || c.isAnonymousClass()){
				c = c.getEnclosingClass(); //Get the actual enclosing file
			}
			if (c.getProtectionDomain().getCodeSource() == null) {
				//This is a proxy or other dynamically generated class, and has no physical container,
				//so just return null.
				return null;
			}
			String packageRoot;
			try {
				//This is the full path to THIS file, but we need to get the package root.
				String thisClass = c.getResource(c.getSimpleName() + ".class").toString();
				packageRoot = StringUtils.replaceLast(thisClass, Pattern.quote(c.getName().replaceAll("\\.", "/") + ".class"), "");
				if(packageRoot.endsWith("!/")){
					packageRoot = StringUtils.replaceLast(packageRoot, "!/", "");
				}
			} catch (Exception e) {
				//Hmm, ok, try this then
				packageRoot = c.getProtectionDomain().getCodeSource().getLocation().toString();
			}
			packageRoot = URLDecoder.decode(packageRoot, "UTF-8");
			return packageRoot;
		} catch (Exception e) {
			throw new RuntimeException("While interrogating " + c.getName() + ", an unexpected exception was thrown.", e);
		}
	}

	private static void descend(File start, List<File> fileList) {
		if (start.isFile()) {
			if (start.getName().endsWith(".class")) {
				fileList.add(start);
			}
		} else {
			for (File child : start.listFiles()) {
				descend(child, fileList);
			}
		}
	}
}
