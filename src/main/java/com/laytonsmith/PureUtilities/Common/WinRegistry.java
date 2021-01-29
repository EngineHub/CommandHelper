/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.laytonsmith.PureUtilities.Common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.laytonsmith.PureUtilities.JavaVersion;

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

	private static final boolean USE_LONG_HANDLES = JavaVersion.GetMajorVersion() >= 11;

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
			Class<?> handleClass = (USE_LONG_HANDLES ? long.class : int.class);
			regOpenKey = USER_CLASS.getDeclaredMethod(
					"WindowsRegOpenKey", new Class[] {handleClass, byte[].class, int.class});
			regCloseKey = USER_CLASS.getDeclaredMethod(
					"WindowsRegCloseKey", new Class[] {handleClass});
			regQueryValueEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegQueryValueEx", new Class[] {handleClass, byte[].class});
			regEnumValue = USER_CLASS.getDeclaredMethod(
					"WindowsRegEnumValue", new Class[] {handleClass, int.class, int.class});
			regQueryInfoKey = USER_CLASS.getDeclaredMethod(
					"WindowsRegQueryInfoKey1", new Class[] {handleClass});
			regEnumKeyEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegEnumKeyEx", new Class[] {handleClass, int.class, int.class});
			regCreateKeyEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegCreateKeyEx", new Class[] {handleClass, byte[].class});
			regSetValueEx = USER_CLASS.getDeclaredMethod(
					"WindowsRegSetValueEx", new Class[] {handleClass, byte[].class, byte[].class});
			regDeleteValue = USER_CLASS.getDeclaredMethod(
					"WindowsRegDeleteValue", new Class[] {handleClass, byte[].class});
			regDeleteKey = USER_CLASS.getDeclaredMethod(
					"WindowsRegDeleteKey", new Class[] {handleClass, byte[].class});
			regOpenKey.setAccessible(true);
			regCloseKey.setAccessible(true);
			regQueryValueEx.setAccessible(true);
			regEnumValue.setAccessible(true);
			regQueryInfoKey.setAccessible(true);
			regEnumKeyEx.setAccessible(true);
			regCreateKeyEx.setAccessible(true);
			regSetValueEx.setAccessible(true);
			regDeleteValue.setAccessible(true);
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
	public static String readString(long hkey, String key, String valueName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			return readString(SYSTEM_ROOT, hkey, key, valueName);
		} else if(hkey == HKEY_CURRENT_USER) {
			return readString(USER_ROOT, hkey, key, valueName);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static String readString(Preferences root, long hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		byte[] valb;
		if(USE_LONG_HANDLES) {
			long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {hkey, toCstr(key), KEY_READ});
			if(handles[1] != REG_SUCCESS) {
				return null;
			}
			valb = (byte[]) regQueryValueEx.invoke(root, new Object[] {handles[0], toCstr(value)});
			regCloseKey.invoke(root, new Object[] {handles[0]});
		} else {
			int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {(int) hkey, toCstr(key), KEY_READ});
			if(handles[1] != REG_SUCCESS) {
				return null;
			}
			valb = (byte[]) regQueryValueEx.invoke(root, new Object[] {handles[0], toCstr(value)});
			regCloseKey.invoke(root, new Object[] {handles[0]});
		}
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
	public static Map<String, String> readStringValues(long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			return readStringValues(SYSTEM_ROOT, hkey, key);
		} else if(hkey == HKEY_CURRENT_USER) {
			return readStringValues(USER_ROOT, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static Map<String, String> readStringValues(Preferences root, long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		HashMap<String, String> results = new HashMap<String, String>();
		if(USE_LONG_HANDLES) {
			long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {hkey, toCstr(key), KEY_READ});
			if(handles[1] != REG_SUCCESS) {
				return null;
			}
			long[] info = (long[]) regQueryInfoKey.invoke(root, new Object[] {handles[0]});

			int count = (int) info[0]; // count
			int maxlen = (int) info[3]; // value length max
			for(int index = 0; index < count; index++) {
				byte[] name = (byte[]) regEnumValue.invoke(root, new Object[] {handles[0], index, (int) (maxlen + 1)});
				String value = readString(hkey, key, new String(name));
				results.put(new String(name).trim(), value);
			}
			regCloseKey.invoke(root, new Object[] {handles[0]});
		} else {
			int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {(int) hkey, toCstr(key), KEY_READ});
			if(handles[1] != REG_SUCCESS) {
				return null;
			}
			int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] {handles[0]});

			int count = info[0]; // count
			int maxlen = info[3]; // value length max
			for(int index = 0; index < count; index++) {
				byte[] name = (byte[]) regEnumValue.invoke(root, new Object[] {handles[0], index, maxlen + 1});
				String value = readString(hkey, key, new String(name));
				results.put(new String(name).trim(), value);
			}
			regCloseKey.invoke(root, new Object[] {handles[0]});
		}
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
	public static List<String> readStringSubKeys(long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			return readStringSubKeys(SYSTEM_ROOT, hkey, key);
		} else if(hkey == HKEY_CURRENT_USER) {
			return readStringSubKeys(USER_ROOT, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static List<String> readStringSubKeys(Preferences root, long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		List<String> results = new ArrayList<String>();
		if(USE_LONG_HANDLES) {
			long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {hkey, toCstr(key), KEY_READ});
			if(handles[1] != REG_SUCCESS) {
				return null;
			}
			long[] info = (long[]) regQueryInfoKey.invoke(root, new Object[] {handles[0]});

			int count = (int) info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
			int maxlen = (int) info[3]; // value length max
			for(int index = 0; index < count; index++) {
				byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[] {handles[0], index, maxlen + 1});
				results.add(new String(name).trim());
			}
			regCloseKey.invoke(root, new Object[] {handles[0]});
		} else {
			int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {(int) hkey, toCstr(key), KEY_READ});
			if(handles[1] != REG_SUCCESS) {
				return null;
			}
			int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] {handles[0]});

			int count = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
			int maxlen = info[3]; // value length max
			for(int index = 0; index < count; index++) {
				byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[] {handles[0], index, maxlen + 1});
				results.add(new String(name).trim());
			}
			regCloseKey.invoke(root, new Object[] {handles[0]});
		}
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
	public static void createKey(long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		long rc;
		Preferences prefs;
		if(hkey == HKEY_LOCAL_MACHINE) {
			prefs = SYSTEM_ROOT;
		} else if(hkey == HKEY_CURRENT_USER) {
			prefs = USER_ROOT;
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
		if(USE_LONG_HANDLES) {
			long[] ret = (long[]) createKey(prefs, hkey, key);
			regCloseKey.invoke(prefs, new Object[] {ret[0]});
			rc = ret[1];
		} else {
			int[] ret = (int[]) createKey(prefs, hkey, key);
			regCloseKey.invoke(prefs, new Object[] {ret[0]});
			rc = ret[1];
		}
		if(rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
		}
	}

	private static Object createKey(Preferences root, long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(USE_LONG_HANDLES) {
			return (long[]) regCreateKeyEx.invoke(root, new Object[] {hkey, toCstr(key)});
		} else {
			return (int[]) regCreateKeyEx.invoke(root, new Object[] {(int) hkey, toCstr(key)});
		}
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
	public static void writeStringValue(long hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(hkey == HKEY_LOCAL_MACHINE) {
			writeStringValue(SYSTEM_ROOT, hkey, key, valueName, value);
		} else if(hkey == HKEY_CURRENT_USER) {
			writeStringValue(USER_ROOT, hkey, key, valueName, value);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	private static void writeStringValue(Preferences root, long hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if(USE_LONG_HANDLES) {
			long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {hkey, toCstr(key), KEY_ALL_ACCESS});
			regSetValueEx.invoke(root, new Object[] {handles[0], toCstr(valueName), toCstr(value)});
			regCloseKey.invoke(root, new Object[] {handles[0]});
		} else {
			int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {(int) hkey, toCstr(key), KEY_ALL_ACCESS});
			regSetValueEx.invoke(root, new Object[] {handles[0], toCstr(valueName), toCstr(value)});
			regCloseKey.invoke(root, new Object[] {handles[0]});
		}
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
	public static void deleteKey(long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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

	private static int deleteKey(Preferences root, long hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// Can return: REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS.
		return (int) regDeleteKey.invoke(root, new Object[] {(USE_LONG_HANDLES ? hkey : (int) hkey), toCstr(key)});
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
	public static void deleteValue(long hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
	private static int deleteValue(Preferences root, long hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc;
		if(USE_LONG_HANDLES) {
			long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {hkey, toCstr(key), KEY_ALL_ACCESS});
			if(handles[1] != REG_SUCCESS) {
				return (int) handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
			}
			rc = ((Integer) regDeleteValue.invoke(root, new Object[] {handles[0], toCstr(value)})).intValue();
			regCloseKey.invoke(root, new Object[] {handles[0]});
		} else {
			int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {(int) hkey, toCstr(key), KEY_ALL_ACCESS});
			if(handles[1] != REG_SUCCESS) {
				return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
			}
			rc = ((Integer) regDeleteValue.invoke(root, new Object[] {handles[0], toCstr(value)})).intValue();
			regCloseKey.invoke(root, new Object[] {handles[0]});
		}
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
