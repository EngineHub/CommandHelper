package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
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
 * @param <T>
 */
public abstract class ExpressionPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class ExpressionPrefilterDocs implements PrefilterDocs {
		@Override
		public String getName() {
			return "expression match";
		}


		@Override
		public String getNameWiki() {
			return "[[Prefilters#expression match|Expression]]";
		}

		@Override
		public String docs() {
			return "An expression allows for a range or complex mathematical expression to be provided. This uses"
					+ " the WorldEdit expression format, which is documented here. https://worldedit.enginehub.org/en/latest/usage/other/expressions/"
					+ " The prefilter name is assigned the value at resolution time. For instance, if the prefilter name"
					+ " were \"height\", then you might provide the following prefilter: array(height: \"height > 50\").";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new ExpressionPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {

	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t) {
		String expression = value.val();
		double dvalue = getProperty(event);
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

	protected abstract double getProperty(T event);

}
