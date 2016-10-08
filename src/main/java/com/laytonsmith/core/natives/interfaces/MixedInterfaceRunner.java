package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.constructs.CClassType;
import java.net.URL;

/**
 * Classes that use InterfaceRunnerFor and represent an object that implements Mixed should implement this interface.
 * See {@link TypeofRunnerFor} for more details.
 */
public interface MixedInterfaceRunner {

    /**
     * See {@link Mixed#docs()}
     * @return
     */
    @ForceImplementation
    String docs();

    /**
     * See {@link Mixed#since()}
     * @return
     */
    @ForceImplementation
    Version since();

    /**
     * See {@link Mixed#getSuperclasses()}
     * @return
     */
    @ForceImplementation
    CClassType [] getSuperclasses();

    /**
     * See {@link Mixed#getInterfaces()}
     * @return
     */
    @ForceImplementation
    CClassType [] getInterfaces();

    /**
     * See {@link Mixed#getSourceJar()}
     * @return
     */
    URL getSourceJar();

    /**
     * See {@link Mixed#getName()}
     * @return
     */
    String getName();

}
