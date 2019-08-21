package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.objects.AccessModifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the base class for all enums in MethodScript. Enums themselves can be automatically generated based on a
 * real enum, or dynamically generated based on user code. Enums marked with {@link MEnum} or {@link MDynamicEnum} are
 * automatically added to the ecosystem, however.
 */
@typeof("ms.lang.enum")
public abstract class MEnumType implements Mixed, com.laytonsmith.core.natives.interfaces.Iterable {


	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(MEnumType.class);

	/**
	 * Generates a new MEnumType subclass.
	 * @param fqcn The fully qualified class name. Generally, this should be gathered from the typeof of the MEnum, if
	 * applicable, but if this is an external enum, or dynamically generated, this may come from other sources.
	 * @param enumClass The underlying java enum class
	 * @param docs This may be null if the enum implements {@code public static String enumDocs()}, otherwise this
	 * should be the docs for the enum class as a whole.
	 * @param since This may be null if the enum implements {@code public static Version enumSince()}, otherwise this
	 * should be the since tag for the enum class as a whole.
	 * @return A subclass of MEnumType. This does not register it in the ecosystem.
	 */
	public static MEnumType FromEnum(FullyQualifiedClassName fqcn, final Class<Enum<?>> enumClass,
			String docs, Version since) {
		return FromPartialEnum(fqcn, enumClass, enumClass.getEnumConstants(), docs, since);
	}

	/**
	 * Generates a new MEnumType subclass.
	 * @param fqcn The fully qualified class name. Generally, this should be gathered from the typeof of the MEnum, if
	 * applicable, but if this is an external enum, or dynamically generated, this may come from other sources.
	 * @param enumClass The underlying java enum class
	 * @param values The list of enum constants. This does not have to be the full list of Enum values in the type, or
	 * indeed, even the enum values from the enumClass. It does have to be an Enum type, however, as we need a
	 * customizable type for documentation purposes.
	 * @param docs This may be null if the enum implements {@code public static String enumDocs()}, otherwise this
	 * should be the docs for the enum class as a whole.
	 * @param since This may be null if the enum implements {@code public static Version enumSince()}, otherwise this
	 * should be the since tag for the enum class as a whole.
	 * @return A subclass of MEnumType. This does not register it in the ecosystem.
	 */
	public static MEnumType FromPartialEnum(FullyQualifiedClassName fqcn, final Class<?> enumClass,
			Enum<?>[] values, String docs, Version since) {
		final Enum<?>[] constants = values;
		return new MEnumType() {
			@Override
			public String docs() {
				if(docs != null) {
					return docs;
				}
				Method enumDocs;
				try {
					enumDocs = enumClass.getDeclaredMethod("enumDocs");
				} catch (NoSuchMethodException | SecurityException ex) {
					return "This enum does not have documentation. Either pass in the docs, or implement public static"
							+ " String enumDocs() in the enum.";
				}
				Object d;
				try {
					d = enumDocs.invoke(null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					d = null;
				}
				if(d instanceof String) {
					return (String) d;
				}
				return "The return type of the enumDocs method is wrong. It must return a String";
			}

			@Override
			public Version since() {
				if(since != null) {
					return since;
				}
				Method enumSince;
				try {
					enumSince = enumClass.getDeclaredMethod("enumSince");
				} catch (NoSuchMethodException | SecurityException ex) {
					return new SimpleVersion(0, 0, 0);
				}
				Object d;
				try {
					d = enumSince.invoke(null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					d = null;
				}
				if(d instanceof Version) {
					return (Version) d;
				}
				return new SimpleVersion(0, 0, 0);
			}

			@Override
			public CClassType[] getSuperclasses() {
				return new CClassType[]{MEnumType.TYPE};
			}

			@Override
			public String getName() {
				return fqcn.getFQCN();
			}

			@Override
			public URL getSourceJar() {
				return ClassDiscovery.GetClassContainer(enumClass);
			}

			@Override
			public boolean isInstanceOf(CClassType type) {
				return Mixed.TYPE.equals(type) || MEnumType.TYPE.equals(type) || this.typeof().equals(type);
			}

			@Override
			public boolean isInstanceOf(Class<? extends Mixed> type) {
				return type.isAssignableFrom(this.getClass());
			}

			@Override
			public CClassType typeof() {
				try {
					return CClassType.get(fqcn);
				} catch (ClassNotFoundException ex) {
					throw new Error(ex);
				}
			}

			@Override
			public String val() {
				return getName();
			}

			@Override
			public List<MEnumTypeValue> getValues() {
				return new AbstractList<MEnumTypeValue>() {
					@Override
					public MEnumTypeValue get(int index) {
						final Enum<?> v = constants[index];
						return new MEnumTypeValue() {
							@Override
							public int ordinal() {
								return index;
							}

							@Override
							public String name() {
								return v.name();
							}

							@Override
							public URL getSourceJar() {
								return ClassDiscovery.GetClassContainer(enumClass);
							}

							@Override
							public Class<? extends Documentation>[] seeAlso() {
								if(SimpleDocumentation.class.isAssignableFrom(v.getDeclaringClass())) {
									try {
										return (Class[]) v.getDeclaringClass().getDeclaredMethod("seeAlso").invoke(v);
									} catch (NoSuchMethodException | SecurityException | IllegalAccessException
											| IllegalArgumentException | InvocationTargetException ex) {
										throw new RuntimeException(ex);
									}
								} else {
									return new Class[0];
								}
							}

							@Override
							public String getName() {
								return v.name();
							}

							@Override
							public String docs() {
								if(SimpleDocumentation.class.isAssignableFrom(v.getDeclaringClass())) {
									try {
										return (String) v.getDeclaringClass().getDeclaredMethod("docs").invoke(v);
									} catch (NoSuchMethodException | SecurityException | IllegalAccessException
											| IllegalArgumentException | InvocationTargetException ex) {
										throw new RuntimeException(ex);
									}
								} else {
									return "";
								}
							}

							@Override
							public Version since() {
								if(SimpleDocumentation.class.isAssignableFrom(v.getDeclaringClass())) {
									try {
										return (Version) v.getDeclaringClass().getDeclaredMethod("since").invoke(v);
									} catch (NoSuchMethodException | SecurityException | IllegalAccessException
											| IllegalArgumentException | InvocationTargetException ex) {
										throw new RuntimeException(ex);
									}
								} else {
									return MSVersion.V0_0_0;
								}
							}

							@Override
							public int hashCode() {
								return v.hashCode();
							}

							@Override
							@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
							public boolean equals(Object obj) {
								return v.equals(obj);
							}

							@Override
							public String toString() {
								return v.toString();
							}

							@Override
							public CClassType typeof() {
								try {
									return CClassType.get(fqcn);
								} catch (ClassNotFoundException ex) {
									throw new Error(ex);
								}
							}

							@Override
							public boolean isInstanceOf(CClassType type) {
								return Construct.isInstanceof(this, type);
							}

							@Override
							public boolean isInstanceOf(Class<? extends Mixed> type) {
								return Construct.isInstanceof(this, type);
							}

							@Override
							public CClassType getContainingClass() {
								return null;
							}

							@Override
							public AccessModifier getAccessModifier() {
								return AccessModifier.PUBLIC;
							}

							@Override
							public Set<ObjectModifier> getObjectModifiers() {
								return EnumSet.of(ObjectModifier.FINAL, ObjectModifier.STATIC,
										ObjectModifier.ABSTRACT);
							}

							@Override
							public ObjectType getObjectType() {
								return ObjectType.ENUM;
							}

							@Override
							public CClassType[] getInterfaces() {
								return new CClassType[0];
							}

							@Override
							public CClassType[] getSuperclasses() {
								return new CClassType[]{MEnumType.TYPE};
							}

							@Override
							public Mixed clone() throws CloneNotSupportedException {
								return this;
							}

							private Target t = Target.UNKNOWN;

							@Override
							public Target getTarget() {
								return t;
							}

							@Override
							public void setTarget(Target target) {
								t = target;
							}

							@Override
							public String val() {
								return getName();
							}
						};
					}

					@Override
					public int size() {
						return constants.length;
					}
				};
			}


		};
	}

	private static final MEnumType ROOT_TYPE = new MEnumType() {
		@Override
		protected List<MEnumTypeValue> getValues() {
			throw new UnsupportedOperationException("The root MEnumType is a meta object, and cannot be used normally."
					+ " There are no values in the class.");
		}

	};

	/**
	 * Returns the meta object representing the ms.lang.enum type. While this is a valid type, it is not an enum per se
	 * and cannot be used like an enum. It can be used as a class type or a documentation getter.
	 * @return
	 */
	public static MEnumType getRootEnumType() {
		return ROOT_TYPE;
	}



	private Target target;

	public MEnumType() {

	}

	@Override
	public String getName() {
		return TYPE.getName();
	}

	@Override
	public String val() {
		return TYPE.getName();
	}

	@Override
	public void setTarget(Target target) {
		this.target = target;
	}

	@Override
	public Target getTarget() {
		return this.target;
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public MEnumType clone() throws CloneNotSupportedException {
		return this;
	}

	@Override
	public String docs() {
		return "This is the base type for all enums in MethodScript.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Iterable.TYPE};
	}

	@Override
	public ObjectType getObjectType() {
		// The individual values in the enum are ENUM, but the container is a class
		return ObjectType.CLASS;
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL, ObjectModifier.ABSTRACT);
	}

	@Override
	public AccessModifier getAccessModifier() {
		return AccessModifier.PUBLIC;
	}

	@Override
	public CClassType getContainingClass() {
		return null;
	}

	@Override
	public boolean isInstanceOf(CClassType type) {
		return TYPE.equals(type);
	}

	@Override
	public boolean isInstanceOf(Class<? extends Mixed> type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CClassType typeof() {
		return TYPE;
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(MEnumType.class);
	}

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[0];
	}

	private volatile List<MEnumTypeValue> values = null;
	private final Object lock = new Object();
	/**
	 * Unlike
	 * @return
	 */
	public List<MEnumTypeValue> values() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		List<MEnumTypeValue> values = this.values;
		if(values == null) {
			synchronized(lock) {
				values = this.values;
				if(values == null) {
					this.values = values = getValues();
				}
			}
		}
		return values;
	}
	@Override
	public Mixed get(String index, Target t) throws ConfigRuntimeException {
		for(MEnumTypeValue v : values()) {
			if(v.name().equals(index)) {
				return v;
			}
		}
		throw new CREIllegalArgumentException(index + " cannot be found in " + typeof(), t);
	}

	@Override
	public Mixed get(int index, Target t) throws ConfigRuntimeException {
		if(index >= values().size()) {
			throw new CREIndexOverflowException("The index " + index + " is out of bounds", t);
		}
		return values().get(index);
	}

	@Override
	public Mixed get(Mixed index, Target t) throws ConfigRuntimeException {
		return get(index.val(), t);
	}

	@Override
	public Set<Mixed> keySet() {
		return values().stream().collect(Collectors.toSet());
	}

	@Override
	public long size() {
		return values().size();
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
	public Mixed slice(int begin, int end, Target t) {
		throw new CREUnsupportedOperationException("Cannot slice an enum", t);
	}

	/**
	 * Returns a list of the underlying enum values. This is roughly equivalent to a list of the Enum java class.
	 * @return
	 */
	protected abstract List<MEnumTypeValue> getValues();

}
