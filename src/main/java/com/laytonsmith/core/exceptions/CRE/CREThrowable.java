package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectType;

/**
 *
 */
@typeof("ms.lang.Throwable")
public class CREThrowable extends AbstractCREException {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CREThrowable.class);

	@ForceImplementation
	public CREThrowable(String msg, Target t) {
		super(msg, t);
	}

	@ForceImplementation
	public CREThrowable(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public String docs() {
		return "All throwable types must extend this class. Otherwise, they will not be allowed to be thrown"
				+ " by the in script system.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	/**
	 * Subclasses can reliably return super.getSuperclasses() for this, if it follows the rule that it only has one
	 * superclass, and that superclass is the underlying java object as well.
	 *
	 * @return
	 */
	@Override
	public CClassType[] getSuperclasses() {
		if(this.getClass() == CREThrowable.class) {
			return new CClassType[]{Mixed.TYPE};
		} else {
//			try {
				return new CClassType[]{
					CClassType.get(
							// The superclass of a subclass to this class will always be of type Mixed,
							// so this cast will never fail, but we need it to convince the compiler this is ok.
							(Class<? extends Mixed>) this.getClass().getSuperclass()
					)
				};
//			} catch(ClassNotFoundException ex) {
//				throw new Error("Subclasses can reliably return super.getSuperclasses() for this, ONLY if it follows"
//						+ " the rule that it only has one superclass, and that superclass is the underlying java"
//						+ " object as well. This appears to be wrong in " + this.getClass());
//			}
		}
	}

	/**
	 * Subclasses can reliably return super.getInterfaces() for this, if it follows the rule that it does not implement
	 * any interfaces.
	 */
	@Override
	public CClassType[] getInterfaces() {
		if(this.getClass() == CREThrowable.class) {
			return new CClassType[]{ArrayAccess.TYPE};
		} else {
			return CClassType.EMPTY_CLASS_ARRAY;
		}
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

	@Override
	public CClassType getContainingClass() {
		return null;
	}

}
