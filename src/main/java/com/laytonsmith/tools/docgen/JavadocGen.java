package com.laytonsmith.tools.docgen;

//import com.laytonsmith.PureUtilities.ClassDiscovery;
//import com.laytonsmith.PureUtilities.FileUtility;
//import com.laytonsmith.PureUtilities.StringUtils;
//import com.laytonsmith.abstraction.Implementation;
//import com.laytonsmith.annotations.api;
//import com.laytonsmith.annotations.typename;
//import com.laytonsmith.core.arguments.Argument;
//import com.laytonsmith.core.arguments.ArgumentBuilder;
//import com.laytonsmith.core.arguments.Generic;
//import com.laytonsmith.core.arguments.Signature;
//import com.laytonsmith.core.functions.Function;
//import com.laytonsmith.core.natives.interfaces.Mixed;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/**
 * This class is meant to be run only during development.
 * It takes the documentation generated from the functions, and adds it
 * as actual javadoc on the function classes themselves.
 */
public class JavadocGen {

//	public static void main(String[] args) throws Exception{
//		try{
//			//First, make sure we aren't running from a jar
//			String url = ClassDiscovery.GetClassPackageHierachy(JavadocGen.class);
//			if(url.startsWith("jar")){
//				System.out.println("Cannot run this class from a jar");
//				System.exit(1);
//			}
//			Implementation.setServerType(Implementation.Type.TEST);
//			//Ok, we aren't, so parse the url into a file object
//			//It will start with file:, and then the path to the
//			//class directory. We are assuming a maven build, so the
//			//actual java sources are two directories up, and down into src.
//			File file = new File(url.substring(5));
//			file = file.getParentFile().getParentFile();
//			file = new File(file, "src/main/java");
//			List<Function> functions = new ArrayList<Function>();
//			for(Class c : ClassDiscovery.GetClassesWithAnnotation(api.class)){
//				if(Function.class.isAssignableFrom(c)){
//					try {
//						functions.add((Function)c.newInstance());
//					} catch (Exception ex) {
//						Logger.getLogger(JavadocGen.class.getName()).log(Level.SEVERE, null, ex);
//					}
//				}
//			}
//
//			for(Function f : functions){
//				//First, find the file associated with this function
//				Class c = f.getClass();
//				while(c.getEnclosingClass() != null){
//					c = c.getEnclosingClass();
//				}
//				//Ok, have the containing class, translate that into a file
//				File classFile = new File(file, c.getName().replaceAll("\\.", "/") + ".java");
//				if(classFile.exists()){
//					if(!classFile.getName().equals("Echoes.java")){
//						continue;
//					}
//					//Found it, now build the javadoc
//					try{
//						String javadoc = buildJavadoc(f);
//						String fileContents = FileUtility.read(classFile);
//						Matcher m = Pattern.compile("(?s)(.*?)(?:/\\*\\*(?:.*?)\\*/)?(.*?public\\s+static\\s+class\\s+" 
//								+ f.getClass().getSimpleName() + ".*)").matcher(fileContents);
//						if(m.find()){
//							fileContents = m.replaceAll("$1" + javadoc + "$2");
//						}
//						System.out.println("did " + f.getName());
//						FileUtility.write(fileContents, classFile);
//						System.exit(0);
//					} catch(Throwable e){
//						throw new Exception("While handling " + f.getName() + ", an exception was thrown", e);
//					}
//				}
//			}
//		} catch(Throwable t){
//			t.printStackTrace();
//		} finally {
//			Thread.sleep(1000);
//			System.exit(0);
//		}
//	}
//	
//	/**
//	 * Hi
//	 * @param lol blah
//	 * @return what
//	 */
//	private String blah(List<String> lol){
//		return null;
//	}
//	
//	private static String buildJavadoc(Function f){
//		Set<String> keywords = new HashSet<String>();
//		keywords.addAll(Arrays.asList(new String[]{"true", "false", "null", "void"}));
//		String doc = "<p>" + f.docs() + "</p>\n";
//		doc = doc.replaceAll("----", "</p>\n\n<p>");
//		ArgumentBuilder builder = f.arguments();
//		if(builder.getOriginalSignatures().size() > 1){
//			doc += "\n<h3>Signatures</h3>\n";
//		} else {
//			doc += "\n<h4>Parameters:</h4>";
//		}
//		for(Signature s : builder.getOriginalSignatures()){
//			if(builder.getOriginalSignatures().size() > 1){
//				doc += "\n<h4>Signature " + s.getSignatureId() + " Parameters:</h4>";
//			}
//			doc += "<ul>";
//			for(Argument a : s.getArguments()){
//				doc += "<li>";
//				if(a.getName() == null){
//					throw new RuntimeException("Unexpected no named argument in " + f.getName());
//				}
//				keywords.add(a.getName());
//				String argDocs = a.docs();
//				if(!argDocs.isEmpty() && !argDocs.endsWith(".")){
//					argDocs += ".";
//				}
//				List<Class<? extends Mixed>> argumentType;
//				boolean optional = a.isOptional();
//				boolean var = a.isVarargs();
//				List<Generic> generics;
//				if(var){
//					//The type is actually the generic type, not the array
//					Generic g;
//					try{
//						g = a.getGenerics().get(0);
//					} catch(IndexOutOfBoundsException e){
//						//Assume ANY
//						g = Generic.ANY;
//					}
//					argumentType = g.getType();
//					generics = g.getSubtypes();
//				} else {
//					argumentType = a.getType();
//					generics = a.getGenerics();
//				}
//				String type;
//				{
//					type = getStringType(a.getType(), keywords);
//					type += buildGenericSubtypes(generics, keywords);
//				}
//				doc += "\n" + (optional?"[":"") + type + (var?"...":"") + " " + a.getName() + (optional?"]":"") + " - " + argDocs;
//				if(a.since() != null){
//					//Added later
//					doc += " (Added in " + a.since() + ".)";
//				}
//				doc += "</li>";
//			}
//			doc += "</ul>";
//		}
//		doc += "<p>";
//		if(f.returnType() == Argument.VOID){
//			doc += "\n<strong>Returns void</strong>";
//		} else if(f.returnType() == Argument.NONE){
//			doc += "\n<strong>No return type</strong>";
//		} else {
//			doc += "\n\n<strong>Returns " + getStringType(f.returnType().getType(), keywords) + "</strong> - ";
//			doc += f.returnType().docs();
//		}
//		doc += "</p>";
//		for(String keyword : keywords){
//			doc = doc.replaceAll("([^a-zA-Z<])" + Pattern.quote(keyword) + "([^a-zA-Z>])", "$1<code>" + keyword + "</code>$2");
//		}
//		doc = doc.replaceAll("\r\n|\n\r", "\n");
//		doc = doc.replaceAll("\n", "\n\t * ");
//		doc = "/**\n\t *" + doc + "\n\t */\n\t";
//		return doc;
//	}
//	
//	private static String buildGenericSubtypes(List<Generic> generics, Set<String> keywords){
//		if(generics.isEmpty()){
//			return "";
//		}
//		List<String> list = new ArrayList<String>();
//		for(Generic g : generics){
//			String built = getStringType(g.getType(), keywords);
//			if(!g.getSubtypes().isEmpty()){
//				built += buildGenericSubtypes(g.getSubtypes(), keywords);
//			}
//			list.add(built);
//		}
//		return "&lt;" + StringUtils.Join(list, ", ") + "&gt;";
//	}
//	
//	private static String getStringType(List<Class<? extends Mixed>> type, Set<String> keywords){
//		List<String> typenames = new ArrayList<String>();
//		for(Class<?> t : type){
//			if(t.isAnnotationPresent(typename.class)){
//				typenames.add(t.getAnnotation(typename.class).value());
//			}
//		}
//		keywords.addAll(typenames);
//		return StringUtils.Join(typenames, "|");
//	}
}
