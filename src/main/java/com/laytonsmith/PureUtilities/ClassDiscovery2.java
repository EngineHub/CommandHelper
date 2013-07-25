
package com.laytonsmith.PureUtilities;

import static com.laytonsmith.PureUtilities.ClassDiscovery.GetClassPackageHierachy;
import com.laytonsmith.PureUtilities.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassMirror.ClassReferenceMirror;
import com.laytonsmith.PureUtilities.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassMirror.MethodMirror;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains methods for dynamically determining things about Classes,
 * without loading them into PermGen. Search criteria is provided,
 * (most notably annotations, however also subclasses) and Class/Field/Method mirrors are
 * returned, eliminating the PermGen requirements, even
 * though all known classes are scanned against. It is then up to the calling method
 * to actually determine if the classes need to be loaded, thereby deferring all
 * logic to actually take up more PermGen space to the calling code, instead of this
 * class.
 */
public class ClassDiscovery2 {

	/**
	 * The default instance.
	 */
	private static ClassDiscovery2 defaultInstance = null;
	
	/**
	 * Returns the default, shared instance. This is usually how you want
	 * to gain a reference to this class, as caching can often times be shared
	 * among multiple tasks, though if you need a private instance, you can
	 * use the constructor to create a new one.
	 * @return 
	 */
	public static ClassDiscovery2 getDefaultInstance(){
		if(defaultInstance == null){
			defaultInstance = new ClassDiscovery2();
		}
		return defaultInstance;
	}
	
	/**
	 * Creates a new instance of the ClassDiscovery class. Normally, you
	 * should probably just use the default instance, as caching across the board
	 * is a good thing, however, it may be the case that you need a standalone
	 * instance, in which case, you can create a new one.
	 */
	public ClassDiscovery2(){
		
	}
	
	/**
	 * Stores the mapping of class name to ClassMirror object. At any given time,
	 * after doDiscovery is called, this will be up to date with all known classes.
	 */
	private Map<URL, Set<ClassMirror>> classCache = new HashMap<URL, Set<ClassMirror>>();
	/**
	 * This cache maps jvm name to the associated ClassMirror object, to speed up
	 * lookups.
	 */
	private Map<String, ClassMirror> jvmNameToMirror = new HashMap<String, ClassMirror>();
	/**
	 * Maps the fuzzy class name to actual Class object.
	 */
	private Map<String, ClassMirror> fuzzyClassCache = new HashMap<String, ClassMirror>();
	/**
	 * List of all URLs from which to pull classes.
	 */
	private Set<URL> urlCache = new HashSet<URL>();
	/**
	 * When a URL is added to urlCache, it is also initially added here.
	 * If there are any URLs in this set, they must be resolved first.
	 */
	private Set<URL> dirtyURLs = new HashSet<URL>();
	
	/**
	 * Cache for class subtypes. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private Map<Class<?>, Set<ClassMirror>> classSubtypeCache = new HashMap<Class<?>, Set<ClassMirror>>();
	/**
	 * Cache for class annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private Map<Class<? extends Annotation>, Set<ClassMirror>> classAnnotationCache = new HashMap<Class<? extends Annotation>, Set<ClassMirror>>();
	/**
	 * Cache for field annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private Map<Class<? extends Annotation>, Set<FieldMirror>> fieldAnnotationCache = new HashMap<Class<? extends Annotation>, Set<FieldMirror>>();
	/**
	 * Cache for method annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private Map<Class<? extends Annotation>, Set<MethodMirror>> methodAnnotationCache = new HashMap<Class<? extends Annotation>, Set<MethodMirror>>();
	
	/**
	 * Looks through all the URLs and pulls out all known classes, and caches them in
	 * the classCache object.
	 */
	private void doDiscovery(){
		if(!dirtyURLs.isEmpty()){
			Iterator<URL> it = dirtyURLs.iterator();
			while(it.hasNext()){
				discover(it.next());
				it.remove();
			}
		}
	}
	
	/**
	 * Does the class discovery for this particular URL. This should
	 * only be called by doDiscovery. Other internal methods should call
	 * doDiscovery, which handles looking through the dirtyURLs.
	 */
	private void discover(URL rootLocation){
		String url = rootLocation.toString();
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
					//Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		File rootLocationFile;
		try{
			rootLocationFile = new File(rootLocation.toURI());
		} catch(URISyntaxException ex){
			//This shouldn't ever happen
			throw new RuntimeException(ex);
		}
		//At this point, we're going to reload the catalog for this url, so clear
		//it out in the cache
		if(!classCache.containsKey(rootLocation)){
			classCache.put(rootLocation, new HashSet<ClassMirror>());
		} else {
			classCache.get(rootLocation).clear();
		}
		Set<ClassMirror> mirrors = classCache.get(rootLocation);
		for(String file : classNameList){
			//Don't consider anonymous classes, or non class resources
			if (!file.matches(".*\\$(?:\\d)*\\.class") && file.endsWith(".class")) {
				ZipReader zr = new ZipReader(new File(rootLocationFile, file));
				try {
					ClassMirror cm = new ClassMirror(zr.getInputStream());
					mirrors.add(cm);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(ClassDiscovery2.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ex) {
					Logger.getLogger(ClassDiscovery2.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
	
	/**
	 * Adds a new discovery URL. This makes the URL eligible to be
	 * included when finding classes/methods/fields with the various
	 * other methods.
	 * @param url 
	 */
	public void addDiscoveryLocation(URL url){
		if(url == null){
			throw new NullPointerException("url cannot be null");
		}
		urlCache.add(url);
		dirtyURLs.add(url);
		classCache.put(url, new HashSet<ClassMirror>());
	}
	
	/**
	 * Clears the internal caches. This is called automatically when a new
	 * discovery location is added with addDiscoveryLocation, but this should
	 * be called if the caches could have become invalidated since the last
	 * load, as well as if the reference to any of the class loaders that loaded
	 * any classes during the course of using this instance need to be garbage
	 * collected.
	 */
	public void invalidateCaches(){
		classCache.clear();
		jvmNameToMirror.clear();
		fuzzyClassCache.clear();
		classAnnotationCache.clear();
		fieldAnnotationCache.clear();
		methodAnnotationCache.clear();
	}
	
	/**
	 * Returns a list of all known classes. The ClassMirror for each
	 * class is returned, and further examination can be done on each class,
	 * or loadClass can be called on the ClassMirror to get the actual Class
	 * object. No ClassLoaders are involved directly in this operation.
	 * @return A list of ClassMirror objects for all known classes
	 */
	public Set<ClassMirror> getKnownClasses(){
		doDiscovery();
		Set<ClassMirror> ret = new HashSet<ClassMirror>();
		for(URL url : urlCache){
			ret.addAll(getKnownClasses(url));
		}
		return ret;
	}
	
	/**
	 * Gets all known classes, only within this URL.
	 * @param url
	 * @return 
	 */
	public List<ClassMirror> getKnownClasses(URL url){
		doDiscovery();
		return new ArrayList<ClassMirror>(classCache.get(url));
	}
	
	/**
	 * Returns a list of known classes that extend the given superclass,
	 * or implement the given interface.
	 * @param superType
	 * @return 
	 */
	public Set<ClassMirror> getClassesThatExtend(Class<?> superType){
		if(superType == java.lang.Object.class){
			//To avoid complication down the road, if this is the case,
			//just return all known classes here.
			return getKnownClasses();
		}
		if(classSubtypeCache.containsKey(superType)){
			return new HashSet<ClassMirror>(classSubtypeCache.get(superType));
		}
		Set<ClassMirror> mirrors = new HashSet<ClassMirror>();
		Set<ClassMirror> knownClasses = getKnownClasses();
		outer: for(ClassMirror m : knownClasses){
			if(m.directlyExtendsFrom(superType)){
				//Trivial case, so just add this now, then continue.
				mirrors.add(m);
				continue;
			}
			//Well, crap, more complicated. Ok, so, the list of supers
			//can probably be walked up even further, so we need to find
			//the supers of these (and make sure it's not in the ClassMirror
			//cache, to avoid loading classes unneccessarily) and then load
			//the actual Class object for them. Essentially, this falls back
			//to loading the class when it
			//can't be found in the mirrors pool.
			Set<ClassReferenceMirror> supers = new HashSet<ClassReferenceMirror>();
			//Get the superclass. If it's java.lang.Object, we're done.
			ClassReferenceMirror su = m.getSuperClass();
			while(!su.getJVMName().equals("Ljava/lang/Object;")){
				supers.add(su);
				ClassMirror find = getClassMirrorFromJVMName(su.getJVMName());
				if(find == null){
					try {
						//Ok, have to Class.forName this one
						Class clazz = ClassUtils.forCanonicalName(su.toString());
						//We can just use isAssignableFrom now
						if(superType.isAssignableFrom(clazz)){
							mirrors.add(m);
							continue outer;
						} else {
							//We need to add change the reference to su
							su = new ClassReferenceMirror("L" + clazz.getSuperclass().getName().replace(".", "/") + ";");
						}
					} catch (ClassNotFoundException ex) {
						//Hmm, ok? I guess something bad happened, so let's break
						//the loop and give up on this class.
						continue outer;
					}
				} else {
					su = find.getSuperClass();
				}
			}
			//Same thing now, but for interfaces
			Deque<ClassReferenceMirror> interfaces = new ArrayDeque<ClassReferenceMirror>();
			Set<ClassReferenceMirror> handled = new HashSet<ClassReferenceMirror>();
			interfaces.addAll(m.getInterfaces());
			//Also have to add all the supers' interfaces too
			for(ClassReferenceMirror r : supers){
				ClassMirror find = getClassMirrorFromJVMName(r.getJVMName());
				if(find == null){
					try {
						Class clazz = Class.forName(r.toString());
						for(Class c : clazz.getInterfaces()){
							interfaces.add(new ClassReferenceMirror("L" + c.getName().replace(".", "/") + ";"));
						}
					} catch (ClassNotFoundException ex) {
						continue outer;
					}
				} else {
					interfaces.addAll(find.getInterfaces());
				}
			}
			while(!interfaces.isEmpty()){
				ClassReferenceMirror in = interfaces.pop();
				if(handled.contains(in)){
					continue;
				}
				handled.add(in);
				supers.add(in);
				ClassMirror find = getClassMirrorFromJVMName(in.getJVMName());
				if(find != null){
					interfaces.addAll(find.getInterfaces());
				} else {
					try {
						//Again, have to check Class.forName
						Class clazz = ClassUtils.forCanonicalName(in.toString());
						if(superType.isAssignableFrom(clazz)){
							mirrors.add(m);
							continue outer;
						}
					} catch (ClassNotFoundException ex) {
						continue outer;
					}
				}
			}
		}
		classSubtypeCache.put(superType, mirrors);
		return mirrors;
	}
	
	private ClassMirror getClassMirrorFromJVMName(String className){
		if(jvmNameToMirror.containsKey(className)){
			return jvmNameToMirror.get(className);
		}
		for(ClassMirror c : getKnownClasses()){
			if(("L" + c.getJVMClassName() + ";").equals(className)){
				jvmNameToMirror.put("L" + c.getJVMClassName()  + ";", c);
				return c;
			}
		}
		//Still not found? Return null then.
		jvmNameToMirror.put(className, null);
		return null;
	}
	
	/**
	 * Returns a list of classes that have been annotated with the specified
	 * annotation. This will work with annotations that have been declared with
	 * the {@link RetentionPolicy#CLASS} property.
	 * @param annotation
	 * @return 
	 */
	public Set<ClassMirror> getClassesWithAnnotation(Class<? extends Annotation> annotation){
		if(classAnnotationCache.containsKey(annotation)){
			return new HashSet<ClassMirror>(classAnnotationCache.get(annotation));
		}
		Set<ClassMirror> mirrors = new HashSet<ClassMirror>();
		for(ClassMirror m : getKnownClasses()){
			if(m.hasAnnotation(annotation)){
				mirrors.add(m);
			}
		}
		classAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}
	
	/**
	 * Returns a list of fields that have been annotated with the specified
	 * annotation. This will work with annotations that have been declared with
	 * the {@link RetentionPolicy#CLASS} property.
	 * @param annotation
	 * @return 
	 */
	public Set<FieldMirror> getFieldsWithAnnotation(Class<? extends Annotation> annotation){
		if(fieldAnnotationCache.containsKey(annotation)){
			return new HashSet<FieldMirror>(fieldAnnotationCache.get(annotation));
		}
		Set<FieldMirror> mirrors = new HashSet<FieldMirror>();
		for(ClassMirror m : getKnownClasses()){
			for(FieldMirror f : m.getFields()){
				if(f.hasAnnotation(annotation)){
					mirrors.add(f);
				}
			}
		}
		fieldAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}
	
	/**
	 * Returns a list of methods that have been annotated with the specified
	 * annotation. This will work with annotations that have been declared with
	 * the {@link RetentionPolicy#CLASS} property.
	 * @param annotation
	 * @return 
	 */
	public Set<MethodMirror> getMethodsWithAnnotation(Class<? extends Annotation> annotation){
		if(methodAnnotationCache.containsKey(annotation)){
			return new HashSet<MethodMirror>(methodAnnotationCache.get(annotation));
		}
		Set<MethodMirror> mirrors = new HashSet<MethodMirror>();
		for(ClassMirror m : getKnownClasses()){
			for(MethodMirror mm : m.getMethods()){
				if(mm.hasAnnotation(annotation)){
					mirrors.add(mm);
				}
			}
		}
		methodAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}
	
	/**
	 * Calls forFuzzyName with initialize true, and the class loader used to load this class.
	 * @param packageRegex
	 * @param className
	 * @return 
	 */
	public ClassMirror forFuzzyName(String packageRegex, String className){
		return forFuzzyName(packageRegex, className, true, ClassDiscovery2.class.getClassLoader());
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
	public ClassMirror forFuzzyName(String packageRegex, String className, boolean initialize, ClassLoader classLoader){
		String index = packageRegex + className;
		if(fuzzyClassCache.containsKey(index)){
			return fuzzyClassCache.get(index);
		}
		Set<ClassMirror> found = new HashSet<ClassMirror>();
		Set<ClassMirror> searchSpace = getKnownClasses();
		for(ClassMirror c : searchSpace){
			if(c.getPackage().getName().matches(packageRegex) && c.getSimpleName().equals(className)){
				found.add(c);
			}
		}
		ClassMirror find;
		if(found.size() == 1){
			find = found.iterator().next();
		} else if(found.isEmpty()){
			find = null;
		} else {
			ClassMirror candidate = null;
			int max = Integer.MAX_VALUE;
			for(ClassMirror f : found){
				int distance = StringUtils.LevenshteinDistance(f.getPackage().getName(), packageRegex);
				if(distance < max){
					candidate = f;
					max = distance;
				}
			}
			find = candidate;
		}
		fuzzyClassCache.put(index, find);
		return find;
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
	
	/**
	 * Returns the container url for this class. This varies based on whether or
	 * not the class files are in a zip/jar or not, so this method standardizes
	 * that. The method may return null, if the class is a dynamically generated
	 * class (perhaps with asm, or a proxy class)
	 *
	 * @param c
	 * @return
	 */
	public static URL GetClassContainer(Class c) {
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
			return new URL(packageRoot);
		} catch (Exception e) {
			throw new RuntimeException("While interrogating " + c.getName() + ", an unexpected exception was thrown.", e);
		}
	}
	
}
