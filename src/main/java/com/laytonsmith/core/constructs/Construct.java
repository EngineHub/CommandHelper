package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 */
public abstract class Construct implements Cloneable, Comparable<Construct>, Mixed {

	public enum ConstructType {

		TOKEN, COMMAND, FUNCTION, VARIABLE, LITERAL, ARRAY, MAP, ENTRY, INT,
		DOUBLE, BOOLEAN, NULL, STRING, VOID, IVARIABLE, CLOSURE, LABEL, SLICE,
		SYMBOL, IDENTIFIER, BRACE, BRACKET, BYTE_ARRAY, RESOURCE, LOCK, MUTABLE_PRIMITIVE,
		CLASS_TYPE, FULLY_QUALIFIED_CLASS_NAME;
	}

	private final ConstructType ctype;
	private final String value;

	private Target target;
	private transient boolean wasIdentifier = false;

	/**
	 * Gets the ConstructType of this Construct
	 * @return
	 * @deprecated Nothing in general should be Construct specific, and should be replaced with more generic
	 * instanceof checks, rather than getting the ConstructType.
	 */
	@Deprecated
	public ConstructType getCType() {
		return ctype;
	}

	/**
	 * Convenience method to check if a Mixed value is of the specified type. If it is not, or it isn't a construct in
	 * the first place, false is returned.
	 *
	 * @param m
	 * @param type
	 * @return
	 */
	public static boolean IsCType(Mixed m, ConstructType type) {
		if(m instanceof Construct) {
			return ((Construct) m).getCType() == type;
		}
		return false;
	}

	/**
	 * This method should only be used by Script when setting the children's target, if it's an ivariable.
	 *
	 * @param target
	 */
	@Override
	public void setTarget(Target target) {
		this.target = target;
	}

	public int getLineNum() {
		return target.line();
	}

	public File getFile() {
		return target.file();
	}

	public int getColumn() {
		return target.col();
	}

	public Target getTarget() {
		return target;
	}

	public Construct(String value, ConstructType ctype, int lineNum, File file, int column) {
		this.value = value;
		Static.AssertNonNull(value, "The string value may not be null.");
		this.ctype = ctype;
		this.target = new Target(lineNum, file, column);
	}

	public Construct(String value, ConstructType ctype, Target t) {
		this.value = value;
		Static.AssertNonNull(value, "The string value may not be null.");
		this.ctype = ctype;
		this.target = t;
	}

	/**
	 * Returns the standard string representation of this Construct. This will never return null.
	 *
	 * @return
	 */
	@Override
	public String val() {
		return value;
	}

	public void setWasIdentifier(boolean b) {
		wasIdentifier = b;
	}

	public boolean wasIdentifier() {
		return wasIdentifier;
	}

	/**
	 * Sets the wasIdentifier property on the left side if and only if both values are constructs. Default value can be
	 * provided if the left value is a construct, but not the right, then this value will be set. If it is null, the
	 * default is preserved.
	 *
	 * @param left
	 * @param right
	 */
	public static void SetWasIdentifierHelper(Mixed left, Mixed right, Boolean defaultValue) {
		if(right instanceof Construct) {
			defaultValue = ((Construct) right).wasIdentifier();
		}
		if(left instanceof Construct && defaultValue != null) {
			((Construct) left).setWasIdentifier(defaultValue);
		}
	}

	/**
	 * Returns the standard string representation of this Construct, except in the case that the construct is a CNull,
	 * in which case it returns java null.
	 *
	 * @param value
	 * @return
	 */
	public static String nval(Mixed value) {
		if(value instanceof CNull) {
			return null;
		}
		return value.val();
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public Construct clone() throws CloneNotSupportedException {
		return (Construct) super.clone();
	}

	/**
	 * This function takes a Construct, and turns it into a JSON value. If the construct is not one of the following, a
	 * MarshalException is thrown: CArray, CBoolean, CDouble, CInt, CNull, CString, CVoid, Command. Currently
	 * unsupported, but will be in the future are: CClosure/CFunction The following map is applied when encoding and
	 * decoding:
	 * <table border='1'>
	 * <tr><th>JSON</th><th>MethodScript</th></tr>
	 * <tr><td>string</td><td>CString, CVoid, Command, but all are decoded into CString</td></tr>
	 * <tr><td>number</td><td>CInt, CDouble, and it is decoded intelligently</td></tr>
	 * <tr><td>boolean</td><td>CBoolean</td></tr>
	 * <tr><td>null</td><td>CNull</td></tr>
	 * <tr><td>array/object</td><td>CArray</td></tr>
	 * </table>
	 *
	 * @param c
	 * @param t
	 * @return
	 * @throws com.laytonsmith.core.exceptions.MarshalException
	 */
	public static String json_encode(Mixed c, Target t, Environment env) throws MarshalException {
		return JSONValue.toJSONString(json_encode0(c, t, env));
	}

	private static Object json_encode0(Mixed c, Target t, Environment env) throws MarshalException {
		if(c.isInstanceOf(CString.TYPE, null, env) || c instanceof Command) {
			return c.val();
		} else if(c instanceof CVoid) {
			return "";
		} else if(c instanceof CInt) {
			return ((CInt) c).getInt();
		} else if(c instanceof CDouble) {
			return ((CDouble) c).getDouble();
		} else if(c instanceof CBoolean) {
			return ((CBoolean) c).getBoolean();
		} else if(c instanceof CNull) {
			return null;
		} else if(c.isInstanceOf(CArray.TYPE, null, env)) {
			CArray ca = (CArray) c;
			if(!ca.inAssociativeMode()) {
				List<Object> list = new ArrayList<Object>();
				for(int i = 0; i < ca.size(env); i++) {
					list.add(json_encode0(ca.get(i, t, env), t, env));
				}
				return list;
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				for(String key : ca.stringKeySet()) {
					map.put(key, json_encode0(ca.get(key, t, env), t, env));
				}
				return map;
			}
		} else {
			throw new MarshalException("The type of " + c.getClass().getSimpleName() + " is not currently supported", c);
		}
	}

	/**
	 * Takes a string and converts it into a Construct
	 *
	 * @param s
	 * @param t
	 * @return
	 * @throws com.laytonsmith.core.exceptions.MarshalException
	 */
	public static Construct json_decode(String s, Target t, Environment env) throws MarshalException {
		if(s == null) {
			return CNull.NULL;
		}
		if("".equals(s.trim())) {
			throw new MarshalException();
		}
		if(s.startsWith("{")) {
			//Object
			JSONObject obj = (JSONObject) JSONValue.parse(s);
			CArray ca = CArray.GetAssociativeArray(t, null, env);
			if(obj == null) {
				//From what I can tell, this happens when the json object is improperly formatted,
				//so go ahead and throw an exception
				throw new MarshalException();
			}
			for(Object key : obj.keySet()) {
				ca.set(convertJSON(key, t, env),
						convertJSON(obj.get(key), t, env), t, env);
			}
			return ca;
		} else if(s.startsWith("[")) {
			//It's an array
			JSONArray array = (JSONArray) JSONValue.parse(s);
			if(array == null) {
				throw new MarshalException();
			}
			CArray carray = new CArray(t, null, env);
			for(int i = 0; i < array.size(); i++) {
				carray.push(convertJSON(array.get(i), t, env), t, env);
			}
			return carray;
		} else {
			//It's a single value, but we're gonna wrap it in an array, then deconstruct it
			s = "[" + s + "]";
			JSONArray array = (JSONArray) JSONValue.parse(s);
			if(array == null) {
				//It's a null value
				return CNull.NULL;
			}
			Object o = array.get(0);
			return convertJSON(o, t, env);
		}
	}

	private static Construct convertJSON(Object o, Target t, Environment env) throws MarshalException {
		if(o instanceof String) {
			return new CString((String) o, Target.UNKNOWN);
		} else if(o instanceof Number) {
			Number n = (Number) o;
			if(n.longValue() == n.doubleValue()) {
				//It's an int
				return new CInt(n.longValue(), Target.UNKNOWN);
			} else {
				//It's a double
				return new CDouble(n.doubleValue(), Target.UNKNOWN);
			}
		} else if(o instanceof Boolean) {
			return CBoolean.get((Boolean) o);
		} else if(o instanceof java.util.List) {
			java.util.List l = (java.util.List) o;
			CArray ca = new CArray(t, null, env);
			for(Object l1 : l) {
				ca.push(convertJSON(l1, t, env), t, env);
			}
			return ca;
		} else if(o == null) {
			return CNull.NULL;
		} else if(o instanceof java.util.Map) {
			CArray ca = CArray.GetAssociativeArray(t, null, env);
			for(Object key : ((java.util.Map) o).keySet()) {
				ca.set(convertJSON(key, t, env),
						convertJSON(((java.util.Map) o).get(key), t, env), t, env);
			}
			return ca;
		} else {
			throw new MarshalException(o.getClass().getSimpleName() + " are not currently supported");
		}
	}

	@Override
	public int compareTo(Construct c) {
		if(this.value.contains(" ") || this.value.contains("\t")
				|| c.value.contains(" ") || c.value.contains("\t")) {
			return this.value.compareTo(c.value);
		}
		try {
			Double d1 = Double.valueOf(this.value);
			Double d2 = Double.valueOf(c.value);
			return d1.compareTo(d2);
		} catch(NumberFormatException e) {
			return this.value.compareTo(c.value);
		}
	}

	/**
	 * Converts a POJO to a Construct, if the type is convertable. This accepts many types of objects, and should be
	 * expanded if a type does fit into the overall type scheme.
	 *
	 * @param o
	 * @return
	 * @throws ClassCastException
	 */
	public static Construct GetConstruct(Object o, Environment env) throws ClassCastException {
		return Construct.GetConstruct(o, false, env);
	}

	/**
	 * Converts a POJO to a Construct, if the type is convertable. This accepts many types of objects, and should be
	 * expanded if a type does fit into the overall type scheme.
	 *
	 * @param o
	 * @param allowResources If true, unknown objects will be converted to a CResource.
	 * @return
	 * @throws ClassCastException
	 */
	public static Construct GetConstruct(Object o, boolean allowResources, Environment env) throws ClassCastException {
		if(o == null) {
			return CNull.NULL;
		} else if(o instanceof CharSequence) {
			return new CString((CharSequence) o, Target.UNKNOWN);
		} else if(o instanceof Number) {
			if(o instanceof Integer || o instanceof Long || o instanceof Byte || o instanceof BigInteger
					|| o instanceof AtomicInteger || o instanceof Short) {
				//integral
				return new CInt(((Number) o).longValue(), Target.UNKNOWN);
			} else {
				//floating point
				return new CDouble(((Number) o).doubleValue(), Target.UNKNOWN);
			}
		} else if(o instanceof Boolean) {
			return CBoolean.get((Boolean) o);
		} else if(o instanceof Map) {
			//associative array
			CArray a = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
			Map m = (Map) o;
			for(Entry<?, ?> entry : (Set<Entry<?, ?>>) m.entrySet()) {
				a.set(entry.getKey().toString(), GetConstruct(entry.getValue(), allowResources, env), Target.UNKNOWN, env);
			}
			return a;
		} else if(o instanceof Collection) {
			//normal array
			CArray a = new CArray(Target.UNKNOWN, null, env);
			Collection l = (Collection) o;
			for(Object obj : l) {
				a.push(GetConstruct(obj, allowResources, env), Target.UNKNOWN, env);
			}
			return a;
		} else {
			throw new ClassCastException(o.getClass().getName() + " cannot be cast to a Construct type");
		}
	}

	/**
	 * Converts a Construct to a POJO, if the type is convertable. The types returned from this method are set, unlike
	 * GetConstruct which is more flexible. The mapping is precisely as follows:
	 * <ul>
	 * <li>boolean -> Boolean</li>
	 * <li>integer -> Long</li>
	 * <li>double -> Double</li>
	 * <li>string -> String</li>
	 * <li>normal array -> ArrayList&lt;Object&gt;</li>
	 * <li>associative array -> SortedMap&lt;String, Object&gt;</li>
	 * <li>null -> null</li>
	 * <li>resource -> Object</li>
	 * </ul>
	 *
	 * @param c
	 * @return
	 * @throws ClassCastException
	 */
	public static Object GetPOJO(Mixed c, Environment env) throws ClassCastException {
		if(c instanceof CNull) {
			return null;
		} else if(c instanceof CString) {
			return c.val();
		} else if(c instanceof CBoolean) {
			return Boolean.valueOf(((CBoolean) c).getBoolean());
		} else if(c instanceof CInt) {
			return Long.valueOf(((CInt) c).getInt());
		} else if(c instanceof CDouble) {
			return Double.valueOf(((CDouble) c).getDouble());
		} else if(c.isInstanceOf(CArray.TYPE, null, env)) {
			CArray ca = (CArray) c;
			if(ca.inAssociativeMode()) {
				//SortedMap
				SortedMap<String, Object> map = new TreeMap<>();
				for(Entry<String, Mixed> entry : ca.getAssociativeArray().entrySet()) {
					map.put(entry.getKey(), GetPOJO(entry.getValue(), env));
				}
				return map;
			} else {
				//ArrayList
				ArrayList<Object> list = new ArrayList<Object>((int) ca.size(env));
				for(Mixed construct : ca.getArray()) {
					list.add(GetPOJO(construct, env));
				}
				return list;
			}
		} else if(c instanceof CResource) {
			return ((CResource) c).getResource();
		} else {
			throw new ClassCastException(c.getClass().getName() + " cannot be cast to a POJO");
		}
	}

	public CString asString() {
		return new CString(val(), target);
	}

	/**
	 * If this type of construct is dynamic, that is to say, if it isn't a constant. Things like 9, and 's' are
	 * constant. Things like {@code @value} are dynamic.
	 *
	 * @return
	 */
	public abstract boolean isDynamic();

	/**
	 * If the underlying Mixed value is a Construct, returns the value of isDynamic. Otherwise, returns true.
	 *
	 * @param m
	 * @return
	 */
	public static boolean IsDynamicHelper(Mixed m) {
		if(m instanceof Construct) {
			return ((Construct) m).isDynamic();
		}
		// TODO: This needs to be changed once the concept of immutability is introduced
		return true;
	}

	/**
	 * Returns the underlying value, as a value that can be directly inserted into code. So, if the value were
	 * {@code This is 'the value'}, then {@code 'This is \'the value\''} would be returned. (That is, characters needing
	 * escapes will be escaped.) It includes the outer quotes as well. Numbers and other primitives may be able to
	 * override this to return a valid value as well. By default, this assumes a string, and returns appropriately.
	 *
	 * @return
	 */
	protected String getQuote() {
		return "'" + val()
				.replace("\\", "\\\\")
				.replace("'", "\\'")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				+ "'";
	}

	/**
	 * Returns the typeof this Construct, as a CClassType. Not all constructs are annotated with the @typeof annotation,
	 * in which case this is considered a "private" object, which can't be directly accessed via MethodScript. In this
	 * case, an IllegalArgumentException is thrown.
	 *
	 * This method may be overridden in special cases, such as dynamic types, but for most types, this
	 *
	 * @return
	 * @throws IllegalArgumentException If the class isn't public facing.
	 */
	@Override
	public CClassType typeof(Environment env) {
		return typeof(this, env);
	}

	/**
	 * Returns the typeof for the given class, using the same mechanism as the default.(Whether or not that subtype
	 * overrode the original typeof() method.
	 *
	 * @param that
	 * @param env
	 * @return
	 */
	public static CClassType typeof(Mixed that, Environment env) {
		return CClassType.get(that.getClass(), Target.UNKNOWN, that.getGenericParameters(), env);
	}

	private final Map<CClassType, GenericParameters> genericParameters = new HashMap<>();

	/**
	 * Returns the generic parameters for this Construct. By default, null, but this MUST be overridden by objects which
	 * have generics.
	 *
	 * @return
	 */
	@Override
	public Map<CClassType, GenericParameters> getGenericParameters() {
		if(genericParameters.isEmpty()) {
			return null;
		} else {
			return genericParameters;
		}
	}

	/**
	 * Returns the GenericParameters that was registered to this class.
	 * @param env The environment.
	 * @return The generic parameters for this class, or null, if none were registered with
	 * {@link #registerGenericParameters}
	 */
	protected GenericParameters getThisGenericParameters(Environment env) {
		return genericParameters.get(this.typeof(env));
	}

	protected final void registerGenericParameters(CClassType type, GenericParameters parameters) {
		this.genericParameters.put(type, parameters);
	}

	/**
	 * Overridden from {@link SimpleDocumentation}. This should just return the value of the typeof annotation,
	 * unconditionally.
	 *
	 * @return
	 */
	@Override
	public final String getName() {
		typeof t = ClassDiscovery.GetClassAnnotation(this.getClass(), typeof.class);
		return t.value();
	}

	// We provide default instances of these methods, though they should in practice never run.
	@Override
	public String docs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Version since() {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[]{};
	}

	@Override
	public CClassType[] getInterfaces() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CClassType[] getSuperclasses() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Everything that extends this must be a CLASS
	 *
	 * @return
	 */
	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

	/**
	 * By default, all native methodscript objects have no modifiers.
	 *
	 * @return
	 */
	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.noneOf(ObjectModifier.class);
	}

	/**
	 * By default, all native methodscript objects are public. If this is not true, this method must be overridden.
	 *
	 * @return
	 */
	@Override
	public AccessModifier getAccessModifier() {
		return AccessModifier.PUBLIC;
	}

	@Override
	public CClassType getContainingClass() {
		return null;
	}

	public static boolean isInstanceof(Mixed that, Class<? extends Mixed> type, Environment env) {
		if(ClassDiscovery.GetClassAnnotation(that.getClass(), typeof.class) == null) {
			// This can happen in cases where we are in the middle of optimization.
			// This can perhaps be improved in the future, when we store the return
			// type with the CFunction, and we can at least handle those cases,
			// but anyways, for now, just return false.
			return false;
		}
		return that.typeof(env).doesExtend(env, CClassType.get(type));
	}

	@Override
	public boolean isInstanceOf(CClassType type, LeftHandGenericUse lhsGenericParameters, Environment env) {
		if(this.getClass().getAnnotation(typeof.class) == null) {
			// TODO: This isn't ideal, but CFunction and other things extend Mixed. Maybe they shouldn't, or
			// maybe they should have a typeof defined on them, but in general, that doesn't make sense, because
			// they currently aren't first class, and can only be accessed by accident.
			return false;
		}
		return InstanceofUtil.isInstanceof(this,
				LeftHandSideType.fromCClassType(type, lhsGenericParameters, Target.UNKNOWN), env);
	}

	/**
	 * Provides a default implementation of hashCode for all constructs. In this implementation, the type of the object
	 * is taken into account, as well as the underlying value.
	 *
	 * @return
	 */
	@Override
	public int hashCode() {
		return Objects.hash(ClassDiscovery.GetClassAnnotation(this.getClass(), typeof.class), val());
	}

}
