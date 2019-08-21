package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.objects.AccessModifier;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author cailin
 */
public abstract class AbstractMixedInterfaceRunner implements MixedInterfaceRunner {

	/**
	 * This returns the class file to which this InterfaceRunner is attached.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Mixed> getSponsorClass() {
		return (Class<? extends Mixed>) this.getClass().getAnnotation(InterfaceRunnerFor.class).value();
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(getSponsorClass());
	}

	@Override
	public String getName() {
		return ClassDiscovery.GetClassAnnotation(getSponsorClass(), typeof.class).value();
	}

	@Override
	public String docs() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Version since() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[0];
	}

	@Override
	public CClassType[] getSuperclasses() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.INTERFACE;
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
	public Mixed clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void setTarget(Target target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Target getTarget() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String val() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[]{};
	}

	@Override
	public CClassType getContainingClass() {
		return null;
	}

	/**
	 * Returns the typeof this Construct, as a string. Not all constructs are annotated with the @typeof annotation, in
	 * which case this is considered a "private" object, which can't be directly accessed via MethodScript. In this
	 * case, an IllegalArgumentException is thrown.
	 *
	 * @return
	 * @throws IllegalArgumentException If the class isn't public facing.
	 */
	@Override
	public final CClassType typeof() {
		return Construct.typeof(this);
	}

	@Override
	public boolean isInstanceOf(CClassType type) {
		return Construct.isInstanceof(this, type);
	}

	@Override
	public boolean isInstanceOf(Class<? extends Mixed> type) {
		return Construct.isInstanceof(this, type);
	}

}
