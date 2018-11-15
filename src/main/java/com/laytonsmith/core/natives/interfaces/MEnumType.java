package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class wraps an enum value inside some MEnum class
 */
public class MEnumType implements Mixed {

	private final CClassType parentType;
	private final String name;
	private final int ordinal;

	private Target target;

	public MEnumType(CClassType parentType, String name, int ordinal) {
		this.parentType = parentType;
		this.name = name;
		this.ordinal = ordinal;
	}

	public CClassType getType() {
		return parentType;
	}

	public String getName() {
		return name;
	}

	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public String val() {
		return name;
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
	public MEnumType clone() throws CloneNotSupportedException {
		return new MEnumType(parentType, name, ordinal);
	}

	@Override
	public String docs() {
		return "";
	}

	@Override
	public Version since() {
		return new SimpleVersion(0, 0, 0);
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CClassType.get("ms.lang.enum")};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[0];
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.ENUM;
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL, ObjectModifier.PUBLIC);
	}

	@Override
	public CClassType getContainingClass() {
		return null;
	}

	@Override
	public boolean isInstanceOf(CClassType type) throws ClassNotFoundException {
		return this.parentType.equals(type);
	}

	@Override
	public boolean isInstanceOf(Class<? extends Mixed> type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CClassType typeof() {
		return parentType;
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(MEnumType.class);
	}

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[0];
	}
}
