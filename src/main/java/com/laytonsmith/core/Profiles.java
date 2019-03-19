package com.laytonsmith.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
public interface Profiles {

	/**
	 * Returns the profile associated with this profile set by id.
	 *
	 * @param id
	 * @return
	 * @throws InvalidProfileException If the profile doesn't exist.
	 */
	Profile getProfileById(String id) throws InvalidProfileException;

	/**
	 * A profile represents an individual connection. Different connections require different information, so most
	 * methods are defined in subclasses. It is expected that subclasses have a constructor that matches the signature
	 * (String id, Map&lt;String, String&gt; elements) that contains the parsed xml for that profile. validation can be
	 * done in the constructor, and an InvalidProfileException can be thrown if there is invalid or missing data.
	 */
	public abstract static class Profile implements Comparable<Profile> {

		private final String id;

		protected Profile(String id) {
			this.id = id;
		}

		/**
		 * Returns the ID for this profile.
		 *
		 * @return
		 */
		public String getID() {
			return id;
		}

		/**
		 * Returns the type of this profile.
		 *
		 * @return
		 */
		public String getType() {
			return this.getClass().getAnnotation(ProfileType.class).type();
		}

		@Override
		public int compareTo(Profile o) {
			return this.id.compareTo(o.id);
		}

		//Subclasses are encouraged to override this
		@Override
		public String toString() {
			return (id == null ? "" : "(" + id + ") ") + getType();
		}
	}

	/**
	 * Subclasses of Profile should tag with this annotation, and provide the type specified.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ProfileType {

		/**
		 * This is the type for the Profile. If this class has this type, it is selected when that type is specified in
		 * the profile.
		 *
		 * @return
		 */
		String type();
	}

	/**
	 * An InvalidProfileException is thrown if the profile provided is invalid, either due to invalid profile data or
	 * bad xml validation.
	 */
	public static class InvalidProfileException extends Exception {

		public InvalidProfileException() {
		}

		public InvalidProfileException(String message) {
			super(message);
		}

		public InvalidProfileException(String message, Throwable t) {
			super(message, t);
		}

		public InvalidProfileException(Throwable t) {
			super(t);
		}

	}

}
