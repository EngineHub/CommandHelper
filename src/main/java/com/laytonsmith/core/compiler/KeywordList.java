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
	private static Map<String, EarlyBindingKeyword> earlyKeywordList;
	private static Map<String, LateBindingKeyword> lateKeywordList;

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
			} catch (InstantiationException | IllegalAccessException ex) {
				Logger.getLogger(KeywordList.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Set<Class<? extends EarlyBindingKeyword>> earlyKeywords = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(Keyword.keyword.class, EarlyBindingKeyword.class);
		earlyKeywordList = new HashMap<>();
		for(Class<? extends EarlyBindingKeyword> k : earlyKeywords) {
			if(k == EarlyBindingKeyword.class) {
				// Skip this one
				continue;
			}
			try {
				EarlyBindingKeyword kk = k.newInstance();
				earlyKeywordList.put(kk.getKeywordName(), kk);
			} catch (InstantiationException | IllegalAccessException ex) {
				Logger.getLogger(KeywordList.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Set<Class<? extends LateBindingKeyword>> lateKeywords = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(Keyword.keyword.class, LateBindingKeyword.class);
		lateKeywordList = new HashMap<>();
		for(Class<? extends LateBindingKeyword> k : lateKeywords) {
			if(k == LateBindingKeyword.class) {
				// Skip this one
				continue;
			}
			try {
				LateBindingKeyword kk = k.newInstance();
				lateKeywordList.put(kk.getKeywordName(), kk);
			} catch (InstantiationException | IllegalAccessException ex) {
				Logger.getLogger(KeywordList.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Returns a set of all keyword names, including early and late binding ones.
	 *
	 * @return
	 */
	public static Set<String> getKeywordNames() {
		Set<String> ret = new HashSet<>(keywordList.keySet());
		ret.addAll(earlyKeywordList.keySet());
		ret.addAll(lateKeywordList.keySet());
		return ret;
	}

	/**
	 * Returns a set of known keywords (including normal, early, and late binding) that can be used for documentation
	 * purposes.
	 *
	 * @return
	 */
	public static Set<KeywordDocumentation> getKeywordList() {
		Set<KeywordDocumentation> ret = new HashSet<>(keywordList.values());
		ret.addAll(earlyKeywordList.values());
		ret.addAll(lateKeywordList.values());
		return ret;
	}

	/**
	 * Returns a normal binding keyword object given the keyword name, or null, if it doesn't exist.
	 *
	 * @param name
	 * @return
	 */
	public static Keyword getKeywordByName(String name) {
		return keywordList.get(name);
	}

	/**
	 * Returns a set of known early binding keywords
	 *
	 * @return
	 */
	public static Set<EarlyBindingKeyword> getEarlyBindingKeywordList() {
		return new HashSet<>(earlyKeywordList.values());
	}

	/**
	 * Returns a set of known late binding keywords
	 *
	 * @return
	 */
	public static Set<LateBindingKeyword> getLateBindingKeywordList() {
		return new HashSet<>(lateKeywordList.values());
	}

	/**
	 * Returns a early binding keyword object given the keyword name, or null, if it doesn't exist.
	 *
	 * @param name
	 * @return
	 */
	public static EarlyBindingKeyword getEarlyBindingKeywordByName(String name) {
		return earlyKeywordList.get(name);
	}

	/**
	 * Returns a late binding keyword object given the keyword name, or null, if it doesn't exist.
	 *
	 * @param name
	 * @return
	 */
	public static LateBindingKeyword getLateBindingKeywordByName(String name) {
		return lateKeywordList.get(name);
	}
}
