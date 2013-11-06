package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.shutdown;
import com.laytonsmith.annotations.startup;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.extensions.Extension;
import com.laytonsmith.core.extensions.MSExtension;
import com.laytonsmith.core.extensions.AbstractExtension;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class ExtensionManager {
        private static final Map<String, Extension> extensions = new HashMap<String, Extension>();

        /**
         * Initializes the extension manager. This operation is not necessarily required,
         * and must be guaranteed to not run more than once per ClassDiscovery object.
         * @param root root directory to load files from.
         * @param cd the ClassDiscovery to use for loading files.
         */
        public static void Initialize(File root, ClassDiscovery cd){
                extensions.clear();
                
                //Look in the extension folder for jars, add them to our class discover, then initialize everything
                DynamicClassLoader dcl = new DynamicClassLoader();
                for(File f : root.listFiles()){
                        if(f.getName().endsWith(".jar")){
                                try {
                                        //First, load it with our custom class loader
                                        URL jar = f.toURI().toURL();
                                        dcl.addJar(jar);
                                        cd.addDiscoveryLocation(jar);
                                        CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.DEBUG, "Loaded " + f.getAbsolutePath(), Target.UNKNOWN);
                                } catch (MalformedURLException ex) {
                                        Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                }
				
                for (ClassMirror<AbstractExtension> extmirror : cd.getClassesWithAnnotationThatExtend(MSExtension.class, AbstractExtension.class)) {			
                        Extension ext;
                        
                        Class<AbstractExtension> extcls = extmirror.loadClass(dcl, true);				
                        try {
                                ext = extcls.newInstance();
                        } catch (InstantiationException ex) {
                                //Error, but skip this one, don't throw an exception ourselves, just log it.
                                Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, 
                                                "Could not instantiate " + extcls.getName() + ": " + ex.getMessage());
								continue;
                        } catch (IllegalAccessException ex) {
                                //Error, but skip this one, don't throw an exception ourselves, just log it.
                                Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, 
                                                "Could not instantiate " + extcls.getName() + ": " + ex.getMessage());
								continue;
                        }
                        
                        extensions.put(ext.getName(), ext);
                }
                
                cd.setDefaultClassLoader(dcl);
        }
        
        /**
         * This should be run each time the "startup" of the runtime occurs.
         * It registers its own shutdown hook.
         */
        public static void Startup(){
                for(Extension ext : extensions.values()) {
                        try {
                                ext.onStartup();
                        } catch (Throwable e) {
                                Logger log = Logger.getLogger(ExtensionManager.class.getName());
                                log.log(Level.SEVERE, ext.getName() + "'s onStartup caused an exception:");
                                log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
                        }
                }
                
                for(MethodMirror mm : ClassDiscovery.getDefaultInstance().getMethodsWithAnnotation(startup.class)){
                        if(!mm.getParams().isEmpty()){
                                //Error, but skip this one, don't throw an exception ourselves, just log it.
                                Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, "Method annotated with @" + startup.class.getSimpleName() 
                                                + " takes parameters; it should not.");
                        } else if(!mm.getModifiers().isStatic()){
                                CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + mm.getDeclaringClass() + "#" + mm.getName() + " is not static,"
                                                + " but it should be.", Target.UNKNOWN);
                        } else {
                                try{
                                        Method m = mm.loadMethod(ClassDiscovery.getDefaultInstance().getDefaultClassLoader(), true);
                                        m.setAccessible(true);
                                        m.invoke(null, (Object[])null);
                                } catch(Throwable e){
                                        CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + mm.getDeclaringClass() + "#" + mm.getName() + " threw an exception during runtime:\n" + StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
                                }
                        }
                }
                
                StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

                        @Override
                        public void run() {
                                for(Extension ext : extensions.values()) {
                                        try {
                                                ext.onShutdown();
                                        } catch (Throwable e) {
                                                Logger log = Logger.getLogger(ExtensionManager.class.getName());
                                                log.log(Level.SEVERE, ext.getName() + "'s onShutdown caused an exception:");
                                                log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
                                        }
                                }
                                
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
                                                } catch(Throwable e){
                                                        CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, "Method " + mm.getDeclaringClass() + "#" + mm.getName() + " threw an exception during runtime:\n" + StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
                                                }
                                        }
                                }
                        }
                });
        }

        public static void PreReloadAliases(boolean reloadGlobals, boolean reloadTimeouts, 
                        boolean reloadExecutionQueue, boolean reloadPersistanceConfig, 
                        boolean reloadPreferences, boolean reloadProfiler, 
                        boolean reloadScripts, boolean reloadExtensions) {
                for(Extension ext : extensions.values()) {
                        try {
                                ext.onPreReloadAliases(reloadGlobals, reloadTimeouts, reloadExecutionQueue, 
                                                reloadPersistanceConfig, reloadPreferences, 
                                                reloadProfiler, reloadScripts, reloadExtensions);
                        } catch (Throwable e) {
                                Logger log = Logger.getLogger(ExtensionManager.class.getName());
                                log.log(Level.SEVERE, ext.getName() + "'s onPreReloadAliases caused an exception:");
                                log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
                        }
                }
        }
        
        public static void PostReloadAliases() {
                for(Extension ext : extensions.values()) {
                        try {
                                ext.onPostReloadAliases();
                        } catch (Throwable e) {
                                Logger log = Logger.getLogger(ExtensionManager.class.getName());
                                log.log(Level.SEVERE, ext.getName() + "'s onPostReloadAliases caused an exception:");
                                log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
                        }
                }
        }
}