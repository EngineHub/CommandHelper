package com.laytonsmith.PureUtilities.ClassLoading;

import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.AbstractMethodMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirrorVisitor;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassReferenceMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ConstructorMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.ProgressIterator;
import com.laytonsmith.PureUtilities.ZipIterator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.objectweb.asm.ClassReader;

/**
 * This class contains methods for dynamically determining things about Classes, without loading them into PermGen.
 * Search criteria is provided, (most notably annotations, however also subclasses) and Class/Field/Method mirrors are
 * returned, eliminating the PermGen requirements, even though all known classes are scanned against. It is then up to
 * the calling method to actually determine if the classes need to be loaded, thereby deferring all logic to actually
 * take up more PermGen space to the calling code, instead of this class.
 */
public class ClassDiscovery {

	private static final boolean IS_DEBUG = java.lang.management.ManagementFactory.getRuntimeMXBean()
			.getInputArguments().toString().contains("jdwp");

	/**
	 * The default instance.
	 */
	private static ClassDiscovery defaultInstance = null;

	/**
	 * Returns the default, shared instance. This is usually how you want to gain a reference to this class, as caching
	 * can often times be shared among multiple tasks, though if you need a private instance, you can use the
	 * constructor to create a new one. Using this instance automatically checks for the jarInfo.ser file in this jar,
	 * and if present, adds it to the cache.
	 *
	 * @return
	 */
	public static synchronized ClassDiscovery getDefaultInstance() {
		if(defaultInstance == null) {
			defaultInstance = new ClassDiscovery();
			//defaultInstance.setDebugMode(true);
		}
		return defaultInstance;
	}

	/**
	 * Can be used to set the default ClassDiscovery instance returned by getDefaultInstance. Setting it to null is
	 * acceptable, and then a new, default ClassDiscovery instance will be generated.
	 *
	 * @param cd
	 */
	public static void setDefaultInstance(ClassDiscovery cd) {
		defaultInstance = cd;
	}

	/**
	 * Creates a new instance of the ClassDiscovery class. Normally, you should probably just use the default instance,
	 * as caching across the board is a good thing, however, it may be the case that you need a standalone instance, in
	 * which case, you can create a new one.
	 */
	public ClassDiscovery() {
		//
	}

	/**
	 * Stores the mapping of class name to ClassMirror object. At any given time, after doDiscovery is called, this will
	 * be up to date with all known classes.
	 */
	private final Map<URL, Set<ClassMirror<?>>> classCache = new HashMap<>();
	/**
	 * This cache maps jvm name to the associated ClassMirror object, to speed up lookups.
	 */
	private final Map<String, ClassMirror<?>> jvmNameToMirror = new HashMap<>();
	/**
	 * Maps the fuzzy class name to actual Class object.
	 */
	private final Map<String, ClassMirror<?>> fuzzyClassCache = new HashMap<>();
	/**
	 * Maps the forName cache.
	 */
	private final Map<String, ClassMirror<?>> forNameCache = new HashMap<>();
	/**
	 * List of all URLs from which to pull classes.
	 */
	private final Set<URL> urlCache = new HashSet<>();
	/**
	 * When a URL is added to urlCache, it is also initially added here. If there are any URLs in this set, they must be
	 * resolved first.
	 */
	private final Set<URL> dirtyURLs = new HashSet<>();
	/**
	 * Cache for class subtypes. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private final Map<Class<?>, Set<ClassMirror<?>>> classSubtypeCache = new HashMap<>();
	/**
	 * Cache for class annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<ClassMirror<?>>> classAnnotationCache = new HashMap<>();
	/**
	 * Cache for field annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<FieldMirror>> fieldAnnotationCache = new HashMap<>();
	/**
	 * Cache for method annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<MethodMirror>> methodAnnotationCache = new HashMap<>();
	/**
	 * Cache for constructor annotations. Whenever a new URL is added to the URL cache, this is cleared.
	 */
	private final Map<Class<? extends Annotation>, Set<ConstructorMirror<?>>> constructorAnnotationCache = new HashMap<>();
	private final Map<Pair<Class<? extends Annotation>, Class<?>>, Set<ClassMirror<?>>>
			classesWithAnnotationThatExtendCache = new HashMap<>();

	/**
	 * Cache for mapping real classes to class mirrors. This is not cleared when a new URL is added, since the mapping
	 * would always be the same anyways.
	 */
	private final Map<Class<?>, ClassMirror<?>> classToMirrorCache = new HashMap<>();
	/**
	 * By default null, but this can be set per instance.
	 */
	private ProgressIterator progressIterator = null;
	/**
	 * External cache. If added before discovery happens for a URL, this will cause the discovery process to be skipped
	 * entirely for a given URL.
	 */
	private final Map<URL, ClassDiscoveryURLCache> preCaches = new HashMap<>();
	/**
	 * If true, debug information will be printed out.
	 */
	private boolean debug;

	/**
	 * May be null, but if set, is the cache retriever.
	 */
	private ClassDiscoveryCache classDiscoveryCache;

	/**
	 * Turns debug mode on. If true, data about what is happening is printed out, as well as timing information.
	 *
	 * @param on
	 */
	public void setDebugMode(boolean on) {
		debug = on;
	}

	/**
	 * Removes the cache for this URL. After calling this, it is ensured that the discovery methods won't be pulling
	 * from a cache. This is used during initial cache creation.
	 *
	 * @param url
	 */
	public void removePreCache(URL url) {
		if(url == null) {
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
		if(url == null) {
			throw new NullPointerException("url cannot be null");
		}
		if(debug) {
			StreamUtils.GetSystemOut().println("Adding precache for " + url);
		}
		preCaches.put(url, cache);
	}

	/**
	 * Sets the class discovery cache. This is optional, but if set is used to potentially speed up caching.
	 *
	 * @param cache
	 */
	public void setClassDiscoveryCache(ClassDiscoveryCache cache) {
		this.classDiscoveryCache = cache;
	}

	/**
	 * Sets the progress iterator for when this class starts up. This is an optional operation.
	 *
	 * @param progressIterator
	 */
	public void setProgressIterator(ProgressIterator progressIterator) {
		this.progressIterator = progressIterator;
	}

	/**
	 * Looks through all the URLs and pulls out all known classes, and caches them in the classCache object.
	 */
	private synchronized void doDiscovery() {
		if(!dirtyURLs.isEmpty()) {
			Iterator<URL> it = dirtyURLs.iterator();
			while(it.hasNext()) {
				discover(it.next());
				it.remove();
			}
		}
	}

	/**
	 * Does the class discovery for this particular URL. This should only be called by doDiscovery. Other internal
	 * methods should call doDiscovery, which handles looking through the dirtyURLs.
	 */
	private synchronized void discover(URL rootLocation) {
		long start = System.currentTimeMillis();
		if(debug) {
			StreamUtils.GetSystemOut().println("Beginning discovery of " + rootLocation);
		}
		try {
			//If the ClassDiscoveryCache is set, just use this.
			if(classDiscoveryCache != null) {
				ClassDiscoveryURLCache cduc = classDiscoveryCache.getURLCache(rootLocation);
				preCaches.put(rootLocation, cduc);
			}

			String url;
			try {
				url = URLDecoder.decode(rootLocation.toString(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				// apparently this should never happen, but we have to catch it anyway
				url = null;
			}

			if(url == null) {
				url = GetClassContainer(ClassDiscovery.class).toString();
			}
			final File rootLocationFile;
			if(!classCache.containsKey(rootLocation)) {
				classCache.put(rootLocation, Collections.synchronizedSet(new HashSet<>()));
			} else {
				classCache.get(rootLocation).clear();
			}
			final Set<ClassMirror<?>> mirrors = classCache.get(rootLocation);
			if(preCaches.containsKey(rootLocation)) {
				if(debug) {
					StreamUtils.GetSystemOut().println("Precache already contains this URL, so using it");
				}
				//No need, already got a cache for this url
				mirrors.addAll(preCaches.get(rootLocation).getClasses());
				return;
			}
			if(debug) {
				StreamUtils.GetSystemOut().println("Precache does not contain data for this URL, so scanning now.");
			}
			url = url.replaceFirst("^jar:", "");
			if(url.endsWith("!/")) {
				url = StringUtils.replaceLast(url, "!/", "");
			}
			if(url.startsWith("file:") && !url.endsWith(".jar")) {
				final AtomicInteger id = new AtomicInteger(0);
//				ExecutorService service = Executors.newFixedThreadPool(10, new ThreadFactory() {
//					@Override
//					public Thread newThread(Runnable r) {
//						return new Thread(r, "ClassDiscovery-Async-" + id.incrementAndGet());
//					}
//				});

				//Remove file: from the front
				String root = url.substring(5);
				rootLocationFile = new File(root);
				List<File> fileList = new ArrayList<>();
				descend(new File(root), fileList);

				//Now, we have all the class files in the package. But, it's the absolute path
				//to all of them. We have to first remove the "front" part
				for(File f : fileList) {
					String file = f.toString();
					if(!file.matches(".*\\$(?:\\d)*\\.class") && file.endsWith(".class")) {
						InputStream stream = null;
						try {
							stream = FileUtil.readAsStream(new File(rootLocationFile,
									f.getAbsolutePath().replaceFirst(Pattern.quote(new File(root).getAbsolutePath() + File.separator), "")));
							ClassReader reader = new ClassReader(stream);
							ClassMirrorVisitor mirrorVisitor = new ClassMirrorVisitor();
							reader.accept(mirrorVisitor, ClassReader.SKIP_FRAMES);
							mirrors.add(mirrorVisitor.getMirror(new URL(url)));
						} catch (IOException ex) {
							Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
						} finally {
							if(stream != null) {
								try {
									stream.close();
								} catch (IOException ex) {
									Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
								}
							}
						}
					}
				}
//				service.shutdown();
//				try {
//					//Doesn't look like 0 is an option, so we'll just wait a day.
//					service.awaitTermination(1, TimeUnit.DAYS);
//				} catch (InterruptedException ex) {
//					Logger.getLogger(ClassDiscovery.class.getName()).log(Level.SEVERE, null, ex);
//				}
			} else if(url.startsWith("file:") && url.endsWith(".jar")) {
				//We are running from a jar
				url = url.replaceFirst("file:", "");
				rootLocationFile = new File(url);
				ZipIterator zi = new ZipIterator(rootLocationFile);
				try {
					zi.iterate(new ZipIterator.ZipIteratorCallback() {
						@Override
						public void handle(String filename, InputStream in) {
							if(!filename.matches(".*\\$(?:\\d)*\\.class") && filename.endsWith(".class")) {
								try {
									ClassReader reader = new ClassReader(in);
									ClassMirrorVisitor mirrorVisitor = new ClassMirrorVisitor();
									reader.accept(mirrorVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
									mirrors.add(mirrorVisitor.getMirror(rootLocationFile.toURI().toURL()));
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
		} catch (RuntimeException e) {
			e.printStackTrace(System.err);
		} finally {
			if(debug) {
				StreamUtils.GetSystemOut().println("Scans finished for " + rootLocation + ", taking " + (System.currentTimeMillis() - start) + " ms.");
			}
		}
	}
	private ClassLoader defaultClassLoader = null;

	/**
	 * Sets the default class loader for the various load methods that are called without a ClassLoader. This is
	 * optional, and if not set, the class loader of this class is used.
	 *
	 * @param cl
	 */
	public void setDefaultClassLoader(ClassLoader cl) {
		defaultClassLoader = cl;
	}

	/**
	 * Gets the classloader set with {@link #setDefaultClassLoader(java.lang.ClassLoader)}, or the builtin default if
	 * none was specified ever. Regardless, never returns null.
	 *
	 * @return
	 */
	public ClassLoader getDefaultClassLoader() {
		if(defaultClassLoader == null) {
			return ClassDiscovery.class.getClassLoader();
		} else {
			return defaultClassLoader;
		}
	}

	/**
	 * Adds the jar that the calling class is in to the discovery location. This is equivalent to running
	 * {@code addDiscoveryLocation(ClassDiscovery.GetClassContainer(this.getClass()));}
	 */
	public void addThisJar() {
		this.addDiscoveryLocation(GetClassContainer(StackTraceUtils.getCallingClass()));
	}

	/**
	 * Adds a new discovery URL. This makes the URL eligible to be included when finding classes/methods/fields with the
	 * various other methods. If the URL already has been added, this has no effect.
	 *
	 * @param url
	 */
	public synchronized void addDiscoveryLocation(URL url) {
		if(url == null) {
			throw new NullPointerException("url cannot be null");
		}
		if(urlCache.contains(url)) {
			//Already here, so just return.
			return;
		}
		urlCache.add(url);
		dirtyURLs.add(url);
		classCache.put(url, new HashSet<>());
	}

	/**
	 * Searches one deep, finding all jar files, and adds them, using addDiscoveryLocation. If folder doesn't exist, is
	 * null, doesn't contain any jars, or otherwise can't be read, nothing happens.
	 *
	 * @param folder
	 */
	public void addAllJarsInFolder(File folder) {
		if(folder != null && folder.exists() && folder.isDirectory()) {
			for(File f : folder.listFiles()) {
				if(f.getName().endsWith(".jar")) {
					try {
						addDiscoveryLocation(f.toURI().toURL());
					} catch (MalformedURLException ex) {
						//
					}
				}
			}
		}
	}

	/**
	 * Remove a discovery URL. Will invalidate caches.
	 *
	 * @param url
	 */
	public synchronized void removeDiscoveryLocation(URL url) {
		if(url == null) {
			throw new NullPointerException("url cannot be null");
		}

		if(!urlCache.contains(url)) {
			//Not here, so just return.
			return;
		}

		urlCache.remove(url);
		dirtyURLs.remove(url);
		preCaches.remove(url);

		invalidateCaches();
	}

	/**
	 * Clears the internal caches. This is called automatically when a new discovery location is added with
	 * addDiscoveryLocation, but this should be called if the caches could have become invalidated since the last load,
	 * as well as if the reference to any of the class loaders that loaded any classes during the course of using this
	 * instance need to be garbage collected.
	 */
	public void invalidateCaches() {
		classCache.clear();
		forNameCache.clear();
		jvmNameToMirror.clear();
		fuzzyClassCache.clear();
		classAnnotationCache.clear();
		fieldAnnotationCache.clear();
		methodAnnotationCache.clear();
		constructorAnnotationCache.clear();
		classesWithAnnotationThatExtendCache.clear();
		dirtyURLs.addAll(urlCache);
	}

	/**
	 * Returns a list of all known classes. The ClassMirror for each class is returned, and further examination can be
	 * done on each class, or loadClass can be called on the ClassMirror to get the actual Class object. No ClassLoaders
	 * are involved directly in this operation.
	 *
	 * @return A list of ClassMirror objects for all known classes
	 */
	public Set<ClassMirror<?>> getKnownClasses() {
		doDiscovery();
		Set<ClassMirror<?>> ret = new HashSet<>();
		for(URL url : urlCache) {
			ret.addAll(getKnownClasses(url));
		}
		return ret;
	}

	/**
	 * Gets all known classes, only within this URL. If this url isn't in the list of discovery locations, it is
	 * automatically added, via {@link #addDiscoveryLocation(java.net.URL)}.
	 *
	 * @param url
	 * @return
	 */
	public List<ClassMirror<?>> getKnownClasses(URL url) {
		if(url == null) {
			throw new NullPointerException("url cannot be null");
		}
		if(!classCache.containsKey(url)) {
			addDiscoveryLocation(url);
		}
		doDiscovery();
		return new ArrayList<>(classCache.get(url));
	}

	/**
	 * Returns a list of known classes that extend the given superclass, or implement the given interface.
	 *
	 * @param <T>
	 * @param superType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<ClassMirror<T>> getClassesThatExtend(Class<T> superType) {
		if(superType == java.lang.Object.class) {
			//To avoid complication down the road, if this is the case,
			//just return all known classes here.
			// Ugh, this double cast though. This is definitely safe, since
			// everything extends Object in java, but to get the compiler to
			// shut up, we have to supress warnings and double cast it.
			return (Set<ClassMirror<T>>) (Set<?>) getKnownClasses();
		}
		if(classSubtypeCache.containsKey(superType)) {
			return new HashSet<>((Set) classSubtypeCache.get(superType));
		}
		doDiscovery();
		Set<ClassMirror<?>> mirrors = new HashSet<>();
		Set<ClassMirror<T>> knownClasses = (Set) getKnownClasses();
		outer:
		for(ClassMirror<T> m : knownClasses) {
			if(doesClassExtend(m, superType)) {
				mirrors.add(m);
			}
		}
		classSubtypeCache.put(superType, mirrors);
		return (Set) mirrors;
	}

	/**
	 * Returns true if subClass extends, implements, or is superClass. This searches the entire known class ecosystem,
	 * including the known ClassMirrors for this information.
	 *
	 * @param subClass
	 * @param superClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean doesClassExtend(ClassMirror<?> subClass, Class<?> superClass) {
		if(subClass.directlyExtendsFrom(superClass)) {
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
		Set<ClassReferenceMirror<?>> supers = new HashSet<>();
		// Interfaces don't have a superclass. If they extend something, that's different.
		if(!subClass.isInterface()) {
			//Get the superclass. If it's java.lang.Object, we're done.
			ClassReferenceMirror<?> su = subClass.getSuperClass();
			while(!"Ljava/lang/Object;".equals(su.getJVMName())) {
				supers.add(su);
				ClassMirror<?> find = getClassMirrorFromJVMName(su.getJVMName());
				if(find == null) {
					try {
						//Ok, have to Class.forName this one
						Class<?> clazz = ClassUtils.forCanonicalName(su.toString(), false, defaultClassLoader);
						//We can just use isAssignableFrom now
						if(superClass.isAssignableFrom(clazz)) {
							return true;
						} else {
							//We need to add change the reference to su
							su = new ClassReferenceMirror<>("L" + clazz.getSuperclass().getName().replace('.', '/') + ";");
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
			for(ClassReferenceMirror<?> r : supers) {
				// Look through the supers. If any of them equal the search class, return true
				if(r.getJVMName().equals(ClassUtils.getJVMName(superClass))) {
					return true;
				}
			}
		}
		//Same thing now, but for interfaces
		Deque<ClassReferenceMirror<?>> interfaces = new ArrayDeque<>();
		Set<ClassReferenceMirror<?>> handled = new HashSet<>();
		interfaces.addAll(subClass.getInterfaces());
		//Also have to add all the supers' interfaces too
		for(ClassReferenceMirror<?> r : supers) {
			ClassMirror<?> find = getClassMirrorFromJVMName(r.getJVMName());
			if(find == null) {
				try {
					Class<?> clazz = Class.forName(r.toString());
					for(Class<?> c : clazz.getInterfaces()) {
						interfaces.add(new ClassReferenceMirror<>("L" + c.getName().replace('.', '/') + ";"));
					}
				} catch (ClassNotFoundException ex) {
					return false;
				}
			} else {
				interfaces.addAll(find.getInterfaces());
			}
		}
		while(!interfaces.isEmpty()) {
			ClassReferenceMirror<?> in = interfaces.pop();
			if(ClassUtils.getJVMName(superClass).equals(in.getJVMName())) {
				//Early short circuit. We know it's in the the list already.
				return true;
			}
			if(handled.contains(in)) {
				continue;
			}
			handled.add(in);
			supers.add(in);
			ClassMirror<?> find = getClassMirrorFromJVMName(in.getJVMName());
			if(find != null) {
				interfaces.addAll(find.getInterfaces());
			} else {
				try {
					//Again, have to check Class.forName
					Class<?> clazz = ClassUtils.forCanonicalName(in.toString(), false, getDefaultClassLoader());
					if(superClass.isAssignableFrom(clazz)) {
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
	 * Unlike {@link #getClassesThatExtend(java.lang.Class)}, this actually loads the matching classes into PermGen, and
	 * returns a Set of these classes. This is useful if you are for sure going to use these classes immediately, and
	 * don't want to have to lazy load them individually.
	 *
	 * @param <T>
	 * @param superType
	 * @return
	 */
	public <T> Set<Class<T>> loadClassesThatExtend(Class<T> superType) {
		return loadClassesThatExtend(superType, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getClassesThatExtend(java.lang.Class)}, this actually loads the matching classes into PermGen, and
	 * returns a Set of these classes. This is useful if you are for sure going to use these classes immediately, and
	 * don't want to have to lazy load them individually.
	 *
	 * @param <T>
	 * @param superType
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public <T> Set<Class<T>> loadClassesThatExtend(Class<T> superType, ClassLoader loader, boolean initialize) {
		Set<Class<T>> set = new HashSet<>();
		for(ClassMirror<T> cm : getClassesThatExtend(superType)) {
			set.add(cm.loadClass(loader, initialize));
		}
		return set;
	}

	private ClassMirror<?> getClassMirrorFromJVMName(String className) {
		if(jvmNameToMirror.containsKey(className)) {
			return jvmNameToMirror.get(className);
		}
		for(ClassMirror<?> c : getKnownClasses()) {
			if(c.getJVMClassName().equals(className)) {
				jvmNameToMirror.put(c.getJVMClassName(), c);
				return c;
			}
		}
		//Still not found? Return null then.
		jvmNameToMirror.put(className, null);
		return null;
	}

	/**
	 * Returns a list of classes that have been annotated with the specified annotation. This will work with annotations
	 * that have been declared with the {@link RetentionPolicy#CLASS} property.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<ClassMirror<?>> getClassesWithAnnotation(Class<? extends Annotation> annotation) {
		if(classAnnotationCache.containsKey(annotation)) {
			return new HashSet<>(classAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<ClassMirror<?>> mirrors = new HashSet<>();
		for(ClassMirror<?> m : getKnownClasses()) {
			if(m.hasAnnotation(annotation)) {
				mirrors.add(m);
			}
		}
		classAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}

	/**
	 * Combines finding classes with a specified annotation, and classes that extend a certain type.
	 *
	 * @param <T> The type that will be returned, based on superClass
	 * @param annotation The annotation that the classes should be tagged with
	 * @param superClass The super class that the classes should extend
	 * @return A set of class mirrors that match the criteria
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<ClassMirror<? extends T>> getClassesWithAnnotationThatExtend(Class<? extends Annotation> annotation, Class<T> superClass) {
		Pair<Class<? extends Annotation>, Class<?>> id = new Pair<>(annotation, superClass);
		if(classesWithAnnotationThatExtendCache.containsKey(id)) {
			// This (insane) double cast is necessary, because the cache will certainly contain the value of the
			// correct type,
			// but there's no way for us to encode T into the generic type of the definition, so we just do this,
			// lie to the compiler, and go about our merry way. We do the same below.
			// I'm totally open to a better approach though.
			return (Set<ClassMirror<? extends T>>) (Object) classesWithAnnotationThatExtendCache.get(id);
		}
		Set<ClassMirror<? extends T>> mirrors = new HashSet<>();
		for(ClassMirror<?> c : getClassesWithAnnotation(annotation)) {
			if(doesClassExtend(c, superClass)) {
				mirrors.add((ClassMirror<T>) c);
			}
		}
		if(superClass.getAnnotation(annotation) != null) {
			// The mechanism above won't automatically add this class, so we need to add it
			// ourselves here.
			mirrors.add(new ClassMirror<>(superClass));
		}
		classesWithAnnotationThatExtendCache.put(id, (Set<ClassMirror<?>>) (Object) mirrors);
		return mirrors;
	}

	/**
	 * Returns the ClassMirror for the given Class, if it exists in the ecosystem. This is useful for obtaining
	 * reflective information that ClassMirror provides, but Class doesn't. Note, however, this will only be able
	 * to find classes that were loaded into the ecosystem in the first place, which may preclude most classes in
	 * the actual ecosystem, including the core Java classes.
	 * @param <T>
	 * @param clazz
	 * @return
	 * @throws NoClassDefFoundError
	 */
	public <T> ClassMirror<T> getMirrorFromClass(Class<T> clazz) throws NoClassDefFoundError {
		ClassMirror<?> cm = classToMirrorCache.get(clazz);
		if(cm == null) {
			for(ClassMirror<?> m : getKnownClasses()) {
				if(m.getClassName().equals(clazz.getName().replace("$", "."))) {
					cm = m;
					classToMirrorCache.put(clazz, cm);
					break;
				}
			}
			if(cm == null) {
				throw new NoClassDefFoundError("ClassDiscovery was unable to locate the class "
						+ clazz + ". This can only load classes that are available to the ClassDiscovery system.");
			}
		}
		return (ClassMirror<T>) cm;

	}

	/**
	 * Unlike {@link #getClassesWithAnnotationThatExtend(java.lang.Class, java.lang.Class)}, this actually loads the
	 * matching classes into PermGen, and returns a Set of these classes. This is useful if you are for sure going to
	 * use these classes immediately, and don't want to have to lazy load them individually.
	 *
	 * @param <T> The type that will be returned, based on superClass
	 * @param annotation The annotation that the classes should be tagged with
	 * @param superClass The super class that the classes should extend
	 * @return A set of classes that match the criteria
	 */
	public <T> Set<Class<? extends T>> loadClassesWithAnnotationThatExtend(Class<? extends Annotation> annotation, Class<T> superClass) {
		return loadClassesWithAnnotationThatExtend(annotation, superClass, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getClassesWithAnnotationThatExtend(java.lang.Class, java.lang.Class)}, this actually loads the
	 * matching classes into PermGen, and returns a Set of these classes. This is useful if you are for sure going to
	 * use these classes immediately, and don't want to have to lazy load them individually.
	 *
	 * @param <T> The type that will be returned, based on superClass
	 * @param annotation The annotation that the classes should be tagged with
	 * @param superClass The super class that the classes should extend
	 * @param loader
	 * @param initialize
	 * @return A set of classes that match the criteria
	 */
	public <T> Set<Class<? extends T>> loadClassesWithAnnotationThatExtend(Class<? extends Annotation> annotation, Class<T> superClass, ClassLoader loader, boolean initialize) {
		Set<Class<? extends T>> set = new HashSet<>();
		for(ClassMirror<? extends T> cm : getClassesWithAnnotationThatExtend(annotation, superClass)) {
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
	 * Unlike {@link #getClassesWithAnnotation(java.lang.Class)}, this actually loads the matching classes into PermGen,
	 * and returns a Set of these classes. This is useful if you are for sure going to use these classes immediately,
	 * and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<Class<?>> loadClassesWithAnnotation(Class<? extends Annotation> annotation) {
		return loadClassesWithAnnotation(annotation, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getClassesWithAnnotation(java.lang.Class)}, this actually loads the matching classes into PermGen,
	 * and returns a Set of these classes. This is useful if you are for sure going to use these classes immediately,
	 * and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public Set<Class<?>> loadClassesWithAnnotation(Class<? extends Annotation> annotation, ClassLoader loader, boolean initialize) {
		Set<Class<?>> set = new HashSet<>();
		for(ClassMirror<?> cm : getClassesWithAnnotation(annotation)) {
			try {
				set.add(cm.loadClass(loader, initialize));
			} catch (NoClassDefFoundError e) {
				if(IS_DEBUG) {
					// This is tough. Normally, we really want to ignore this error, but during development, it can be
					// a critical error to see to diagnose a very hard to find error. So we compromize here, and only
					// print error details out while in debug mode.
					System.err.println("While trying to process " + cm.toString() + ", an error occurred. It it"
							+ " probably safe to ignore this error, but if you're debugging to figure out why an"
							+ " expected class is not showing up, then this is probably why.");
					e.printStackTrace(System.err);
				}
			}
		}
		return set;
	}

	/**
	 * Returns a list of fields that have been annotated with the specified annotation. This will work with annotations
	 * that have been declared with the {@link RetentionPolicy#CLASS} property.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<FieldMirror> getFieldsWithAnnotation(Class<? extends Annotation> annotation) {
		if(fieldAnnotationCache.containsKey(annotation)) {
			return new HashSet<>(fieldAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<FieldMirror> mirrors = new HashSet<>();
		for(ClassMirror<?> m : getKnownClasses()) {
			for(FieldMirror f : m.getFields()) {
				if(f.hasAnnotation(annotation)) {
					mirrors.add(f);
				}
			}
		}
		fieldAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}

	/**
	 * Unlike {@link #getFieldsWithAnnotation(java.lang.Class)}, this actually loads the matching field's containing
	 * classes into PermGen, and returns a Set of Field objects. This is useful if you are for sure going to use these
	 * fields immediately, and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<Field> loadFieldsWithAnnotation(Class<? extends Annotation> annotation) {
		return loadFieldsWithAnnotation(annotation, ClassDiscovery.class.getClassLoader(), true);
	}

	/**
	 * Unlike {@link #getFieldsWithAnnotation(java.lang.Class)}, this actually loads the matching field's containing
	 * classes into PermGen, and returns a Set of Field objects. This is useful if you are for sure going to use these
	 * fields immediately, and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public Set<Field> loadFieldsWithAnnotation(Class<? extends Annotation> annotation, ClassLoader loader, boolean initialize) {
		Set<Field> ret = new HashSet<>();
		for(FieldMirror fm : getFieldsWithAnnotation(annotation)) {
			try {
				Field f = fm.loadField(loader, initialize);
				ret.add(f);
			} catch (ClassNotFoundException ex) {
				throw new NoClassDefFoundError(ex.getMessage());
			}
		}
		return ret;
	}

	/**
	 * Returns all methods, including constructors, with the specified annotations
	 *
	 * @param annotation
	 * @return
	 */
	public Set<MethodMirror> getMethodsWithAnnotation(Class<? extends Annotation> annotation) {
		if(methodAnnotationCache.containsKey(annotation)) {
			return new HashSet<>(methodAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<MethodMirror> mirrors = new HashSet<>();
		for(ClassMirror<?> m : getKnownClasses()) {
			for(MethodMirror mm : m.getMethods()) {
				if(mm.hasAnnotation(annotation)) {
					mirrors.add(mm);
				}
			}
		}
		methodAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}

	/**
	 * Unlike {@link #getMethodsWithAnnotation(java.lang.Class)}, this actually loads the matching method's containing
	 * classes into PermGen, and returns a Set of Method objects. This is useful if you are for sure going to use these
	 * methods immediately, and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<Method> loadMethodsWithAnnotation(Class<? extends Annotation> annotation) {
		return loadMethodsWithAnnotation(annotation, getDefaultClassLoader(), true);
	}

	/**
	 * Unlike {@link #getMethodsWithAnnotation(java.lang.Class)}, this actually loads the matching method's containing
	 * classes into PermGen, and returns a Set of Method objects. This is useful if you are for sure going to use these
	 * methods immediately, and don't want to have to lazy load them individually.
	 *
	 * @param annotation
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public Set<Method> loadMethodsWithAnnotation(Class<? extends Annotation> annotation, ClassLoader loader, boolean initialize) {
		try {
			Set<Method> set = new HashSet<>();
			for(MethodMirror mm : getMethodsWithAnnotation(annotation)) {
				set.add(mm.loadMethod(loader, initialize));
			}
			return set;
		} catch (ClassNotFoundException ex) {
			throw new NoClassDefFoundError(ex.getMessage());
		}
	}

	/**
	 * Returns all ConstructorMirrors with the given annotation.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<ConstructorMirror<?>> getConstructorsWithAnnotation(Class<? extends Annotation> annotation) {
		if(constructorAnnotationCache.containsKey(annotation)) {
			return new HashSet<>(constructorAnnotationCache.get(annotation));
		}
		doDiscovery();
		Set<ConstructorMirror<?>> mirrors = new HashSet<>();
		for(ClassMirror<?> m : getKnownClasses()) {
			for(ConstructorMirror<?> mm : m.getConstructors()) {
				if(mm.hasAnnotation(annotation)) {
					mirrors.add(mm);
				}
			}
		}
		constructorAnnotationCache.put(annotation, mirrors);
		return mirrors;
	}

	/**
	 * Loads all Constructors with the given annotation.
	 *
	 * @param annotation
	 * @return
	 */
	public Set<Constructor<?>> loadConstructorsWithAnnotation(Class<? extends Annotation> annotation) {
		return loadConstructorsWithAnnotation(annotation, getDefaultClassLoader(), true);
	}

	/**
	 * Loads all Constructors with the given annotation, using the specified classloader.
	 *
	 * @param annotation
	 * @param loader
	 * @param initialize
	 * @return
	 */
	public Set<Constructor<?>> loadConstructorsWithAnnotation(Class<? extends Annotation> annotation, ClassLoader loader, boolean initialize) {
		Set<Constructor<?>> set = new HashSet<>();
		for(AbstractMethodMirror m : getConstructorsWithAnnotation(annotation)) {
			try {
				Class<?> c = m.getDeclaringClass().loadClass(loader, initialize);
				outer:
				for(Constructor<?> cc : c.getDeclaredConstructors()) {
					Class<?>[] params = cc.getParameterTypes();
					if(m.getParams().size() != params.length) {
						continue;
					}
					for(int i = 0; i < params.length; i++) {
						ClassReferenceMirror<?> crm = m.getParams().get(i);
						ClassReferenceMirror<?> crm2 = new ClassReferenceMirror<>(ClassUtils.getJVMName(params[i]));
						if(!crm.equals(crm2)) {
							continue outer;
						}
					}
					set.add(cc);
				}
			} catch (ClassNotFoundException ex) {
				throw new NoClassDefFoundError();
			}
		}
		return set;
	}

	/**
	 * Returns the ClassMirror object for a given class name. Either the JVM name, or canonical name works.
	 *
	 * @param className
	 * @return
	 * @throws java.lang.ClassNotFoundException
	 */
	public ClassMirror<?> forName(String className) throws ClassNotFoundException {
		if(forNameCache.containsKey(className)) {
			return forNameCache.get(className);
		}
		for(ClassMirror<?> c : getKnownClasses()) {
			if(c.getClassName().equals(className) || c.getJVMClassName().equals(className)) {
				forNameCache.put(className, c);
				return c;
			}
		}
		throw new ClassNotFoundException(className);
	}

	/**
	 * Calls forFuzzyName with initialize true, and the class loader used to load this class.
	 *
	 * @param packageRegex
	 * @param className
	 * @return
	 */
	public ClassMirror<?> forFuzzyName(String packageRegex, String className) {
		return forFuzzyName(packageRegex, className, true, getDefaultClassLoader());
	}

	/**
	 * Returns a class given a "fuzzy" package name, that is, the package name provided is a regex. The class name must
	 * match exactly, but the package name will be the closest match, or undefined if there is no clear candidate. If no
	 * matches are found, null is returned.
	 *
	 * @param packageRegex
	 * @param className
	 * @param initialize
	 * @param classLoader
	 * @return
	 */
	public ClassMirror<?> forFuzzyName(String packageRegex, String className, boolean initialize, ClassLoader classLoader) {
		String index = packageRegex + className;
		if(fuzzyClassCache.containsKey(index)) {
			return fuzzyClassCache.get(index);
		}
		Set<ClassMirror<?>> found = new HashSet<>();
		Set<ClassMirror<?>> searchSpace = getKnownClasses();
		for(ClassMirror<?> c : searchSpace) {
			if(c.getPackage().getName().matches(packageRegex) && c.getSimpleName().equals(className)) {
				found.add(c);
			}
		}
		ClassMirror<?> find;
		if(found.size() == 1) {
			find = found.iterator().next();
		} else if(found.isEmpty()) {
			find = null;
		} else {
			ClassMirror<?> candidate = null;
			int max = Integer.MAX_VALUE;
			for(ClassMirror<?> f : found) {
				int distance = StringUtils.LevenshteinDistance(f.getPackage().getName(), packageRegex);
				if(distance < max) {
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
		if(start.isFile()) {
			if(start.getName().endsWith(".class")) {
				fileList.add(start);
			}
		} else {
			File[] list = start.listFiles();
			if(list == null) {
				StreamUtils.GetSystemOut().println("Could not list files in " + start);
				return;
			}
			for(File child : start.listFiles()) {
				descend(child, fileList);
			}
		}
	}

	/**
	 * Returns the container url for this class. This varies based on whether or not the class files are in a zip/jar or
	 * not, so this method standardizes that. The method may return null, if the class is a dynamically generated class
	 * (perhaps with asm, or a proxy class)
	 *
	 * @param c
	 * @return
	 */
	public static URL GetClassContainer(Class<?> c) {
		if(c == null) {
			throw new NullPointerException("The Class passed to this method may not be null");
		}
		while(c.isMemberClass() || c.isAnonymousClass()) {
			c = c.getEnclosingClass(); //Get the actual enclosing file
		}
		if(c.getProtectionDomain().getCodeSource() == null) {
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
			if(packageRoot.matches("jar:file:.*!/")) {
				packageRoot = StringUtils.replaceLast(packageRoot, "!/", "");
				packageRoot = packageRoot.replaceFirst("jar:", "");
			}
			return new URL(packageRoot);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("While interrogating " + c.getName() + ", an unexpected exception was thrown.", e);
		} catch (MalformedURLException e) {
			throw new RuntimeException("While interrogating " + c.getName() + ", an unexpected exception was thrown for potential URL: \"" + packageRoot + "\"", e);
		}
	}

	private static final Map<Pair<Class<?>, Class<? extends Annotation>>, Annotation> ANNOTATION_CACHE =
			new HashMap<>();

	/**
	 * {@code Class.getAnnotation} is relatively slow, so this provides a utility cache in front of it, so that
	 * multiple calls will not incur the penalty hit. Only the first call does the lookup.
	 *
	 * Unlike the other methods in this class, there is no way to clear the cache, because this only works with
	 * concrete Java classes, and classes can't change annotations at runtime.
	 * @param <T> The annotation type
	 * @param clazz The class to find the annotation on
	 * @param annotation The annotation class
	 * @return The annotation, or null if the specified class does not have that annotation.
	 */
	public static <T extends Annotation> T GetClassAnnotation(Class<?> clazz, Class<T> annotation) {
		Pair<Class<?>, Class<? extends Annotation>> pair = new Pair(clazz, annotation);
		Annotation t = ANNOTATION_CACHE.get(pair);
		if(t == null) {
			t = clazz.getAnnotation(annotation);
			ANNOTATION_CACHE.put(pair, t);
		}
		// This cast should always work, but since we're shoving all the annotations into the cache, the compiler
		// can't know that the returned type will be T.
		return (T) t;
	}
}
