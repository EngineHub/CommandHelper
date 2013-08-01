
package com.laytonsmith.PureUtilities;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
		classLoaders.add(new URLClassLoader(new URL[]{url}, getParent()));
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		checkDestroy();
		try{
			//If the superclass knows about it, we're done.
			return super.loadClass(name, resolve);
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
