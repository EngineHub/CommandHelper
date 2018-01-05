package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cailin
 */
public abstract class AbstractMixedInterfaceRunner implements MixedInterfaceRunner {

    /**
     * This returns the class file to which this InterfaceRunner is attached.
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Mixed> getSponsorClass() {
	return (Class<? extends Mixed>)
		this.getClass().getAnnotation(InterfaceRunnerFor.class).value();
    }

    @Override
    public URL getSourceJar() {
	return ClassDiscovery.GetClassContainer(getSponsorClass());
    }

    @Override
    public String getName() {
	return getSponsorClass().getAnnotation(typeof.class).value();
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
	return EnumSet.of(ObjectModifier.PUBLIC);
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

}
