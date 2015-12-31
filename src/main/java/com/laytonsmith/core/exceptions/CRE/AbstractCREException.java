package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Exceptions should extend this class, which provides default implementations of various utility methods.
 */
public abstract class AbstractCREException extends ConfigRuntimeException implements Documentation, Mixed, ArrayAccess {

	private final static Class[] EMPTY_CLASS = new Class[0];

	public AbstractCREException(String msg, Target t){
		super(msg, t);
	}

	public AbstractCREException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

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

	/**
	 * Alias for {@link #getName() }
	 * @return
	 */
	public String getExceptionType(){
		return getName();
	}

	/**
	 * Returns the name of the exception. If the exception is an instanceof AbstractCREException,
	 * this is equivalent to calling {@link #getName() }. Otherwise, the java class name is returned.
	 * @param ex
	 * @return
	 */
	public static String getExceptionName(ConfigRuntimeException ex){
		if(ex instanceof AbstractCREException){
			return ((AbstractCREException)ex).getName();
		} else {
			return ex.getClass().getName();
		}
	}

	private CArray exceptionObject = null;

	/**
	 * Returns a standardized CArray given this exception.
	 * @return
	 */
	public CArray getExceptionObject(){
		if(exceptionObject == null){
			CArray ret = new CArray(Target.UNKNOWN);
			ret.set("classType", new CClassType(this.getName(), Target.UNKNOWN), Target.UNKNOWN);
			ret.set("message", this.getMessage());
			CArray stackTrace = new CArray(Target.UNKNOWN);
			ret.set("stackTrace", stackTrace, Target.UNKNOWN);
			for(StackTraceElement e : this.stackTraceTrail){
				CArray element = new CArray(Target.UNKNOWN);
				element.set("procedureName", e.getProcedureName());
				try {
					element.set("file", e.getDefinedAt().file().getCanonicalPath());
				} catch (IOException ex) {
					// This shouldn't happen, but if it does, we want to fall back to something marginally useful
					element.set("file", e.getDefinedAt().file().getAbsolutePath());
				}
				element.set("line", new CInt(e.getDefinedAt().line(), Target.UNKNOWN), Target.UNKNOWN);
			}
			exceptionObject = ret;
		}
		return exceptionObject;
	}

	/**
	 * Casts the CRE to an AbstractCREException, or throws an error if it is not convertable.
	 * @param ex
	 * @return
	 */
	public static AbstractCREException getAbstractCREException(ConfigRuntimeException ex){
		if(ex instanceof AbstractCREException){
			return (AbstractCREException)ex;
		}
		throw new Error("Unexpected CRE exception that isn't convertable to AbstractCREException");
	}

	@Override
	public String val() {
		return getName() + ":" + getMessage();
	}


	/**
	 * These methods are required because we don't actually want to extend CArray, we want to be our own
	 * class of Object (at least for now). These basically just call through to our underlying CArray though.
	 * @param index
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	@Override
	public Construct get(String index, Target t) throws ConfigRuntimeException {
		return exceptionObject.get(index, t);
	}

	@Override
	public Construct get(int index, Target t) throws ConfigRuntimeException {
		return exceptionObject.get(index, t);
	}

	@Override
	public Construct get(Construct index, Target t) throws ConfigRuntimeException {
		return exceptionObject.get(index, t);
	}

	@Override
	public Set<Construct> keySet() {
		return exceptionObject.keySet();
	}

	@Override
	public long size() {
		return exceptionObject.size();
	}

	@Override
	public boolean isAssociative() {
		return exceptionObject.isAssociative();
	}

	@Override
	public boolean canBeAssociative() {
		return exceptionObject.canBeAssociative();
	}

	@Override
	public Construct slice(int begin, int end, Target t) {
		return exceptionObject.slice(begin, end, t);
	}

	@Override
	public AbstractCREException clone() throws CloneNotSupportedException {
		AbstractCREException obj = (AbstractCREException)super.clone();

		return obj;
	}

}
