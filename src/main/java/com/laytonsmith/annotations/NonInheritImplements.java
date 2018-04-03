package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A class that must implement the methods of the given interface, but whose subclasses are not necessarily required to
 * implement them. The value must be an interface, or a compile error will be given. Note that classes that are tagged
 * with this are not directly castable to the specified interface, but will have all the methods required of them at
 * compile time. Further, it is also allowed for a subclass to be tagged with this, yet a superclass (which may or may
 * not be tagged with it) to actually provide the implementation. It is also an error to tag a class which is already an
 * instanceof this interface.
 *
 * A good use case for this mechanism is the case of serialization. Which it is useful to provide a default
 * serialization method, not all subclasses may want to be serializable themselves. However, if the superclass defines
 * itself as serializable, all subclasses would then be required to implement these methods, even if they do not
 * want to be serializable.
 *
 * For convenience, this interface also provides a Cast method and an Instanceof method, which replace the Java cast
 * mechanism and instanceof mechanism to be aware of the fact that the cast appears to not work.
 * @author cailin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NonInheritImplements {
	Class<?> value();


	/**
	 * Provides helper methods for operations on a {@link NonInheritImplements} ecosystem.
	 */
	public static class Helper {

		// No constructing allowed!
		private Helper(){}

		/**
		 * Casts a given object to the given type, assuming it actually is truly castable to the specified type. (Either
		 * because it implements the interface, or NonInheritImplements it.) Proxies are used as necessary to actually
		 * provide a first class interface.
		 * @param <T> The return interface type
		 * @param castType The return interface type
		 * @param o The object to cast
		 * @return The original object, cast to the appropriate type
		 * @throws If the underlying object does not implement the correct type
		 */
		public static <T> T Cast(Class<T> castType, Object o) throws ClassCastException {
			NonInheritImplements nii = o.getClass().getAnnotation(NonInheritImplements.class);
			if(castType.isAssignableFrom(o.getClass())) {
				return castType.cast(o);
			} else if(nii != null) {
				Class<?> iface = nii.value();
				@SuppressWarnings("unchecked")
				T ifaceProxy = (T) Proxy.newProxyInstance(o.getClass().getClassLoader(), new Class[]{iface}, (Object proxy, Method method, Object[] args) -> {
					return o.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(o, args);
				});
				return ifaceProxy;
			} else {
				throw new ClassCastException("The object cannot be cast to " + castType.getName());
			}
		}

		/**
		 * Works similar to the java {@code o instanceof value} mechanism, but is aware of the NonInheritImplements
		 * mechanism. It also checks to see if the object is castable using the normal cast mechanism too, but in
		 * general should not replace the instanceof keyword for normal use.
		 * @param o
		 * @param value
		 * @return
		 */
		public static boolean Instanceof(Object o, Class<?> value) {
			return value.isAssignableFrom(o.getClass())
					|| o.getClass().getAnnotation(NonInheritImplements.class) != null;
		}
	}
}
