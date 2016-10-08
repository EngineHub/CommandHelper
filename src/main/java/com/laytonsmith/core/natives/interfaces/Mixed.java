package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * Mixed is the root type of all MethodScript objects and primitives.
 */
@typeof("mixed")
public interface Mixed extends Cloneable, Documentation {

    public String val();

    public void setTarget(Target target);

    public Mixed clone() throws CloneNotSupportedException;

    @Override
    public String getName();

    @Override
    @ForceImplementation
    public String docs();

    @Override
    @ForceImplementation
    public Version since();

    /**
     * Returns a list of the classes that this *directly* extends. This is not always
     * equivalent to the classes that the underlying java class extends. All classes must
     * override this method, but if the class is a phantom class (that is, it implements Mixed,
     * but does not have a typeof annotation) then it can simply throw an UnsupportedOperationException.
     *
     * For true interfaces, this means the values that this interface also extends.
     * @return
     */
    @ForceImplementation
    public CClassType[] getSuperclasses();

    /**
     * Returns a list of the interfaces that this *directly* implements. This is not always
     * equivalent to the interfaces that the underlying java class extends. All classes must
     * override this method, but if the class is a phantom class (that is, it implements Mixed,
     * but does not have a typeof annotation) then it can simply throw an UnsupportedOperationException.
     * @return
     */
    @ForceImplementation
    public CClassType[] getInterfaces();

}
