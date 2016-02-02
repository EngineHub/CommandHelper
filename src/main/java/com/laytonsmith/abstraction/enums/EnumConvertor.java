
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;

/**
 * Maps an enum class to another enum class. By default, the converter uses a heuristic
 * to convert the enums. The general convention is usually that enums are named the same, except
 * in one off cases. If the enum isn't mapped 1:1, then the converter will need to provide the
 * conversion information by overriding the get(Abstracted|Concrete)EnumCustom methods, which
 * contain a switch (or similar) statement that does the proper conversion. Usually both
 * or neither methods will need overriding. There is a runtime unit test, which checks
 * the enums for missing values at runtime, and reports any values that can't be converted, allowing
 * for easier and faster identification of failure conditions. This is not an ideal model, but
 * does allow for enums themselves to be abstracted away from a particular platform.
 */
public abstract class EnumConvertor<Abstracted extends Enum, Concrete extends Enum> {
	
	private Class<? extends Abstracted> abstractedClass;
	private Class<? extends Concrete> concreteClass;
	
	/**
	 * This is changed reflectively by the startup mechanism. Please do not
	 * change the name of this variable.
	 */
	private boolean useError = true;
	protected EnumConvertor(){
		abstractionenum annotation = this.getClass().getAnnotation(abstractionenum.class);
		if(annotation == null){
			throw new Error(this.getClass() + " is not annotated with @abstractionenum.");
		}
		
		this.abstractedClass = (Class<Abstracted>)annotation.forAbstractEnum();
		this.concreteClass = (Class<Concrete>)annotation.forConcreteEnum();
	}
	
	/**
	 * Given a concrete Enum, returns the abstract version. This is generally
	 * called in platform specific code. The platform is given a platform specific
	 * enum, and it needs to return control to the abstract code, so it calls
	 * MyEnumConvertor.getConverter().getAbstractedEnum(PlatformSpecificEnum.VALUE),
	 * which in turn abstractly handles the conversion from platform to abstract enum.
	 * @param concrete The concrete, platform specific enum
	 * @return The abstract, platform independent enum
	 * @throws IllegalArgumentException If the enum lookup failed
	 */
	public final Abstracted getAbstractedEnum(Concrete concrete) {
		if(concrete == null){
			return null;
		}
		try{
			return getAbstractedEnumCustom(concrete);
		} catch(IllegalArgumentException e){
			doLog(concreteClass, abstractedClass, concrete);
			return null;
		}
	}
	
	/**
	 * Can be overridden by subclasses that have a non 1:1 mapping. It should
	 * return the abstract enum, given a concrete enum. This should be used in the case
	 * where the heuristic isn't valid.
	 * @param concrete The concrete enum
	 * @return The abstract enum
	 * should be taken.
	 */
	protected Abstracted getAbstractedEnumCustom(Concrete concrete) throws IllegalArgumentException {
		return (Abstracted) Enum.valueOf(abstractedClass, concrete.name());
	}
	
	/**
	 * Given an abstract Enum, returns the concrete version. This is generally
	 * called in platform specific code. The platform is given an abstract
	 * enum, and it needs to convert to the platform specific enum, so it calls
	 * MyEnumConvertor.getConverter().getConcreteEnum(AbstractEnum.VALUE),
	 * which in turn abstractly handles the conversion from abstract to platform enum.
	 * @param abstracted The abstract, platform independent enum
	 * @return The concrete, platform specific enum
	 * @throws IllegalArgumentException If the enum lookup failed
	 */
	public final Concrete getConcreteEnum(Abstracted abstracted){
		if(abstracted == null){
			return null;
		}
		try{
			return getConcreteEnumCustom(abstracted);
		} catch(IllegalArgumentException e){
			doLog(abstractedClass, concreteClass, abstracted);
			return null;
		}
	}
	
	/**
	 * Can be overridden by subclasses that have a non 1:1 mapping. It should
	 * return the concrete enum, given an abstract enum. This should be used in the case
	 * where the heuristic isn't valid.
	 * @param abstracted The abstract enum
	 * @return The concrete enum
	 * should be taken.
	 */
	protected Concrete getConcreteEnumCustom(Abstracted abstracted) throws IllegalArgumentException {
		return (Concrete) Enum.valueOf(concreteClass, abstracted.name());
	}
	
	private void doLog(Class from, Class to, Enum value){
		String message = "When trying to convert " + from.getName() + "." + value.name() + " to a "
				+ to.getName() + ", no match was found. This may be caused by an old plugin version, or a newer server version.";
		LogLevel level = LogLevel.WARNING;
		if(useError){
			level = LogLevel.ERROR;
		} else {
			message += " This may or may not cause further problems during runtime.";
		}
		CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, level, message, Target.UNKNOWN);
	}
}
