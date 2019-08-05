package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.FileWriteMode;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The TranslationMaster contains all translations, as well as the individual page translations.
 */
public class TranslationMaster {

	/**
	 * Supported values are the language codes found under the translation section of this page:
	 * https://api.cognitive.microsofttranslator.com/languages?api-version=3.0 along with the
	 * special code "art" which is a programmatically generated test language, that is always
	 * available.
	 */
	private static final String[] SUPPORTED_LOCALES = {
		"art", // as in artificial, this is the official language code for made up languages
		"ko",
	};

	private final Map<String, LocaleTranslationMaster> allLocales = new HashMap<>();
	private final File translationDb;
	private final TranslationSummary translationSummary;

	/**
	 * Creates an object that represents a full translation database, located at the given local location.
	 * @param translationDb
	 * @throws IOException
	 */
	public TranslationMaster(File translationDb) throws IOException {
		this.translationDb = translationDb;
		translationSummary = new TranslationSummary(translationDb);
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
		File file;
		Map<String, TranslationMemory> blocks;
		String locale;

		/**
		 * Given the master translation file, rewrites the local page blocks with the data in the master file, if they
		 * aren't marked with override master.
		 * @param master
		 */
		private void rewriteFromMaster(Map<String, TranslationMemory> master) {
			for(TranslationMemory tm : blocks.values()) {
				if(!tm.isOverrideMaster()) {
					if(master.containsKey(tm.getEnglishKey())) {
						TranslationMemory masterTm = master.get(tm.getEnglishKey());
						blocks.put(tm.getEnglishKey(), masterTm);
					}
				}
			}
		}
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
			Map<String, TranslationMemory> tmem = TranslationMemory.fromTmemFile(translationSummary, localeName,
					FileUtil.read(f));
			if(f.getCanonicalFile().equals(master)) {
				ltm.master = tmem;
				for(TranslationMemory t : ltm.master.values()) {
					ltm.maxId = java.lang.Math.max(ltm.maxId, t.getId());
				}
			} else if(f.getName().endsWith(".tmem.xml")) {
				PageTranslations pt = new PageTranslations();
				pt.file = f;
				pt.blocks = tmem;
				pt.locale = localeName;
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

	public void createTranslationMemory(String toLocation, String inputString) {
		Set<String> segments = TranslationMaster.findSegments(inputString);
		for(String segment : segments) {
			int id = -1;
			if(!translationSummary.containsTranslation(segment)) {
				id = getNewId();
				translationSummary.addTranslation(segment, id);
			}
			for(String locale : SUPPORTED_LOCALES) {
				File location = new File(translationDb, String.format(toLocation, locale));
				TranslationMemory tm;
				if(this.hasMasterTranslation(locale, segment)) {
					tm = this.getLocaleMaster(locale).get(segment);
				} else {
					tm = this.generateNewTranslation(locale, segment, id);
				}
				this.addTranslation(locale, location, tm);
			}
		}
	}

	/**
	 * Adds a new translation to the memory.
	 * @param locale The locale.
	 * @param page The file where the page lives.
	 * @param memory The translation memory itself.
	 */
	private void addTranslation(String locale, File page, TranslationMemory memory) {
		initNewLocale(locale);
		page = standardizeFile(page);
		LocaleTranslationMaster ltm = allLocales.get(locale);

		ltm.master.put(memory.getEnglishKey(), memory);
		PageTranslations pt;
		if(!ltm.pages.containsKey(page)) {
			pt = new PageTranslations();
			pt.file = page;
			pt.locale = locale;
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
		translationSummary.save();
		for(LocaleTranslationMaster ltm : allLocales.values()) {
			File masterFile = new File(translationDb, ltm.locale + "/master.tmem.xml");
			FileUtil.write(TranslationMemory.generateTranslationFile(ltm.master), masterFile,
					FileWriteMode.OVERWRITE, true);

			for(Map.Entry<File, PageTranslations> fpt : ltm.pages.entrySet()) {
				File f = fpt.getKey();
				PageTranslations pt = fpt.getValue();
				pt.rewriteFromMaster(ltm.master);
				String page = TranslationMemory.generateTranslationFile(pt.blocks);
				FileUtil.write(page, f, FileWriteMode.OVERWRITE, true);
			}
		}
	}

	/**
	 * Generates a new, unique id for the given locale. This can be used to tie page specific translation memories
	 * back to the master file.
	 * @return
	 */
	public int getNewId() {
		return translationSummary.getNextId();
	}

	/**
	 * For brand new segments, some default translation must be used. By default, the English version is
	 * used for the auto translation, but in the future, where possible, a machine translation may be used
	 * instead. It will never fill the manual translation field however. The remaining fields are filled out
	 * with default values, such as the id.
	 * @param locale
	 * @param englishKey
	 * @param id
	 * @return
	 */
	public TranslationMemory generateNewTranslation(String locale, String englishKey, int id) {
		if("art".equals(locale)) {
			// Eventually, once I get all the escapes done, this can be programmatically translated. For now,
			// it's the same as other locales.
			return new TranslationMemory(translationSummary, englishKey, locale, "", "", "", id, false);
		} else {
			return new TranslationMemory(translationSummary, englishKey, locale, "", "", "", id, false);
		}
	}

	private static final String[] SEGMENT_SEP = new String[]{
		"==+",
		"\n\n",
		"^\\*",
	};
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

	private static final String URL_PATTERN
			= "(?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static final String TABLE_PATTERN_STRING = "(?s)(\\{\\|.*?\n\\|\\})";
	private static final Pattern TABLE_PATTERN = Pattern.compile(TABLE_PATTERN_STRING);

	/**
	 * Unique segments are values that are very often repeated, and deserve being removed from other
	 * segments, and being their own segment.
	 */
	private static final String[] UNIQUE_SEGMENTS = new String[] {
		"%s([[%s|Examples...]])",
		"%sFind a bug in this page? %sEdit this page yourself, then submit a pull request.%s%s",
		"%sFind a bug in this page? %sEdit this page yourself, then submit a pull request.%s"
			+ " (Note this page is automatically generated from the documentation in the source code.)%s",
	};


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
		inputString = inputString.replaceAll("(?s)<script.*?</script>", "");
		inputString = inputString.replaceAll("(?s)<%CODE.*?%>", "");
		inputString = inputString.replaceAll("(?s)%%CODE.*?%%", "");
		inputString = inputString.replaceAll("(?s)<%ALIAS.*?%>", "");
		inputString = inputString.replaceAll("(?s)%%ALIAS.*?%%", "");
		inputString = inputString.replaceAll("\\{\\{.*?\\}\\}", "%s");
		inputString = inputString.replaceAll("\\[\\[.*?\\|(.*?)\\]\\]", "[[%s|$1]]");
		inputString = inputString.replaceAll("\\[\\[File:.*?\\]\\]", "");
		inputString = inputString.replaceAll("\\[" + URL_PATTERN + "( .*?)\\]", "[%s$1]");
		inputString = inputString.replaceAll(URL_PATTERN, "%s");
		inputString = inputString.replaceAll("(?s)<.*?>", "%s");

		for(String uniqueSegment : UNIQUE_SEGMENTS) {
			if(inputString.contains(uniqueSegment)) {
				// We have to add it here so it ends up in the page file
				segments.add(uniqueSegment);
				inputString = inputString.replace(uniqueSegment, "");
			}
		}

		// Process tables
		Matcher m = TABLE_PATTERN.matcher(inputString);
		while(m.find()) {
			String table = m.group(1);
			table = table.replaceAll("\\{\\|.*\n", "");
			table = table.replaceAll("\\|\\}", "");
			table = table.replaceAll("\\|\\-\\s*\n", "");
			table = table.replaceAll("!.*\n", "");
			segments.addAll(Arrays.asList(table.split("\\|\\||(?:(?:^|\n)\\|)")));
		}
		inputString = inputString.replaceAll(TABLE_PATTERN_STRING, "");

		segments.addAll(Arrays.asList(SPLIT_PATTERN.split(inputString)));

		return segments.stream()
			.filter(string -> string != null)
			.map(string -> {
				string = string.trim();
				string = string.replace("\n", " ");
				return string;
			})
			.filter((string) -> {
				if(string.isEmpty()) {
					return false;
				} else if(string.matches("(?:\\s*%s\\s*)+")) {
					return false;
				} else if(string.matches("\\s*")) {
					return false;
				}
				return true;
			})
			.collect(Collectors.toSet());
	}

	/**
	 * Validates the database, returning a set of errors. If the set is empty, then this means there are no errors.
	 * @return
	 */
	public Set<String> validate() {
		Set<String> errors = new HashSet<>();
		errors.addAll(translationSummary.validate());
		for(LocaleTranslationMaster ltm : allLocales.values()) {
			for(TranslationMemory tm : ltm.master.values()) {
				for(TranslationMemory tm2 : ltm.master.values()) {
					if(tm == tm2) {
						continue;
					}
					if(tm.getId() == tm2.getId()) {
						errors.add("Two entries in the master.tmem.xml file for the " + ltm.locale + " locale"
								+ " have the same id: " + tm.getId());
					}
					if(tm.getEnglishKey().equals(tm2.getEnglishKey())) {
						errors.add("Two entries in the master.tmem.xml file for the " + ltm.locale
								+ " locale have the same key: "
								+ ltm.locale + "-" + tm.getId() + " and "
								+ ltm.locale + "-" + tm2.getId());
					}
				}
				int summaryId = this.translationSummary.getTranslationId(tm.getEnglishKey());
				if(summaryId != tm.getId()) {
					errors.add("The id defined in master.tmem.xml for the " + ltm.locale + " locale for "
							+ tm.getId() + " does not exist in the summary file!");
				}
			}
			for(PageTranslations pt : ltm.pages.values()) {
				for(TranslationMemory tm : pt.blocks.values()) {
					for(TranslationMemory tm2 : pt.blocks.values()) {
						if(tm == tm2) {
							continue;
						}
						if(tm.getId() == tm2.getId()) {
							errors.add("Two entries in the " + pt.file + " file for the " + ltm.locale + " locale"
									+ " have the same id: " + tm.getId());
						}
						if(tm.getEnglishKey().equals(tm2.getEnglishKey())) {
							errors.add("Two entries in the " + pt.file + " file for the " + ltm.locale + " locale"
									+ " have the same key: "
									+ ltm.locale + "-" + tm.getId() + " and "
									+ ltm.locale + "-" + tm2.getId());
						}
					}

					int summaryId = this.translationSummary.getTranslationId(tm.getEnglishKey());
					if(summaryId != tm.getId()) {
						errors.add("The id defined in " + pt.file + " for the " + ltm.locale + " locale for "
								+ tm.getId() + " does not exist in the summary file!");
					}

					int masterId = allLocales.get(pt.locale).master.get(tm.getEnglishKey()).getId();
					if(masterId != tm.getId()) {
						errors.add("The id defined in " + pt.file + " for the " + ltm.locale + " locale for "
								+ tm.getId() + " does not exist in the master.tmem.xml file!");
					}
				}
			}
		}
		return errors;
	}

	/**
	 * Returns the number of unique segments in the summary.
	 * @return
	 */
	public int size() {
		return translationSummary.size();
	}

}
