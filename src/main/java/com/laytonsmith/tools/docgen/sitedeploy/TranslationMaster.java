package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.FileWriteMode;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The TranslationMaster contains all translations, as well as the individual page translations.
 */
public class TranslationMaster {
	private final Map<String, LocaleTranslationMaster> allLocales = new HashMap<>();
	private final File translationDb;

	/**
	 * Creates an object that represents a full translation database, located at the given local location.
	 * @param translationDb
	 * @throws IOException
	 */
	public TranslationMaster(File translationDb) throws IOException {
		this.translationDb = translationDb;
		initialize();
	}

	private void initialize() throws IOException {
		File[] locales = translationDb.listFiles((File file) -> file.isDirectory());
		for(File locale : locales) {
			String l = locale.getName();
			initLocale(l, locale);
		}
	}

	private class LocaleTranslationMaster {
		int maxId = 0;
		String locale;
		Map<String, TranslationMemory> master;
		Map<File, PageTranslations> pages;
	}
	private class PageTranslations {
		Map<String, TranslationMemory> blocks;
	}

	private void initNewLocale(String localeName) {
		if(!allLocales.containsKey(localeName)) {
			LocaleTranslationMaster ltm;
			ltm = new LocaleTranslationMaster();
			ltm.locale = localeName;
			ltm.master = new HashMap<>();
			ltm.pages = new HashMap<>();
			allLocales.put(localeName, ltm);
		}
	}

	private void initLocale(String localeName, File locale) throws IOException {
		initNewLocale(localeName);
		LocaleTranslationMaster ltm = allLocales.get(localeName);
		// Be sure to increment nextId to the max id in the locale
		File master = new File(locale, "master.tmem.xml").getCanonicalFile();
		FileUtil.recursiveFind(locale, (File f) -> {
			if(f.isDirectory()) {
				return;
			}
			Map<String, TranslationMemory> tmem = TranslationMemory.fromTmemFile(localeName, FileUtil.read(f));
			if(f.getCanonicalFile().equals(master)) {
				ltm.master = tmem;
				for(TranslationMemory t : ltm.master.values()) {
					ltm.maxId = java.lang.Math.max(ltm.maxId, t.getId());
				}
			} else if(f.getName().endsWith(".tmem.xml")) {
				PageTranslations pt = new PageTranslations();
				pt.blocks = tmem;
				ltm.pages.put(f, pt);
			} else {
				System.out.println("Skipping non tmem file " + f.getAbsolutePath());
			}
		});
		if(ltm.master == null) {
			throw new IOException("Missing master translation file!");
		}
	}

	private File standardizeFile(File page) {
		try {
			return page.getCanonicalFile();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns true if the master translation file has a translation for the given key.
	 * @param locale
	 * @param key
	 * @return
	 */
	public boolean hasMasterTranslation(String locale, String key) {
		initNewLocale(locale);
		return allLocales.get(locale).master.containsKey(key);
	}

	/**
	 * Adds a new translation to the memory.
	 * @param locale The locale.
	 * @param page The file where the page lives.
	 * @param memory The translation memory itself.
	 */
	public void addTranslation(String locale, File page, TranslationMemory memory) {
		initNewLocale(locale);
		page = standardizeFile(page);
		LocaleTranslationMaster ltm = allLocales.get(locale);

		ltm.master.put(memory.getEnglishKey(), memory);
		PageTranslations pt;
		if(!ltm.pages.containsKey(page)) {
			pt = new PageTranslations();
			pt.blocks = new HashMap<>();
			ltm.pages.put(page, pt);
		} else {
			pt = ltm.pages.get(page);
		}

		pt.blocks.put(memory.getEnglishKey(), memory);
	}

	/**
	 * Returns the master map for this locale. The master translation map isn't used by the site itself,
	 * it is only used when generating new page translations. The master file contains the real translations
	 * however, and individual page files inherit the translations from the master file by default. The master
	 * file is where most translations should go, however, given a particular page's context, it can be overridden
	 * if mistranslated.
	 * @param locale
	 * @return
	 */
	public Map<String, TranslationMemory> getLocaleMaster(String locale) {
		return allLocales.get(locale).master;
	}

	/**
	 * Writes out the translation db. If no changes were made to the model, nothing should change on the file
	 * system.
	 * @throws IOException
	 */
	public void save() throws IOException {
		System.out.println("Would write out stuff now");
		for(LocaleTranslationMaster ltm : allLocales.values()) {
			File masterFile = new File(translationDb, ltm.locale + "/master.tmem.xml");
			FileUtil.write(TranslationMemory.generateTranslationFile(ltm.master), masterFile,
					FileWriteMode.OVERWRITE, true);
		}
	}

	/**
	 * Generates a new, unique id for the given locale. This can be used to tie page specific translation memories
	 * back to the master file.
	 * @param locale
	 * @return
	 */
	public int getNewIdForLocale(String locale) {
		return ++allLocales.get(locale).maxId;
	}

	/**
	 * For brand new segments, some default translation must be used. By default, the English version is
	 * used for the auto translation, but in the future, where possible, a machine translation may be used
	 * instead. It will never fill the manual translation field however. The remaining fields are filled out
	 * with default values, such as the id.
	 * @param locale
	 * @param englishKey
	 * @return
	 */
	public TranslationMemory generateNewTranslation(String locale, String englishKey) {
		if("art".equals(locale)) {
			// Eventually, once I get all the escapes done, this can be programmatically translated. For now,
			// it's the same as other locales.
			return new TranslationMemory(englishKey, locale, "", "", englishKey, this.getNewIdForLocale(locale));
		} else {
			return new TranslationMemory(englishKey, locale, "", "", englishKey, this.getNewIdForLocale(locale));
		}
	}

	private static final String[] SEGMENT_SEP = new String[]{"==+", Pattern.quote("|-"), "\n\n"};
	private static final Pattern SPLIT_PATTERN;
	static {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for(String s : SEGMENT_SEP) {
			if(!first) {
				b.append("|");
			}
			b.append(s);
			first = false;
		}
		SPLIT_PATTERN = Pattern.compile("(?:" + b.toString() + ")");
	}

	/**
	 * Splits an input string intos segments, which can be used to create smaller individual memories, increasing
	 * the chance of collisions, as well as reducing the chance of retranslation needed when just parts of a page
	 * change. This is a best effort attempt, and isn't perfect.
	 * @param inputString
	 * @return
	 */
	public static Set<String> findSegments(String inputString) {
		Set<String> segments = new HashSet<>();
		// First, remove all things that shouldn't be translated, code blocks, html, etc
		inputString = inputString.replaceAll("\r", "");
		inputString = inputString.replaceAll("(?s)<%CODE.*?%>", "");
		inputString = inputString.replaceAll("(?s)%%CODE.*?%%", "");
		inputString = inputString.replaceAll("\\{\\{.*?\\}\\}", "");
		// Process tables

		// TODO

		for(String s : SPLIT_PATTERN.split(inputString)) {
			if(s.matches("\\s*")) {
				continue;
			}
			segments.add(s.replace("\n", " ").trim());
		}
		return segments;
	}

}
