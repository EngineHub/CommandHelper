package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public abstract class ExpressionPrefilterMatcher<T extends BindableEvent> implements PrefilterMatcher<T> {

	@Override
	public String filterType() {
		return "expression match";
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {

	}

	@Override
	public boolean matches(Mixed value, T event, Target t) {
		String expression = value.val();
		String key = getKey();
		double dvalue = getProperty();
		String exp = expression.substring(1, expression.length() - 1);
		boolean inequalityMode = false;
		if(exp.contains("<") || exp.contains(">") || exp.contains("==")) {
			inequalityMode = true;
		}
		String eClass = "com.sk89q.worldedit.internal.expression.Expression";
		String errClass = "com.sk89q.worldedit.internal.expression.ExpressionException";
		Class eClazz;
		Class errClazz;
		try {
			eClazz = Class.forName(eClass);
			errClazz = Class.forName(errClass);
		} catch (ClassNotFoundException cnf) {
			throw new CREPluginInternalException("You are missing a required dependency: " + eClass, t, cnf);
		}
		try {
			Object e = ReflectionUtils.invokeMethod(eClazz, null, "compile",
					new Class[]{String.class, String[].class}, new Object[]{exp, new String[]{key}});
			double val = (double) ReflectionUtils.invokeMethod(eClazz, e, "evaluate",
					new Class[]{double[].class},
					new Object[]{new double[]{dvalue}});
			if(inequalityMode) {
				if(val == 0) {
					return false;
				}
			} else {
				if(val != dvalue) {
					return false;
				}
			}
		} catch (ReflectionUtils.ReflectionException rex) {
			if(rex.getCause().getClass().isAssignableFrom(errClazz)) {
				throw new CREPluginInternalException("Your expression was invalidly formatted", t, rex.getCause());
			} else {
				throw new CREPluginInternalException(rex.getMessage(),
						t, rex.getCause());
			}
		}
		return true;
	}

	/**
	 * The key is the prefilter name, which is what's is replaced in the expression. Unfortunately, there isn't a good
	 * way to provide this directly through the prefilter mechanisms without leaking the abstraction.
	 * @return
	 */
	protected abstract String getKey();

	protected abstract double getProperty();

}
