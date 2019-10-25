package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.AnnotationMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.GCUtil;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
//import java.nio.file.Files;
//import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtensionManager {

	private static final Map<URL, ExtensionTracker> EXTENSIONS = new HashMap<>();
	private static final List<File> LOCATIONS = new ArrayList<>();

	/**
	 * Allow an external source (such as a Bukkit plugin) register it's own functions and events. EXPERIMENTAL! Could
	 * have bad side-effects! The use of this function is for really advanced users. There is no guarantee of the
	 * fitness of this function for ANY use. You have been warned.
	 *
	 * @param url
	 * @param tracker
	 */
	public static void RegisterTracker(URL url, ExtensionTracker tracker) {
		if(EXTENSIONS.containsKey(url) || EXTENSIONS.containsValue(tracker)) {
			return;
		}

		EXTENSIONS.put(url, tracker);
	}

	/**
	 * Allow external sources to unregister their own trackers. EXPERIMENTAL! Could have bad side-effects! The use of
	 * this function is for really advanced users. There is no guarantee of the fitness of this function for ANY use.
	 * You have been warned.
	 *
	 * @param url
	 * @return
	 */
	public static ExtensionTracker UnregisterTracker(URL url) {
		if(!url.equals(ClassDiscovery.GetClassContainer(ExtensionManager.class))) {
			ExtensionTracker trk = EXTENSIONS.remove(url);
			trk.shutdownTracker();

			return trk;
		}

		return null;
	}

	/**
	 * EXPERIMENTAL! could have bad side-effects! Allow external sources to unregister their own trackers.
	 *
	 * @param tracker
	 * @return
	 */
	public static ExtensionTracker UnregisterTracker(ExtensionTracker tracker) {
		return UnregisterTracker(tracker.container);
	}

	/**
	 * Process the given location for any jars. If the location is a jar, add it directly. If the location is a
	 * directory, look for jars in it.
	 *
	 * @param location file or directory
	 * @return
	 */
	private static List<File> getFiles(File location) {
		List<File> toProcess = new ArrayList<>();

		if(location.isDirectory()) {
			for(File f : location.listFiles()) {
				if(f.getName().endsWith(".jar")) {
					try {
						// Add the trimmed absolute path.
						toProcess.add(f.getCanonicalFile());
					} catch (IOException ex) {
						Static.getLogger().log(Level.SEVERE, "Could not get exact"
								+ " path for " + f.getAbsolutePath(), ex);
					}
				}
			}
		} else if(location.getName().endsWith(".jar")) {
			try {
				// Add the trimmed absolute path.
				toProcess.add(location.getCanonicalFile());
			} catch (IOException ex) {
				Static.getLogger().log(Level.SEVERE, "Could not get exact path"
						+ " for " + location.getAbsolutePath(), ex);
			}
		}

		return toProcess;
	}

	public static Map<URL, ExtensionTracker> getTrackers() {
		return Collections.unmodifiableMap(EXTENSIONS);
	}

	public static void Cache(File extCache, Class... extraClasses) {
		// We will only cache on Windows, as Linux doesn't natively lock
		// files that are in use. Windows prevents any modification, making
		// it harder for server owners on Windows to update the jars.
		boolean onWindows = (OSUtils.GetOS() == OSUtils.OS.WINDOWS);

		if(!onWindows) {
			return;
		}

		// Create the directory if it doesn't exist.
		extCache.mkdirs();

		// Try to delete any loose files in the cache dir, so that we
		// don't load stuff we aren't supposed to. This is in case the shutdown
		// cleanup wasn't successful on the last run.
		for(File f : extCache.listFiles()) {
//			try {
				FileUtil.recursiveDelete(f);
//				Files.delete(f.toPath());
//			} catch (IOException ex) {
//				Static.getLogger().log(Level.WARNING,
//						"[CommandHelper] Could not delete loose file "
//						+ f.getAbsolutePath() + ": " + ex.getMessage());
//			}
		}

		// The cache, cd and dcl here will just be thrown away.
		// They are only used here for the purposes of discovering what a given
		// jar has to offer.
		ClassDiscoveryCache cache = new ClassDiscoveryCache(
				CommandHelperFileLocations.getDefault().getCacheDirectory());
		cache.setLogger(Static.getLogger());
		DynamicClassLoader dcl = new DynamicClassLoader();
		ClassDiscovery cd = new ClassDiscovery();

		cd.setClassDiscoveryCache(cache);
		cd.addDiscoveryLocation(ClassDiscovery.GetClassContainer(ExtensionManager.class));
		for(Class klazz : extraClasses) {
			cd.addDiscoveryLocation(ClassDiscovery.GetClassContainer(klazz));
		}

		//Look in the given locations for jars, add them to our class discovery.
		List<File> toProcess = new ArrayList<>();

		for(File location : LOCATIONS) {
			toProcess.addAll(getFiles(location));
		}

		// Load the files into the discovery mechanism.
		for(File file : toProcess) {
			if(!file.canRead()) {
				continue;
			}

			URL jar;
			try {
				jar = file.toURI().toURL();
			} catch (MalformedURLException ex) {
				Static.getLogger().log(Level.SEVERE, null, ex);
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
		for(ClassMirror<? extends AbstractExtension> extmirror
				: cd.getClassesWithAnnotationThatExtend(
						MSExtension.class, AbstractExtension.class)) {
			if(extmirror.equals(new ClassMirror<>(AbstractExtension.class))) {
				continue;
			}
			AnnotationMirror plug = extmirror.getAnnotation(MSExtension.class);

			URL plugURL = extmirror.getContainer();

			// Get the internal name that this extension exposes.
			if(plugURL != null && plugURL.getPath().endsWith(".jar")) {
				File f;
				try {
					f = new File(URLDecoder.decode(plugURL.getFile(), "UTF8"));
				} catch (UnsupportedEncodingException ex) {
					Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
					continue;
				}

				// Skip extensions that originate from commandhelpercore.
				if(plugURL.equals(ClassDiscovery.GetClassContainer(ExtensionManager.class))) {
					done.add(f);
					continue;
				}

				// Skip files already processed.
				if(done.contains(f)) {
					MSLog.GetLogger().Log(MSLog.Tags.EXTENSIONS, LogLevel.WARNING,
							f.getAbsolutePath() + " contains more than one extension"
							+ " descriptor. Bug someone about it!", Target.UNKNOWN);

					continue;
				}

				done.add(f);

				String name = plug.getValue("value").toString();

				// Just in case we have two plugins with the same internal name,
				// lets track and rename them using a number scheme.
				if(namecount.containsKey(name.toLowerCase())) {
					int i = namecount.get(name.toLowerCase());
					name += "-" + i;
					namecount.put(name.toLowerCase(), ++i);

					MSLog.GetLogger().Log(MSLog.Tags.EXTENSIONS, LogLevel.WARNING,
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
					FileUtil.copy(f, newFile, true);
//					Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException ex) {
					Static.getLogger().log(Level.SEVERE, "Could not copy '"
							+ f.getName() + "' to cache: " + ex.getMessage());
				}
			}
		}

		Set<ClassMirror<?>> classes = cd.getClassesWithAnnotation(api.class);

		// Now process @api annotated extensions, ignoring ones already processed.
		for(ClassMirror klass : classes) {
			URL plugURL = klass.getContainer();

			if(plugURL != null && plugURL.getPath().endsWith(".jar")) {
				File f;
				try {
					f = new File(URLDecoder.decode(plugURL.getFile(), "UTF8"));
				} catch (UnsupportedEncodingException ex) {
					Logger.getLogger(ExtensionManager.class.getName()).log(Level.SEVERE, null, ex);
					continue;
				}

				// Skip files already processed.
				if(done.contains(f)) {
					continue;
				}

				// Copy the file if it's a valid extension.
				// No special processing needed.
				if(cd.doesClassExtend(klass, Event.class)
						|| cd.doesClassExtend(klass, Function.class)) {
					// We're processing it here instead of above, complain about it.
					MSLog.GetLogger().Log(MSLog.Tags.EXTENSIONS, LogLevel.WARNING,
							f.getAbsolutePath() + " is an old-style extension!"
							+ " Bug the author to update it to the new extension system!",
							Target.UNKNOWN);

					// Only process this file once.
					done.add(f);

					File newFile = new File(extCache, "oldstyle-" + f.getName());

					try {
						FileUtil.copy(f, newFile, true);
//						Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException ex) {
						Static.getLogger().log(Level.SEVERE, "Could not copy '"
								+ f.getName() + "' to cache: " + ex.getMessage());
					}
				}
			}
		}

		// Shut down the original dcl to "unlock" the processed jars.
		// The cache and cd instances will just fall into oblivion.
		dcl.destroy();

		// Explicit call. Without this, jar files won't actually get unlocked on
		// Windows.
		GCUtil.BlockUntilGC();
	}

	/**
	 * Initializes the extension manager. This operation is not necessarily required, and must be guaranteed to not run
	 * more than once per ClassDiscovery object.
	 *
	 * @param cd the ClassDiscovery to use for loading files.
	 */
	public static void Initialize(ClassDiscovery cd) {
		EXTENSIONS.clear();

		// Look in the extension folder for jars, add them to our class discover,
		// then initialize everything
		List<File> toProcess = new ArrayList<>();

		// Grab files from the cache if on Windows. Otherwise just load
		// directly from the stored locations.

		if(OSUtils.GetOS().isWindows()) {
			toProcess.addAll(getFiles(CommandHelperFileLocations.getDefault().getExtensionCacheDirectory()));
		} else {
			for(File location : LOCATIONS) {
				toProcess.addAll(getFiles(location));
			}
		}

		DynamicClassLoader dcl = new DynamicClassLoader();
		cd.setDefaultClassLoader(dcl);

		for(File f : toProcess) {
			if(f.getName().endsWith(".jar")) {
				try {
					//First, load it with our custom class loader
					URL jar = f.toURI().toURL();

					dcl.addJar(jar);
					cd.addDiscoveryLocation(jar);

					MSLog.GetLogger().Log(MSLog.Tags.EXTENSIONS, LogLevel.DEBUG, "Loaded " + f.getAbsolutePath(), Target.UNKNOWN);
				} catch (MalformedURLException ex) {
					Static.getLogger().log(Level.SEVERE, null, ex);
				}
			}
		}

		// Grab all known lifecycle classes, and use them. If more than one
		// lifecycle is found per URL, it's stored and used, but the first
		// one found defines the internal name.
		for(ClassMirror<? extends AbstractExtension> extmirror : cd.getClassesWithAnnotationThatExtend(MSExtension.class, AbstractExtension.class)) {
			if(extmirror.equals(new ClassMirror<>(AbstractExtension.class))) {
				continue;
			}
			Extension ext;
			URL url = extmirror.getContainer();
			Class<? extends AbstractExtension> extcls;

			if(extmirror.getModifiers().isAbstract()) {
				Static.getLogger().log(Level.SEVERE, "Probably won't be able to"
						+ " instantiate " + extmirror.getClassName() + ": The"
						+ " class is marked as abstract! Will try anyway.");
			}

			try {
				extcls = extmirror.loadClass(dcl, true);
			} catch (Throwable ex) {
				// May throw anything, and kill the loading process.
				// Lets prevent that!
				Static.getLogger().log(Level.SEVERE, "Could not load class '"
						+ extmirror.getClassName() + "'");
				ex.printStackTrace();
				continue;
			}

			try {
				ext = extcls.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				//Error, but skip this one, don't throw an exception ourselves, just log it.
				Static.getLogger().log(Level.SEVERE, "Could not instantiate "
						+ extcls.getName() + ": " + ex.getMessage());
				continue;
			}

			ExtensionTracker trk = EXTENSIONS.get(url);

			if(trk == null) {
				trk = new ExtensionTracker(url, cd, dcl);

				EXTENSIONS.put(url, trk);
			}

			// Grab the identifier for the first lifecycle we come across and
			// use it.
			if(trk.identifier == null) {
				trk.identifier = ext.getName();
				trk.version = ext.getVersion();
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
		classes.stream().forEach((klass) -> {
			URL url = klass.getContainer();

			if(cd.doesClassExtend(klass, Event.class)
					|| cd.doesClassExtend(klass, Function.class)) {

				Class c;

				try {
					c = klass.loadClass(dcl, true);
				} catch (Throwable ex) {
					// May throw anything, and kill the loading process.
					// Lets prevent that!
					Static.getLogger().log(Level.SEVERE, "Could not load class '"
							+ klass.getClassName() + "'");
					ex.printStackTrace();
					return;
				}

				ExtensionTracker trk = EXTENSIONS.get(url);

				if(trk == null) {
					trk = new ExtensionTracker(url, cd, dcl);
					if(trk.identifier == null) {
						trk.identifier = StringUtils.replaceLast(new java.io.File(url.getPath().replaceFirst("/", ""))
								.getName(), ".jar", "");
					}
					EXTENSIONS.put(url, trk);
				}

				// Instantiate, register and store.
				try {
					if(Event.class.isAssignableFrom(c)) {
						Class<Event> cls = (Class<Event>) c;

						if(klass.getModifiers().isAbstract()) {
							// Abstract? Looks like they accidently @api'd
							// a cheater class. We can't be sure that it is fully
							// defined, so complain to the console.
							MSLog.GetLogger().Log(MSLog.Tags.EXTENSIONS, LogLevel.ERROR,
									"Class " + c.getName() + " in " + url + " is"
									+ " marked as an event but is also abstract."
									+ " Bugs might occur! Bug someone about this!",
									Target.UNKNOWN);
						}

						Event e = cls.newInstance();
						events.add(e.getName());

						trk.registerEvent(e);
					} else if(Function.class.isAssignableFrom(c)) {
						Class<Function> cls = (Class<Function>) c;

						if(klass.getModifiers().isAbstract()) {
							// Abstract? Looks like they accidently @api'd
							// a cheater class. We can't be sure that it is fully
							// defined, so complain to the console.
							MSLog.GetLogger().Log(MSLog.Tags.EXTENSIONS, LogLevel.ERROR,
									"Class " + c.getName() + " in " + url + " is"
									+ " marked as a function but is also abstract."
									+ " Bugs might occur! Bug someone about this!",
									Target.UNKNOWN);
						}

						Function f = cls.newInstance();
						functions.add(f.getName());

						trk.registerFunction(f);
					}
				} catch (InstantiationException ex) {
					Static.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
				} catch (IllegalAccessException ex) {
					Static.getLogger().log(Level.SEVERE, null, ex);
				}
			}
		});

		// Lets print out the details to the console, if we are in debug mode.
		try {
			if(Prefs.DebugMode()) {
				StreamUtils.GetSystemOut().println(Implementation.GetServerType().getBranding()
						+ ": Loaded " + functions.size() + " function" + (functions.size() == 1 ? "." : "s."));
				StreamUtils.GetSystemOut().println(Implementation.GetServerType().getBranding()
						+ ": Loaded " + events.size() + " event" + (events.size() == 1 ? "." : "s."));
			}
		} catch (Throwable e) {
			// Prefs weren't loaded, probably caused by running tests.
		}
	}

	/**
	 * To be run when we are shutting everything down.
	 */
	public static void Cleanup() {
		// Shutdown and release all the extensions
		Shutdown();
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			trk.shutdownTracker();
		}

		EXTENSIONS.clear();

		// Clean up the loaders and discovery instances.
		ClassDiscovery.getDefaultInstance().invalidateCaches();
		ClassLoader loader = ClassDiscovery.getDefaultInstance().getDefaultClassLoader();

		if(loader instanceof DynamicClassLoader) {
			DynamicClassLoader dcl = (DynamicClassLoader) loader;
			dcl.destroy();
		}

		// Explicit call. Without this, jar files won't actually get unlocked on
		// Windows.
		GCUtil.BlockUntilGC();

		File cacheDir = CommandHelperFileLocations.getDefault().getExtensionCacheDirectory();

		if(!cacheDir.exists() || !cacheDir.isDirectory()) {
			return;
		}

		// Try to delete any loose files in the cache dir.
		for(File f : cacheDir.listFiles()) {
//			try {
				FileUtil.recursiveDelete(f);
//				Files.delete(f.toPath());
//			} catch (IOException ex) {
//				StreamUtils.GetSystemOut().println("[CommandHelper] Could not delete loose file "
//						+ f.getAbsolutePath() + ": " + ex.getMessage());
//			}
		}
	}

	/**
	 * This should be run each time the "startup" of the runtime occurs or extensions are reloaded.
	 */
	public static void Startup() {
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			for(Extension ext : trk.getExtensions()) {
				try {
					ext.onStartup();
				} catch (Throwable e) {
					Logger log = Static.getLogger();
					log.log(Level.SEVERE, ext.getClass().getName()
							+ "'s onStartup caused an exception:");
					log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}
	}

	/**
	 * This should be run each time the "shutdown" of the runtime occurs or extensions are reloaded.
	 */
	public static void Shutdown() {
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			for(Extension ext : trk.getExtensions()) {
				try {
					ext.onShutdown();
				} catch (Throwable e) {
					Logger log = Static.getLogger();
					log.log(Level.SEVERE, ext.getClass().getName()
							+ "'s onShutdown caused an exception:");
					log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}
	}

	public static void PreReloadAliases(AliasCore.ReloadOptions options) {
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			for(Extension ext : trk.getExtensions()) {
				try {
					ext.onPreReloadAliases(options);
				} catch (Throwable e) {
					Logger log = Static.getLogger();
					log.log(Level.SEVERE, ext.getClass().getName()
							+ "'s onPreReloadAliases caused an exception:");
					log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}
	}

	public static void PostReloadAliases() {
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			for(Extension ext : trk.getExtensions()) {
				try {
					ext.onPostReloadAliases();
				} catch (Throwable e) {
					Logger log = Static.getLogger();
					log.log(Level.SEVERE, ext.getClass().getName()
							+ "'s onPreReloadAliases caused an exception:");
					log.log(Level.SEVERE, StackTraceUtils.GetStacktrace(e));
				}
			}
		}
	}

	public static void AddDiscoveryLocation(File file) {
		try {
			LOCATIONS.add(file.getCanonicalFile());
		} catch (IOException ex) {
			Static.getLogger().log(Level.SEVERE, null, ex);
		}
	}

	public static Set<Event> GetEvents() {
		Set<Event> retn = new HashSet<>();

		for(ExtensionTracker trk : EXTENSIONS.values()) {
			retn.addAll(trk.getEvents());
		}

		return retn;
	}

	public static Set<Event> GetEvents(Driver type) {
		Set<Event> retn = new HashSet<>();

		for(ExtensionTracker trk : EXTENSIONS.values()) {
			retn.addAll(trk.getEvents(type));
		}

		return retn;
	}

	public static Event GetEvent(Driver type, String name) {
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			Set<Event> events = trk.getEvents(type);

			for(Event event : events) {
				if(event.getName().equalsIgnoreCase(name)) {
					return event;
				}
			}
		}

		return null;
	}

	public static Event GetEvent(String name) {
		for(ExtensionTracker trk : EXTENSIONS.values()) {
			Set<Event> events = trk.getEvents();

			for(Event event : events) {
				if(event.getName().equalsIgnoreCase(name)) {
					return event;
				}
			}
		}

		return null;
	}

	/**
	 * This runs the hooks on all events. This should be called each time the server "starts up".
	 */
	public static void RunHooks() {
		for(Event event : GetEvents()) {
			try {
				event.hook();
			} catch (UnsupportedOperationException ex) {
			}
		}
	}

	public static FunctionBase GetFunction(Construct c, api.Platforms platform,
			Set<Class<? extends Environment.EnvironmentImpl>> envs)
			throws ConfigCompileException {
		if(platform == null) {
			//Default to the Java interpreter
			platform = api.Platforms.INTERPRETER_JAVA;
		}

		if(c instanceof CFunction) {
			functionLoop: for(ExtensionTracker trk : EXTENSIONS.values()) {
				if(trk.functions.get(platform).containsKey(c.val())
						&& trk.supportedPlatforms.get(c.val()).contains(platform)) {
					FunctionBase func = trk.functions.get(platform).get(c.val());
					if(envs != null) {
						api api = func.getClass().getAnnotation(api.class);
						for(Class<? extends Environment.EnvironmentImpl> epl : api.environments()) {
							if(!envs.contains(epl)) {
								continue functionLoop;
							}
						}
					}
					return func;
				}
			}

			throw new ConfigCompileException("The function \"" + c.val()
					+ "\" does not exist in the " + platform.platformName(),
					c.getTarget());
		} else {
			throw new ConfigCompileException("Expecting CFunction type", c.getTarget());
		}
	}

	public static Set<FunctionBase> GetFunctions(api.Platforms platform,
			Set<Class<? extends Environment.EnvironmentImpl>> envs) {
		if(platform == null) {
			Set<FunctionBase> retn = new HashSet<>();

			for(api.Platforms p : api.Platforms.values()) {
				retn.addAll(GetFunctions(p, envs));
			}

			return retn;
		}

		Set<FunctionBase> retn = new HashSet<>();

		for(ExtensionTracker trk : EXTENSIONS.values()) {
			addList: for(FunctionBase func : trk.functions.get(platform).values()) {
				// Functions which use a given environment are not valid if the current runtime does not contain
				// that environment.
				if(envs != null) {
					api api = func.getClass().getAnnotation(api.class);
					for(Class<? extends Environment.EnvironmentImpl> epl : api.environments()) {
						if(!envs.contains(epl)) {
							continue addList;
						}
					}
				}
				retn.add(func);
			}
		}

		return retn;
	}
}
