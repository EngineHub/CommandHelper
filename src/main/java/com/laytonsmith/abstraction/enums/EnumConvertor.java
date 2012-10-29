/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author Layton
 */
public abstract class EnumConvertor<Abstracted extends Enum, Concrete extends Enum> {
	
	private Class<? extends Abstracted> abstractedClass;
	private Class<? extends Concrete> concreteClass;
	
	/**
	 * This is changed reflectively by the startup mechanism.
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
	
	private static class UseDefault extends RuntimeException{ }
	
	public final Abstracted getAbstractedEnum(Concrete concrete){
		try{
			try{
				return getAbstractedEnumCustom(concrete);
			} catch(UseDefault e){
				return (Abstracted)Enum.valueOf(abstractedClass, concrete.name());
			}
		} catch(IllegalArgumentException e){
			doLog(concreteClass, abstractedClass, concrete);
			throw e;
		}
	}
	
	protected Abstracted getAbstractedEnumCustom(Concrete concrete){
		throw new UseDefault();
	}
	
	public final Concrete getConcreteEnum(Abstracted abstracted){
		try{
			try{
				return getConcreteEnumCustom(abstracted);
			} catch(UseDefault e){
				return (Concrete)Enum.valueOf(concreteClass, abstracted.name());		
			}
		} catch(IllegalArgumentException e){
			doLog(abstractedClass, concreteClass, abstracted);
			throw e;
		}
	}
	
	protected Concrete getConcreteEnumCustom(Abstracted abstracted){
		throw new UseDefault();
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
