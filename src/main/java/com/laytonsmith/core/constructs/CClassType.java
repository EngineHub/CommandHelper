package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.MEnumType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectDefinition;
import com.laytonsmith.core.objects.ObjectDefinitionNotFoundException;
import com.laytonsmith.core.objects.ObjectDefinitionTable;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.core.objects.UserObject;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A CClassType represents a reference to a MethodScript class.
 */
@typeof("ms.lang.ClassType")
@SuppressWarnings("checkstyle:overloadmethodsdeclarationorder")
public final class CClassType extends Construct implements com.laytonsmith.core.natives.interfaces.Iterable {

	public static final String PATH_SEPARATOR = FullyQualifiedClassName.PATH_SEPARATOR;

	/**
	 * The NATIVE_CACHE caches natively defined classes. These have generally different lifetime rules, namely, they
	 * always exist, and can't be undefined. Thus, we cache these here statically in CClassType, whereas the user class
	 * cache exists as part of the environment. TODO: Consider serializing this at compile time and loading it in at
	 * startup
	 */
	private static final ClassTypeCache NATIVE_CACHE = new ClassTypeCache();

	// The only types that can be created here are the ones that don't have a real class associated with them, or the
	// TYPE value itself
	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE;
	public static final CClassType AUTO;
	/**
	 * Used to differentiate between null and uninitialized. Note that use of this type beyond simply checking if
	 * something is java == to this is an Error.
	 *
	 * NOTE: This must come before the below static blocks are run.
	 */
	private static final Mixed UNINITIALIZED = new Mixed() {
		// <editor-fold defaultstate="collapsed" desc="Fake Mixed Implementation">
		@Override
		public String val() {
			throw new Error();
		}

		@Override
		public void setTarget(Target target) {
			throw new Error();
		}

		@Override
		public Target getTarget() {
			throw new Error();
		}

		@SuppressWarnings("RedundantThrows")
		@Override
		public Mixed clone() throws CloneNotSupportedException {
			throw new Error();
		}

		@Override
		public String getName() {
			throw new Error();
		}

		@Override
		public String docs() {
			throw new Error();
		}

		@Override
		public Version since() {
			throw new Error();
		}

		@Override
		public CClassType[] getSuperclasses() {
			throw new Error();
		}

		@Override
		public CClassType[] getInterfaces() {
			throw new Error();
		}

		@Override
		public ObjectType getObjectType() {
			throw new Error();
		}

		@Override
		public Set<ObjectModifier> getObjectModifiers() {
			throw new Error();
		}

		@Override
		public AccessModifier getAccessModifier() {
			throw new Error();
		}

		@Override
		public CClassType getContainingClass() {
			throw new Error();
		}

		@Override
		public boolean isInstanceOf(CClassType type, LeftHandGenericUse lhsGenericParameters, Environment env) {
			throw new Error();
		}

		@Override
		public CClassType typeof() {
			throw new Error();
		}

		@Override
		public URL getSourceJar() {
			throw new Error();
		}

		@Override
		public Class<? extends Documentation>[] seeAlso() {
			throw new Error();
		}
		// </editor-fold>
	};

	static {
		try {
			TYPE = new CClassType("ms.lang.ClassType", Target.UNKNOWN, null, CClassType.class);
			AUTO = new CClassType("auto", Target.UNKNOWN, null, null);
		} catch(ClassNotFoundException e) {
			throw new Error(e);
		}
	}

	/**
	 * This should generally be used instead of creating a new empty array in getInterfaces, if no interfaces are
	 * implemented by this class. This saves memory.
	 */
	public static final CClassType[] EMPTY_CLASS_ARRAY = new CClassType[0];

	static {
		NATIVE_CACHE.add(FullyQualifiedClassName.forNativeClass(CClassType.class), null, TYPE);
	}

	@StandardField
	private final FullyQualifiedClassName fqcn;
	@StandardField
	private final GenericParameters genericParameters;

	/**
	 * This is an invalid instance of the underlying type that can only be used for Documentation purposes or finding
	 * out meta information about the class.
	 *
	 * DO NOT USE THIS VALUE WITHOUT FIRST CALLING {@link #instantiateInvalidType}
	 */
	private Mixed invalidType = UNINITIALIZED;

	/**
	 * If this was constructed against a native class, we can do some optimizations in the course of operation. This may
	 * be null, and all code in this class must support the mechanisms if this is null anyways, but if it isn't null,
	 * then this can perhaps be used to help optimize.
	 */
	private final Class<? extends Mixed> nativeClass;

	private final GenericDeclaration genericDeclaration;

	/**
	 * Returns the singular instance of CClassType that represents this native type.
	 *
	 * <p>
	 * IMPORTANT: The type MUST be fully qualified AND exist as a real, instantiable class, or this will cause errors.
	 * The only time this method is preferred vs {@link #get(FullyQualifiedClassName, Environment)} is when used to
	 * define the TYPE value. The native class must also be provided at the same time, which is used for various
	 * operations to increase efficiency when dealing with native classes. If the type is defined with generics, use
	 * {@link #getWithGenericDeclaration(Class, GenericDeclaration)}.
	 *
	 * Unlike the other getters, this will not throw a ClassNotFoundException, it will instead throw an Error.
	 *
	 * @param type The native class
	 * @return A CClassType representing this native class
	 */
	public static CClassType get(Class<? extends Mixed> type) {
		return getWithGenericDeclaration(type, null);
	}

	/**
	 * Returns the singular instance of CClassType that represents this type.
	 *
	 * <p>
	 * IMPORTANT: The type MUST be fully qualified AND exist as a real, instantiable class, or this will cause errors.
	 * The only time this method is preferred vs {@link #get(FullyQualifiedClassName, Environment)} is when used to
	 * define the TYPE value. The native class must also be provided at the same time, which is used for various
	 * operations to increase efficiency when dealing with native classes.
	 *
	 * Unlike the other getters, this will not throw a ClassNotFoundException, it will instead throw an Error.
	 *
	 * @param type The native class
	 * @return A CClassType representing this native class
	 */
	public static CClassType getWithGenericDeclaration(Class<? extends Mixed> type, GenericDeclaration generics) {
		FullyQualifiedClassName fqcn = FullyQualifiedClassName.forNativeClass(type);
		CClassType classtype;
		if(!NATIVE_CACHE.containsNakedClassType(fqcn)) {
			// hasn't been defined yet
			classtype = defineClass(fqcn, generics, null, type);
		} else {
			classtype = getNakedClassType(fqcn, null);
		}
		return classtype;
	}

	/**
	 * Returns the "naked class type". This is the type without any parameters defined. In general, this represents a
	 * non-instantiatable class, but can be used in certain circumstances, particularly when the compiler needs to
	 * verify the generic declaration.
	 *
	 * @param type The type to get
	 * @param env The environment. For native class definitions only, this may be null, but is a required parameter in
	 * general. It's only safe to send null here if you're certain the class represented by {@code type} exists in the
	 * native cache.
	 * @return The naked class type. This may return null if the class is not yet defined.
	 */
	public static CClassType getNakedClassType(FullyQualifiedClassName type, Environment env) {
		try {
			NativeTypeList.getNativeClass(type);
		} catch(ClassNotFoundException e) {
			// Ignored, because we just want to load the Java class, which this method does.
		}
		if(NATIVE_CACHE.containsNakedClassType(type)) {
			return NATIVE_CACHE.getNakedClassType(type);
		} else {
			Objects.requireNonNull(env);
			ClassTypeCache cache = env.getEnv(GlobalEnv.class).GetClassCache();
			return cache.getNakedClassType(type);
		}
	}

	/**
	 * Returns the "naked class type". This is the type without any parameters defined. In general, this represents a
	 * non-instantiatable class, but can be used in certain circumstances, particularly when the compiler needs to
	 * verify the generic declaration or for very generic instanceof checks.
	 *
	 * @param env The environment. If the FQCN represents a native class, this can safely be null, but otherwise, and *
	 * in general, is a required parameter.
	 * @return The naked class for this CClassType, maybe just this object if the type already represents the naked
	 * type.
	 */
	public CClassType getNakedType(Environment env) {
		return getNakedClassType(this.getFQCN(), env);
	}

	/**
	 * <p>
	 * <strong>NOTE:</strong> This can only be used in specific cases where it is known that the type doesn't have
	 * generic parameters, or where you specifically want the naked type. This version of get should not be used in
	 * general.
	 * </p>
	 * Returns the singular instance of CClassType that represents this type. If it doesn't exist, it creates it,
	 * stores, and returns that instance. Note that in general, == is not supported for these types. This method will
	 * only succeed on types that don't have a generic declaration, for ones with, you must use
	 * {@link #get(FullyQualifiedClassName, Target, GenericParameters, Environment)}.
	 *
	 * @param type The fully qualified class type
	 * @param env The environment. If the FQCN represents a native class, this can safely be null, but otherwise, and in
	 * general, is a required parameter.
	 * @return The CClassType object for this FQCN
	 */
	public static CClassType get(FullyQualifiedClassName type, Environment env) {
		return get(type, Target.UNKNOWN, null, env);
	}

	/**
	 * Returns the singular instance of CClassType that represents this type. If it doesn't exist, it creates it,
	 * stores, and returns that instance. Note that in general, == is not supported for these types, even though in
	 * general it is correct to say that for each type, there will only be one instance.
	 *
	 * @param nakedType The top level type. This doesn't technically have to be the naked type, however, the parameters
	 * that are perhaps contained in this class are discarded.
	 * @param t The code target where this instance is being used.
	 * @param generics The generics to be added to this CClassType
	 * @return The CClassType object with the given generic parameter set
	 * @throws NoClassDefFoundError If there are generic parameters defined as non-null, but the naked class has not
	 * been defined yet. This shouldn't be possible except due to bugs in the code, because the general approach for
	 * native classes is to use the TYPE value, and in creation of user classes, it's important that the compiler does
	 * this correctly, rather than user code. Creation of the FullyQualifiedClassName may throw a ClassNotFoundException
	 * however, but that will be handled elsewhere.
	 * @throws CREGenericConstraintException In general, the generic parameters that are required must be provided (or
	 * be able to be inferred) and so if these are malformed, this will be thrown. For instance, if the naked type
	 * defines a new T() constraint, then a class will be required in the generics, this cannot be inferred. If it's not
	 * provided, then this will be thrown.
	 */
	public static CClassType get(CClassType nakedType, Target t, GenericParameters generics, Environment env) {
		return get(nakedType.getFQCN(), t, generics, env);
	}

	/**
	 * Returns the singular instance of CClassType that represents this type. If it doesn't exist, it creates it,
	 * stores, and returns that instance. Note that in general, == is not supported for these types, even though in
	 * general it is correct to say that for each type, there will only be one instance.
	 *
	 * @param type The fully qualified class name.
	 * @param t The code target where this instance is being used.
	 * @param generics The generics to be added to this CClassType
	 * @return The CClassType object with the given generic parameter set
	 *
	 * @throws NoClassDefFoundError If there are generic parameters defined as non-null, but the naked class has not
	 * been defined yet. This shouldn't be possible except due to bugs in the code, because the general approach for
	 * native classes is to use the TYPE value, and in creation of user classes, it's important that the compiler does
	 * this correctly, rather than user code. Creation of the FullyQualifiedClassName may throw a ClassNotFoundException
	 * however, but that will be handled elsewhere.
	 * @throws CREGenericConstraintException In general, the generic parameters that are required must be provided (or
	 * be able to be inferred) and so if these are malformed, this will be thrown. For instance, if the naked type
	 * defines a new T() constraint, then a class will be required in the generics, this cannot be inferred. If it's not
	 * provided, then this will be thrown.
	 */
	public static CClassType get(FullyQualifiedClassName type, Target t, GenericParameters generics, Environment env) {
		Objects.requireNonNull(type);
		CClassType naked = getNakedClassType(type, env);
		if(naked == null) {
			throw new NoClassDefFoundError("Naked class for " + type.getFQCN()
					+ " is not yet defined, it must be defined before use.");
		}
		if(naked.getGenericDeclaration() != null && generics == null) {
			for(Constraints c : naked.getGenericDeclaration().getConstraints()) {
				// Inferred parameters, assuming these all pass, it's fine
				c.convertFromDiamond(t);
			}
		}

		if(naked.getGenericDeclaration() == null) {
			if(generics != null) {
				throw new CRECastException("Generic parameters passed to " + type.getFQCN() + ", but none are"
						+ " defined on the type.", t);
			}
			// This is the class itself, no need to look up further.
			return naked;
		}

		ClassTypeCache cache = type.getNativeClass() != null || type.getFQCN().equals("auto")
				? NATIVE_CACHE : env.getEnv(GlobalEnv.class).GetClassCache();

		CClassType ctype;
		if(cache.contains(type, generics)) {
			ctype = cache.get(type, generics);
		} else {
			// The constructor adds it to the cache
			ctype = new CClassType(naked, t, generics, cache);
		}
		return ctype;
	}

	/**
	 * This function defines a brand new class type. This should exclusively be used in a class definition scenario, and
	 * never when simply looking up an existing class. The created CClassType is returned.
	 */
	public static CClassType defineClass(FullyQualifiedClassName fqcn, GenericDeclaration genericDeclaration,
			Environment env, Class<? extends Mixed> nativeClass) {
		try {
			return new CClassType(fqcn, Target.UNKNOWN, true, genericDeclaration, env,
					nativeClass);
		} catch(ClassNotFoundException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * INTERNAL ONLY: This can only be used for defining CClassType itself, as well as any other special types such as
	 * AUTO.
	 */
	private CClassType(String type, Target t, GenericDeclaration genericDeclaration, Class<? extends Mixed> nativeClass)
			throws ClassNotFoundException {
		this(FullyQualifiedClassName.forFullyQualifiedClass(type), t, false, genericDeclaration, null,
				nativeClass);
	}

	/**
	 * Creates a new naked CClassType object.
	 *
	 * @param type The name of the type
	 * @param t The code target
	 * @param newDefinition If true, this function MUST NOT throw a ClassNotFoundException.
	 * @param genericDeclaration The generic declaration for this class. May be null if no generics are being defined.
	 * @param env The environment. The class will be added to the environment's cache if this is a user class. This can
	 * safely be null for native classes.
	 * @param nativeClass The native class, if that's applicable. May be null, and all code will have to accept that
	 * this value might be null for user classes.
	 */
	@SuppressWarnings("ConvertToStringSwitch")
	private CClassType(FullyQualifiedClassName type, Target t, boolean newDefinition,
			GenericDeclaration genericDeclaration, Environment env, Class<? extends Mixed> nativeClass)
			throws ClassNotFoundException {
		super(type.getFQCN(), ConstructType.CLASS_TYPE, t);
		fqcn = type;
		this.genericParameters = null;
		this.genericDeclaration = genericDeclaration;
		this.nativeClass = nativeClass;

		if(!newDefinition) {
			boolean found = false;
			String localFQCN = fqcn.getFQCN();
			if(localFQCN.equals("auto") || localFQCN.equals("ms.lang.ClassType")) {
				// If we get here, we are within this class, and calling resolveNativeType won't work,
				// but anyways, we know we exist, so mark it as found. It is important to note, however,
				// if we end up defining more magic types within this class, this block needs to be updated.
				found = true;
			}
			// Do this to ensure at construction time that the class really does exist. We can't actually construct
			// the instance yet, because this might be the stack for the TYPE assignment, which means that this class
			// is not initialized yet. See the docs for instantiateInvalidType().
			// This works because we assume that resolveNativeTypes only uses the ClassMirror system. If that assumption
			// changes, we will need to basically re-implement that ourselves.
			if(!found) {
				if(fqcn.getNativeClass() != null) {
					found = true;
				} else {
					found = null != NativeTypeList.resolveNativeType(fqcn.getFQCN());
				}
			}
			// TODO: When user types are added, we will need to do some more digging here, and probably need
			// to pass in the CompilerEnvironment somehow.

			if(!found) {
				throw new ClassNotFoundException("Could not find class of type " + type);
			}
		} else {
			ClassTypeCache cache = nativeClass != null || type.getFQCN().equals("auto")
					? NATIVE_CACHE : env.getEnv(GlobalEnv.class).GetClassCache();
			cache.add(type, null, this);
		}
	}

	/**
	 * Creates a genericized version of an existing naked class definition.
	 *
	 * @param nakedType The naked type, that is, the one that contains just the generic declaration with no parameters.
	 * @param t Code target
	 * @param genericParameters The concrete generic parameters
	 */
	private CClassType(CClassType nakedType, Target t, GenericParameters genericParameters, ClassTypeCache cache) {
		super(nakedType.getFQCN() + (genericParameters == null ? "" : genericParameters.toString()),
				ConstructType.CLASS_TYPE, t);
		this.genericDeclaration = nakedType.getGenericDeclaration(); // same declaration as "parent" class
		fqcn = nakedType.fqcn;
		this.genericParameters = genericParameters;
		this.nativeClass = nakedType.nativeClass;
		cache.add(nakedType.fqcn, genericParameters, this);
	}

	/**
	 * While we would prefer to instantiate invalidType in the constructor, we can't, because this initializes the type,
	 * which occurs first when TYPE is initialized, that is, before the class is valid. Therefore, we cannot actually do
	 * that in the constructor, we need to lazy load it. We do take pains in the constructor to ensure that there is at
	 * least no way this will throw a ClassCastException, so given that, we are able to supress that exception here.
	 */
	private void instantiateInvalidType(Environment env) {
		if(this.invalidType != UNINITIALIZED) {
			return;
		}
		synchronized(this) {
			if(this.invalidType != UNINITIALIZED) {
				return;
			}
			@SuppressWarnings("LocalVariableHidesMemberVariable")
			String fqcn = this.fqcn.getFQCN();
			try {
				if("auto".equals(fqcn)) {
					invalidType = null;
				} else if("ms.lang.ClassType".equals(fqcn)) {
					invalidType = this;
				} else {
					// TODO: For now, we must use this mechanism, since we don't populate the ODT with
					// all the native classes. But once we do, we should remove this check entirely here.
					if(NativeTypeList.getNativeTypeList().contains(this.fqcn)) {
						invalidType = NativeTypeList.getInvalidInstanceForUse(this.fqcn);
					} else {
						ObjectDefinitionTable odt = env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable();
						ObjectDefinition od = odt.get(this.fqcn);
						invalidType = new UserObject(Target.UNKNOWN, null, env, od, null);
					}
				}
			} catch(ClassNotFoundException | ObjectDefinitionNotFoundException ex) {
				throw new Error(ex);
			}
		}
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	/**
	 * Returns true if checkClass extends, implements, or otherwise derives from superClass
	 */
	public static boolean doesExtend(CClassType checkClass, CClassType superClass) {
		if(checkClass.equals(superClass)) {
			// more efficient check
			return true;
		}
		if(checkClass.nativeClass != null && superClass.nativeClass != null
				&& superClass.nativeClass.isAssignableFrom(checkClass.nativeClass)) {
			// Since native classes are not allowed to extend multiple superclasees, but
			// in general, they are allowed to advertise that they do, for the sake of
			// methodscript, this can only be used to return true. If it returns true, it
			// definitely is, but if it returns false, that does not explicitly mean that
			// it doesn't. However, this check is faster, so we can do it and in 99% of
			// cases get a performance boost.
			return true;
		}
		try {
			// TODO: This is currently being done in a very lazy way. It needs to be reworked.
			// For now, this is ok, but will not work once user types are added.
			if(checkClass.nativeClass != null && superClass.nativeClass != null) {
				if(!superClass.nativeClass.isAssignableFrom(checkClass.nativeClass)) {
					return false;
				}
			} else {
				// TODO
				throw new ClassNotFoundException();
			}
		} catch(ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		return true;
	}

	/**
	 * Returns true if this class extends the specified one
	 */
	public boolean doesExtend(CClassType superClass) {
		return doesExtend(this, superClass);
	}

	/**
	 * Works like {@link #doesExtend(com.laytonsmith.core.constructs.CClassType, com.laytonsmith.core.constructs.CClassType)
	 * }, however rethrows the {@link ClassNotFoundException} that doesExtend throws as an {@link Error}. This should
	 * not be used unless the class names come from hardcoded values.
	 */
	public static boolean unsafeDoesExtend(CClassType checkClass, CClassType superClass) {
		return doesExtend(checkClass, superClass);
	}

	/**
	 * Performs an unsafe check to see if this class extends the specified one
	 */
	public boolean unsafeDoesExtend(CClassType superClass) {
		return unsafeDoesExtend(this, superClass);
	}

	/**
	 * Returns true if the specified class extends this one
	 */
	public boolean isExtendedBy(CClassType checkClass) throws ClassNotFoundException {
		return doesExtend(checkClass, this);
	}

	/**
	 * Performs an unsafe check to see if the specified class extends this one
	 */
	public boolean unsafeIsExtendedBy(CClassType checkClass) {
		return unsafeDoesExtend(checkClass, this);
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	/**
	 * Returns the superclasses for the underlying type, not the superclasses for ClassType itself.
	 */
	public CClassType[] getTypeSuperclasses(Environment env) {
		instantiateInvalidType(env);
		return Stream.of(invalidType).flatMap(e -> Stream.of(e.getSuperclasses()))
				.collect(Collectors.toSet()).toArray(CClassType.EMPTY_CLASS_ARRAY);
	}

	/**
	 * Returns the interfaces for the underlying type, not the interfaces for ClassType itself.
	 */
	public CClassType[] getTypeInterfaces(Environment env) {
		instantiateInvalidType(env);
		return Stream.of(invalidType).flatMap(e -> Stream.of(e.getInterfaces()))
				.collect(Collectors.toSet()).toArray(CClassType.EMPTY_CLASS_ARRAY);
	}

	/**
	 * Returns the package that this class is in. If the class is not in a package, null is returned.
	 */
	public CPackage getPackage() {
		if(!val().contains(PATH_SEPARATOR)) {
			return null;
		}
		String[] parts = val().split(Pattern.quote(PATH_SEPARATOR));
		return new CPackage(Target.UNKNOWN, ArrayUtils.slice(parts, 0, parts.length - 2));
	}

	/**
	 * Returns the name of the class type without the package. If this is a type union, then each type is simplified,
	 * and returned as a string such as "int|string".
	 */
	public String getSimpleName() {
		return fqcn.getSimpleName();
	}

	@Override
	public String docs() {
		return "A ClassType is a value that represents an object type. This includes primitives or other value types.";
	}

	public String getTypeDocs(Environment env) {
		instantiateInvalidType(env);
		return invalidType.docs();
	}

	public Version getTypeSince(Environment env) {
		instantiateInvalidType(env);
		return invalidType.since();
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	/**
	 * Returns the fully qualified class name for the class. Note that this is just the name of the class, not the
	 * complete type definition. See {@link #getTypeDefinition}.
	 *
	 * @return <code>ms.lang.ClassType</code> for instance.
	 */
	public FullyQualifiedClassName getFQCN() {
		return fqcn;
	}

	/**
	 * Returns the type definition, including generic definitions.
	 *
	 * @return <code>ms.lang.ClassType&lt;T&gt;</code> for instance.
	 */
	public String getTypeDefinition() {
		return val();
	}

	public boolean isEnum() {
		if("ms.lang.enum".equals(fqcn.getFQCN())) {
			// By default, this returns true when something is instanceof a thing, but in this case, we don't want
			// that, because ironically, ms.lang.enum is itself not an enum.
			return false;
		}
		return doesExtend(MEnumType.TYPE);
	}

	@Override
	public CClassType typeof() {
		return CClassType.TYPE;
	}

	// TODO: These getters will eventually be re-done to support static methods, but for now that is out of scope,
	// so we just specifically support enums for now.
	@Override
	public Mixed get(String index, Target t, Environment env) throws ConfigRuntimeException {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).get(index, t, env);
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	@Override
	public Mixed get(int index, Target t, Environment env) throws ConfigRuntimeException {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).get(index, t, env);
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	@Override
	public Mixed get(Mixed index, Target t, Environment env) throws ConfigRuntimeException {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).get(index, t, env);
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	@Override
	public Set<Mixed> keySet() {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).keySet();
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		return new HashSet<>();
	}

	@Override
	public long size() {
		if(isEnum()) {
			try {
				return NativeTypeList.getNativeEnumType(fqcn).size();
			} catch(ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		return 0;
	}

	@Override
	public boolean isAssociative() {
		return true;
	}

	@Override
	public boolean canBeAssociative() {
		return true;
	}

	@Override
	public Mixed slice(int begin, int end, Target t, Environment env) {
		throw new CREUnsupportedOperationException("Unsupported operation", t);
	}

	/**
	 * If this was constructed against a native class, we can do some optimizations in the course of operation. This may
	 * be null, and all code that uses this method must support the mechanisms if this is null anyways, but if it isn't
	 * null, then this can perhaps be used to help optimize.
	 */
	public Class<? extends Mixed> getNativeType() {
		return nativeClass;
	}

	@Override
	public boolean getBooleanValue(Target t) {
		return true;
	}

	/**
	 * Returns the generic declaration on the class itself.
	 *
	 * @return null if no generics were defined on this class, or else the {@link GenericDeclaration} for this
	 * ClassType.
	 */
	public GenericDeclaration getGenericDeclaration() {
		return genericDeclaration;
	}

	/**
	 * Returns the generic parameters on the instance of the class.
	 *
	 * @return null if no generics are defined on this class, or they were, but it's the naked class.
	 */
	public GenericParameters getGenericParameters() {
		return this.genericParameters;
	}

	// For accessor reasons, this must remain an inner class. The class should be public, but the methods private.
	public static class ClassTypeCache {

		private final Map<Pair<FullyQualifiedClassName, GenericParameters>, CClassType> cache;

		public ClassTypeCache() {
			cache = Collections.synchronizedMap(new HashMap<>());
		}

		/**
		 * Adds a new class to the cache.
		 *
		 * @param fqcn The fully qualified class name
		 * @param parameters The parameters for this instance. This may be null, both if the class has no generic
		 * definition, but also if this is the naked class.
		 * @param type The CClassType.
		 */
		private void add(FullyQualifiedClassName fqcn, GenericParameters parameters, CClassType type) {
			cache.put(new Pair<>(fqcn, parameters), type);
		}

		/**
		 * Returns the naked class, that is, the class without a defined parameter set. This may return null if the
		 * class has not been defined at all. Note that all classes without generic parameters are considered "naked",
		 * but then those would have been added with a null parameter set anyways, which should be equivalent.
		 */
		private CClassType getNakedClassType(FullyQualifiedClassName fqcn) {
			return get(fqcn, null);
		}

		/**
		 * Gets the CClassType instance for this FQCN and parameter set.
		 *
		 * @param fqcn The fully qualified class name
		 * @param declaration The parameter declaration. Null if this is a type without a parameter declaration, or if
		 * you wish to get the naked class.
		 */
		private CClassType get(FullyQualifiedClassName fqcn, GenericParameters declaration) {
			return cache.get(new Pair<>(fqcn, declaration));
		}

		/**
		 * Returns true if the cache contains this class.
		 */
		private boolean contains(FullyQualifiedClassName fqcn, GenericParameters declaration) {
			return get(fqcn, declaration) != null;
		}

		/**
		 * Returns true if the cache contains this class.
		 */
		private boolean containsNakedClassType(FullyQualifiedClassName fqcn) {
			return contains(fqcn, null);
		}
	}
}
