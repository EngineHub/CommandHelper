package com.laytonsmith.tools.docgen.localization;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.SAXDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.SAXException;

/**
 * A TranslationMemory is an language segment, that keys the english translation to the given translation. It optionally
 * includes a comment which may assist the translator in determining the context
 * of the translation. There is also an automatic translation field. If a manual translation is missing, then
 * the automatic translation will be used by default, but may be overwritten manually. When a new translation memory
 * appears, it will inherit the translation (if any) from the master translation file, and then may or may not
 * attempt a machine translated version. If both the automatic and manual translation are missing, the original
 * is kept.
 *
 * The serialization uses xml format. This was a difficult decision, because json seems more appropriate, and would
 * be generally easier to deal with in the code, however, this increases complexity of the translators themselves, as
 * things such as newlines, and quotes need escaping. With xml, a CDATA block can be added, and then very little
 * needs to actually be escaped within the blocks.
 */
public class TranslationMemory implements Comparable<TranslationMemory> {

	private static final String BEGIN_BLOCK = "<translations>\n<comment></comment>\n";
	private static final String END_BLOCK = "</translations>\n";

	private final String englishKey;
	private final String locale;
	private final String comment;
	private final String translation;
	private final String automaticTranslation;
	private final int translationId;
	private final Boolean isMachineTranslatable;
	private final boolean overrideMaster;

	public TranslationMemory(TranslationSummary summary, String englishKey, String locale, String comment,
			String translation,
			String automaticTranslation, int id, boolean overrideMaster) {
		this.englishKey = englishKey;
		this.locale = locale;
		this.comment = comment;
		this.translation = translation;
		this.automaticTranslation = automaticTranslation;
		this.translationId = id;
		this.overrideMaster = overrideMaster;
		isMachineTranslatable = summary.isMachineTranslatable(englishKey);
	}

	public String getEnglishKey() {
		return englishKey;
	}

	public String getLocale() {
		return locale;
	}

	public String getComment() {
		return comment;
	}

	public int getId() {
		return translationId;
	}

	public boolean isOverrideMaster() {
		return overrideMaster;
	}

	@Override
	public String toString() {
		return "(" + locale + ") " + englishKey + " ------> " + translation;
	}

	public String toTmemString() {
		StringBuilder b = new StringBuilder();
		b.append("<translationBlock>\n");
		b.append("\t<id>").append(locale).append("-").append(translationId).append("</id>\n");
		b.append("\t<key>").append(escape(englishKey)).append("</key>\n");
		b.append("\t<comment>").append(escape(comment)).append("</comment>\n");
		b.append("\t<translation>").append(escape(translation)).append("</translation>\n");
		b.append("\t<auto>").append(escape(automaticTranslation)).append("</auto>\n");
		b.append("\t<overrideMaster>").append(overrideMaster).append("</overrideMaster>\n");
		b.append("</translationBlock>\n");
		return b.toString();
	}

	private String escape(String input) {
		if(input == null || "".equals(input)) {
			return "";
		}
		input = input.replace("]]>", "]]]]><![CDATA[>");
		return "<![CDATA[" + input + "]]>";
	}

	public static String generateTranslationFile(Map<String, TranslationMemory> memories) {
		StringBuilder b = new StringBuilder();
		List<TranslationMemory> values = new ArrayList<>(memories.values());
		Collections.sort(values);
		for(TranslationMemory tm : values) {
			b.append(tm.toTmemString());
		}
		return BEGIN_BLOCK + b.toString() + END_BLOCK;
	}

	public static Map<String, TranslationMemory> fromTmemFile(TranslationSummary translationSummary,
			String locale, String fileContents) throws IOException {
		Map<String, TranslationMemory> memories = new HashMap<>();
		if("".equals(fileContents)) {
			return memories;
		}
		SAXDocument sax = new SAXDocument(fileContents, "UTF-8");
		final MutableObject<String> id = new MutableObject<>();
		final MutableObject<String> key = new MutableObject<>();
		final MutableObject<String> comment = new MutableObject<>();
		final MutableObject<String> translation = new MutableObject<>();
		final MutableObject<String> automaticTranslation = new MutableObject<>();
		final MutableObject<Boolean> overrideMaster = new MutableObject<>();

		sax.addListener("/translations/translationBlock/id", (xpath, tag, attr, contents) -> {
			id.setObject(contents);
		});

		sax.addListener("/translations/translationBlock/key", (xpath, tag, attr, contents) -> {
			key.setObject(contents);
		});

		sax.addListener("/translations/translationBlock/comment", (xpath, tag, attr, contents) -> {
			comment.setObject(contents);
		});

		sax.addListener("/translations/translationBlock/translation", (xpath, tag, attr, contents) -> {
			translation.setObject(contents);
		});

		sax.addListener("/translations/translationBlock/auto", (xpath, tag, attr, contents) -> {
			automaticTranslation.setObject(contents);
		});

		sax.addListener("/translations/translationBlock/overrideMaster", (xpath, tag, attr, contents) -> {
			overrideMaster.setObject(Boolean.valueOf(contents));
		});

		sax.addListener("/translations/translationBlock", (xpath, tag, attr, contents) -> {
			int intId = Integer.parseInt(id.getObject().replaceAll(locale + "-(.*)", "$1"));
			TranslationMemory tm = new TranslationMemory(
					translationSummary,
					key.getObject(),
					locale,
					comment.getObject(),
					translation.getObject(),
					automaticTranslation.getObject(),
					intId,
					overrideMaster.getObject());
			memories.put(key.getObject(), tm);
			id.setObject(null);
			key.setObject(null);
			comment.setObject(null);
			translation.setObject(null);
			automaticTranslation.setObject(null);
			overrideMaster.setObject(null);
		});

		try {
			sax.parse();
		} catch (SAXException ex) {
			throw new IOException(ex);
		}
		return memories;
	}

	@Override
	public int compareTo(TranslationMemory t) {
		return new Integer(this.translationId).compareTo(t.translationId);
	}
}
