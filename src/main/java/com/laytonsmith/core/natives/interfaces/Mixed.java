package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import java.util.Set;

/**
 * Mixed is the root type of all MethodScript objects and primitives.
 */
@typeof("mixed")
public interface Mixed extends Cloneable, Documentation {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final CClassType TYPE = CClassType.get("mixed");

    public String val();

    public void setTarget(Target target);

    public Mixed clone() throws CloneNotSupportedException;

    /**
     * Overridden from {@link SimpleDocumentation}. This should just return the value of the typeof annotation,
     * unconditionally.
     *
     * @return
     */
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
     * For true interfaces (that is, classes that return {@link ObjectType#INTERFACE}, this means the
     * values that this interface also extends.
     * @return
     */
    @ForceImplementation
    public CClassType[] getSuperclasses();

    /**
     * Returns a list of the interfaces that this *directly* implements. This is not always
     * equivalent to the interfaces that the underlying java class extends. All classes must
     * override this method, but if the class is a phantom class (that is, it implements Mixed,
     * but does not have a typeof annotation) then it can simply throw an UnsupportedOperationException.
     *
     * If this is an interface, this should return an empty array always.
     * @return
     */
    @ForceImplementation
    public CClassType[] getInterfaces();

    /**
     * Returns information about this class, whether it is a class, whether it is final, etc.
     */
    public ObjectType getObjectType();

    /**
     * Returns modification information about this class, i.e. if it is final
     * @return
     */
    public Set<ObjectModifier> getObjectModifiers();

    /**
     * Returns the containing class for this object. If null is returned, that means this is a top level class.
     * @return
     */
    public CClassType getContainingClass();

}
