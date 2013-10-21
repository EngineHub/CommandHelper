
package com.laytonsmith.database;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.XMLDocument;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * A Profiles object represents an xml document that lists out all the profiles available on
 * the system. A profile can then be retrieved based on the profile name. XML validation
 * also occurs during startup with this class.
 */
public class Profiles {
	
	private final XMLDocument document;
	private final Map<String, Profile> profiles = new HashMap<String, Profile>();
	private static Map<String, Class<Profile>> profileTypes = null;
	
	/**
	 * 
	 * @param xml
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException
	 */
	public Profiles(String xml) throws InvalidProfileException {
		try{
			document = new XMLDocument(xml);
		} catch(SAXException ex){
			throw new InvalidProfileException(ex);
		}
		parse();
	}
	
	/**
	 * 
	 * @param profileFile
	 * @throws IOException
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException 
	 */
	public Profiles(File profileFile) throws IOException, InvalidProfileException{
		this(FileUtil.readAsStream(profileFile));
	}
	
	/**
	 * 
	 * @param profileData
	 * @throws IOException
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException 
	 */
	public Profiles(InputStream profileData) throws IOException, InvalidProfileException{
		try{
			document = new XMLDocument(profileData);
		} catch(SAXException ex){
			throw new InvalidProfileException(ex);
		}
		parse();
	}
	
	private void parse() throws InvalidProfileException {
		int profileCount;
		try {
			profileCount = document.countNodes("/profiles/profile");
		} catch (XPathExpressionException ex) {
			throw new InvalidProfileException("Missing root /profiles element");
		}
		
		for(int i = 1; i < profileCount + 1; i++){
			String id;
			String type;
			try {
				id = document.getNode("/profiles/profile[" + i + "]/@id");
			} catch(XPathExpressionException ex){
				throw new InvalidProfileException("All <profile> elements must have an id attribute.");
			}
			try {
				type = document.getNode("/profiles/profile[" + i + "]/type");
			} catch(XPathExpressionException ex){
				throw new InvalidProfileException("All <profile> elements must have a type attribute.");
			}
			List<String> children;
			try {
				children = document.getChildren("/profiles/profile[" + i + "]");
			} catch (XPathExpressionException ex) {
				//Shouldn't happen, this is our fault.
				throw new RuntimeException(ex);
			}
			Map<String, String> elements = new HashMap<String, String>();
			for(String child : children){
				try {
					elements.put(child, document.getNode("/profiles/profile[" + i + "]/" + child));
				} catch (XPathExpressionException ex) {
					//Shouldn't happen
					throw new RuntimeException(ex);
				}
			}
			//Type is only used internally, so we remove it at this point.
			elements.remove("type");
			//If the profile already exists in the set, throw an exception. (Profiles are sorted by ID, so that needs to be unique.)
			if(profiles.containsKey(id)){
				throw new InvalidProfileException("Duplicate profile id found: \"" + id + "\"");
			}
			profiles.put(id, getProfile0(id, type, elements));
		}
		//Profiles are all built and validated now.
	}
	
	/**
	 * Returns the profile associated with this profile set by id.
	 * @param id
	 * @return
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException If the profile doesn't exist.
	 */
	public Profile getProfileById(String id) throws InvalidProfileException {
		if(!profiles.containsKey(id)){
			throw new InvalidProfileException("No profile by the name \"" + id + "\" was found.");
		}
		return profiles.get(id);
	}
	
	@Override
	public String toString(){
		return StringUtils.Join(profileTypes, "=", "; ");
	}
	
	/**
	 * Private version that allows specifying the id.
	 * @param id
	 * @param type
	 * @param data
	 * @return
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException 
	 */
	private static Profile getProfile0(String id, String type, Map<String, String> data) throws InvalidProfileException{
		if(profileTypes == null){
			profileTypes = new HashMap<String, Class<Profile>>();
			for(Class<Profile> p : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(ProfileType.class, Profile.class)){
				String t = p.getAnnotation(ProfileType.class).type();
				profileTypes.put(t, p);
			}
		}
		if(!profileTypes.containsKey(type)){
			throw new InvalidProfileException("Unknown type \"" + type + "\"");
		}
		Profile profile = ReflectionUtils.newInstance(profileTypes.get(type), new Class[]{String.class, Map.class}, 
				new Object[]{id, data});
		return profile;
	}
	
	/**
	 * Utility method for retrieving a Profile object given pre-parsed connection information.
	 * The type is assumed to be apart of the data provided. No id is required as part of the data.
	 * @param data
	 * @return
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException 
	 */
	public static Profile getProfile(Map<String, String> data) throws InvalidProfileException {
		if(!data.containsKey("type")){
			throw new InvalidProfileException("Missing \"type\"");
		}
		String type = data.get("type");
		data.remove("type");
		return getProfile(type, data);
	}
	
	/**
	 * Utility method for retrieving a Profile object given pre-parsed connection
	 * information, including the type. No id is required as part of the data.
	 * @param type
	 * @param data
	 * @return
	 * @throws com.laytonsmith.database.Profiles.InvalidProfileException If anything is invalid about the
	 * data, including the type being unknown.
	 */
	public static Profile getProfile(String type, Map<String, String> data) throws InvalidProfileException {
		return getProfile0(null, type, data);
	}
	
	
	/**
	 * A profile represents an individual connection. Different connections
	 * require different information, so most methods are defined in subclasses.
	 * It is expected that subclasses have a constructor that matches the signature
	 * (String id, Map&lt;String, String&gt; elements) that contains the parsed xml for that profile.
	 * validation can be done in the constructor, and an InvalidProfileException
	 * can be thrown if there is invalid or missing data.
	 */
	public static abstract class Profile implements Comparable<Profile>{
		private final String id;
		protected Profile(String id){
			this.id = id;
		}
		
		/**
		 * Returns the ID for this profile.
		 * @return 
		 */
		public String getID(){
			return id;
		}
		
		/**
		 * Returns the type of this profile.
		 * @return 
		 */
		public String getType(){
			return this.getClass().getAnnotation(ProfileType.class).type();
		}

		@Override
		public int compareTo(Profile o) {
			return this.id.compareTo(o.id);
		}
		
		/**
		 * Given the connection details, this should return the proper 
		 * connection string that the actual database connector will use
		 * to create a connection with this profile. Additionally, during this
		 * step, it should be verified that the SQL driver is present.
		 * @return 
		 * @throws SQLException If the database driver doesn't exist.
		 */
		public abstract String getConnectionString() throws SQLException;

		//Subclasses are encouraged to override this
		@Override
		public String toString(){
			return (id == null ? "" : "(" + id + ") ") + getType();
		}
	}
	
	/**
	 * Subclasses of Profile should tag with this annotation, and provide the type
	 * specified.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ProfileType {
		/**
		 * This is the type for the Profile. If this class has this type, it is
		 * selected when that type is specified in the profile.
		 * @return 
		 */
		String type();
	}
	
	/**
	 * An InvalidProfileException is thrown if the profile provided is invalid,
	 * either due to invalid profile data or bad xml validation.
	 */
	public static class InvalidProfileException extends Exception {

		public InvalidProfileException() {
		}
		
		public InvalidProfileException(String message){
			super(message);
		}
		
		public InvalidProfileException(String message, Throwable t){
			super(message, t);
		}
		
		public InvalidProfileException(Throwable t){
			super(t);
		}
		 
	}
}
