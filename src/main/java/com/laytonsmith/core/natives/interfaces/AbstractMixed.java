package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

/**
 * Provides a basic Mixed implementation. This assumes that the object is a public, top level object. If it's also
 * a class, instead use AbstractMixedClass.
 */
public abstract class AbstractMixed implements Mixed {

	private Target t = Target.UNKNOWN;

	@Override
	public Mixed clone() throws CloneNotSupportedException {
		return (Mixed) super.clone();
	}

	@Override
	public void setTarget(Target target) {
		t = target;
	}

	@Override
	public Target getTarget() {
		return t;
	}

	@Override
	public String val() {
		return this.toString();
	}

	@Override
	public String getName() {
		return typeof().getName();
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.noneOf(ObjectModifier.class);
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
		if(type.getNativeType() != null) {
			return type.getNativeType().isAssignableFrom(this.getClass());
		}
		return Construct.isInstanceof(this, type);
	}

	@Override
	public boolean isInstanceOf(Class<? extends Mixed> type) {
		return type.isAssignableFrom(this.getClass());
	}

	/**
	 * Returns the typeof this Mixed, as a CClassType. Not all constructs are annotated with the @typeof annotation,
	 * in which case this is considered a "private" object, which can't be directly accessed via MethodScript. In this
	 * case, an IllegalArgumentException is thrown.
	 *
	 * This method may be overridden in special cases, such as dynamic types, but for most types, this
	 * @return
	 * @throws IllegalArgumentException If the class isn't public facing.
	 */
	@Override
	public CClassType typeof() {
		return Construct.typeof(this);
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	private static final Class[] EMPTY_CLASS = new Class[0];

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Documentation>[] seeAlso() {
		seealso seealso = this.getClass().getAnnotation(seealso.class);
		if(seealso != null) {
			return seealso.value();
		} else {
			return EMPTY_CLASS;
		}
	}
}
