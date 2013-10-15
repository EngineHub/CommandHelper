
package com.laytonsmith.PureUtilities.ClassLoading;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class extends ClassLoader, but allows for new jars/classpath elements
 * to be added at runtime. Note that already loaded classes won't be affected
 * if a new jar is added, but new requests for classes will be.
 */
public class DynamicClassLoader extends ClassLoader {
	
	private final Set<URLClassLoader> classLoaders = new LinkedHashSet<URLClassLoader>();
	private final Set<URL> urls = new HashSet<URL>();
	private boolean destroyed = false;
	
	/**
	 * Adds a jar to this class loader instance. This can be done at runtime,
	 * however note that jars cannot be removed, and if a jar already defines
	 * a class (or the class has already been loaded) it cannot be changed inside this
	 * instance. Adding the same URL twice has no effect.
	 * @param url The url to the jar.
	 */
	public synchronized void addJar(URL url){
		checkDestroy();
		if(urls.contains(url)){
			return;
		}
		urls.add(url);
		classLoaders.add(new URLClassLoader(new URL[]{url}, DynamicClassLoader.class.getClassLoader()));
	}

	@Override
	protected synchronized Package getPackage(String name) {
		for(ClassLoader c : classLoaders){
			Package p = (Package) ReflectionUtils.invokeMethod(c.getClass(), c, "getPackage", new Class[]{String.class}, new Object[]{name});
			if(p != null){
				return p;
			}
		}
		return null;
	}

	@Override
	protected synchronized Package[] getPackages() {
		List<Package> packages = new ArrayList<Package>();
		for(ClassLoader c : classLoaders){
			packages.addAll(Arrays.asList((Package[])ReflectionUtils.invokeMethod(c.getClass(), c, "getPackages")));
		}
		return packages.toArray(new Package[packages.size()]);
	}
	

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		checkDestroy();
		try{
			//If the parent class knows about it, we're done.
			Class c = Class.forName(name, resolve, DynamicClassLoader.class.getClassLoader());
			return c;
		} catch(ClassNotFoundException ex){
			//Otherwise we need to find the class ourselves.
			for(URLClassLoader url : classLoaders){
				try{
					Class c = url.loadClass(name);
					if(resolve){
						resolveClass(c);
					}
					return c;
				} catch(ClassNotFoundException ex1){
					//Don't care, move on to the next class loader
				}
			}
			throw new ClassNotFoundException(name);
		}
	}
	
	/**
	 * When this class is no longer needed, this method can be called
	 * to destroy all the internal references and "lock" the class for
	 * future use. If any method (other than destroy) is attempted to be
	 * called in this instance after this method is called, it will throw
	 * a runtime exception, which should hopefully help catch errors faster.
	 * TODO: Use this in the code!
	 */
	public synchronized void destroy(){
		destroyed = true;
		classLoaders.clear();
		urls.clear();
	}
	
	private void checkDestroy(){
		if(destroyed){
			throw new RuntimeException("Cannot access this instance of " + DynamicClassLoader.class.getSimpleName() + ", as it has already been destroyed.");
		}
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		super.finalize();
		destroy();
	}
	
}
