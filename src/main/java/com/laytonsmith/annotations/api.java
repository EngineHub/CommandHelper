package com.laytonsmith.annotations;

import com.laytonsmith.core.PlatformResolver;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.functions.bash.BashPlatformResolver;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Marks a function as an API function, which includes it in the list of functions.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
public @interface api {

	public enum Platforms {
		INTERPRETER_JAVA(null, "Java Interpreter"),
		COMPILER_BASH(new BashPlatformResolver(), "Bash Compiler");
		private final PlatformResolver resolver;
		private final String platformName;

		/**
		 * Returns the platform specific resolver, which is able to override base functionality, which will be adjusted
		 * as needed. If the resolver is null, one does not exist, implying that the default is fine.
		 *
		 * @return
		 */
		public PlatformResolver getResolver() {
			return this.resolver;
		}

		public String platformName() {
			return this.platformName;
		}

		private Platforms(PlatformResolver resolver, String platformName) {
			this.resolver = resolver;
			this.platformName = platformName;
		}
	}

	/**
	 * Returns the platform this is implemented for. The default is {@link api.Platforms#INTERPRETER_JAVA}.
	 *
	 * @see {@link api.Platforms#INTERPRETER_JAVA}.
	 * @return
	 */
	Platforms[] platform() default {api.Platforms.INTERPRETER_JAVA};

	/**
	 * Returns the environments this api element relies on. The default is an empty array,
	 * but note that GlobalEnv.class is
	 * implied for all elements, and it is not required to add that to this list. This list
	 * is what determines if a compile error should be displayed or not, if this function is
	 * used in an unsupported environment. There is no other functionality implied by this,
	 * so it is not going to cause an error if missing, but it will cause errors that could
	 * have been caught at compile time, to be runtime errors instead. Therefore, it is
	 * still important to get this correct.
	 *
	 * @return
	 */
	Class<? extends Environment.EnvironmentImpl>[] environments() default {};

	/**
	 * If this api element is enabled. The default is {@code true}, but you can temporarily disable an element by
	 * setting this to false.
	 *
	 * @return
	 */
	boolean enabled() default true;

	/**
	 * This is a list of valid classes that are valid to be tagged with this annotation.
	 */
	public static enum ValidClasses {
		FUNCTION(com.laytonsmith.core.functions.FunctionBase.class),
		EVENT(com.laytonsmith.core.events.Event.class);
		private static List<Class> classes = null;
		Class classType;

		private ValidClasses(Class c) {
			classType = c;
		}

		/**
		 * Returns a copy of the list of valid classes that may be tagged with the api annotation.
		 *
		 * @return
		 */
		public static List<Class> Classes() {
			if(classes == null) {
				Class[] cc = new Class[ValidClasses.values().length];
				for(int i = 0; i < ValidClasses.values().length; i++) {
					cc[i] = ValidClasses.values()[i].classType;
				}
				classes = Arrays.asList(cc);
			}
			return new ArrayList<Class>(classes);
		}

		/**
		 * Returns true if the specified class extends a valid class.
		 *
		 * @param c
		 * @return
		 */
		public static boolean IsValid(Class c) {
			for(Class cc : Classes()) {
				if(cc.isAssignableFrom(c)) {
					return true;
				}
			}
			return false;
		}
	}
}
