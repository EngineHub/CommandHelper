package com.laytonsmith.PureUtilities.ClassLoading;

import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassReferenceMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.ProgressIterator;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ZipIterator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class contains methods for dynamically determining things about Classes,
 * without loading them into PermGen. Search criteria is provided, (most notably
 * annotations, however also subclasses) and Class/Field/Method mirrors are
 * returned, eliminating the PermGen requirements, even though all known classes
 * are scanned against. It is then up to the calling method to actually
 * determine if the classes need to be loaded, thereby deferring all logic to
 * actually take up more PermGen space to the calling code, instead of this
 * class.
 */
public class ClassDiscovery {

	/**
	 * The default instance.
	 */
	private static ClassDiscovery defaultInstance = null;

	/**
	 * Returns the default, shared instance. This is usually how you want to
	 * gain a reference to this class, as caching can often times be shared
	 * among multiple tasks, though if you need a private instance, you can use
	 * the constructor to create a new one. Using this instance automatically checks
	 * for the jarInfo.ser file in this jar, and if present, adds it to the cache.
	 *
	 * @return
	 */
	public static synchronized ClassDiscovery getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new ClassDiscovery();
			//defaultInstance.setDebugMode(true);
		}
		return defaultInstance;
	}

	/**
	 * Can be used to set the default ClassDiscovery instance returned by
	 * getDefaultInstance. Setting it to null is acceptable, and then a new,
	 * default ClassDiscovery instance will be generated.
	 *
	 * @param cd
	 */
	public static void setDefaultInstance(ClassDiscovery cd) {
		defaultInstance = cd;
	}

	/**
	 * Creates a new instance of the ClassDiscovery class. Normally, you should
	 * probably just use the default instance, as caching across the board is a
	 * good thing, however, it may be the case that you need a standalone
	 * instance, in which case, you can create a new one.
	 */
	public ClassDiscovery() {
		//
	}
	
	/**
	 * Stores the mapping of class name to ClassMirror object. At any given
	 * time, after doDiscovery is called, this will be up to date with all known
	 * classes.
	 */
	private final Map<URL, Set<ClassMirror<?>>> classCache = new HashMap<URL, Set<ClassMirror<?>>>();
	/**
	 * This cache maps jvm name to the associated ClassMirror object, to speed
	 * up lookups.
	 */
	private final Map<String, ClassMirror<?>> jvmNameToMirror = new HashMap<String, ClassMirror<?>>();
	/**
	 * Maps the fuzzy class name to actual Class object.
	 */
	private final Map<String, ClassMirror<?>> fuzzyClassCache = new HashMap<String, ClassMirror<?>>();
	/**
	 * Maps the forName cache.
	 */
	private final Map<String, ClassMirror<?>> forNameCache = new HashMap<String, ClassMirror<?>>();
	/**
	 * List of all URLs from which to pull classes.
	 */
	private final Set<URL> urlCache = new HashSet<URL>();
	/**
	 * When a URL is added to urlCache, it is also initially added here. If
	 * there are any URLs in this set, they must be resolved first.
	 */
	private final Set<URL> dirtyURLs = new HashSet<URL>();
	/**
	 * Cache for class subtypes. Whenever a new URL is added to the URL cache,
	 * this is cleared.
	 */
	private final Map<Class<?>, Set<ClassMirror<?>>> classSubtypeCache = new HashMap<Class<?>, Set<ClassMirror<?>>>();
	/**
	 * Cache for class annotations. Whenever a new URL is added to the URL
	 * cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<ClassMirror<?>>> classAnnotationCache = new HashMap<Class<? extends Annotation>, Set<ClassMirror<?>>>();
	/**
	 * Cache for field annotations. Whenever a new URL is added to the URL
	 * cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<FieldMirror>> fieldAnnotationCache = new HashMap<Class<? extends Annotation>, Set<FieldMirror>>();
	/**
	 * Cache for method annotations. Whenever a new URL is added to the URL
	 * cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<MethodMirror>> methodAnnotationCache = new HashMap<Class<? extends Annotation>, Set<MethodMirror>>();
	/**
	 * By default null, but this can be set per instance.
	 */
	private ProgressIterator progressIterator = null;
	/**
	 * External cache. If added before discovery happens for a URL, this will
	 * cause the discovery process to be skipped entirely for a given URL.
	 */
	private final Map<URL, ClassDiscoveryURLCache> preCaches = new HashMap<URL, ClassDiscoveryURLCache>();
	/**
	 * If true, debug information will be printed out.
	 */
	private boolean debug;
	
	/**
	 * May be null, but if set, is the cache retriever.
	 */
	private ClassDiscoveryCache classDiscoveryCache;
	
	/**
	 * Turns debug mode on. If true, data about what is happening is printed out,
	 * as well as timing information.
	 * @param on 
	 */
	public void setDebugMode(boolean on){
		debug = on;
	}

	/**
	 * Removes the cache for this URL. After calling this, it is ensured that
	 * the discovery methods won't be pulling from a cache. This is used during
	 * initial cache creation.
	 *
	 * @param url
	 */
	public void removePreCache(URL url) {
		if (url == null) {
			throw new NullPointerException("url cannot be null");
		}
		preCaches.remove(url);
	}

	/**
	 * Adds a pre cache for a given URL.
	 *
	 * @param url
	 * @param cache
	 */
	public void addPreCache(URL url, ClassDiscoveryURLCache cache) {
		if (url == null) {
			throw new NullPointerException("url cannot be null");
		}
		if(debug){
			System.out.println("Adding precache for " + url);
		}
		preCaches.put(url, cache);
	}
	
	/**
	 * Sets the class discovery cache. This is optional, but if set
	 * is used to potentially speed up caching.
	 * @param cache 
	 */
	public void setClassDiscoveryCache(ClassDiscoveryCache cache){
		this.classDiscoveryCache = cache;
	}

	/**
	 * Sets the progress iterator for when this class starts up. This is an
	 * optional operation.
	 *
	 * @param progressIterator
	 */
	public void setProgressIterator(ProgressIterator progressIterator) {
		this.progressIterator = progressIterator;
	}

	/**
	 * Looks through all the URLs and pulls out all known classes, and caches
	 * them in the classCache object.
	 */
	private synchronized void doDiscovery() {
		if (!dirtyURLs.isEmpty()) {
			Iterator<URL> it = dirtyURLs.iterator();
			while (it.hasNext()) {
				discover(it.next());
				it.remove();
			}
		}
	}

	/**
	 * Does the class discovery for this particular URL. This should only be
	 * called by doDiscovery. Other internal methods should call doDiscovery,
	 * which handles looking through the dirtyURLs.
	 */
	private synchronized void discover(URL rootLocation) {
		long start = System.currentTimeMillis();
		if(debug){
			System.out.println("Beginning discovery of " + rootLocation);
		}
		try {
			//If the ClassDiscoveryCache is set, just use this.
			if(classDiscoveryCache != null){
				ClassDiscoveryURLCache cduc = classDiscoveryCache.getURLCache(rootLocation);
				preCaches.put(rootLocation, cduc);
			}
			String url = rootLocation.toString();
			if (url == null) {
				url = GetClassContainer(ClassDiscovery.class).toString();
			}
			final File rootLocationFile;
			if (!classCache.containsKey(rootLocation)) {
				classCache.put(rootLocation, Collections.synchronizedSet(new HashSet<ClassMirror<?>>()));
			} else {
				classCache.get(rootLocation).clear();
			}
			final Set<ClassMirror<?>> mirrors = classCache.get(rootLocation);
			if (preCaches.containsKey(rootLocation)) {
				if(debug){
					System.out.println("Precache already contains this URL, so using it");
				}
				//No need, already got a cache for this url
				mirrors.addAll(preCaches.get(rootLocation).getClasses());
				return;
			}
			if(debug){
				System.out.println("Precache does not contain data for this URL, so scanning now.");
			}
			url = url.replaceFirst("^jar:", "");
			if (url.endsWith("!/")) {
				url = StringUtils.replaceLast(url, "!/", "");
			}
			if (url.startsWith("file:") && !url.endsWith(".jar")) {
				final AtomicInteger id = new AtomicInteger(0);
				ExecutorService service = Executors.newFixedThreadPool(10, new ThreadFactory() {
					public Thread newThread(Runnable r) {
						return new Thread(r, "ClassDiscovery-Async-" + id.incrementAndGet());
					}
				});
				
				//Remove file: from the front
				String root = url.substring(5);
				rootLocationFile = new File(root);
				List<File> fileList = new ArrayList<File>();
				descend(new File(root), fileList);

				//Now, we have all the class files in the package. But, it's the absolute path
				//to all of them. We have to first remove the "front" part
				for (File f : fileList) {
					String file = f.toString();
					if (!file.matches(".*\\$(?:\\d)*\\.class") && file.endsWith(".class")) {
						InputStream stream = null;
						try {
							stream = FileUtil.readAsStream(new File(rootLocationFile,
									f.getAbsolutePath().replaceFirst(Pattern.quote(new File(root).getAbsolutePath() + File.separator), "")));
							ClassMirror cm = new ClassMirror(stream);
							mirrors.add(cm);
						} catch (IOException ex) {
							Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
						} finally {
							if (stream != null) {
								try {
									stream.close();
								} catch (IOException ex) {
									Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
								}
							}
						}
					}
				}
				service.shutdown();
				try {
					//Doesn't look like 0 is an option, so we'll just wait a day.
					service.awaitTermination(1, TimeUnit.DAYS);
				} catch (InterruptedException ex) {
					Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if (url.startsWith("file:") && url.endsWith(".jar")) {
				//We are running from a jar
				url = url.replaceFirst("file:", "");
				rootLocationFile = new File(url);
				ZipIterator zi = new ZipIterator(rootLocationFile);
				try {
					zi.iterate(new ZipIterator.ZipIteratorCallback() {
						public void handle(String filename, InputStream in) {
							if (!filename.matches(".*\\$(?:\\d)*\\.class") && filename.endsWith(".class")) {
								try {
									ClassMirror cm = new ClassMirror(in);
									mirrors.add(cm);
								} catch (IOException ex) {
									Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
								}

							}
						}
					}, progressIterator);
				} catch (IOException ex) {
					Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				throw new RuntimeException("Unknown url type: " + rootLocation);
			}
		} finally {
			if(debug){
				System.out.println("Scans finished for " + rootLocation + ", taking " + (System.currentTimeMillis() - start) + " ms.");
			}
		}
	}
	private ClassLoader defaultClassLoader = null;

	/**
	 * Sets the default class loader for the various load methods that are
	 * called without a ClassLoader. This is optional, and if not set, the class
	 * loader of this class is used.
	 *
	 * @param cl
	 */
	public void setDefaultClassLoader(ClassLoader cl) {
		defaultClassLoader = cl;
	}

	/**
	 * Gets the classloader set with {@link #setDefaultClassLoader(java.lang.ClassLoader)}, or the
	 * builtin default if none was specified ever. Regardless, never returns null.
	 * @return 
	 */
	public ClassLoader getDefaultClassLoader() {
		if (defaultClassLoader == null) {
			return ClassDiscovery.class.getClassLoader();
		} else {
			return defaultClassLoader;
		}
	}

	/**
	 * Adds a new discovery URL. This makes the URL eligible to be included when
	 * finding classes/methods/fields with the various other methods. If the URL
	 * already has been added, this has no effect.
	 *
	 * @param url
	 */
	public synchronized void addDiscoveryLocation(URL url) {
		if (url == null) {
			throw new NullPointerException("url cannot be null");
		}
		if (urlCache.contains(url)) {
			//Already here, so just return.
			return;
		}
		urlCache.add(url);
		dirtyURLs.add(url);
		classCache.put(url, new HashSet<ClassMirror<?>>());
	}
	
	/**
	 * Remove a discovery URL. Will invalidate caches.
	 * 
	 * @param url
	 */
	public synchronized void removeDiscoveryLocation(URL url) {
		if (url == null) {
			throw new NullPointerException("url cannot be null");
		}
		
		if (!urlCache.contains(url)) {
			//Not here, so just return.
			return;
		}

		urlCache.remove(url);
		dirtyURLs.remove(url);
		preCaches.remove(url);

		invalidateCaches();
	}

	/**
	 * Clears the internal caches. This is called automatically when a new
	 * discovery location is added with addDiscoveryLocation, but this should be
	 * called if the caches could have become invalidated since the last load,
	 * as well as if the reference to any of the class loaders that loaded any
	 * classes during the course of using this instance need to be garbage
	 * collected.
	 */
	public void invalidateCaches() {
		classCache.clear();
		forNameCache.clear();
		jvmNameToMirror.clear();
		fuzzyClassCache.clear();
		classAnnotationCache.clear();
		fieldAnnotationCache.clear();
		methodAnnotationCache.clear();
		dirtyURLs.addAll(urlCache);
	}

	/**
	 * Returns a list of all known classes. The ClassMirror for each class is
	 * returned, and further examination can be done on each class, or loadClass
	 * can be called on the ClassMirror to get the actual Class object. No
	 * ClassLoaders are involved directly in this operation.
	 *
	 * @return A list of ClassMirror objects for all known classes
	 */
	public Set<ClassMirror<?>> getKnownClasses() {
		doDiscovery();
		Set<ClassMirror<?>> ret = new HashSet<ClassMirror<?>>();
		for (URL url : urlCache) {
			ret.addAll(getKnownClasses(url));
		}
		return ret;
	}

	/**
	 * Gets all known classes, only within this URL. If this url isn't in the
	 * list of discovery locations, it is automatically added, via
	 * {@link #addDiscoveryLocation(java.net.URL)}.
	 *
	 * @param url
	 * @return
	 */
	public List<ClassMirror<?>> getKnownClasses(URL url) {
		if (url == null) {
			throw new NullPointerException("url cannot be null");
		}
		if (!classCache.containsKey(url)) {
			addDiscoveryLocation(url);
		}
		doDiscovery();
		return new ArrayList<ClassMirror<?>>(classCache.get(url));
	}

	/**
	 * Returns a list of known classes that extend the given superclass, or
	 * implement the given interface.
	 *
	 * @param <T>
	 * @param superType
	 * @return
	 */
	public <T> Set<ClassMirror<T>> getClassesThatExtend(Class<T> superType) {
		if (superType == java.lang.Object.class) {
			//To avoid complication down the road, if this is the case,
			//just return all known classes here.
			return (Set) getKnownClasses();
		}
		if (classSubtypeCache.containsKey(superType)) {
			return new HashSet<ClassMirror<T>>((Set) classSubtypeCache.get(superType));
		}
		doDiscovery();
		Set<ClassMirror<?>> mirrors = new HashSet<ClassMirror<?>>();
		Set<ClassMirror<T>> knownClasses = (Set) getKnownClasses();
		outer:
		for (ClassMirror m : knownClasses) {
			if(doesClassExtend(m, superType)){
				mirrors.add(m);
			}
		}
		classSubtypeCache.put(superType, mirrors);
		return (Set) mirrors;
	}
	
	/**
	 * Returns true iff subClass extends, implements, or is superClass.
	 * This searches the entire known class ecosystem, including the known ClassMirrors
	 * for this information.
	 * @param subClass
	 * @param superClass
	 * @return 
	 */
	public boolean doesClassExtend(ClassMirror subClass, Class superClass){
		if (subClass.directlyExtendsFrom(superClass)) {
				//Trivial case, so just add this now, then continue.
				return true;
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
			ClassReferenceMirror su = subClass.getSuperClass();
			while (!su.getJVMName().equals("Ljava/lang/Object;")) {
				supers.add(su);
				ClassMirror find = getClassMirrorFromJVMName(su.getJVMName());
				if (find == null) {
					try {
						//Ok, have to Class.forName this one
						Class clazz = ClassUtils.forCanonicalName(su.toString());
						//We can just use isAssignableFrom now
						if (superClass.isAssignableFrom(clazz)) {
							return true;
						} else {
							//We need to add change the reference to su
							su = new ClassReferenceMirror("L" + clazz.getSuperclass().getName().replace(".", "/") + ";");
						}
					} catch (ClassNotFoundException ex) {
						//Hmm, ok? I guess something bad happened, so let's break
						//the loop and give up on this class.
						return false;
					}
				} else {
					su = find.getSuperClass();
				}
			}
			//Same thing now, but for interfaces
			Deque<ClassReferenceMirror> interfaces = new ArrayDeque<ClassReferenceMirror>();
			Set<ClassReferenceMirror> handled = new HashSet<ClassReferenceMirror>();
			interfaces.addAll(subClass.getInterfaces());
			//Also have to add all the supers' interfaces too
			for (ClassReferenceMirror r : supers) {
				ClassMirror find = getClassMirrorFromJVMName(r.getJVMName());
				if (find == null) {
					try {
						Class clazz = Class.forName(r.toString());
						for (Class c : clazz.getInterfaces()) {
							interfaces.add(new ClassReferenceMirror("L" + c.getName().replace(".", "/") + ";"));
						}
					} catch (ClassNotFoundException ex) {
						return false;
					}
				} else {
					interfaces.addAll(find.getInterfaces());
				}
			}
			while (!interfaces.isEmpty()) {
				ClassReferenceMirror in = interfaces.pop();
				if(ClassUtils.getJVMName(superClass).equals(in.getJVMName())){
					//Early short circuit. We know it's in the the list already.
					return true;
				}
				if (handled.contains(in)) {
					continue;
				}
				handled.add(in);
				supers.add(in);
				ClassMirror find = getClassMirrorFromJVMName(in.getJVMName());
				if (find != null) {
					interfaces.addAll(find.getInterfaces());
				} else {
					try {
						//Again, have to check Class.forName
						Class clazz = ClassUtils.forCanonicalName(in.toString());
						if (superClass.isAssignableFrom(clazz)) {
							return true;
						}
					} catch (ClassNotFoundException ex) {
						return false;
					}
				}
			}
			//Nope.
			return false;
	}

	/**
	 * Unlike {@link #getClassesThatExtend(java.lang.Class)}, this actually
	 * loads the matching classes into PermGen, and returns a Set of these
	 * classes. This is useful if you are for sure going to use these classes
	 * immediately, and don't want to have to lazy load them individually.
	 *
	 * @param <T>
	 * @param superType
	 * @return
	 */
	public <T> Set<Class<T>> loadClassesThatExtend(Class<T> superType) {
		return loadClassesThatExtend(superType, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getClassesThatExtend(java.lang.Class)}, this actually
	 * loads the matching classes into PermGen, and returns a Set of these
	 * classes. This is useful if you are for sure going to use these classes
	 * immediately, and don't want to have to lazy load them individually.
	 *
	 * @param <T>
	 * @param superType
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public <T> Set<Class<T>> loadClassesThatExtend(Class<T> superType, ClassLoader loader, boolean initialize) {
		Set<Class<T>> set = new HashSet<Class<T>>();
		for (ClassMirror<T> cm : getClassesThatExtend(superType)) {
			set.add(cm.loadClass(loader, initialize));
		}
		return set;
	}

	private ClassMirror getClassMirrorFromJVMName(String className) {
		if (jvmNameToMirror.containsKey(className)) {
			return jvmNameToMirror.get(className);
		}
		for (ClassMirror c : getKnownClasses()) {
			if (("L" + c.getJVMClassName() + ";").equals(className)) {
				jvmNameToMirror.put("L" + c.getJVMClassName() + ";", c);
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
	 *
	 * @param annotation
	 * @return
	 */
	public Set<ClassMirror<?>> getClassesWithAnnotation(Class<? extends Annotation> annotation) {
		if (classAnnotationCache.containsKey(annotation)) {
			return new HashSet<ClassMirror<?>>(classAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<ClassMirror<?>> mirrors = new HashSet<ClassMirror<?>>();
		for (ClassMirror m : getKnownClasses()) {
			if (m.hasAnnotation(annotation)) {
				mirrors.add(m);
			}
		}
		classAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}
	
	/**
	 * Combines finding classes with a specified annotation, and classes that extend a certain type.
	 * @param <T> The type that will be returned, based on superClass
	 * @param annotation The annotation that the classes should be tagged with
	 * @param superClass The super class that the classes should extend
	 * @return A set of class mirrors that match the criteria
	 */
	public <T> Set<ClassMirror<T>> getClassesWithAnnotationThatExtend(Class<? extends Annotation> annotation, Class<T> superClass){
		Set<ClassMirror<T>> mirrors = new HashSet<ClassMirror<T>>();
		for(ClassMirror<?> c : getClassesWithAnnotation(annotation)){
			if(doesClassExtend(c, superClass)){
				mirrors.add((ClassMirror<T>)c);
			}
		}
		return mirrors;
	}
	
	/**
	 * Unlike {@link #getClassesWithAnnotationThatExtend(java.lang.Class, java.lang.Class)}, this actually 
	 * loads the matching classes into PermGen, and returns a Set of these classes.
	 * This is useful if you are for sure going to use these classes immediately, and don't want to have
	 * to lazy load them individually.
	 * @param <T> The type that will be returned, based on superClass
	 * @param annotation The annotation that the classes should be tagged with
	 * @param superClass The super class that the classes should extend
	 * @return A set of classes that match the criteria
	 */
	public <T> Set<Class<T>> loadClassesWithAnnotationThatExtend(Class<? extends Annotation> annotation, Class<T> superClass){
		return loadClassesWithAnnotationThatExtend(annotation, superClass, getDefaultClassLoader(), true);
	}
	
	/**
	 * Unlike {@link #getClassesWithAnnotationThatExtend(java.lang.Class, java.lang.Class)}, this actually 
	 * loads the matching classes into PermGen, and returns a Set of these classes.
	 * This is useful if you are for sure going to use these classes immediately, and don't want to have
	 * to lazy load them individually.
	 * @param <T> The type that will be returned, based on superClass
	 * @param annotation The annotation that the classes should be tagged with
	 * @param superClass The super class that the classes should extend
	 * @param loader
	 * @param initialize
	 * @return A set of classes that match the criteria
	 */
	public <T> Set<Class<T>> loadClassesWithAnnotationThatExtend(Class<? extends Annotation> annotation, Class<T> superClass, ClassLoader loader, boolean initialize){
		Set<Class<T>> set = new HashSet<Class<T>>();
		for (ClassMirror<T> cm : getClassesWithAnnotationThatExtend(annotation, superClass)) {
			try {
				set.add(cm.loadClass(loader, initialize));
			} catch (NoClassDefFoundError e) {
				//Ignore this for now?
				//throw new Error("While trying to process " + cm.toString() + ", an error occurred.", e);
			}
		}
		return set;
	}

	/**
	 * Unlike {@link #getClassesWithAnnotation(java.lang.Class)}, this actually
	 * loads the matching classes into PermGen, and returns a Set of these
	 * classes. This is useful if you are for sure going to use these classes
	 * immediately, and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<Class> loadClassesWithAnnotation(Class<? extends Annotation> annotation) {
		return loadClassesWithAnnotation(annotation, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getClassesWithAnnotation(java.lang.Class)}, this actually
	 * loads the matching classes into PermGen, and returns a Set of these
	 * classes. This is useful if you are for sure going to use these classes
	 * immediately, and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public Set<Class> loadClassesWithAnnotation(Class<? extends Annotation> annotation, ClassLoader loader, boolean initialize) {
		Set<Class> set = new HashSet<Class>();
		for (ClassMirror<?> cm : getClassesWithAnnotation(annotation)) {
			try {
				set.add(cm.loadClass(loader, initialize));
			} catch (NoClassDefFoundError e) {
				//Ignore this for now?
				//throw new Error("While trying to process " + cm.toString() + ", an error occurred.", e);
			}
		}
		return set;
	}

	/**
	 * Returns a list of fields that have been annotated with the specified
	 * annotation. This will work with annotations that have been declared with
	 * the {@link RetentionPolicy#CLASS} property.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<FieldMirror> getFieldsWithAnnotation(Class<? extends Annotation> annotation) {
		if (fieldAnnotationCache.containsKey(annotation)) {
			return new HashSet<FieldMirror>(fieldAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<FieldMirror> mirrors = new HashSet<FieldMirror>();
		for (ClassMirror m : getKnownClasses()) {
			for (FieldMirror f : m.getFields()) {
				if (f.hasAnnotation(annotation)) {
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
	 *
	 * @param annotation
	 * @return
	 */
	public Set<MethodMirror> getMethodsWithAnnotation(Class<? extends Annotation> annotation) {
		if (methodAnnotationCache.containsKey(annotation)) {
			return new HashSet<MethodMirror>(methodAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<MethodMirror> mirrors = new HashSet<MethodMirror>();
		for (ClassMirror m : getKnownClasses()) {
			for (MethodMirror mm : m.getMethods()) {
				if (mm.hasAnnotation(annotation)) {
					mirrors.add(mm);
				}
			}
		}
		methodAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}

	/**
	 * Unlike {@link #getMethodsWithAnnotation(java.lang.Class)}, this actually
	 * loads the matching method's containing classes into PermGen, and returns
	 * a Set of Method objects. This is useful if you are for sure going to use
	 * these methods immediately, and don't want to have to lazy load them
	 * individually.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<Method> loadMethodsWithAnnotation(Class<? extends Annotation> annotation) {
		return loadMethodsWithAnnotation(annotation, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getMethodsWithAnnotation(java.lang.Class)}, this actually
	 * loads the matching method's containing classes into PermGen, and returns
	 * a Set of Method objects. This is useful if you are for sure going to use
	 * these methods immediately, and don't want to have to lazy load them
	 * individually.
	 *
	 * @param annotation
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public Set<Method> loadMethodsWithAnnotation(Class<? extends Annotation> annotation, ClassLoader loader, boolean initialize) {
		try {
			Set<Method> set = new HashSet<Method>();
			for (MethodMirror mm : getMethodsWithAnnotation(annotation)) {
				set.add(mm.loadMethod(loader, initialize));
			}
			return set;
		} catch (ClassNotFoundException ex) {
			throw new NoClassDefFoundError();
		}
	}

	/**
	 * Returns the ClassMirror object for a given class name. Either the JVM
	 * name, or canonical name works.
	 *
	 * @param className
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public ClassMirror forName(String className) throws ClassNotFoundException {
		if (forNameCache.containsKey(className)) {
			return forNameCache.get(className);
		}
		for (ClassMirror<?> c : getKnownClasses()) {
			if (c.getClassName().equals(className) || c.getJVMClassName().equals(className)) {
				forNameCache.put(className, c);
				return c;
			}
		}
		throw new ClassNotFoundException(className);
	}

	/**
	 * Calls forFuzzyName with initialize true, and the class loader used to
	 * load this class.
	 *
	 * @param packageRegex
	 * @param className
	 * @return
	 */
	public ClassMirror forFuzzyName(String packageRegex, String className) {
		return forFuzzyName(packageRegex, className, true, getDefaultClassLoader());
	}

	/**
	 * Returns a class given a "fuzzy" package name, that is, the package name
	 * provided is a regex. The class name must match exactly, but the package
	 * name will be the closest match, or undefined if there is no clear
	 * candidate. If no matches are found, null is returned.
	 *
	 * @param packageRegex
	 * @param className
	 * @param initialize
	 * @param classLoader
	 * @return
	 */
	public ClassMirror forFuzzyName(String packageRegex, String className, boolean initialize, ClassLoader classLoader) {
		String index = packageRegex + className;
		if (fuzzyClassCache.containsKey(index)) {
			return fuzzyClassCache.get(index);
		}
		Set<ClassMirror> found = new HashSet<ClassMirror>();
		Set<ClassMirror<?>> searchSpace = getKnownClasses();
		for (ClassMirror c : searchSpace) {
			if (c.getPackage().getName().matches(packageRegex) && c.getSimpleName().equals(className)) {
				found.add(c);
			}
		}
		ClassMirror find;
		if (found.size() == 1) {
			find = found.iterator().next();
		} else if (found.isEmpty()) {
			find = null;
		} else {
			ClassMirror candidate = null;
			int max = Integer.MAX_VALUE;
			for (ClassMirror f : found) {
				int distance = StringUtils.LevenshteinDistance(f.getPackage().getName(), packageRegex);
				if (distance < max) {
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
			File [] list = start.listFiles();
			if(list == null){
				System.out.println("Could not list files in " + start);
				return;
			}
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
		while (c.isMemberClass() || c.isAnonymousClass()) {
			c = c.getEnclosingClass(); //Get the actual enclosing file
		}
		if (c.getProtectionDomain().getCodeSource() == null) {
			//This is a proxy or other dynamically generated class, and has no physical container,
			//so just return null.
			return null;
		}
		String packageRoot = null;
		//This is the full path to THIS file, but we need to get the package root below.
		String thisClass = c.getResource(c.getSimpleName() + ".class").toString();
		try {
			try {
				packageRoot = StringUtils.replaceLast(thisClass, Pattern.quote(c.getName().replaceAll("\\.", "/") + ".class"), "");
			} catch (Exception e) {
				//Hmm, ok, try this then
				packageRoot = c.getProtectionDomain().getCodeSource().getLocation().toString();
			}
			packageRoot = URLDecoder.decode(packageRoot, "UTF-8");
			return new URL(packageRoot);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("While interrogating " + c.getName() + ", an unexpected exception was thrown.", e);
		} catch (MalformedURLException e) {
			throw new RuntimeException("While interrogating " + c.getName() + ", an unexpected exception was thrown for potential URL: \"" + packageRoot + "\"", e);
		}
	}
}
