package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import java.net.URL;

/**
 *
 * @author cailin
 */
public abstract class AbstractMixedInterfaceRunner implements MixedInterfaceRunner {

    @SuppressWarnings("unchecked")
    protected Class<? extends Mixed> getParentClass() {
	return (Class<? extends Mixed>)
		this.getClass().getAnnotation(InterfaceRunnerFor.class).value();
    }

    @Override
    public URL getSourceJar() {
	return ClassDiscovery.GetClassContainer(getParentClass());
    }

    @Override
    public String getName() {
	return getParentClass().getAnnotation(typeof.class).value();
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
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CClassType[] getSuperclasses() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

}
