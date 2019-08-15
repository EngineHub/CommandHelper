/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.FileWriteMode;
import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.SAXDocument;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xml.sax.SAXException;

/**
 * A translation summary is a file that contains information about a translation that applies to all locales.
 */
public class TranslationSummary {

	public class TranslationSummaryEntry implements Comparable<TranslationSummaryEntry> {
		private final String englishKey;
		private final int id;
		private Boolean eligibleForMachineTranslation = null;
		private boolean untranslatable = false;
		private boolean suspectSegment = false;
		private String comment;

		public TranslationSummaryEntry(String englishKey, int id) {
			this.englishKey = englishKey;
			this.id = id;
		}

		@Override
		public int compareTo(TranslationSummaryEntry t) {
			return new Integer(this.id).compareTo(t.id);
		}

		public String getEnglishKey() {
			return englishKey;
		}

		public int getId() {
			return id;
		}

		public Boolean getEligibleForMachineTranslation() {
			return eligibleForMachineTranslation;
		}

		public String getComment() {
			return comment;
		}

		public boolean isUntranslatable() {
			return untranslatable;
		}

		public boolean isSuspectSegment() {
			return suspectSegment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public void setEligibleForMachineTranslation(Boolean eligibleForMachineTranslation) {
			this.eligibleForMachineTranslation = eligibleForMachineTranslation;
		}

		public void setSuspectSegment(boolean suspectSegment) {
			this.suspectSegment = suspectSegment;
		}

		public void setUntranslatable(boolean untranslatable) {
			this.untranslatable = untranslatable;
		}
	}

	private final Map<String, TranslationSummaryEntry> entries = new HashMap<>();
	private final File translationDb;
	private int nextId = 0;

	public TranslationSummary(File translationDb) throws IOException {
		this.translationDb = translationDb;
		initialize();
	}

	private String toSummaryString() {
		StringBuilder b = new StringBuilder();
		b.append("<summary>");
		List<TranslationSummaryEntry> values = new ArrayList<>(entries.values());
		Collections.sort(values);
		for(TranslationSummaryEntry tse : values) {
			b.append("<translationEntry>\n");
			b.append("\t<id>").append(tse.id).append("</id>\n");
			b.append("\t<key>").append(escape(tse.englishKey)).append("</key>\n");
			b.append("\t<eligibleForMachineTranslation>").append(tse.eligibleForMachineTranslation)
					.append("</eligibleForMachineTranslation>\n");
			b.append("\t<comment>").append(escape(tse.comment)).append("</comment>\n");
			b.append("\t<untranslatable>").append(tse.untranslatable).append("</untranslatable>\n");
			b.append("\t<suspectSegment>").append(tse.suspectSegment).append("</suspectSegment>\n");
			b.append("</translationEntry>\n");
		}
		b.append("</summary>\n");
		return b.toString();
	}

	private String escape(String input) {
		if(input == null || "".equals(input)) {
			return "";
		}
		input = input.replace("]]>", "]]]]><![CDATA[>");
		return "<![CDATA[" + input + "]]>";
	}

	public int getNextId() {
		return ++nextId;
	}

	/**
	 * Checks if the summary file contains this translation already.
	 * @param key
	 * @return
	 */
	public boolean containsTranslation(String key) {
		return entries.containsKey(key);
	}

	/**
	 * Returns the translation id
	 * @param key
	 * @return
	 */
	public int getTranslationId(String key) {
		return entries.get(key).id;
	}

	/**
	 * Returns a list of all the summary translation memories.
	 * @return
	 */
	public List<TranslationSummaryEntry> getAllMemories() {
		return new ArrayList<>(entries.values());
	}

	/**
	 * Returns a translation memory summary based on the string key
	 * @param key
	 * @return
	 */
	public TranslationSummaryEntry getMemory(String key) {
		return entries.get(key);
	}

	/**
	 * Adds a new translation summary to the summary database.
	 * @param key
	 * @param id
	 */
	public void addTranslation(String key, int id) {
		if(containsTranslation(key)) {
			throw new Error("The summary already contains an entry with key! " + key);
		}
		TranslationSummaryEntry v = new TranslationSummaryEntry(key, id);
		entries.put(key, v);
	}

	/**
	 * Writes out the summary xml.
	 * @throws IOException
	 */
	public void save() throws IOException {
		FileUtil.write(toSummaryString(), new File(translationDb, "summary.xml"), FileWriteMode.OVERWRITE, true);
	}

	/**
	 * Returns whether or not the given translation is eligible for machine translation. False and null should have
	 * the same effect, that is, it should not be machine translated.
	 * @param key
	 * @return
	 */
	public Boolean isMachineTranslatable(String key) {
		return entries.get(key).eligibleForMachineTranslation;
	}

	private void initialize() throws IOException {
		File f = new File(translationDb, "summary.xml");
		if(!f.exists()) {
			FileUtil.write("<summary></summary>", f);
		}
		SAXDocument sd = new SAXDocument(new FileInputStream(f));

		MutableObject<Integer> id = new MutableObject<>();
		MutableObject<String> key = new MutableObject<>();
		MutableObject<Boolean> eligibleForMachineTranslation = new MutableObject<>();
		MutableObject<String> comment = new MutableObject<>();
		MutableObject<Boolean> untranslatable = new MutableObject<>();
		MutableObject<Boolean> suspectSegment = new MutableObject<>(false);

		sd.addListener("/summary/translationEntry/id",
				(String xpath, String tag, Map<String, String> attr, String contents) -> {
			id.setObject(Integer.parseInt(contents));
		});

		sd.addListener("/summary/translationEntry/key",
				(String xpath, String tag, Map<String, String> attr, String contents) -> {
			key.setObject(contents);
		});

		sd.addListener("/summary/translationEntry/eligibleForMachineTranslation",
				(String xpath, String tag, Map<String, String> attr, String contents) -> {
			Boolean b;
			if("true".equals(contents)) {
				b = true;
			} else if("false".equals(contents)) {
				b = false;
			} else {
				b = null;
			}
			eligibleForMachineTranslation.setObject(b);
		});

		sd.addListener("/summary/translationEntry/comment",
				(String xpath, String tag, Map<String, String> attr, String contents) -> {
			comment.setObject(contents);
		});

		sd.addListener("/summary/translationEntry/untranslatable",
				(xpath, tag, attr, contents) -> {
			untranslatable.setObject(Boolean.valueOf(contents));
		});

		sd.addListener("/summary/translationEntry/suspectSegment", (xpath, tag, attr, contents) -> {
			suspectSegment.setObject(Boolean.valueOf(contents));
		});

		sd.addListener("/summary/translationEntry",
				(String xpath, String tag, Map<String, String> attr, String contents) -> {
			TranslationSummaryEntry tse = new TranslationSummaryEntry(key.getObject(), id.getObject());
			tse.eligibleForMachineTranslation = eligibleForMachineTranslation.getObject();
			tse.comment = comment.getObject();
			tse.untranslatable = untranslatable.getObject();
			tse.suspectSegment = suspectSegment.getObject();
			TranslationSummary.this.entries.put(key.getObject(), tse);

			nextId = java.lang.Math.max(nextId, tse.id);

			id.setObject(null);
			key.setObject(null);
			eligibleForMachineTranslation.setObject(null);
			comment.setObject(null);
			untranslatable.setObject(false);
			suspectSegment.setObject(false);
		});

		try {
			sd.parse();
		} catch (SAXException ex) {
			throw new IOException(ex);
		}

	}

	public Set<String> validate() {
		Set<String> errors = new HashSet<>();
		for(TranslationSummaryEntry tse : entries.values()) {
			// Ensure unique ids
			for(TranslationSummaryEntry tse2 : entries.values()) {
				if(tse == tse2) {
					continue;
				}
				if(tse.id == tse2.id) {
					errors.add("Two entries in the summary.xml file have the same id: " + tse.id);
				}
				if(tse.englishKey.equals(tse2.englishKey)) {
					errors.add("Two entries in the summary.xml file have the same key: " + tse.id + " and " + tse2.id);
				}
			}
		}
		return errors;
	}

	/**
	 * Returns the number of translation segments in total.
	 * @return
	 */
	public int size() {
		return entries.size();
	}
}
