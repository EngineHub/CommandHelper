package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.net.URL;

/**
 * Exceptions should extend this class, which provides default implementations of various utility methods.
 */
public abstract class AbstractException extends ConfigRuntimeException implements Documentation {

//	public AbstractException(String msg, Target t){
//		super(msg, tempGetExceptionType(), t);
//	}

	private final static Class[] EMPTY_CLASS = new Class[0];

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		seealso see = this.getClass().getAnnotation(seealso.class);
		if(see == null){
			return EMPTY_CLASS;
		} else {
			return see.value();
		}
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	@Override
	public String getName() {
		typeof to = this.getClass().getAnnotation(typeof.class);
		if(to == null){
			throw new Error("ConfigRuntimeException subtypes must annotate themselves with @typeof, if they are instantiateable.");
		} else {
			return to.value();
		}
	}

}
