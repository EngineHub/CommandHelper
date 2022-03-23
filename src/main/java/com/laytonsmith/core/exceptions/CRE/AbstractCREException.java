package com.laytonsmith.core.exceptions.CRE;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exceptions should extend this class, which provides default implementations of various utility methods.
 */
public abstract class AbstractCREException extends ConfigRuntimeException implements Documentation, Mixed, ArrayAccess {

	private static final Class[] EMPTY_CLASS = new Class[0];

	private List<StackTraceElement> stackTrace = null;

	private final Map<CClassType, GenericParameters> genericParameters = new HashMap<>();

	public AbstractCREException(String msg, Target t) {
		super(msg, t);
	}

	public AbstractCREException(String msg, Target t, Throwable cause) {
		super(msg, t, cause);
	}

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		seealso see = this.getClass().getAnnotation(seealso.class);
		if(see == null) {
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
		typeof to = ClassDiscovery.GetClassAnnotation(this.getClass(), typeof.class);
		if(to == null) {
			throw new Error("ConfigRuntimeException subtypes must annotate themselves with @typeof, if they are"
					+ " instantiateable.");
		} else {
			return to.value();
		}
	}

	@Override
	public AccessModifier getAccessModifier() {
		return AccessModifier.PUBLIC;
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.noneOf(ObjectModifier.class);
	}

	/**
	 * Alias for {@link #getName() }
	 *
	 * @param env
	 * @return
	 */
	public CClassType getExceptionType(Environment env) {
		return Construct.typeof(this, env);
	}

	/**
	 * Returns the name of the exception. If the exception is an instanceof AbstractCREException, this is equivalent to
	 * calling {@link #getName()}. Otherwise, the java class name is returned.
	 *
	 * @param ex
	 * @return
	 */
	public static String getExceptionName(ConfigRuntimeException ex) {
		if(ex instanceof AbstractCREException) {
			return ((AbstractCREException) ex).getName();
		} else {
			return ex.getClass().getName();
		}
	}

	private CArray exceptionObject = null;

	/**
	 * Returns a standardized CArray given this exception.
	 *
	 * @return
	 */
	public CArray getExceptionObject(Environment env) {
		CArray ret = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
		ret.set("classType", this.getExceptionType(env), Target.UNKNOWN, env);
		ret.set("message", this.getMessage(), env);
		CArray stackTrace = new CArray(Target.UNKNOWN, null, env);
		ret.set("stackTrace", stackTrace, Target.UNKNOWN, env);
		for(StackTraceElement e : this.getCREStackTrace()) {
			CArray element = e.getObjectFor(env);
			stackTrace.push(element, Target.UNKNOWN, env);
		}
		ret.set("causedBy", getCausedBy(this.getCause()), Target.UNKNOWN, env);
		return ret;
	}

	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
	public static AbstractCREException getFromCArray(CArray exception, Target t, Environment env)
			throws ClassNotFoundException {
		FullyQualifiedClassName classType = FullyQualifiedClassName
				.forName(exception.get("classType", t, env).val(), t, env);
		Class<? extends Mixed> clzz = NativeTypeList.getNativeClass(classType);
		Throwable cause = null;
		if(exception.get("causedBy", t, env).isInstanceOf(CArray.TYPE, null, env)) {
			// It has a cause
			cause = new CRECausedByWrapper((CArray) exception.get("causedBy", t, env));
		}
		String message = exception.get("message", t, env).val();
		List<StackTraceElement> st = new ArrayList<>();
		for(Mixed consStElement : ArgumentValidation.getArray(exception.get("stackTrace", t, env), t, env).asList(env)) {
			CArray stElement = ArgumentValidation.getArray(consStElement, t, env);
			int line = ArgumentValidation.getInt32(stElement.get("line", t, env), t, env);
			File f = new File(stElement.get("file", t, env).val());
			int col = ArgumentValidation.getInt32(stElement.get("col", t, env), t, env);
			st.add(new StackTraceElement(stElement.get("id", t, env).val(), new Target(line, f, col)));
		}
		// Now we have parsed everything into POJOs
		Class[] types = new Class[]{String.class, Target.class, Throwable.class};
		Object[] args = new Object[]{message, t, cause};
		AbstractCREException ex = (AbstractCREException) ReflectionUtils.newInstance(clzz, types, args);
		ex.stackTrace = st;
		return ex;
	}

	private static Mixed getCausedBy(Throwable causedBy) {
		if(causedBy == null || !(causedBy instanceof CRECausedByWrapper)) {
			return CNull.NULL;
		}
		CRECausedByWrapper cre = (CRECausedByWrapper) causedBy;
		CArray ret = cre.getException();
		return ret;
	}

	/**
	 * Casts the CRE to an AbstractCREException, or throws an error if it is not convertable.
	 *
	 * @param ex
	 * @return
	 */
	public static AbstractCREException getAbstractCREException(ConfigRuntimeException ex) {
		if(ex instanceof AbstractCREException) {
			return (AbstractCREException) ex;
		}
		throw new Error("Unexpected CRE exception that isn't convertable to AbstractCREException");
	}

	@Override
	public String val() {
		return getName() + ":" + getMessage();
	}

	/**
	 * These methods are required because we don't actually want to extend CArray, we want to be our own class of Object
	 * (at least for now). These basically just call through to our underlying CArray though.
	 *
	 * @param index
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	@Override
	public Mixed get(String index, Target t, Environment env) throws ConfigRuntimeException {
		return exceptionObject.get(index, t, env);
	}

	@Override
	public Mixed get(int index, Target t, Environment env) throws ConfigRuntimeException {
		return exceptionObject.get(index, t, env);
	}

	@Override
	public Mixed get(Mixed index, Target t, Environment env) throws ConfigRuntimeException {
		return exceptionObject.get(index, t, env);
	}

	@Override
	public Set<Mixed> keySet(Environment env) {
		return exceptionObject.keySet(env);
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
	public Mixed slice(int begin, int end, Target t, Environment env) {
		return exceptionObject.slice(begin, end, t, env);
	}

	@Override
	public AbstractCREException clone() throws CloneNotSupportedException {
		AbstractCREException obj = (AbstractCREException) super.clone();

		return obj;
	}

	/**
	 * Freezes the stack trace. This should be called when an element that has added a frame to the stack
	 * trace gets an AbstractCREException thrown. This will record the current stack trace, which is then
	 * returned by {@link #getCREStackTrace()}. If this isn't called, then the stack trace will continue
	 * to change as the elements are popped, leading to an invalid stacktrace.
	 *
	 * Exception handlers up the stack should always call this method anyways, only the first call will
	 * cause the stacktrace to be recorded, and there's no way for up stack code to know if it had already
	 * been called.
	 * @param manager
	 */
	public void freezeStackTraceElements(StackTraceManager manager) {
		if(this.stackTrace == null) {
			this.stackTrace = manager.getCurrentStackTrace();
		}
	}

	/**
	 * NOTE!!! This is probably not what you're looking for, you're probably looking for
	 * {@link #freezeStackTraceElements(com.laytonsmith.core.exceptions.StackTraceManager)}.
	 * <p>
	 * Sets the stacktrace if it is not already frozen. For asynchronous processing, it may sometimes be needed
	 * to get the stacktrace before going asynchronous, for if an exception is thrown in the asynchronous code,
	 * at which point, this can be set in the newly generated exception.
	 * <p>
	 * If the stacktrace was already set, this is an Error, because this should never happen in the usual case.
	 * @param st
	 */
	public void setStackTraceElements(List<StackTraceElement> st) {
		if(this.stackTrace != null) {
			throw new RuntimeException("The stacktrace was already set, and it cannot be set again");
		}
		this.stackTrace = st;
	}

	public List<StackTraceElement> getCREStackTrace() {
		if(this.stackTrace == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(this.stackTrace);
	}

	@Override
	public Version since() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String docs() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CClassType[] getInterfaces() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CClassType[] getSuperclasses() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInstanceOf(CClassType type, LeftHandGenericUse lhs, Environment env) {
		return InstanceofUtil.isInstanceof(this, LeftHandSideType.fromCClassType(type, lhs, Target.UNKNOWN), env);
	}

	@Override
	public CClassType typeof(Environment env) {
		return Construct.typeof(this, env);
	}

	@Override
	public final Map<CClassType, GenericParameters> getGenericParameters() {
		if(genericParameters.isEmpty()) {
			return null;
		} else {
			return genericParameters;
		}
	}

	protected final void registerGenerics(CClassType type, GenericParameters genericParameters) {
		this.genericParameters.put(type, genericParameters);
	}

	@Override
	public boolean getBooleanValue(Environment env, Target t) {
		return true;
	}

}
