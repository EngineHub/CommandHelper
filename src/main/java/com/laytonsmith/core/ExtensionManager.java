package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.StackTraceUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.shutdown;
import com.laytonsmith.annotations.startup;
import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class ExtensionManager {

	/**
	 * Initializes the extension manager. This operation is not necessarily required,
	 * and is guaranteed to not run more than once per classloader startup.
	 */
	public static void Initialize(File root){
		//Look in the extension folder for jars, add them to our class discover, then initialize everything
		for(File f : root.listFiles()){
			if(f.getName().endsWith(".jar")){
				try {
					//First, load it with our custom class loader
					URLClassLoader child = new URLClassLoader (new URL[]{f.toURI().toURL()}, ExtensionManager.class.getClassLoader());
					Set<ClassLoader> cl = new HashSet<ClassLoader>();
					cl.add(child);
					ClassDiscovery.GetClassesWithinPackageHierarchy("jar:" + f.toURI().toURL().toString(), cl);
					ClassDiscovery.InstallDiscoveryLocation("jar:" + f.toURI().toURL().toString());
					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.DEBUG, "Loaded " + f.getAbsolutePath(), Target.UNKNOWN);
				} catch (MalformedURLException ex) {
					Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
	
	/**
	 * This should be run each time the "startup" of the runtime occurs.
	 * It registers its own shutdown hook.
	 */
	public static void Startup(){
		
		for(Method m : ClassDiscovery.GetMethodsWithAnnotation(startup.class)){
			if(m.getParameterTypes().length != 0){
				//Error, but skip this one, don't throw an exception ourselves, just log it.
				Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, "Method annotated with @" + startup.class.getSimpleName() 
						+ " takes parameters; it should not.");
			} else {
				try{
					m.setAccessible(true);
					m.invoke(null);
				} catch(Exception e){
					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + m.getDeclaringClass() + "#" + m.getName() + " threw an exception during runtime:\n" + StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
				}
			}
		}
		
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			public void run() {
				for(Method m : ClassDiscovery.GetMethodsWithAnnotation(shutdown.class)){
					if(m.getParameterTypes().length != 0){
						//Error, but skip this one, don't throw an exception ourselves, just log it.
						CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method annotated with @" + shutdown.class.getSimpleName() 
								+ " takes parameters; it should not. (Found in " + m.getDeclaringClass().getName() + "#" + m.getName() + ")", Target.UNKNOWN);
					} else {
						try{
							m.setAccessible(true);
							m.invoke(null);
						} catch(Exception e){
							CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + m.getDeclaringClass() + "#" + m.getName() + " threw an exception during runtime:\n" + StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
						}
					}
				}
			}
		});
	}
}
