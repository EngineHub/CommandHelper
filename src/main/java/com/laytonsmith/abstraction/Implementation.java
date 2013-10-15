package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Prefs;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * This class dynamically detects the server version being run, using various
 * checks as needed.
 *
 * @author layton
 */
public final class Implementation {

	//TODO: Ideally, this would not be static based, and instead, upon server startup, something would
	//pass an implementation of an EnvironmentProvider in, and that implement some number of methods
	//that could be used to hook in to the server.
	private Implementation() {
	}
	private static Implementation.Type serverType = null;
	private static boolean useAbstractEnumThread = true;
	
	/**
	 * Sets whether or not we should verify enums when setServerType is called.
	 * Defaults to true.
	 * @param on 
	 */
	public static void useAbstractEnumThread(boolean on){
		useAbstractEnumThread = on;
	}

	public static void setServerType(Implementation.Type type) {
		if (serverType == null) {
			serverType = type;
		} else {
			if (type != Type.TEST) { //This could potentially happen, but we don't care in the case that we
				//are testing, so don't error out here. (Failures may occur elsewhere though... :()
				throw new RuntimeException("Server type is already set! Cannot re-set!");
			}
		}

		//Fire off our abstractionenum checks in a new Thread
		if (type != Type.TEST && type != Type.SHELL && useAbstractEnumThread) {
			Thread abstractionenumsThread;
			abstractionenumsThread = new Thread(new Runnable() {
				public void run() {
					try {
						//This goes ahead and runs the cache, which we can offset to our own thread.
						StaticLayer.GetConvertor().getMaxBlockID();
						try {
							//Let the server startup data blindness go by first, so we display any error messages prominently,
							//since an Error is a case of very bad code that shouldn't have been released to begin with.
							Thread.sleep(15000);
						} catch (InterruptedException ex) {
							//
						}
						Set<Class> abstractionenums = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(abstractionenum.class);
						for (Class c : abstractionenums) {
							abstractionenum annotation = (abstractionenum) c.getAnnotation(abstractionenum.class);
							if (EnumConvertor.class.isAssignableFrom(c)) {
								EnumConvertor<Enum, Enum> convertor;
								try {
									//Now, if this is not the current server type, skip it
									if (annotation.implementation() != serverType) {
										continue;
									}
									//Next, verify usage of the annotation (it is an error if not used properly)
									//All EnumConvertor subclasses should have public static getConvertor methods, let's grab it now
									Method m = c.getDeclaredMethod("getConvertor");
									convertor = (EnumConvertor<Enum, Enum>) m.invoke(null);
									//Go through and check for a proper mapping both ways, from concrete to abstract, and vice versa.
									//At this point, if there is an error, it is only a warning, NOT an error.
									Class abstractEnum = annotation.forAbstractEnum();
									Class concreteEnum = annotation.forConcreteEnum();
									checkEnumConvertors(convertor, abstractEnum, concreteEnum, false);
									checkEnumConvertors(convertor, concreteEnum, abstractEnum, true);

								} catch (IllegalAccessException ex) {
									throw new Error(ex);
								} catch (IllegalArgumentException ex) {
									throw new Error(ex);
								} catch (InvocationTargetException ex) {
									throw new Error(ex);
								} catch (NoSuchMethodException ex) {
									throw new Error(serverType.getBranding() + ": The method with signature public static " + c.getName() + " getConvertor() was not found in " + c.getName()
											+ " Please add the following code: \n"
											+ "private static " + c.getName() + " instance;\n"
											+ "public static " + c.getName() + " getConvertor(){\n"
											+ "\tif(instance == null){\n"
											+ "\t\tinstance = new " + c.getName() + "();\n"
											+ "\t}\n"
											+ "\treturn instance;\n"
											+ "}\n"
											+ "If you do not know what  error is, please report this to the developers.");
								}
							} else {
								throw new Error("Only classes that extend EnumConvertor may use @abstractionenum. " + c.getName() + " does not, yet it uses the annotation.");
							}

						}
					} catch (Exception e) {
						boolean debugMode;
						try {
							debugMode = Prefs.DebugMode();
						} catch(RuntimeException ex){
							//Set it to true if we fail to load prefs, which can happen
							//with a buggy front end.
							debugMode = true;
						}
						if (debugMode) {
							//If we're in debug mode, sure, go ahead and print the stack trace,
							//but otherwise we don't want to bother the user.
							e.printStackTrace();
						}
					}
				}
			}, "Abstraction Enum Verification Thread");
			abstractionenumsThread.setPriority(Thread.MIN_PRIORITY);
			abstractionenumsThread.setDaemon(true);
			abstractionenumsThread.start();
		}
	}

	private static void checkEnumConvertors(EnumConvertor convertor, Class to, Class from, boolean isToConcrete) {
		for (Object enumConst : from.getEnumConstants()) {
			ReflectionUtils.set(EnumConvertor.class, convertor, "useError", false);
			try {
				if (isToConcrete) {
					convertor.getConcreteEnum((Enum) enumConst);
				} else {
					convertor.getAbstractedEnum((Enum) enumConst);
				}
			} catch (IllegalArgumentException e) {
				//Ignored, it should have already logged it
			}
			ReflectionUtils.set(EnumConvertor.class, convertor, "useError", true);
		}
	}

	/**
	 * These are all the supported server types
	 */
	public static enum Type {

		TEST("test-backend"),
		BUKKIT("CommandHelper"),
		SHELL("MethodScript");
		//GLOWSTONE,
		//SINGLE_PLAYER
		private final String branding;

		/**
		 *
		 * @param branding This MUST be a universally acceptable folder name.
		 */
		private Type(String branding) {
			this.branding = branding;
		}

		/**
		 * Returns the branding string for this implementation.
		 *
		 * @return
		 */
		public String getBranding() {
			return branding;
		}
	}

	/**
	 * Returns the server type currently running
	 *
	 * @return
	 */
	public static Type GetServerType() {
		if (serverType == null) {
			throw new RuntimeException("Server type has not been set yet! Please call Implementation.setServerType with the appropriate implementation.");
		}
		return serverType;
	}
}
