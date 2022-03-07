package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import static com.laytonsmith.core.events.prefilters.Prefilters.FastExpressionMatch;
import static com.laytonsmith.core.events.prefilters.Prefilters.FastRegexMatch;
import static com.laytonsmith.core.events.prefilters.Prefilters.FastStringMatch;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @param <T>
 */
public abstract class MacroPrefilterMatcher<T extends BindableEvent> implements PrefilterMatcher<T> {

	@Override
	public String filterType() {
		return "macro match";
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {

	}

	@Override
	public boolean matches(Mixed value, T event, Target t) {
		String expression = value.val();
		String key = getKey();
		Object javaObject = getProperty();
		if(expression.isEmpty()) {
			return false;
		} else if(expression.charAt(0) == '(' && expression.charAt(expression.length() - 1) == ')') {
			try {
				return FastExpressionMatch(expression, key, (double) javaObject, t);
			} catch (ClassCastException ex) {
				throw new RuntimeException("Unexpected class type, please report this bug to the developers.", ex);
			}
		} else if(expression.charAt(0) == '/' && expression.charAt(expression.length() - 1) == '/') {
			return FastRegexMatch(expression, javaObject.toString());
		} else {
			return FastStringMatch(expression, javaObject.toString());
		}
	}

	protected abstract String getKey();

	/**
	 * The property to check against. While this returns Object, this should return either a double or a String.
	 * @return
	 */
	protected abstract Object getProperty();


}
