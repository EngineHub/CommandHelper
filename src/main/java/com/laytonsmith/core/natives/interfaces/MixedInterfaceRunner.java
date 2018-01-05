package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.constructs.CClassType;
import java.net.URL;
import java.util.Set;

/**
 * Classes that use InterfaceRunnerFor and represent an object that implements Mixed should implement this interface.
 * See {@link TypeofRunnerFor} for more details.
 */
public interface MixedInterfaceRunner extends Mixed {

    /**
     * See {@link Mixed#docs()}
     * @return
     */
    @ForceImplementation
    @Override
    String docs();

    /**
     * See {@link Mixed#since()}
     * @return
     */
    @ForceImplementation
    @Override
    Version since();

    /**
     * See {@link Mixed#getSuperclasses()}
     * @return
     */
    @ForceImplementation
    @Override
    CClassType [] getSuperclasses();

    /**
     * See {@link Mixed#getInterfaces()}
     * @return
     */
    @ForceImplementation
    @Override
    CClassType [] getInterfaces();

    /**
     * See {@link Mixed#getSourceJar()}
     * @return
     */
    @Override
    URL getSourceJar();

    /**
     * See {@link Mixed#getName()}
     * @return
     */
    @Override
    String getName();

    /**
     * See {@link Mixed#getObjectType()}
     * @return
     */
    @Override
    ObjectType getObjectType();

    /**
     * See {@link Mixed#getObjectModifiers()}
     * @return
     */
    @Override
    Set<ObjectModifier> getObjectModifiers();

    /**
     * See {@link Mixed#getContainingClass()}
     * @return
     */
    @Override
    CClassType getContainingClass();

}
