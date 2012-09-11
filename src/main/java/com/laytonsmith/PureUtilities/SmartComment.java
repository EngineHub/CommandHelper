package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a javadoc style comment block, at its lowest level.
 * @author lsmith
 */
public class SmartComment {
	
	private static final Pattern ANNOTATION = Pattern.compile("@[a-zA-Z][a-zA-Z0-9]*");
	private String raw;
	private String body;
	private Map<String, List<String>> annotations = new HashMap<String, List<String>>();
	/**
	 * Creates a new smart comment. A smart comment is defined as a comment block that starts
	 * with slash star star, and ends with star slash. Newlines and duplicate whitespace are ignored
	 * in the comment, and if a line starts with a star, it is also ignored.
	 * @param comment 
	 */
	public SmartComment(String comment){
		comment = comment.trim();
		if(comment.startsWith("/**")){
			comment = comment.substring(3);
		}
		if(comment.endsWith("*/")){
			comment = comment.substring(0, comment.length() - 2);
		}
		String [] lines = comment.split("\n|\r\n|\n\r");
		StringBuilder b = new StringBuilder();
		for(String line : lines){
			line = line.replaceAll("\\s+", " ");
			if(line.startsWith(" *")){
				line = line.substring(2);
			} else if(line.startsWith("*")){
				line = line.substring(1);
			}
			b.append(" ").append(line);
		}
		raw = b.toString().replaceAll("\\s+", " ").trim();
		String [] words = raw.split(" ");
		StringBuilder buffer = new StringBuilder();
		String lastAnnotation = null;
		for(String word : words){
			if(ANNOTATION.matcher(word).matches()){
				processBuffer(lastAnnotation, buffer.toString());
				lastAnnotation = word;
				buffer = new StringBuilder();
			} else {
				buffer.append(" ").append(word);
			}
		}
		processBuffer(lastAnnotation, buffer.toString());
	}
	
	private void processBuffer(String lastAnnotation, String buffer){
		if(lastAnnotation == null){
			body = buffer.trim();
		} else {
			addAnnotation(lastAnnotation, buffer.trim());
		}
	}
	
	private void addAnnotation(String name, String value){
		if(!annotations.containsKey(name)){
			annotations.put(name, new ArrayList<String>());
		}
		annotations.get(name).add(value);
	}
	
	/**
	 * Gets the body of the comment block.
	 * @return 
	 */
	public String getBody(){
		return body;
	}
	
	/**
	 * Gets a list of annotation values for the comment block.
	 * @param annotation
	 * @return 
	 */
	public List<String> getAnnotations(String annotation){
		if(!annotation.startsWith("@")){
			annotation = "@" + annotation;
		}
		return new ArrayList<String>(annotations.get(annotation));
	}
	
//	public static void main(String [] args){
//		SmartComment s = new SmartComment(
//				  "/**\n"
//				+ " * This is a comment block\n"
//				+ " * @param type this is the value of a param\n"
//				+ " * @empty\n"
//				+ " * @empty\n"
//				+ " * @param second param value        weee!\n"
//				+ " */\n");
//		System.out.println(s.getBody());
//		System.out.println(s.getAnnotations("empty"));
//		System.out.println(s.getAnnotations("param"));
//	}
}
