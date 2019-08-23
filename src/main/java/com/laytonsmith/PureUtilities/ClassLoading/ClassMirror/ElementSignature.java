package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An element signature is a JVM format string which represents the type of an element that interacts with generics.
 * This will be null for all types where it is just a plain type, and in
 * that case, no generics were used. However, if you have a field that is declared such as {@code List<File>} then
 * this would have the value {@code Ljava/util/List<Ljava/io/File;>;} and the type stored in type
 * would be {@code Ljava/util/List;}. The type is the only thing available in actual classes, the remaining
 * generics data is normally lost, but we retain this information to allow for reification at runtime!
 * <p>
 * It is worth pointing out that normally a new data type classification which is normally unavailable can be
 * present here. If the data type starts with the JVM name {@code T}, then it is a template reference. The base
 * type of the declaration will be java.lang.Object if the generic definition was unbounded, and whatever the
 * value in type is if it was bounded.
 * <p>
 * There are a few formats of values, depending on the complexity of the definition.
 * <table border="1">
 *  <tr><th>Signature</th><th>Original Java</th><th>Description</th></tr>
 *	<tr><td>TT;</td><td>T field</td><td>Template type declaration on a field, with no generic parameters.
 *  Note that normally, if an element is declared without generics, the signature will be null, so having
 *  a template parameter in the signature is the only way you won't have generics.</td></tr>
 *  <tr><td>()TT;</td><td>T method()</td><td>Template declaration on a method return value</td></tr>
 *  <tr>
 *    <td>(Ljava/lang/String;Ljava/util/Map&lt;[Ljava/lang/String;Ljava/lang/String;>;)Ljava/lang/String;</td>
 *    <td>String method(String, Map&lt;String[], String&gt;)</td>
 *    <td>Method declaration with String return type, and 2 parameters, including inner generics</td></tr>
 *  <tr>
 *    <td>Ljava/util/Set&lt;Ljava/lang/Class&lt;+Ljava/util/List;>;>;</td>
 *    <td>Set&lt;Class&lt;? extends List>> field</td>
 *    <td>Inner declaration that has wildcard</td></tr>
 *  <tr>
 *    <td>&lt;J:Ljava/lang/Object;>(Ljava/lang/Class&lt;TJ;>;)TJ;</td>
 *    <td>&lt;J> J method(Class&lt;J> c)</td>
 *    <td>Template type declared on method</td></tr>
 *  <tr>
 *    <td>&lt;J:Ljava/lang/String;>(Ljava/lang/Class&lt;TJ;>;)TJ;</td>
 *    <td>&lt;J extends String> J method(Class&lt;J> c)</td>
 *    <td>Template type defined on method, and upper bounded with extends</td></tr>
 *  <tr>
 *    <td>(Ljava/lang/Class&lt;*>;)Ljava/lang/Class&lt*>;</td>
 *    <td>Class&lt;*> method(Class&lt;?> c)</td>
 *    <td>Wildcard template</td></tr>
 *  <tr>
 *    <td>(Ljava/lang/Class&lt;*>;)Ljava/lang/Class&lt;-Ljava/lang/String;>;</td>
 *    <td>Class&lt;? super String> method(Class&gt;?> c)</td>
 *    <td>A lower bounded wildcard</td></tr>
 *  <tr>
 *    <td>()Lmy/obj/Name<TT;>.InnerClass</td>
 *    <td>InnerClass method()</td>
 *    <td>A method that returns an instance of a non-static inner class</td></tr>
 * </table>
 * <p>
 * The main points to notice here are the general format is {@code (params;...)returnType;} for methods, and just
 * {@code returnType;} for fields, and {@code ? extends Class} is {@code +Class}, and generic parameters are
 * included in angle brackets.
 * <p>
 * Where templates are present, it can be difficult to calculate the effective compile time value, since the value
 * can come from either the class definition, the caller site, or just be undefined entirely with naked references.
 * However, there are a few guidelines for determining the best reified type for a given template. If the signature
 * is {@code TT;} or {@code ()TT;} then this may mean that T is defined in the class signature, in which case it
 * can be found by calling {@link ClassMirror#getGenerics()}.
 */
public final class ElementSignature implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final ClassReferenceMirror[] EMPTY_CLASS_REFERENCE_MIRROR = new ClassReferenceMirror[0];

	/**
	 * Returns a new ElementSignature object with the specified signature, or null if the signature is null.
	 * @param signature
	 * @return
	 */
	public static ElementSignature GetSignature(String signature) {
		// Inner non-static classes are not currently handled, so let's just disable processing of this for now.
		// We can revist this later if we actually need this functionality, but the parsing logic needs to change.
//		if(signature == null) {
			return null;
//		}
//		return new ElementSignature(signature);
	}

	private final String signature;

	private final ClassReferenceMirror type;
	private final ClassReferenceMirror genericDeclaration;
	private final ClassReferenceMirror[] parameters;

	private ElementSignature(String signature) {
		this.signature = signature;
		// Parse the signature.

		boolean inGenericDeclaration = false;
		boolean inParameters = false;

		char start = signature.charAt(0);
		if(start == '<') {
			inGenericDeclaration = true;
			signature = signature.substring(1);
		}

		if(start == '(') {
			inParameters = true;
			signature = signature.substring(1);
		}

		StringBuilder buffer = new StringBuilder();

		@SuppressWarnings("LocalVariableHidesMemberVariable")
		ClassReferenceMirror genericDeclaration = null;
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		ClassReferenceMirror[] parameters = null;
		try {
			for(int i = 0; i < signature.length(); i++) {
				char c = signature.charAt(i);
				if(inGenericDeclaration) {
					if(c == '>') {
						String[] parts = buffer.toString().split(":");
						genericDeclaration = new ClassReferenceMirror(parts[1], parts[0]);
						buffer = new StringBuilder();
						inGenericDeclaration = false;
						// Parameters always follow generic templates, since fields can't define a template
						inParameters = true;
						i++;
						continue;
					} else {
						buffer.append(c);
					}
				}
				if(inParameters) {
					if(c == ')') {
						parameters = parseParameters(buffer.toString());
						buffer = new StringBuilder();
						inParameters = false;
						continue;
					} else {
						buffer.append(c);
					}
				}
				if(!inGenericDeclaration && !inParameters) {
					buffer.append(c);
				}
			}

			this.genericDeclaration = genericDeclaration;
			this.parameters = parameters;
			this.type = parseParameters(buffer.toString())[0];
		} catch (StackOverflowError e) {
			throw new Error("Got SOE while processing " + signature, e);
		}
	}

	private ClassReferenceMirror[] parseParameters(String params) {
		if("".equals(params)) {
			return EMPTY_CLASS_REFERENCE_MIRROR;
		}

		List<ClassReferenceMirror> ret = new ArrayList<>();
		int arrayStack = 0;
		for(int i = 0; i < params.length(); i++) {
			char c = params.charAt(i);
			if(c == 'Z') {
				// Booelan
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "Z"));
				arrayStack = 0;
			} else if(c == 'B') {
				// Byte
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "B"));
				arrayStack = 0;
			} else if(c == 'S') {
				// Short
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "S"));
				arrayStack = 0;
			} else if(c == 'I') {
				// Int
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "I"));
				arrayStack = 0;
			} else if(c == 'J') {
				// Long
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "J"));
				arrayStack = 0;
			} else if(c == 'F') {
				// Float
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "F"));
				arrayStack = 0;
			} else if(c == 'D') {
				// Double
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "D"));
				arrayStack = 0;
			} else if(c == 'C') {
				// Char
				ret.add(new ClassReferenceMirror(StringUtils.stringMultiply(arrayStack, "[") + "C"));
				arrayStack = 0;
			} else if(c == 'V') {
				// Void (can't have an array of void)
				ret.add(new ClassReferenceMirror("V"));
			} else if(c == '[') {
				arrayStack++;
			} else if(c == 'L' || c == 'T') {
				boolean isObject = c == 'L';
				StringBuilder b = new StringBuilder();
				int angleStack = 0;
				for(int j = i + 1; j < params.length(); j++) {
					i = j;
					char z = params.charAt(j);
					if(z == ';' && angleStack == 0) {
						ret.add(parseObject(c + b.toString() + z));
						break;
					} else if(z == '<') {
						angleStack++;
						b.append(z);
					} else if(z == '>') {
						angleStack--;
						b.append(z);
					} else {
						b.append(z);
					}
				}
			}
		}
		return ret.toArray(new ClassReferenceMirror[ret.size()]);
	}

	private ClassReferenceMirror parseObject(String obj) {
		if("*".equals(obj)) {
			return ClassReferenceMirror.WILDCARD;
		}
		if(!obj.contains("<")) {
			// Not a generic, just create it as is
			return new ClassReferenceMirror<>(obj);
		}
		String rootType = obj.replaceAll("(.*?)<.*>;", "$1");
		ClassReferenceMirror[] params = parseParameters(obj.replaceAll(".*?<(.*)>;", "$1"));
		return new ClassReferenceMirror(rootType + ";", params);
	}

	public boolean isMethod() {
		return signature.contains("(");
	}

	/**
	 * This is the type of the element. For methods, this will be the return type, for constructors
	 * @return
	 */
	public ClassReferenceMirror getType() {
		return new ClassReferenceMirror(signature.replaceAll("(?:<.*>)?(?:\\(.*\\))?(.*?)", "$1"));
	}

	@Override
	public String toString() {
		return (genericDeclaration == null ? "" : genericDeclaration.toString())
				+ (parameters == null ? "" : Arrays.deepToString(parameters))
				+ type.toString();
	}

}
