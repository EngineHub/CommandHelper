package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class KeywordList {

	private static Map<String, Keyword> keywordList;

	static {
		refreshKeywordList();
	}

	/**
	 * Refreshes the internal keyword list cache
	 */
	public static void refreshKeywordList() {
		Set<Class<? extends Keyword>> keywords = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(Keyword.keyword.class, Keyword.class);
		keywordList = new HashMap<>();
		for(Class<? extends Keyword> k : keywords) {
			if(k == Keyword.class) {
				// Skip this one
				continue;
			}
			try {
				Keyword kk = k.newInstance();
				keywordList.put(kk.getKeywordName(), kk);
			} catch(InstantiationException | IllegalAccessException ex) {
				Logger.getLogger(KeywordList.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Returns a set of known keywords
	 *
	 * @return
	 */
	public static Set<Keyword> getKeywordList() {
		return new HashSet<>(keywordList.values());
	}

	/**
	 * Returns a set of keyword names
	 *
	 * @return
	 */
	public static Set<String> getKeywordNames() {
		return new HashSet<>(keywordList.keySet());
	}

	/**
	 * Returns a keyword object given the keyword name, or null, if it doesn't exist.
	 *
	 * @param name
	 * @return
	 */
	public static Keyword getKeywordByName(String name) {
		return keywordList.get(name);
	}
}
