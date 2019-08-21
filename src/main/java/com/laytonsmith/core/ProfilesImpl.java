package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.XMLDocument;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * A Profiles object represents an xml document that lists out all the profiles available on the system. A profile can
 * then be retrieved based on the profile name. XML validation also occurs during startup with this class.
 */
public class ProfilesImpl implements Profiles {

	private final XMLDocument document;
	private final Map<String, Profile> profiles = new HashMap<>();
	private static Map<String, Class<? extends Profile>> profileTypes = null;

	/**
	 *
	 * @param xml
	 * @throws InvalidProfileException
	 */
	public ProfilesImpl(String xml) throws InvalidProfileException {
		try {
			document = new XMLDocument(xml);
		} catch (SAXException ex) {
			throw new InvalidProfileException(ex);
		}
		parse();
	}

	/**
	 *
	 * @param profileFile
	 * @throws IOException
	 * @throws InvalidProfileException
	 */
	public ProfilesImpl(File profileFile) throws IOException, InvalidProfileException {
		this(FileUtil.readAsStream(profileFile));
	}

	/**
	 *
	 * @param profileData
	 * @throws IOException
	 * @throws InvalidProfileException
	 */
	public ProfilesImpl(InputStream profileData) throws IOException, InvalidProfileException {
		try {
			document = new XMLDocument(profileData);
		} catch (SAXException ex) {
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

		for(int i = 1; i < profileCount + 1; i++) {
			String id;
			String type;
			try {
				id = document.getNode("/profiles/profile[" + i + "]/@id");
			} catch (XPathExpressionException ex) {
				throw new InvalidProfileException("All <profile> elements must have an id attribute.");
			}
			try {
				type = document.getNode("/profiles/profile[" + i + "]/type");
			} catch (XPathExpressionException ex) {
				throw new InvalidProfileException("All <profile> elements must have a type attribute.");
			}
			List<String> children;
			try {
				children = document.getChildren("/profiles/profile[" + i + "]");
			} catch (XPathExpressionException ex) {
				//Shouldn't happen, this is our fault.
				throw new RuntimeException(ex);
			}
			Map<String, String> elements = new HashMap<>();
			for(String child : children) {
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
			if(profiles.containsKey(id)) {
				throw new InvalidProfileException("Duplicate profile id found: \"" + id + "\"");
			}
			profiles.put(id, getProfile0(id, type, elements));
		}
		//Profiles are all built and validated now.
	}

	/**
	 * Returns the profile associated with this profile set by id.
	 *
	 * @param id
	 * @return
	 * @throws InvalidProfileException If the profile doesn't exist.
	 */
	@Override
	public Profile getProfileById(String id) throws InvalidProfileException {
		if(!profiles.containsKey(id)) {
			throw new InvalidProfileException("No profile by the name \"" + id + "\" was found.");
		}
		return profiles.get(id);
	}

	@Override
	public String toString() {
		return StringUtils.Join(profileTypes, "=", "; ");
	}

	/**
	 * Private version that allows specifying the id.
	 *
	 * @param id
	 * @param type
	 * @param data
	 * @return
	 * @throws InvalidProfileException
	 */
	private static Profile getProfile0(String id, String type, Map<String, String> data) throws InvalidProfileException {
		if(profileTypes == null) {
			profileTypes = new HashMap<>();
			for(Class<? extends Profile> p : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(ProfileType.class, Profile.class)) {
				if(p == Profile.class) {
					continue;
				}
				String t = p.getAnnotation(ProfileType.class).type();
				profileTypes.put(t, p);
			}
		}
		if(!profileTypes.containsKey(type)) {
			throw new InvalidProfileException("Unknown type \"" + type + "\"");
		}
		try {
			return ReflectionUtils.newInstance(profileTypes.get(type), new Class[]{String.class, Map.class},
					new Object[]{id, data});
		} catch (ReflectionUtils.ReflectionException ex) {
			if(ex.getCause().getCause() instanceof InvalidProfileException) {
				throw (InvalidProfileException) ex.getCause().getCause();
			}
			throw ex;
		}
	}

	/**
	 * Utility method for retrieving a Profile object given pre-parsed connection information. The type is assumed to be
	 * apart of the data provided. No id is required as part of the data.
	 *
	 * @param data
	 * @return
	 * @throws InvalidProfileException
	 */
	public static Profile getProfile(Map<String, String> data) throws InvalidProfileException {
		if(!data.containsKey("type")) {
			throw new InvalidProfileException("Missing \"type\"");
		}
		String type = data.get("type");
		data.remove("type");
		return getProfile(type, data);
	}

	/**
	 * Utility method for retrieving a Profile object given pre-parsed connection information, including the type. No id
	 * is required as part of the data.
	 *
	 * @param type
	 * @param data
	 * @return
	 * @throws InvalidProfileException If anything is invalid about the data, including the type being unknown.
	 */
	public static Profile getProfile(String type, Map<String, String> data) throws InvalidProfileException {
		return getProfile0(null, type, data);
	}

}
