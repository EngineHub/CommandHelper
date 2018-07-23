package com.laytonsmith.PureUtilities.Common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

@SuppressWarnings("StaticNonFinalUsedInInitialization")
public final class WinRegistry {

	public static final int HKEY_CURRENT_USER = 0x80000001;
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	public static final int REG_SUCCESS = 0;
	public static final int REG_NOTFOUND = 2;
	public static final int REG_ACCESSDENIED = 5;

	private static final int KEY_ALL_ACCESS = 0xf003f;
	private static final int KEY_READ = 0x20019;
	private static final Preferences USER_ROOT = Preferences.userRoot();
	private static final Preferences SYSTEM_ROOT = Preferences.systemRoot();
	private static final Class<? extends Preferences> USER_CLASS = USER_ROOT.getClass();
	private static Method regOpenKey = null;
	private static Method regCloseKey = null;
	private static Method regQueryValueEx = null;
	private static Method regEnumValue = null;
	private static Method regQueryInfoKey = null;
	private static Method regEnumKeyEx = null;
	private static Method regCreateKeyEx = null;
	private static Method regSetValueEx = null;
	private static Method regDeleteKey = null;
	private static Method regDeleteValue = null;

	static {
		try {
			regOpenKey = USER_CLASS.getDeclaredMethod("WindowsRegOpenKey",
					new Class[]{int.class, byte[].class, int.class});
			regOpenKey.setAccessible(true);
			regCloseKey = USER_CLASS.getDeclaredMethod("WindowsRegCloseKey",
					new Class[]{int.class});
			regCloseKey.setAccessible(true);
			regQueryValueEx = USER_CLASS.getDeclaredMethod("WindowsRegQueryValueEx",
					new Class[]{int.class, byte[].class});
			regQueryValueEx.setAccessible(true);
			regEnumValue = USER_CLASS.getDeclaredMethod("WindowsRegEnumValue",
					new Class[]{int.class, int.class, int.class});
			regEnumValue.setAccessible(true);
			regQueryInfoKey = USER_CLASS.getDeclaredMethod("WindowsRegQueryInfoKey1",
					new Class[]{int.class});
			regQueryInfoKey.setAccessible(true);
			regEnumKeyEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegEnumKeyEx", new Class[]{int.class, int.class,
						int.class});
			regEnumKeyEx.setAccessible(true);
			regCreateKeyEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegCreateKeyEx", new Class[]{int.class,
						byte[].class});
			regCreateKeyEx.setAccessible(true);
			regSetValueEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegSetValueEx", new Class[]{int.class,
						byte[].class, byte[].class});
			regSetValueEx.setAccessible(true);
			regDeleteValue = USER_CLASS.getDeclaredMethod(
					"WindowsRegDeleteValue", new Class[]{int.class,
						byte[].class});
			regDeleteValue.setAccessible(true);
			regDeleteKey = USER_CLASS.getDeclaredMethod(
					"WindowsRegDeleteKey", new Class[]{int.class,
						byte[].class});
			regDeleteKey.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new Error(e);
		}
	}

	private WinRegistry() {
	}

	/**
	 * Read a value from key and value name
	 *
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @param valueName
	 * @return the value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static String readString(int hkey, String key, String valueName)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			return readString(SYSTEM_ROOT, hkey, key, valueName);
		} else if(hkey == HKEY_CURRENT_USER) {
			return readString(USER_ROOT, hkey, key, valueName);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static String readString(Preferences root, int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[]{
			new Integer(hkey), toCstr(key), new Integer(KEY_READ)});
		if(handles[1] != REG_SUCCESS) {
			return null;
		}
		byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[]{
			new Integer(handles[0]), toCstr(value)});
		regCloseKey.invoke(root, new Object[]{new Integer(handles[0])});
		return (valb != null ? new String(valb).trim() : null);
	}

	/**
	 * Read value(s) and value name(s) form given key
	 *
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @return the value name(s) plus the value(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Map<String, String> readStringValues(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			return readStringValues(SYSTEM_ROOT, hkey, key);
		} else if(hkey == HKEY_CURRENT_USER) {
			return readStringValues(USER_ROOT, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static Map<String, String> readStringValues(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		HashMap<String, String> results = new HashMap<String, String>();
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[]{
			new Integer(hkey), toCstr(key), new Integer(KEY_READ)});
		if(handles[1] != REG_SUCCESS) {
			return null;
		}
		int[] info = (int[]) regQueryInfoKey.invoke(root,
				new Object[]{new Integer(handles[0])});

		int count = info[0]; // count
		int maxlen = info[3]; // value length max
		for(int index = 0; index < count; index++) {
			byte[] name = (byte[]) regEnumValue.invoke(root, new Object[]{
				new Integer(handles[0]), new Integer(index), new Integer(maxlen + 1)});
			String value = readString(hkey, key, new String(name));
			results.put(new String(name).trim(), value);
		}
		regCloseKey.invoke(root, new Object[]{new Integer(handles[0])});
		return results;
	}

	/**
	 * Read the value name(s) from a given key
	 *
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @return the value name(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<String> readStringSubKeys(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			return readStringSubKeys(SYSTEM_ROOT, hkey, key);
		} else if(hkey == HKEY_CURRENT_USER) {
			return readStringSubKeys(USER_ROOT, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static List<String> readStringSubKeys(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		List<String> results = new ArrayList<String>();
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[]{
			new Integer(hkey), toCstr(key), new Integer(KEY_READ)
		});
		if(handles[1] != REG_SUCCESS) {
			return null;
		}
		int[] info = (int[]) regQueryInfoKey.invoke(root,
				new Object[]{new Integer(handles[0])});

		int count = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
		int maxlen = info[3]; // value length max
		for(int index = 0; index < count; index++) {
			byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[]{
				new Integer(handles[0]), new Integer(index), new Integer(maxlen + 1)
			});
			results.add(new String(name).trim());
		}
		regCloseKey.invoke(root, new Object[]{new Integer(handles[0])});
		return results;
	}

	/**
	 * Create a key
	 *
	 * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void createKey(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int[] ret;
		if(hkey == HKEY_LOCAL_MACHINE) {
			ret = createKey(SYSTEM_ROOT, hkey, key);
			regCloseKey.invoke(SYSTEM_ROOT, new Object[]{new Integer(ret[0])});
		} else if(hkey == HKEY_CURRENT_USER) {
			ret = createKey(USER_ROOT, hkey, key);
			regCloseKey.invoke(USER_ROOT, new Object[]{new Integer(ret[0])});
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
		if(ret[1] != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
		}
	}

	private static int[] createKey(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return (int[]) regCreateKeyEx.invoke(root,
				new Object[]{new Integer(hkey), toCstr(key)});
	}

	/**
	 * Write a value in a given key/value name
	 *
	 * @param hkey
	 * @param key
	 * @param valueName
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void writeStringValue(int hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			writeStringValue(SYSTEM_ROOT, hkey, key, valueName, value);
		} else if(hkey == HKEY_CURRENT_USER) {
			writeStringValue(USER_ROOT, hkey, key, valueName, value);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[]{
			new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS)});

		regSetValueEx.invoke(root,
				new Object[]{
					new Integer(handles[0]), toCstr(valueName), toCstr(value)
				});
		regCloseKey.invoke(root, new Object[]{new Integer(handles[0])});
	}

	/**
	 * Delete a given key
	 *
	 * @param hkey
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void deleteKey(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int rc = -1;
		if(hkey == HKEY_LOCAL_MACHINE) {
			rc = deleteKey(SYSTEM_ROOT, hkey, key);
		} else if(hkey == HKEY_CURRENT_USER) {
			rc = deleteKey(USER_ROOT, hkey, key);
		}
		if(rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
		}
	}

	private static int deleteKey(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int rc = ((Integer) regDeleteKey.invoke(root,
				new Object[]{new Integer(hkey), toCstr(key)})).intValue();
		return rc;  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
	}

	/**
	 * delete a value from a given key/value name
	 *
	 * @param hkey
	 * @param key
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void deleteValue(int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int rc = -1;
		if(hkey == HKEY_LOCAL_MACHINE) {
			rc = deleteValue(SYSTEM_ROOT, hkey, key, value);
		} else if(hkey == HKEY_CURRENT_USER) {
			rc = deleteValue(USER_ROOT, hkey, key, value);
		}
		if(rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
		}
	}

	// =====================
	private static int deleteValue(Preferences root, int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		int[] handles = (int[]) regOpenKey.invoke(root, new Object[]{
			new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS)});
		if(handles[1] != REG_SUCCESS) {
			return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
		}
		int rc = ((Integer) regDeleteValue.invoke(root,
				new Object[]{
					new Integer(handles[0]), toCstr(value)
				})).intValue();
		regCloseKey.invoke(root, new Object[]{new Integer(handles[0])});
		return rc;
	}

	// utility
	private static byte[] toCstr(String str) {
		byte[] result = new byte[str.length() + 1];

		for(int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}
}
