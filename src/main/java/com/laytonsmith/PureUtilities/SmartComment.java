package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a javadoc style comment block, at its lowest level. The rules of smart comment are that it must start with
 * /** and end with &#42;/. Within the comment, the first whitespace characters, * and optionally a single space after
 * will be removed, so the line " * Text" would be simply "Text". Annotations are supported, there are two types of
 * annotations, embedded, and normal. An embedded annotation is a data transformation construct, and a normal annotation
 * is stored separately from the "body" of the comment, and multiple of the same annotation are allowed. Embedded
 * annotations are transformed at parse time, and you provide the callback to do the transformation. For instance, the
 * embedded annotation {&#64; code myCode} can be configured to return "&lt;code&gt;myCode&lt;/code&gt;" Newlines and
 * spaces are preserved in the body of the comment, but newlines are not stored with annotation parameters.
 *
 */
public class SmartComment {

	private static final Pattern ANNOTATION = Pattern.compile("@[a-zA-Z][a-zA-Z0-9]*");
	private static final Pattern EMBEDDED_ANNOTATION = Pattern.compile("\\{@([a-zA-Z][a-zA-Z0-9]*) +(.*?)\\}");
	private static final String LINE_START = "[\\t ]*\\* ?";
	private String raw;
	private String body;
	private Map<String, List<String>> annotations = new HashMap<String, List<String>>();
	private Map<String, Replacement> rplcmnt = new HashMap<String, Replacement>();

	/**
	 * Creates a new smart comment.
	 *
	 * @param comment The comment to be parsed
	 */
	public SmartComment(String comment) {
		this(comment, null);
	}

	/**
	 * Creates a new smart comment.
	 *
	 * @param comment The comment to be parsed
	 * @param replacements This is used to replace embedded annotations with some other text. For instance, if the
	 * comment contained {
	 * @ code myCode }, (minus spaces) it may be used to return "&lt;code&gt;myCode&lt;/code&gt;". By default, if a
	 * particular embedded annotation has no handler, the embedded text is simply used as is.
	 */
	public SmartComment(String comment, Map<String, Replacement> replacements) {
		if(replacements == null) {
			replacements = new HashMap<String, Replacement>();
		}

		//Remove the @ at the beginning, if present.
		for(String key : replacements.keySet()) {
			rplcmnt.put(key.replaceFirst("@", ""), replacements.get(key));
		}

		comment = comment.trim();
		if(comment.startsWith("/**")) {
			comment = comment.substring(3);
		}
		if(comment.endsWith("*/")) {
			comment = comment.substring(0, comment.length() - 2);
		}
		String[] lines = comment.split("\n|\r\n|\n\r");
		StringBuilder b = new StringBuilder();
		for(String line : lines) {
			line = line.replaceFirst(LINE_START, "");
			b.append("\n").append(line);
		}

		raw = replaceEmbedded(b.toString().trim());

		String[] words = raw.split(" |\n");
		StringBuilder buffer = new StringBuilder();
		String lastAnnotation = null;
		int annotationIndex = -1;
		for(String word : words) {
			if(ANNOTATION.matcher(word).matches()) {
				if(annotationIndex == -1) {
					Matcher m = ANNOTATION.matcher(raw);
					m.find();
					annotationIndex = m.start();
				}
				processBuffer(lastAnnotation, buffer.toString());
				lastAnnotation = word;
				buffer = new StringBuilder();
			} else {
				buffer.append(" ").append(word);
			}
		}
		processBuffer(lastAnnotation, buffer.toString());
		if(annotationIndex == -1) {
			body = raw;
		} else {
			body = raw.substring(0, annotationIndex).trim();
		}
	}

	private String replaceEmbedded(String string) {
		//Replace embedded annotations
		Matcher embedded = EMBEDDED_ANNOTATION.matcher(string);
		while(embedded.find()) {
			String key = embedded.group(1);
			String data = embedded.group(2);
			if(rplcmnt.containsKey(key)) {
				string = string.replaceAll(Pattern.quote(embedded.group(0)), rplcmnt.get(key).replace(data));
			} else {
				string = string.replaceAll(Pattern.quote(embedded.group(0)), data);
			}
		}
		return string;
	}

	private void processBuffer(String lastAnnotation, String buffer) {
		if(lastAnnotation != null) {
			addAnnotation(lastAnnotation, buffer.trim());
		}
	}

	private void addAnnotation(String name, String value) {
		if(!annotations.containsKey(name)) {
			annotations.put(name, new ArrayList<String>());
		}
		annotations.get(name).add(value);
	}

	/**
	 * Gets the body of the comment block.
	 *
	 * @return
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Gets a list of annotation values for the comment block.
	 *
	 * @param annotation
	 * @return
	 */
	public List<String> getAnnotations(String annotation) {
		if(!annotation.startsWith("@")) {
			annotation = "@" + annotation;
		}
		return new ArrayList<String>(annotations.get(annotation));
	}

	public static interface Replacement {

		/**
		 * Given the matched data in an embedded annotation, returns the transformed data, which is replaced in the
		 * text.
		 *
		 * @param data
		 * @return
		 */
		public String replace(String data);
	}

}
