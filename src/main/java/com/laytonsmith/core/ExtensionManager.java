package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.DynamicClassLoader;
import com.laytonsmith.PureUtilities.StackTraceUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.shutdown;
import com.laytonsmith.annotations.startup;
import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class ExtensionManager {

	/**
	 * Initializes the extension manager. This operation is not necessarily required,
	 * and must be guaranteed to not run more than once per ClassDiscovery object.
	 */
	public static void Initialize(File root, ClassDiscovery cd){
		//Look in the extension folder for jars, add them to our class discover, then initialize everything
		DynamicClassLoader dcl = new DynamicClassLoader();
		for(File f : root.listFiles()){
			if(f.getName().endsWith(".jar")){
				try {
					//First, load it with our custom class loader
					URL jar = new URL("jar:" + f.toURI().toURL() + "!/");
					dcl.addJar(jar);
					cd.addDiscoveryLocation(jar);
					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.DEBUG, "Loaded " + f.getAbsolutePath(), Target.UNKNOWN);
				} catch (MalformedURLException ex) {
					Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		cd.setDefaultClassLoader(dcl);
	}
	
	/**
	 * This should be run each time the "startup" of the runtime occurs.
	 * It registers its own shutdown hook.
	 */
	public static void Startup(){
		
		for(MethodMirror mm : ClassDiscovery.getDefaultInstance().getMethodsWithAnnotation(startup.class)){
			if(!mm.getParams().isEmpty()){
				//Error, but skip this one, don't throw an exception ourselves, just log it.
				Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, "Method annotated with @" + startup.class.getSimpleName() 
						+ " takes parameters; it should not.");
			} else {
				try{
					Method m = mm.loadMethod(ClassDiscovery.getDefaultInstance().getDefaultClassLoader(), true);
					m.setAccessible(true);
					m.invoke(null);
				} catch(Exception e){
					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + mm.getDeclaringClass() + "#" + mm.getName() + " threw an exception during runtime:\n" + StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
				}
			}
		}
		
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			public void run() {
				for(MethodMirror mm : ClassDiscovery.getDefaultInstance().getMethodsWithAnnotation(shutdown.class)){
					if(!mm.getParams().isEmpty()){
						//Error, but skip this one, don't throw an exception ourselves, just log it.
						CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method annotated with @" + shutdown.class.getSimpleName() 
								+ " takes parameters; it should not. (Found in " + mm.getDeclaringClass() + "#" + mm.getName() + ")", Target.UNKNOWN);
					} else {
						try{
							Method m = mm.loadMethod(ClassDiscovery.getDefaultInstance().getDefaultClassLoader(), true);
							m.setAccessible(true);
							m.invoke(null);
						} catch(Exception e){
							CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + mm.getDeclaringClass() + "#" + mm.getName() + " threw an exception during runtime:\n" + StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
						}
					}
				}
			}
		});
	}
}
