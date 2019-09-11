package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.FileWriteMode;
import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.ProgressIterator;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.functions.FunctionList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The TranslationMaster contains all translations, as well as the individual page translations.
 */
public class TranslationMaster {

	private static final ProgressIterator NULLPI = (current, total) -> {};
	private final Map<Locale, LocaleTranslationMaster> allLocales = new HashMap<>();
	private final File translationDb;
	private final TranslationSummary translationSummary;
	private final ProgressIterator progressCallback;

	/**
	 * Creates an object that represents a full translation database, located at the given local location.
	 * @param translationDb
	 * @throws IOException
	 */
	public TranslationMaster(File translationDb) throws IOException {
		this(translationDb, NULLPI);
	}

	/**
	 * Gets the location of the translation database.
	 * @return
	 */
	public File getTranslationDb() {
		return translationDb;
	}

	/**
	 * Creates an object that represents a full translation database, located at the given local location.
	 * @param translationDb
	 * @param callback If desired, keeps a count of the loading progress. Useful for UI components. Null is supported,
	 * and disables this checking.
	 * @throws IOException
	 */
	public TranslationMaster(File translationDb, ProgressIterator callback) throws IOException {
		this.translationDb = translationDb;
		if(callback == null) {
			this.progressCallback = NULLPI;
		} else {
			this.progressCallback = callback;
		}
		translationSummary = new TranslationSummary(translationDb);
		initialize();
	}

	private void initialize() throws IOException {
		File[] locales = translationDb.listFiles((File file) -> file.isDirectory());
		int total = 0;
		for(File locale : locales) {
			if(locale.getName().equals(".git")) {
				continue;
			}
			total += prepLocale(locale);
		}

		MutableObject<Integer> current = new MutableObject<>(0);
		for(File locale : locales) {
			if(locale.getName().equals(".git")) {
				continue;
			}
			Locale l = Locale.fromLocale(locale.getName());
			initLocale(l, locale, current, total);
		}
	}

	private class LocaleTranslationMaster {
		int maxId = 0;
		Locale locale;
		Map<String, TranslationMemory> master;
		Map<File, PageTranslations> pages;
	}
	private class PageTranslations {
		File file;
		/**
		 * Set of EnglishKeys on this page
		 */
		Set<String> blocks;
		Locale locale;
	}

	private void initNewLocale(Locale locale) {
		if(!allLocales.containsKey(locale)) {
			LocaleTranslationMaster ltm;
			ltm = new LocaleTranslationMaster();
			ltm.locale = locale;
			ltm.master = new HashMap<>();
			ltm.pages = new HashMap<>();
			allLocales.put(locale, ltm);
		}
	}

	private int prepLocale(File locale) throws IOException {
		MutableObject<Integer> i = new MutableObject<>(0);
		FileUtil.recursiveFind(locale, (File f) -> {
			if(f.isDirectory()) {
				return;
			}
			i.setObject(i.getObject() + 1);
		});
		return i.getObject();
	}

	private void initLocale(Locale locale, File localeFile, MutableObject<Integer> current, int total)
			throws IOException {
		initNewLocale(locale);
		LocaleTranslationMaster ltm = allLocales.get(locale);
		// Be sure to increment nextId to the max id in the locale
		File master = new File(localeFile, "master.tmem.xml").getCanonicalFile();
		FileUtil.recursiveFind(localeFile, (File f) -> {
			if(f.isDirectory()) {
				return;
			}
			Map<String, TranslationMemory> tmem = TranslationMemory.fromTmemFile(locale, FileUtil.read(f));
			if(f.getCanonicalFile().equals(master)) {
				ltm.master = tmem;
				for(TranslationMemory t : ltm.master.values()) {
					ltm.maxId = java.lang.Math.max(ltm.maxId, t.getId());
				}
			} else if(f.getName().endsWith(".tmem.xml")) {
				PageTranslations pt = new PageTranslations();
				pt.file = f;
				pt.blocks = new HashSet<>(tmem.keySet());
				pt.locale = locale;
				ltm.pages.put(f, pt);
			} else {
				System.out.println("Skipping non tmem file " + f.getAbsolutePath());
			}
			current.setObject(current.getObject() + 1);
			progressCallback.progressChanged(current.getObject(), total);
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
	public boolean hasMasterTranslation(Locale locale, String key) {
		initNewLocale(locale);
		return allLocales.get(locale).master.containsKey(key);
	}

	public void createTranslationMemory(String toLocation, String inputString) {
		Set<String> segments = TranslationMaster.findSegments(inputString);
		for(String segment : segments) {
			int id;
			if(!translationSummary.containsTranslation(segment)) {
				id = getNewId();
				translationSummary.addTranslation(segment, id);
			} else {
				id = translationSummary.getMemory(segment).getId();
			}
			for(Locale locale : Locale.values()) {
				File location = new File(translationDb, String.format(toLocation, locale.getLocale()));
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
	private void addTranslation(Locale locale, File page, TranslationMemory memory) {
		initNewLocale(locale);
		page = standardizeFile(page);
		LocaleTranslationMaster ltm = allLocales.get(locale);

		ltm.master.put(memory.getEnglishKey(), memory);
		PageTranslations pt;
		if(!ltm.pages.containsKey(page)) {
			pt = new PageTranslations();
			pt.file = page;
			pt.locale = locale;
			pt.blocks = new HashSet<>();
			ltm.pages.put(page, pt);
		} else {
			pt = ltm.pages.get(page);
		}

		pt.blocks.add(memory.getEnglishKey());
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
	public Map<String, TranslationMemory> getLocaleMaster(Locale locale) {
		return allLocales.get(locale).master;
	}

	/**
	 * Writes out the translation db. If no changes were made to the model, nothing should change on the file
	 * system.
	 * @throws IOException
	 */
	public void save() throws IOException {
		save(NULLPI);
	}

	public void save(ProgressIterator pi) throws IOException {
		if(pi == null) {
			pi = NULLPI;
		}
		int total = 1;
		int current = 1;
		if(pi != NULLPI) {
			for(LocaleTranslationMaster ltm : allLocales.values()) {
				for(Map.Entry<File, PageTranslations> fpt : ltm.pages.entrySet()) {
					total++;
				}
			}
		}
		translationSummary.save();
		pi.progressChanged(current, total);
		for(LocaleTranslationMaster ltm : allLocales.values()) {
			File masterFile = new File(translationDb, ltm.locale.getLocale() + "/master.tmem.xml");
			FileUtil.write(TranslationMemory.generateTranslationFile(ltm.master), masterFile,
					FileWriteMode.OVERWRITE, true);

			for(Map.Entry<File, PageTranslations> fpt : ltm.pages.entrySet()) {
				File f = fpt.getKey();
				PageTranslations pt = fpt.getValue();
				Map<String, TranslationMemory> blocks = new HashMap<>();
				pt.blocks.forEach((key) -> {
					blocks.put(key, ltm.master.get(key));
				});
				String page = TranslationMemory.generateTranslationFile(blocks);
				FileUtil.write(page, f, FileWriteMode.OVERWRITE, true);
				current++;
				pi.progressChanged(current, total);
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
	 * Returns a list of supported locales, including the artificial locale.
	 * @return
	 */
	public List<Locale> getLocales() {
		return new ArrayList<>(allLocales.keySet());
	}

	/**
	 * Returns a list of all the pages.
	 * @return
	 */
	public List<String> getPages() {
		List<String> pages = new ArrayList<>();
		String toReplace = new File(translationDb.getAbsolutePath(), "art").getAbsolutePath();
		for(File f : allLocales.get(Locale.ART).pages.keySet()) {
			pages.add(f.getAbsolutePath().replaceFirst(Pattern.quote(toReplace), ""));
		}
		Collections.sort(pages);
		return pages;
	}

	/**
	 * Returns a list of all TranslationMemories that are on the given page.
	 * @param locale
	 * @param page
	 * @return
	 */
	public List<TranslationMemory> getMemoriesForPage(Locale locale, String page) {
		Map<String, TranslationMemory> master = allLocales.get(locale).master;
		Set<String> keys = allLocales.get(locale)
				.pages.get(new File(translationDb, locale.getLocale() + "/" + page))
				.blocks;
		return new ArrayList<>(keys.stream().map((key) -> master.get(key)).collect(Collectors.toList()));
	}

	/**
	 * Returns the full list of memories for the given locale
	 * @param locale
	 * @return
	 */
	public List<TranslationMemory> getMemoriesForLocale(Locale locale) {
		return new ArrayList<>(allLocales.get(locale).master.values());
	}

	/**
	 * Returns just the summary translation entries for all memories.
	 * @return
	 */
	public List<TranslationSummary.TranslationSummaryEntry> getMasterMemories() {
		return translationSummary.getAllMemories();
	}

	/**
	 * Returns the translation summary for the given key.
	 * @param key
	 * @return
	 */
	public TranslationSummary.TranslationSummaryEntry getSummaryForKey(String key) {
		return translationSummary.getMemory(key);
	}

	/**
	 * Creates a new segment with default values.
	 * @param locale
	 * @param englishKey
	 * @param id
	 * @return
	 */
	public TranslationMemory generateNewTranslation(Locale locale, String englishKey, int id) {
		return new TranslationMemory(englishKey, locale, "", "", "", id);
	}

	private static final String[] SEGMENT_SEP = new String[]{
		"==+",
		"\n\n",
		"\n\\*",
		"\n#"
	};
	private static final Pattern SPLIT_PATTERN;

	/**
	 * Function names frequently appear throughout the code as
	 */
	private static final Set<String> FUNCTION_IDENTIFIERS
			= FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA, null)
			.stream()
			.map((function) -> {
				return "\\[\\[%s\\|" + function.getName() + "\\]\\]\\(?\\)?";
			})
			.collect(Collectors.toSet());

	private static final Set<String> FUNCTION_NAMES
			= FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA, null)
			.stream()
			.map((f) -> f.getName())
			.collect(Collectors.toSet());

	private static final Set<String> CLASS_NAMES = NativeTypeList.getNativeTypeList()
			.stream()
			.map((f) -> f.getFQCN())
			.collect(Collectors.toSet());

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
	 * segments, and being their own segment. These are literals, not regex.
	 */
	private static final String[] UNIQUE_SEGMENTS = new String[] {
		"%s([[%s|Examples...]])",
		"%sFind a bug in this page? %sEdit this page yourself, then submit a pull request.%s%s",
		"%sFind a bug in this page? %sEdit this page yourself, then submit a pull request.%s"
			+ " (Note this page is automatically generated from the documentation in the source code.)%s",
		"The output would be:",
		"The output might be:",
	};

	/**
	 * Some segments, after all the processing is done, result in useless segments, that shouldn't be translated.
	 * These are essentially a blacklist of segments. Comparison is done as is, if the final segment equals this,
	 * it is filtered out.
	 */
	private static final Set<String> USELESS_SEGMENTS = new HashSet<>(Arrays.asList(
		",", "%%", "<%", "%>"
	));

	/**
	 * Most templates are purely functional, and so should always be removed. Only ones that are specifically
	 * intended to be displayed as strings should be translated, and those can be whitelisted here.
	 */
	private static final Set<String> NO_REMOVE_TEMPLATES = new HashSet<>(Arrays.asList(
			"NOTE"
	));

	/**
	 * These are segments that are in the frame, and therefore in all pages. They are outside of the normal
	 * parsing system, so need to be added manually.
	 */
	private static final Set<String> FRAME_SEGMENTS = new HashSet<>(Arrays.asList(
			"Home", "Docs", "Help", "%s Team. All rights reserved.", "About", "Privacy Policy", "Sponsors"
	));


	/**
	 * Splits an input string into segments, which can be used to create smaller individual memories, increasing
	 * the chance of collisions, as well as reducing the chance of retranslation needed when just parts of a page
	 * change. This is a best effort attempt, and isn't perfect.
	 * @param inputString
	 * @return
	 */
	public static Set<String> findSegments(String inputString) {
		Set<String> segments = new HashSet<>();
		// First, remove all things that shouldn't be translated, code blocks, html, etc
		inputString = inputString.replaceAll("\r", "");
		inputString = inputString.replaceAll("\\\\\n", "");
		inputString = inputString.replaceAll("(?s)<script.*?</script>", "");
		{
			// Some templates are immune to this, because it breaks the segments otherwise. So we need to first %s a few
			// special templates.
			inputString = inputString.replaceAll("%%CURRENT_VERSION%%", "%s");
			inputString = inputString.replaceAll("<%CURRENT_VERSION%>", "%s");
			inputString = inputString.replaceAll("(?s)%%.*?%%", "");
			// Template removal. We can't use regex here, because <% %> templates can be nested. Eventually, we want
			// to use the whitelist, but for now, just remove all templates.
			char c1;
			char c2;
			int count = 0;
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < inputString.length(); i++) {
				c1 = inputString.charAt(i);
				c2 = '\0';
				if(i + 1 < inputString.length()) {
					c2 = inputString.charAt(i + 1);
				}
				if(c1 == '<' && c2 == '%') {
					count++;
					i++;
					continue;
				}
				if(c1 == '%' && c2 == '>') {
					count--;
					i++;
					continue;
				}
				if(count > 0) {
					continue;
				}
				b.append(c1);
			}
			inputString = b.toString();
		}
		inputString = inputString.replaceAll("(?s)<pre.*?</pre>", "");
		inputString = inputString.replaceAll("\\{\\{.*?\\}\\}", "%s");
		inputString = inputString.replaceAll("\\[\\[.*?\\|(.*?)\\]\\]", "[[%s|$1]]");
		inputString = inputString.replaceAll("\\[\\[File:.*?\\]\\]", "");
		inputString = inputString.replaceAll("\\[\\[Image:.*?\\]\\]", "");
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

		for(String functionNames : FUNCTION_IDENTIFIERS) {
			inputString = inputString.replaceAll(functionNames, "%s");
		}

		// Process tables
		Matcher m = TABLE_PATTERN.matcher(inputString);
		while(m.find()) {
			String table = m.group(1);
			table = table.replaceAll("\\{\\|.*\n", "");
			table = table.replaceAll("\\|\\}", "");
			table = table.replaceAll("\\|\\-\\s*\n", "");
			table = table.replaceAll("!.*\n", "");
			for(String cell : Arrays.asList(table.split("\\|\\||(?:(?:^|\n)\\|)"))) {
				segments.addAll(Arrays.asList(SPLIT_PATTERN.split(cell)));
			}
		}
		inputString = inputString.replaceAll(TABLE_PATTERN_STRING, "");

		segments.addAll(Arrays.asList(SPLIT_PATTERN.split(inputString)));
		segments.addAll(FRAME_SEGMENTS);

		return segments.stream()
			.filter(string -> string != null)
			.map(string -> {
				string = string.trim();
				string = string.replace("\n", " ");
				for(String className : CLASS_NAMES) {
					// These are fully qualified, so they are certainly not meant to be translated.
					string = string.replace(className, "%s");
				}
				// Removing beginning and ending %s in the string has no impact on whether or not a string
				// matches, but does increase the performance of the regex, and simplifies the segment for
				// translators. We also want to remove symbols and whitespace where it's not necessary.
				string = string.replaceAll("^(?:%s|\\s|[,#*])*(.*?)(?:%s|\\s)*$", "$1");
				// Collapse multiple %s into one
				string = string.replaceAll("%s(?:%s)+", "%s");
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
			// Strings that are just symbols (or %s) in their entirety can be removed.
			.filter((string) -> !string.matches("^(?:[^a-zA-Z]|%s)+$"))
			// Segments that are entirely just a function name are removed.
			.filter((string) -> !FUNCTION_NAMES.contains(string))
			.filter((string) -> !USELESS_SEGMENTS.contains(string))
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
//			for(PageTranslations pt : ltm.pages.values()) {
//				for(TranslationMemory tm : pt.blocks.values()) {
//					for(TranslationMemory tm2 : pt.blocks.values()) {
//						if(tm == tm2) {
//							continue;
//						}
//						if(tm.getId() == tm2.getId()) {
//							errors.add("Two entries in the " + pt.file + " file for the " + ltm.locale + " locale"
//									+ " have the same id: " + tm.getId());
//						}
//						if(tm.getEnglishKey().equals(tm2.getEnglishKey())) {
//							errors.add("Two entries in the " + pt.file + " file for the " + ltm.locale + " locale"
//									+ " have the same key: "
//									+ ltm.locale + "-" + tm.getId() + " and "
//									+ ltm.locale + "-" + tm2.getId());
//						}
//					}
//
//					int summaryId = this.translationSummary.getTranslationId(tm.getEnglishKey());
//					if(summaryId != tm.getId()) {
//						errors.add("The id defined in " + pt.file + " for the " + ltm.locale + " locale for "
//								+ tm.getId() + " does not exist in the summary file!");
//					}
//
//					int masterId = allLocales.get(pt.locale).master.get(tm.getEnglishKey()).getId();
//					if(masterId != tm.getId()) {
//						errors.add("The id defined in " + pt.file + " for the " + ltm.locale + " locale for "
//								+ tm.getId() + " does not exist in the master.tmem.xml file!");
//					}
//				}
//			}
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
