package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.AnnotationMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Extensions.ExtensionManagerBase;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.shutdown;
import com.laytonsmith.annotations.startup;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Layton
 */
public class ExtensionManager extends ExtensionManagerBase<ExtensionTracker> {
	private static ExtensionManager instance;

	public static ExtensionManager getInstance() {
		if (instance == null) {
			instance = new ExtensionManager();
		}
		
		return instance;
	}
	
	/**
	 * Cache extension files. On non-windows, this function leaves early, as 
	 * the whole purpose of this is to enable updating jar files while the
	 * process is still running.
	 * @param extCache Where the extensions will be cached to.
	 * @param generalCache Where annotation caches will be saved to.
	 * @param extraClasses Other classes to include when setting up the 
	 * temporary ClassDiscovery.
	 */
	@Override
	public void cache(File extCache, File generalCache, Class... extraClasses) {
		// We will only cache on Windows, as Linux doesn't natively lock
		// files that are in use. Windows prevents any modification, making
		// it harder for server owners on Windows to update the jars.
		boolean onWindows = (OSUtils.GetOS() == OSUtils.OS.WINDOWS);

		if (!onWindows) {
			return;
		}

		// Using System.out here instead of the logger as the logger might not
		// immediately print to the console.
		System.out.println("[CommandHelper] Caching extensions...");

		// Create the directory if it doesn't exist.
		extCache.mkdirs();

		// Try to delete any loose files in the cache dir, so that we
		// don't load stuff we aren't supposed to. This is in case the shutdown
		// cleanup wasn't successful on the last run.
		for (File f : extCache.listFiles()) {
			try {
				Files.delete(f.toPath());
			} catch (IOException ex) {
				log(Level.WARNING, "[CommandHelper] Could not delete loose file "
						+ f.getAbsolutePath() + ": " + ex.getMessage());
			}
		}

		// The cache, cd and dcl here will just be thrown away.
		// They are only used here for the purposes of discovering what a given 
		// jar has to offer.
		ClassDiscoveryCache cache = new ClassDiscoveryCache(generalCache);
		cache.setLogger(logger);
		
		DynamicClassLoader dcl = new DynamicClassLoader();
		ClassDiscovery cd = new ClassDiscovery();

		cd.setClassDiscoveryCache(cache);
		cd.addDiscoveryLocation(ClassDiscovery.GetClassContainer(ExtensionManager.class));
		for (Class extra : extraClasses) {
			cd.addDiscoveryLocation(ClassDiscovery.GetClassContainer(extra));
		}

		//Look in the given locations for jars, add them to our class discovery.
		List<File> toProcess = new ArrayList<>();

		for (File location : locations) {
			toProcess.addAll(getFiles(location));
		}

		// Load the files into the discovery mechanism.
		for (File file : toProcess) {
			if (!file.canRead()) {
				continue;
			}

			URL jar;
			try {
				jar = file.toURI().toURL();
			} catch (MalformedURLException ex) {
				log(Level.SEVERE, null, ex);
				continue;
			}

			dcl.addJar(jar);
			cd.addDiscoveryLocation(jar);
		}

		cd.setDefaultClassLoader(dcl);

		// Loop thru the found lifecycles, copy them to the cache using the name
		// given in the lifecycle. If more than one jar has the same internal
		// name, the filename will be given a number.
		Set<File> done = new HashSet<>();
		Map<String, Integer> namecount = new HashMap<>();

		// First, cache new lifecycle style extensions. They will be renamed to
		// use their internal name.
		for (ClassMirror<AbstractExtension> extmirror
				: cd.getClassesWithAnnotationThatExtend(
						MSExtension.class, AbstractExtension.class)) {
			AnnotationMirror plug = extmirror.getAnnotation(MSExtension.class);

			URL plugURL = extmirror.getContainer();

			// Get the internal name that this extension exposes.
			if (plugURL != null && plugURL.getPath().endsWith(".jar")) {
				File f;

				try {
					f = new File(plugURL.toURI());
				} catch (URISyntaxException ex) {
					log(Level.SEVERE, "Could not figure out where this file resides!", ex);
					continue;
				}

				// Skip extensions that originate from commandhelpercore.
				if (plugURL.equals(ClassDiscovery.GetClassContainer(ExtensionManager.class))) {
					done.add(f);
				}

				// Skip files already processed.
				if (done.contains(f)) {
					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.WARNING,
							f.getAbsolutePath() + " contains more than one extension"
							+ " descriptor. Bug someone about it!", Target.UNKNOWN);

					continue;
				}

				done.add(f);

				String name = plug.getValue("value").toString();

				// Just in case we have two plugins with the same internal name,
				// lets track and rename them using a number scheme.
				if (namecount.containsKey(name.toLowerCase())) {
					int i = namecount.get(name.toLowerCase());
					name += "-" + i;
					namecount.put(name.toLowerCase(), i++);

					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.WARNING,
							f.getAbsolutePath() + " contains a duplicate internally"
							+ " named extension (" + name + "). Bug someone"
							+ " about it!", Target.UNKNOWN);
				} else {
					namecount.put(name.toLowerCase(), 1);
				}

				// Rename the jar to use the plugin's internal name and 
				// copy it into the cache.
				File newFile = new File(extCache, name.toLowerCase() + ".jar");

				try {
					Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException ex) {
					log(Level.SEVERE, "Could not copy '" + f.getName()
						+ "' to cache: " + ex.getMessage());
				}
			}
		}

		Set<ClassMirror<?>> classes = cd.getClassesWithAnnotation(api.class);

		// Now process @api annotated extensions, ignoring ones already processed.
		for (ClassMirror klass : classes) {
			URL plugURL = klass.getContainer();

			if (plugURL != null && plugURL.getPath().endsWith(".jar")) {
				File f;

				try {
					f = new File(plugURL.toURI());
				} catch (URISyntaxException ex) {
					log(Level.SEVERE, "Could not figure out where this file resides!", ex);
					continue;
				}

				// Skip files already processed.
				if (done.contains(f)) {
					continue;
				}

				// Copy the file if it's a valid extension.
				// No special processing needed.
				if (cd.doesClassExtend(klass, Event.class)
						|| cd.doesClassExtend(klass, Function.class)) {
					// We're processing it here instead of above, complain about it.
					log(Level.WARNING, f.getAbsolutePath() + " is an old-style"
							+ " extension! Bug the author to update it to the"
							+ " new extension system!");

					// Only process this file once.
					done.add(f);

					File newFile = new File(extCache, "oldstyle-" + f.getName());

					try {
						Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException ex) {
						log(Level.SEVERE, "Could not copy '" + f.getName()
							+ "' to cache: " + ex.getMessage());
					}
				}
			}
		}

		System.out.println("[CommandHelper] Extension caching complete.");

		// Shut down the original dcl to "unlock" the processed jars.
		// The cache and cd instances will just fall into oblivion.
		dcl.destroy();

		// Explicit call. Without this, jar files won't actually get unlocked on
		// Windows. Of course, this is hit and miss, but that's fine; we tried.
		System.gc();
	}

	/**
	 * Initializes the extension manager. This operation is not necessarily
	 * required, and must be guaranteed to not run more than once per
	 * ClassDiscovery object.
	 *
	 * @param cd the ClassDiscovery to use for loading files.
	 */
	@Override
	public void load(ClassDiscovery cd) {
		extensions.clear();

		// Look in the extension folder for jars, add them to our class discover,
		// then initialize everything
		List<File> toProcess = new ArrayList<>();

		// Grab files from the cache if on Windows. Otherwise just load
		// directly from the stored locations.
		boolean onWindows = (OSUtils.GetOS() == OSUtils.OS.WINDOWS);

		if (onWindows) {
			toProcess.addAll(getFiles(CommandHelperFileLocations.getDefault().getExtensionCacheDirectory()));
		} else {
			for (File location : locations) {
				toProcess.addAll(getFiles(location));
			}
		}

		DynamicClassLoader dcl = new DynamicClassLoader();
		cd.setDefaultClassLoader(dcl);

		for (File f : toProcess) {
			if (f.getName().endsWith(".jar")) {
				try {
					//First, load it with our custom class loader
					URL jar = f.toURI().toURL();

					dcl.addJar(jar);
					cd.addDiscoveryLocation(jar);

					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.DEBUG, 
							"Loaded " + f.getAbsolutePath(), Target.UNKNOWN);
				} catch (MalformedURLException ex) {
					log(Level.SEVERE, null, ex);
				}
			}
		}

		// Grab all known lifecycle classes, and use them. If more than one 
		// lifecycle is found per URL, it's stored and used, but the first
		// one found defines the internal name.
		for (ClassMirror<AbstractExtension> extmirror : cd.getClassesWithAnnotationThatExtend(MSExtension.class, AbstractExtension.class)) {
			Extension ext;
			URL url = extmirror.getContainer();
			Class<AbstractExtension> extcls;
			
			if (extmirror.getModifiers().isAbstract()) {
				log(Level.SEVERE, "Probably won't be able to instantiate " 
					+ extmirror.getClassName() + ": The class is marked as"
					+ " abstract! Will try anyway.");
			}
			
			try {
				extcls = extmirror.loadClass(dcl, true);
			} catch (NoClassDefFoundError ex) {
				ex.printStackTrace();
				continue;
			}
			
			try {
				ext = extcls.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				//Error, but skip this one, don't throw an exception ourselves, just log it.
				log(Level.SEVERE, "Could not instantiate " + extcls.getName() 
					+ ": " + ex.getMessage());
				continue;
			}

			ExtensionTracker trk = extensions.get(url);

			if (trk == null) {
				Version ver;
				
				try {
					ver = ext.getVersion();
				} catch (AbstractMethodError ex) {
					ver = new SimpleVersion("0.0.0");
				}
				
				trk = new ExtensionTracker(ext.getName(), ver, url, cd, dcl);

				extensions.put(url, trk);
			}

			trk.allExtensions.add(ext);
		}

		// Lets store info about the functions and events extensions have.
		// This will aide in gracefully unloading stuff later.
		Set<ClassMirror<?>> classes = cd.getClassesWithAnnotation(api.class);

		// Temp tracking for loading messages later on.
		List<String> events = new ArrayList<>();
		List<String> functions = new ArrayList<>();

		// Loop over the classes, instantiate and register functions and events,
		// and store the instances in their trackers.
		for (ClassMirror klass : classes) {
			URL url = klass.getContainer();

			if (cd.doesClassExtend(klass, Event.class)
					|| cd.doesClassExtend(klass, Function.class)) {
				Class c = klass.loadClass(dcl, true);

				ExtensionTracker trk = extensions.get(url);

				if (trk == null) {
					trk = new ExtensionTracker("<undefined>", new SimpleVersion("0.0.0"), url, cd, dcl);

					extensions.put(url, trk);
				}

				// Instantiate, register and store.
				try {
					if (Event.class.isAssignableFrom(c)) {
						Class<Event> cls = (Class<Event>) c;
						
						if (klass.getModifiers().isAbstract()) {
							// Abstract? Looks like they accidently @api'd
							// a cheater class. We can't be sure that it is fully
							// defined, so complain to the console.
							CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, 
								"Class " + c.getName() + " in " + url + " is"
								+ " marked as an event but is also abstract."
								+ " Bugs might occur! Bug someone about this!",
								Target.UNKNOWN);
						}
						
						Event e = cls.newInstance();
						events.add(e.getName());

						trk.registerEvent(e);
					} else if (Function.class.isAssignableFrom(c)) {
						Class<Function> cls = (Class<Function>) c;
						
						if (klass.getModifiers().isAbstract()) {
							// Abstract? Looks like they accidently @api'd
							// a cheater class. We can't be sure that it is fully
							// defined, so complain to the console.
							CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, 
								"Class " + c.getName() + " in " + url + " is"
								+ " marked as a function but is also abstract."
								+ " Bugs might occur! Bug someone about this!",
								Target.UNKNOWN);
						}
						
						Function f = cls.newInstance();
						functions.add(f.getName());
						
						trk.registerFunction(f);
					}
				} catch (InstantiationException | IllegalAccessException ex) {
					log(Level.SEVERE, ex.getMessage(), ex);
				}
			}
		}

		// Lets print out the details to the console, if we are in debug mode.
		if (Prefs.isInitialized() && Prefs.DebugMode()) {
			Collections.sort(events);
			String eventString = StringUtils.Join(events, ", ", ", and ", " and ");
			Collections.sort(functions);
			String functionString = StringUtils.Join(functions, ", ", ", and ", " and ");

			System.out.println(Implementation.GetServerType().getBranding()
				+ ": Loaded the following functions: " + functionString.trim());
			System.out.println(Implementation.GetServerType().getBranding()
				+ ": Loaded " + functions.size() + " function" + (functions.size() == 1 ? "." : "s."));
			System.out.println(Implementation.GetServerType().getBranding()
				+ ": Loaded the following events: " + eventString.trim());
			System.out.println(Implementation.GetServerType().getBranding()
				+ ": Loaded " + events.size() + " event" + (events.size() == 1 ? "." : "s."));
		}
	}

	/**
	 * To be run when we are shutting everything down.
	 */
	public void cleanup(File extCache) {
		// Shutdown and release all the extensions
		for (ExtensionTracker trk : extensions.values()) {
			trk.shutdownTracker();
		}

		extensions.clear();

		// Clean up the loaders and discovery instances.
		ClassDiscovery.getDefaultInstance().invalidateCaches();
		ClassLoader loader = ClassDiscovery.getDefaultInstance().getDefaultClassLoader();

		if (loader instanceof DynamicClassLoader) {
			DynamicClassLoader dcl = (DynamicClassLoader) loader;
			dcl.destroy();
		}

		// Explicit call. Without this, jar files won't actually get unlocked on
		// Windows. Of course, this is hit and miss, but that's fine; we tried.
		System.gc();

		if (extCache == null || !extCache.exists() || !extCache.isDirectory()) {
			return;
		}

		// Try to delete any loose files in the cache dir.
		for (File f : extCache.listFiles()) {
			try {
				Files.delete(f.toPath());
			} catch (IOException ex) {
				System.out.println("[CommandHelper] Could not delete loose file "
						+ f.getAbsolutePath() + ": " + ex.getMessage());
			}
		}
	}

	/**
	 * This should be run each time the "startup" of the runtime occurs. It
	 * registers its own shutdown hook.
	 */
	@SuppressWarnings("deprecation")
	public void startup() {
		for (ExtensionTracker trk : extensions.values()) {
			for (Extension ext : trk.getExtensions()) {
				try {
					ext.onStartup();
				} catch (Throwable e) {
					log(Level.SEVERE, ext.getClass().getName()
							+ "'s onStartup caused an exception:");
					log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}

		// Deprecated. Soon to be removed!
		for (MethodMirror mm : ClassDiscovery.getDefaultInstance().getMethodsWithAnnotation(startup.class)) {
			if (!mm.getParams().isEmpty()) {
				//Error, but skip this one, don't throw an exception ourselves, just log it.
				CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR,
					"Method annotated with @" + startup.class.getSimpleName()
					+ " takes parameters; it should not.", Target.UNKNOWN);
			} else if (!mm.getModifiers().isStatic()) {
				CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR,
					"Method " + mm.getDeclaringClass() + "#" + mm.getName()
					+ " is not static, but it should be.", Target.UNKNOWN);
			} else {
				try {
					Method m = mm.loadMethod(ClassDiscovery.getDefaultInstance().getDefaultClassLoader(), true);
					m.setAccessible(true);
					m.invoke(null, (Object[]) null);
				} catch (Throwable e) {
					CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR,
						"Method " + mm.getDeclaringClass() + "#"
						+ mm.getName() + " threw an exception during runtime:\n"
						+ StackTraceUtils.GetStacktrace(e), Target.UNKNOWN);
				}
			}
		}

		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			@Override
			public void run() {
				for (ExtensionTracker trk : extensions.values()) {
					for (Extension ext : trk.getExtensions()) {
						try {
							ext.onShutdown();
						} catch (Throwable e) {
							log(Level.SEVERE, ext.getClass().getName()
									+ "'s onStartup caused an exception:");
							log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
						}
					}
				}
				
				// Deprecated. Soon to be removed!
				for (MethodMirror mm : ClassDiscovery.getDefaultInstance().getMethodsWithAnnotation(shutdown.class)) {
					if (!mm.getParams().isEmpty()) {
						//Error, but skip this one, don't throw an exception ourselves, just log it.
						CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR,
							"Method annotated with @" + shutdown.class.getSimpleName()
							+ " takes parameters; it should not. (Found in "
							+ mm.getDeclaringClass() + "#" + mm.getName() + ")", 
							Target.UNKNOWN);
					} else {
						try {
							Method m = mm.loadMethod(ClassDiscovery.getDefaultInstance().getDefaultClassLoader(), true);
							m.setAccessible(true);
							m.invoke(null);
						} catch (Throwable e) {
							CHLog.GetLogger().Log(CHLog.Tags.EXTENSIONS, LogLevel.ERROR, 
								"Method " + mm.getDeclaringClass() + "#" 
								+ mm.getName() + " threw an exception"
								+ " during runtime:\n" + StackTraceUtils.GetStacktrace(e), 
								Target.UNKNOWN);
						}
					}
				}
			}
		});
	}

	public void preReloadAliases(boolean reloadGlobals, boolean reloadTimeouts,
			boolean reloadExecutionQueue, boolean reloadPersistenceConfig,
			boolean reloadPreferences, boolean reloadProfiler,
			boolean reloadScripts, boolean reloadExtensions) {
		for (ExtensionTracker trk : extensions.values()) {
			for (Extension ext: trk.getExtensions()) {
				try {
					ext.onPreReloadAliases(reloadGlobals,
						reloadTimeouts, reloadExecutionQueue,
						reloadPersistenceConfig, reloadPreferences,
						reloadProfiler, reloadScripts, reloadExtensions);
				} catch (Throwable e) {
					log(Level.SEVERE, ext.getClass().getName()
							+ "'s onPreReloadAliases caused an exception:");
					log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}
	}

	public void postReloadAliases() {
		for (ExtensionTracker trk : extensions.values()) {
			for (Extension ext: trk.getExtensions()) {
				try {
					ext.onPostReloadAliases();
				} catch (Throwable e) {
					log(Level.SEVERE, ext.getClass().getName()
							+ "'s onPreReloadAliases caused an exception:");
					log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}
	}
	
	public Set<Event> getEvents() {
		Set<Event> retn = new HashSet<>();
		
		for (ExtensionTracker trk: extensions.values()) {
			retn.addAll(trk.getEvents());
		}
		
		return retn;
	}
	
	public Set<Event> getEvents(Driver type) {
		Set<Event> retn = new HashSet<>();
		
		for (ExtensionTracker trk: extensions.values()) {
			retn.addAll(trk.getEvents(type));
		}
		
		return retn;
	}
	
	public Event getEvent(Driver type, String name) {
		for (ExtensionTracker trk: extensions.values()) {
			Set<Event> events = trk.getEvents(type);
			
			for (Event event: events) {
				if (event.getName().equalsIgnoreCase(name)) {
					return event;
				}
			}
		}
		
		return null;
	}
	
	public Event getEvent(String name) {
		for (ExtensionTracker trk: extensions.values()) {
			Set<Event> events = trk.getEvents();
			
			for (Event event: events) {
				if (event.getName().equalsIgnoreCase(name)) {
					return event;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * This runs the hooks on all events. This should be called each time
	 * the server "starts up".
	 */
	public void runEventHooks(){
		for(Event event : getEvents()){
			try{
				event.hook();
			} catch(UnsupportedOperationException ex){}
		}
	}
	
	/**
	 * Get a given function's code, given the MScript representation and platform.
	 * @param c
	 * @param platform
	 * @return
	 * @throws ConfigCompileException 
	 */
    public FunctionBase getFunction(Construct c, api.Platforms platform) throws ConfigCompileException {
        if(platform == null){
            //Default to the Java interpreter
            platform = api.Platforms.INTERPRETER_JAVA;
        }
		
        if (c instanceof CFunction) {
			for (ExtensionTracker trk: extensions.values()) {
				if(trk.functions.get(platform).containsKey(c.val()) 
						&& trk.supportedPlatforms.get(c.val()).contains(platform)){
					return trk.functions.get(platform).get(c.val());  
				}
			}
			
			throw new ConfigCompileException("The function \"" + c.val() + 
					"\" does not exist in the " + platform.platformName(),
							c.getTarget());
        } else {
			throw new ConfigCompileException("Expecting CFunction type", c.getTarget());
		}
    }

	/**
	 * Get functions exposed for a given platform.
	 * @param platform
	 * @return
	 */
    public Set<FunctionBase> getFunctions(api.Platforms platform) {
        if(platform == null){
            Set<FunctionBase> retn = new HashSet<>();
			
            for(api.Platforms p : api.Platforms.values()){
                retn.addAll(getFunctions(p));
            }
			
            return retn;
        }
		
        Set<FunctionBase> retn = new HashSet<>();
		
		for (ExtensionTracker trk: extensions.values()) {
			for(String name : trk.functions.get(platform).keySet()){
				retn.add(trk.functions.get(platform).get(name));
			}
		}
		
        return retn;
    }
}
