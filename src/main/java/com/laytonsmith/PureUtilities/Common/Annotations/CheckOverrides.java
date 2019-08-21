package com.laytonsmith.PureUtilities.Common.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.MustUseOverride;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 *
 */
@SupportedAnnotationTypes({"java.lang.Override", "com.laytonsmith.annotations.MustUseOverride"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CheckOverrides extends AbstractProcessor {

	@Target({ElementType.METHOD, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SuppressCheckOverrides {}

	private static final boolean ENABLED = true;

	private static Map<Class, Set<Method>> methods = null;
	private static final Set<Class> INTERFACES_WITH_MUST_USE_OVERRIDE = new HashSet<>();
	private static final Pattern METHOD_SIGNATURE = Pattern.compile("[a-zA-Z0-9_]+\\((.*)\\)");
	private static final Pattern CLASS_TEMPLATES = Pattern.compile("^.*?<(.*)>?$");

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(!ENABLED) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "CheckOverrides processor is turned off!");
			return false;
		}
		setup();
		if(!roundEnv.processingOver()) {
			for(Element element : roundEnv.getElementsAnnotatedWith(MustUseOverride.class)) {
				String className = element.toString();
				Class c = null;
				try {
					c = getClassFromName(className);
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace(System.err);
				}
				if(c != null) {
					if(!c.isInterface()) {
						String msg = "Only interfaces may be annotated with " + MustUseOverride.class.getName();
						System.err.println(msg);
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
					}
					INTERFACES_WITH_MUST_USE_OVERRIDE.add(c);
				}
			}
			for(Element element : roundEnv.getElementsAnnotatedWith(Override.class)) {
				String className = element.getEnclosingElement().toString();
				Class c = null;
				try {
					c = getClassFromName(className);
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace(System.err);
				}

				if(c != null && !c.isInterface()) {
					//StreamUtils.GetSystemOut().println("Dealing with " + c.getName() + ".." + element.toString());
					//We have to do a bit of massaging to turn "method(java.lang.String[], java.lang.String)
					//into a Method object.
					Matcher m = METHOD_SIGNATURE.matcher(element.toString());
					String methodName = element.getSimpleName().toString();
					Class[] argTypes;
					boolean isTemplate = false;
					if(!m.find()) {
						argTypes = new Class[0];
					} else {
						String inner = m.group(1);
						String[] args;
						if("".equals(inner.trim())) {
							args = ArrayUtils.EMPTY_STRING_ARRAY;
						} else {
							//Take out generics, since we can't really deal with them, and they make parsing
							//the args harder.
							inner = removeGenerics(inner);
							args = StringUtils.trimSplit(inner, ",");
						}
						//StreamUtils.GetSystemOut().println("Args length: " + args.length);
						argTypes = new Class[args.length];
						for(int i = 0; i < args.length; i++) {
							try {
								argTypes[i] = getClassFromName(args[i]);
							} catch (ClassNotFoundException e) {
								//It may be a template parameter, so check in the enclosing class name for that
								//template type.
								String codeClassName = element.getEnclosingElement().asType().toString();
								Matcher mm = CLASS_TEMPLATES.matcher(codeClassName);
								boolean found = false;
								if(mm.find()) {
									String[] templates = removeGenerics(mm.group(1)).split(",");
									String baseClass = args[i].replaceAll("\\[\\]", "");
									for(String template : templates) {
										if(baseClass.equals(template)) {
											//Ok, it's found.
											isTemplate = true;
											found = true;
											args[i] = args[i].replaceFirst(Pattern.quote(template), "java.lang.Object");
											break;
										}
									}
								}
								if(!isTemplate || !found) {
									//Oh, there aren't any. Well, I don't know why this would happen.
									e.printStackTrace(System.err);
								}
								try {
									argTypes[i] = Class.forName(args[i]);
								} catch (ClassNotFoundException ex) {
									//Won't happen
								}
							}
						}
					}
					if(isTemplate) {
						//Template parameters that extend something break this, because the annotation
						//processor doesn't provide the information to us. So, for instance, if you have
						//a template parameter MyClass<T extends List> and a method in that class
						//void myMethod(T); then the signature of that method is actually
						//void myMethod(List), but since we don't have a way of getting "List"
						//from the APT, we can't really do anything to detect if you've overridden
						//void myMethod(T) vs void myMethod(Object). So we have to settle here for
						//missing an error, instead of erroring out when there isn't actually one,
						//and remove all the methods with this name and type. We can, however,
						//avoid removing methods with different number of arguments, since we
						//can guarantee those aren't overridden.
						for(Method method : c.getDeclaredMethods()) {
							if(method.getName().equals(methodName)
									&& method.getParameterTypes().length == argTypes.length) {
								methods.get(c).remove(method);
							}
						}
					} else {
						//Arg types are now all provided, and so is the method name.
						try {
							Method method = c.getDeclaredMethod(methodName, argTypes);
							//Ok, remove it from the list of methods, cause we know it's overridden.
							//.remove won't work, because we need to also remove co-return types present
							Iterator<Method> it = methods.get(c).iterator();
							while(it.hasNext()) {
								Method next = it.next();
								if(next.getName().equals(method.getName())
										&& checkSignatureForCompatibility(next.getParameterTypes(), method.getParameterTypes())) {
									it.remove();
								}
							}
						} catch (NoSuchMethodException | SecurityException ex) {
							ex.printStackTrace(System.err);
						}
					}
					if(methods.get(c).isEmpty()) {
						methods.remove(c);
					}
				}
			}
			//Now all the overridden methods have been removed from the list of methods in all the
			//classes. We now need to go through and find out which of the remaining methods *could*
			//be overriden, as many may not be overrides anyways.
			Set<Method> methodsInError = new HashSet<>();
			for(Class c : methods.keySet()) {
				Set<Method> mm = methods.get(c);
				for(Method m : mm) {
					//Get the superclass/superinterfaces that this class extends/implements
					//all the way up to Object
					Set<Class> supers = new HashSet<>();
					getAllSupers(c, supers, true);
					//Ok, now look through all the superclasses' methods, and find any that
					//match the signature. If they do, it's an error.
					List<Method> compare = new ArrayList<>();
					for(Class s : supers) {
						compare.addAll(getOverridableMethods(s));
					}
					methodLoop: for(Method superM : compare) {
						if(m.getName().equals(superM.getName())) {
							if(checkSignatureForCompatibility(superM.getParameterTypes(), m.getParameterTypes())) {
								//Oops, found a bad method.
								if(m.isAnnotationPresent(SuppressCheckOverrides.class)) {
									continue;
								}
								Class<?> container = m.getDeclaringClass();
								do {
									if(container.isAnnotationPresent(SuppressCheckOverrides.class)) {
										continue methodLoop;
									}
									container = container.getEnclosingClass();
								} while(container != null);
								methodsInError.add(m);
							}
						} //else different method altogether
					}
				}
			}
			if(!methodsInError.isEmpty()) {
				//Some package names are pretty verbose, and will more than
				//likely be included with an import, so let's trim the
				//error message down some so it's easier to read
				final List<String> verbosePackages = Arrays.asList(new String[]{
					"java.lang",
					"java.util",
					"java.io"
				});
				//Build a sorted set, so these go in order.
				SortedSet<String> stringMethodsInError = new TreeSet<>();
				for(Method m : methodsInError) {
					stringMethodsInError.add(m.getDeclaringClass().getName() + "."
							+ m.getName() + "(" + StringUtils.Join(Arrays.asList(m.getParameterTypes()), ", ", ", ", ", ", "", new StringUtils.Renderer<Class<?>>() {
						@Override
						public String toString(Class<?> item) {
							String name = ClassUtils.getCommonName(item);
							for(String v : verbosePackages) {
								if(name.matches(Pattern.quote(v) + "\\.([^\\.]*?)$")) {
									return name.replaceFirst(Pattern.quote(v) + "\\.", "");
								}
							}
							return name;
						}
					}) + ")");
				}
				final StringBuilder b = new StringBuilder();
				b.append("There ")
						.append(StringUtils.PluralTemplateHelper(stringMethodsInError.size(),
								"is a method which overrides or implements a method in a super class/super interface,"
								+ " but doesn't use the @Override tag. Please tag this method",
								"are %d methods which override or implement a method in a super class/super interface"
								+ " but don't use the @Override tag. Please tag these methods"))
						.append(" with @Override to continue the build process.")
						.append(StringUtils.NL)
						.append(StringUtils.Join(stringMethodsInError, StringUtils.NL));
				System.err.println(b.toString());
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, b.toString());
			} else {
				StreamUtils.GetSystemOut().println("No @Override annotations were found to be missing.");
			}
		}
		return false;
	}

	private static void getAllSupers(Class c, Set<Class> building, boolean first) {
		if(c == null || c == Object.class) {
			return;
		}
		if(!first) {
			building.add(c);
		} else {
			//everything extends Object, and we're gonna use
			//that as our stop marker later, so go ahead and
			//add this now.
			building.add(Object.class);
		}
		getAllSupers(c.getSuperclass(), building, false);
		for(Class cc : c.getInterfaces()) {
			if(INTERFACES_WITH_MUST_USE_OVERRIDE.contains(cc)) {
				building.add(cc);
			}
		}
	}

	/**
	 * Checks to see if an argument signature is compatible. That is, if the parameter types match.
	 *
	 * @param superArgs The super class arguments to check
	 * @param subArgs The sub class arguments to check
	 * @return
	 */
	private static boolean checkSignatureForCompatibility(Class[] superArgs, Class[] subArgs) {
		if(superArgs.length != subArgs.length) {
			return false;
		}
		for(int i = 0; i < superArgs.length; i++) {
			if(superArgs[i] != subArgs[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes generics from an identifier.
	 *
	 * @param identifier
	 * @return
	 */
	private static String removeGenerics(String identifier) {
		StringBuilder b = new StringBuilder();
		int genericCount = 0;
		for(int i = 0; i < identifier.length(); i++) {
			char ch = identifier.charAt(i);
			if(ch == '<') {
				genericCount++;
				continue;
			}
			if(ch == '>') {
				genericCount--;
				continue;
			}
			if(genericCount == 0) {
				b.append(ch);
			}
		}
		return b.toString();
	}

	private static Class getClassFromName(String className) throws ClassNotFoundException {
		return ClassUtils.forCanonicalName(className, false, CheckOverrides.class.getClassLoader());
	}

	private static void setup() {
		if(methods == null) {
			methods = new HashMap<>();

			List<ClassMirror<?>> classes = ClassDiscovery.getDefaultInstance().getKnownClasses(ClassDiscovery.GetClassContainer(CheckOverrides.class));
			for(ClassMirror cm : classes) {
				Class c = cm.loadClass(CheckOverrides.class.getClassLoader(), false);
				if(c.isInterface()) {
					continue;
				}
				Set<Method> mm = getPotentiallyOverridingMethods(c);
				if(!mm.isEmpty()) {
					methods.put(c, mm);
				}
			}
		}
	}

	/**
	 * Returns a list of potentially overriding methods in a class. That is, the non-private, non-static methods.
	 *
	 * @param c
	 * @return
	 */
	private static Set<Method> getPotentiallyOverridingMethods(Class c) {
		Set<Method> methodList = new HashSet<>();
		for(Method m : c.getDeclaredMethods()) {
			//Ignore static or public methods, since those can't override anything
			if((m.getModifiers() & Modifier.PRIVATE) == 0 && (m.getModifiers() & Modifier.STATIC) == 0
					&& !m.isSynthetic()) {
				methodList.add(m);
			}
		}
		return methodList;
	}

	/**
	 * Returns a list of overridable methods in a class. This includes non-static, non-private, non-final methods.
	 *
	 * @param c
	 * @return
	 */
	private static List<Method> getOverridableMethods(Class c) {
		List<Method> methodList = new ArrayList<>();
		for(Method m : c.getDeclaredMethods()) {
			if((m.getModifiers() & Modifier.PRIVATE) == 0
					&& (m.getModifiers() & Modifier.STATIC) == 0
					&& (m.getModifiers() & Modifier.FINAL) == 0
					&& !m.isSynthetic()) {
				methodList.add(m);
			}
		}
		return methodList;
	}
}
