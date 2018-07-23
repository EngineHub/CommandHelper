package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * In programming, a Visitor pattern (also called Double Dispatch) is used to provide an easy and type safe way of
 * selecting the method to handle an object of a specific type, without resorting to usage of {@code instanceof} chains
 * or a {@code switch} statement. Consider the following example:
 *
 * <pre><code>
 * interface UserID {}
 * class PhoneNumber implements UserID {}
 * class GeneratedIDV1 implements UserID {}
 * class GeneratedIDV2 implements UserID {}
 *
 * void mainAmerican(UserID o) {
 *   if(o instanceof PhoneNumber) handlePhoneNumberAmerican();
 *   if(o instanceof GeneratedIDV1() handleGIDV1American();
 *   if(o instanceof GeneratedIDV2) handleGIDV2American();
 * }
 * void mainCanadian(UserID o) {
 *   if(o instanceof PhoneNumber) handlePhoneNumberCanadian();
 *   if(o instanceof GeneratedIDV1() handleGIDV1Canadian();
 *   if(o instanceof GeneratedIDV2) handleGIDV2Canadian();
 * }
 * </code></pre>
 *
 * This code can be better written with a Vistor pattern
 *
 * <pre><code>
 *
 *   public static interface UserIDVisitable {
 *	void accept(UserIDVisitor v);
 *    }
 *    public static interface UserIDVisitor {
 *	void handle(PhoneNumber m);
 *	void handle(GeneratedIDV1 c);
 *	void handle(GeneratedIDV2 c);
 *   }
 *   public static interface UserID extends UserIDVisitable {}
 *
 *   public static class PhoneNumber implements UserID {
 *	{@code @Override}
 *	public void accept(UserIDVisitor visitor) {
 *		visitor.handle(this);
 *	}
 *    }
 *   public static abstract class GeneratedID implements UserID {}
 *   public static class GeneratedIDV1 extends GeneratedID {
 *	{@code @Override}
 *	public void accept(UserIDVisitor visitor) {
 *		visitor.handle(this);
 *	}
 *   }
 *   public static class GeneratedIDV2 extends GeneratedID {
 *	{@code @Override}
 *	public void accept(UserIDVisitor visitor) {
 *		visitor.handle(this);
 *	}
 *   }
 *
 *
 *   public static class CanadianVisitor implements UserIDVisitor {
 *
 *	{@code @Override}
 *	public void handle(PhoneNumber m) {
 *		System.out.println("Canadian PhoneNumber");
 *	}
 *
 *	{@code @Override}
 *	public void handle(GeneratedIDV1 c) {
 *		System.out.println("Canadian GeneratedIDV1");
 *	}
 *
 *	{@code @Override}
 *	public void handle(GeneratedIDV2 c) {
 *		System.out.println("Canadian GeneratedIDV2");
 *	}
 *
 *  }
 *
 *   public static class AmericanVisitor implements UserIDVisitor {
 *
 *	{@code @Override}
 *	public void handle(PhoneNumber m) {
 *		System.out.println("American PhoneNumber");
 *	}
 *
 *	{@code @Override}
 *	public void handle(GeneratedIDV1 c) {
 *		System.out.println("American GeneratedIDV1");
 *	}
 *
 *	{@code @Override}
 *	public void handle(GeneratedIDV2 c) {
 *		System.out.println("American GeneratedIDV2");
 *	}
 *
 *    }
 *
 *    void mainAmerican(UserID o) {
 *       o.accept(new AmericanVisitor());
 *    }
 *
 *    void mainCanadian(UserID o) {
 *	 o.accept(new CanadianVisitor());
 *    }
 * </code></pre>
 *
 * Alas, this introduces a new problem. Now we have to go back to classes A, B, and C and make them extend Visitable.
 * This might be ok, but in cases where we don't want to mix concerns, or perhaps in code we do not control, possible.
 * Furthermore, it creates duplicated code, because each class will simply call visitor.handle(this) in all cases, and
 * generally substantially increases our code size. Also, if we create a new subtype for which different handling is
 * required, we must manually remember to update the UserIDVisitor interface with the new signature.
 *
 * {@link ExhaustiveVisitor} solves all three of these problems. Instead of the above code, we can now use the
 * following:
 *
 * <pre><code>
 *   public static interface UserID {}
 *
 *   public static class PhoneNumber implements UserID {}
 *   public static abstract class GeneratedID implements UserID {}
 *   public static class GeneratedIDV1 extends GeneratedID {}
 *   public static class GeneratedIDV2 extends GeneratedID {}
 *
 *   {@code @ExhaustiveVisitor.VisitorInfo(baseClass = UserID.class, directSubclassOnly = false)}
 *    public static class CanadianVisitor extends ExhaustiveVisitor<UserID> {
 *	public void visit(PhoneNumber n) {
 *		System.out.println("Canadian PhoneNumber");
 *	}
 *
 *	public void visit(GeneratedIDV1 id) {
 *		System.out.println("Canadian GeneratedIDV1");
 *	}
 *
 *	public void visit(GeneratedIDV2 id) {
 *		System.out.println("Canadian GeneratedIDV1");
 *	}
 *   }
 *
 *   {@code @ExhaustiveVisitor.VisitorInfo(baseClass = UserID.class, directSubclassOnly = false)}
 *   public static class AmericanVisitor extends ExhaustiveVisitor<UserID> {
 *	public void visit(PhoneNumber n) {
 *		System.out.println("American PhoneNumber");
 *	}
 *
 *	public void visit(GeneratedIDV1 id) {
 *		System.out.println("American GeneratedIDV1");
 *	}
 *
 *	public void visit(GeneratedIDV2 id) {
 *		System.out.println("American GeneratedIDV1");
 *	}
 *   }
 *
 *   void mainAmerican(UserID o) {
 *      new AmericanVisitor().visit(o);
 *   }
 *
 *   void mainCanadian(UserID o) {
 *	new CanadianVisitor().visit(o);
 *   }
 * </code></pre>
 *
 * This code has multiple advantages: No need to change the base classes, less code overall, no code duplication, and no
 * extra visitor interfaces need be created and maintained. With the addition of the compiler changes, this also becomes
 * a compile error if you forget a required implementation of visit().
 *
 * The VisitorInfo annotation is optional, but if provided, will control whether or not only direct subclasses must be
 * implemented, or if all known subclasses must be.
 *
 *
 * @author cailin
 */
public class ExhaustiveVisitor<T> {

	/**
	 * The name of the visit method
	 */
	private static final String VISIT = "visit";

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface VisitorInfo {

		/**
		 * If true, then the visitor subclass is only required to implement methods that represent direct subclasses of
		 * the base class. If an object needs visiting, it simply uses the method for which it extends from. If this is
		 * false, then all possible subclass values must be implemented, even if they simply call other visit methods.
		 * The default is false.
		 *
		 * @return
		 */
		boolean directSubclassOnly() default false;
	}

	/**
	 * Calls the appropriate subclassed method based on the runtime type of the parameter passed in.
	 *
	 * @param object
	 */
	public final void visit(T object) {
		VisitorInfo info = this.getClass().getDeclaredAnnotation(VisitorInfo.class);
		Method candidate = null;
		Class<?> searchFor = object.getClass();
		for(Method m : this.getClass().getMethods()) {
			if(VISIT.equals(m.getName())) {
				Class<?> visitParam = m.getParameterTypes()[0];
				if(info != null && info.directSubclassOnly()) {
					if(visitParam.isAssignableFrom(searchFor)) {
						candidate = m;
						break;
					}
				} else {
					if(visitParam == searchFor) {
						candidate = m;
						break;
					}
				}
			}
		}
		if(candidate == null) {
			throw new NoSuchMethodError("Missing implementation of method with signature (or superclass of): "
					+ " public void visit(" + searchFor.getName().replace("$", ".") + ") in class "
					+ this.getClass().getName());
		}
		try {
			candidate.invoke(this, object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void verify(ClassMirror<? extends ExhaustiveVisitor> classMirror) throws ClassNotFoundException {
		Class<? extends ExhaustiveVisitor> clazz = classMirror.loadClass();
		System.out.println("Verifying " + clazz);
		VisitorInfo info = clazz.getAnnotation(VisitorInfo.class);
		Class<?> baseClass = classMirror.getGenerics()
				.get(new ClassMirror<>(ExhaustiveVisitor.class).getClassReference()).get(0).loadClass();
		List<String> uhohs = new ArrayList<>();
		// Make sure all public visit methods have only one parameter (which extends T) and return void
		Set<Class<?>> handledClasses = new HashSet<>();
		for(Method m : clazz.getMethods()) {
			if(m.getDeclaringClass() == ExhaustiveVisitor.class) {
				// This is the method defined in this class, which doesn't need to be checked. Skip it.
				continue;
			}
			if(VISIT.equals(m.getName()) && (m.getModifiers() & Modifier.PUBLIC) != 0) {
				if(m.getReturnType() != void.class) {
					uhohs.add("Return type of public visit() methods must be void, but "
							+ clazz.getName() + " " + m + " does not conform");
				}
				if(m.getParameterTypes().length != 1) {
					uhohs.add("Public visit() methods must accept exactly one parameter, but"
							+ clazz.getName() + " " + m + " does not conform");
				} else {
					Class<?> param = m.getParameterTypes()[0];
					if(baseClass.isAssignableFrom(param)) {
						handledClasses.add(param);
					} else {
						uhohs.add("Public visit() methods parameters must extend the given base class's type, but the"
								+ " parameter of method " + m + " in " + clazz.getName()
								+ " has a disjoint type than "
								+ baseClass.getName() + ". Make the method non-public, or rename it, if you would"
								+ " like to keep the method.");
					}
				}
			}
		}

		// Make sure that all subclasses are accounted for, taking into
		// account the value of directSubclassOnly
		Set<Class<?>> needsToHandle = new HashSet<>();
		for(Class<?> c : ClassDiscovery.getDefaultInstance().loadClassesThatExtend(baseClass)) {
			if((c.getModifiers() & Modifier.ABSTRACT) != 0) {
				// Abstract class, skip this, because an item can never be a concrete instance of this, and
				// thus is not required to be implemented
				continue;
			}
			if(info != null && info.directSubclassOnly()) {
				if(c.getSuperclass() == baseClass || Arrays.asList(c.getInterfaces()).contains(c)) {
					needsToHandle.add(c);
				}
			} else {
				needsToHandle.add(c);
			}
		}
		if(!needsToHandle.equals(handledClasses)) {
			String s = clazz.getName() + " is missing needed implementations of the visit method. It is required"
					+ " that it handle the following: " + needsToHandle + ", however, it only handles the following:"
					+ " " + handledClasses + ". Please add the following implementations:\n";
			needsToHandle.removeAll(handledClasses);
			for(Class<?> n : needsToHandle) {
				s += "public void visit(" + n.getName().replace("$", ".") + " obj) { /* Implement me */ }\n";
			}
			uhohs.add(s);
		}
		if(!uhohs.isEmpty()) {
			throw new RuntimeException(StringUtils.Join(uhohs, "\n"));
		}
	}

}
