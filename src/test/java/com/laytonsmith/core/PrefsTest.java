package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Type;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 *
 */
public class PrefsTest {

	@Test
	@Ignore
	public void testPrefs() throws Exception {
		Map<String, Preferences.Preference> prefs;
		File prefsFile = new File("plugins/CommandHelper/preferences.ini");
		Prefs.init(prefsFile);
		prefsFile.deleteOnExit();
		Field f = Preferences.class.getDeclaredField("prefs");
		f.setAccessible(true);
		Field storedPrefs = Prefs.class.getDeclaredField("prefs");
		storedPrefs.setAccessible(true);
		prefs = (Map<String, Preferences.Preference>) f.get(storedPrefs.get(null));
		assertNotNull(prefs);
		for(String name : prefs.keySet()) {
			String[] parts = name.split("-");
			StringBuilder b = new StringBuilder();
			for(String part : parts) {
				if(part.length() > 1) {
					b.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
				} else {
					b.append(part.toUpperCase());
				}
			}
			String functionName = b.toString();
			Map<String, Preferences.Preference> prfs = (Map<String, Preferences.Preference>) prefs.get(name);
			for(Preferences.Preference p : prfs.values()) {
				//Ok, functionName is the name of the method. Let's first make sure it exists.
				Method function;
				try {
					function = Prefs.class.getDeclaredMethod(functionName);
				} catch (NoSuchMethodException e) {
					fail("Need method " + functionName + " to be included in Prefs.");
					return;
				}
				//Now we need to make sure that it returns the correct type
				Class returnType = function.getReturnType();
				if(p.allowed == Type.BOOLEAN && returnType.equals(Boolean.class)
						|| p.allowed == Type.DOUBLE && returnType.equals(Double.class)
						|| p.allowed == Type.INT && returnType.equals(Integer.class)
						|| p.allowed == Type.STRING && returnType.equals(String.class)
						|| p.allowed == Type.NUMBER && returnType.equals(Double.class)) {
					//Good
				} else {
					fail("Incorrect return type for " + functionName);
				}
			}
		}
	}
}
