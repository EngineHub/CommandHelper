package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.Common.Annotations.AggressiveDeprecation;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.Map;

/**
 * Use PrefilterMatchers instead. This class was deprecated on 2022/03/08, and will remain supported for no less than
 * 1 year or until version >= 3.3.6, or until all core prefilters are swapped to the new mechanism.
 * Instead of using this class, use declarative matchers. Events can override the
 * {@link Event#getPrefilters()} method to integrate into this new system. Docs for the prefilters should be replaced
 * with empty brackets, and as a separate deprecation phase, those will also be removed.
 *
 * @deprecated
 */
@Deprecated
public final class Prefilters {

	private Prefilters() {
	}

	/**
	 * Use PrefilterMatchers instead.
	 *
	 * @deprecated
	 */
	@Deprecated
	public enum PrefilterType {
		/**
		 * Checks if indexes 'x', 'y', 'z' and 'world' (or 0, 1, 2, 3) of a location array match. The location is
		 * matched via block matching, for instance if the array's x parameter is 1, 1.3 will match.
		 */
		LOCATION_MATCH,
		/**
		 * Simple boolean match.
		 */
		BOOLEAN_MATCH,
		/**
		 * String matches are just exact string matches.
		 */
		STRING_MATCH,
		/**
		 * Math match parses numbers out and checks to see if the numbers are equivalent. i.e. 1.0 does equal 1.
		 */
		MATH_MATCH,
		/**
		 * Regexes allow for more complex matching. A full blown regular expression is accepted as the argument.
		 */
		REGEX,
		/**
		 * An expression allows for more complex numerical matching. Similar to a regex, but designed for numerical
		 * values. This requires WorldEdit in plugins, lib, or in the server root to function.
		 */
		EXPRESSION,
		/**
		 * A macro expression allows for either an exact string match, or a regular expression, or an expression. It is
		 * parsed according to the format of the prefilter. In general, this should be used most often for things that
		 * are not definitively another type, so as to give scripts more flexibility.
		 */
		MACRO
	}

	@Deprecated
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7")
	public static void match(Map<String, Mixed> map, String key,
			String actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, actualValue, type, null);
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static void match(Map<String, Mixed> map, String key,
			String actualValue, PrefilterType type, Environment env) throws PrefilterNonMatchException {
		match(map, key, new CString(actualValue, Target.UNKNOWN), type, env);
	}

	@Deprecated
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7")
	public static void match(Map<String, Mixed> map, String key,
			int actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, actualValue, type, null);
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static void match(Map<String, Mixed> map, String key,
			int actualValue, PrefilterType type, Environment env) throws PrefilterNonMatchException {
		match(map, key, new CInt(actualValue, Target.UNKNOWN), type, env);
	}

	@Deprecated
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7")
	public static void match(Map<String, Mixed> map, String key,
			double actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, actualValue, type, null);
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static void match(Map<String, Mixed> map, String key,
			double actualValue, PrefilterType type, Environment env) throws PrefilterNonMatchException {
		match(map, key, new CDouble(actualValue, Target.UNKNOWN), type, env);
	}

	@Deprecated
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7")
	public static void match(Map<String, Mixed> map, String key,
			boolean actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, actualValue, type, null);
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static void match(Map<String, Mixed> map, String key,
			boolean actualValue, PrefilterType type, Environment env) throws PrefilterNonMatchException {
		match(map, key, CBoolean.get(actualValue), type, env);
	}

	@Deprecated
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7")
	public static void match(Map<String, Mixed> map, String key,
			MCLocation actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, actualValue, type, null);
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static void match(Map<String, Mixed> map, String key,
			MCLocation actualValue, PrefilterType type, Environment env) throws PrefilterNonMatchException {
		match(map, key, ObjectGenerator.GetGenerator().location(actualValue, false, env), type, env);
	}

	@Deprecated
	@AggressiveDeprecation(deprecationDate = "2022-04-06", removalVersion = "3.3.7")
	public static void match(Map<String, Mixed> map, String key,
			Mixed actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, actualValue, type, null);
	}

	/**
	 * Given a prototype and the actual user provided value, determines if it matches. If it doesn't, it throws an
	 * exception. If the value is not provided, or it does match, it returns void, which means that the test passed, and
	 * the event matches.
	 * @deprecated Use PrefilterMatchers instead
	 */
	@Deprecated
	public static void match(Map<String, Mixed> map, String key,
			Mixed actualValue, PrefilterType type, Environment env) throws PrefilterNonMatchException {
		if(map.containsKey(key)) {
			switch(type) {
				case STRING_MATCH:
					StringMatch(map.get(key).val(), actualValue.val());
					break;
				case MATH_MATCH:
					MathMatch(map.get(key), actualValue, env);
					break;
				case EXPRESSION:
					Mixed exp = map.get(key);
					if(!exp.val().isEmpty()
							&& exp.val().charAt(0) == '(' && exp.val().charAt(exp.val().length() - 1) == ')') {
						ExpressionMatch(exp, key, actualValue, env);
					} else {
						throw new CREFormatException("Prefilter expecting expression type, and \""
								+ exp.val() + "\" does not follow expression format. "
								+ "(Did you surround it in parenthesis?)", exp.getTarget());
					}
					break;
				case REGEX:
					String regex = map.get(key).val();
					if(!regex.isEmpty()
							&& regex.charAt(0) == '/' && regex.charAt(regex.length() - 1) == '/') {
						RegexMatch(regex, actualValue);
					} else {
						throw new CREFormatException("Prefilter expecting regex type, and \""
								+ regex + "\" does not follow regex format", map.get(key).getTarget());
					}
					break;
				case MACRO:
					MacroMatch(key, map.get(key), actualValue, env);
					break;
				case BOOLEAN_MATCH:
					BooleanMatch(map.get(key), actualValue, env);
					break;
				case LOCATION_MATCH:
					LocationMatch(map.get(key), actualValue, env);
					break;
			}
		}
	}

	private static void BooleanMatch(Mixed bool1, Mixed bool2, Environment env) throws PrefilterNonMatchException {
		if(ArgumentValidation.getBoolean(bool1, Target.UNKNOWN, env) != ArgumentValidation.getBoolean(bool2, Target.UNKNOWN, env)) {
			throw new PrefilterNonMatchException();
		}
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static boolean FastLocationMatch(Mixed location1, MCLocation location2, Environment env) {
		MCLocation l1 = ObjectGenerator.GetGenerator().location(location1, null, location1.getTarget(), env);
		MCLocation l2 = location2;
		return !((!l1.getWorld().equals(l2.getWorld())) || (l1.getBlockX() != l2.getBlockX()) || (l1.getBlockY() != l2.getBlockY()) || (l1.getBlockZ() != l2.getBlockZ()));
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	private static void LocationMatch(Mixed location1, Mixed location2, Environment env) throws PrefilterNonMatchException {
		if(!FastLocationMatch(location1, ObjectGenerator.GetGenerator().location(location2, null, Target.UNKNOWN, env), env)) {
			throw new PrefilterNonMatchException();
		}
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static boolean FastStringMatch(String string1, String string2) {
		return string1.equals(string2);
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	private static void StringMatch(String string1, String string2) throws PrefilterNonMatchException {
		if(!FastStringMatch(string1, string2)) {
			throw new PrefilterNonMatchException();
		}
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	private static void MathMatch(Mixed one, Mixed two, Environment env) throws PrefilterNonMatchException {
		try {
			double dOne = ArgumentValidation.getNumber(one, Target.UNKNOWN, env);
			double dTwo = ArgumentValidation.getNumber(two, Target.UNKNOWN, env);
			if(dOne != dTwo) {
				throw new PrefilterNonMatchException();
			}
		} catch (ConfigRuntimeException e) {
			throw new PrefilterNonMatchException();
		}
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static boolean FastExpressionMatch(String expression, String key, double dvalue, Target t) {
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

	private static void ExpressionMatch(Mixed expression, String key, Mixed dvalue, Environment env) throws PrefilterNonMatchException {
		if(!FastExpressionMatch(expression.val(), key, ArgumentValidation.getDouble(dvalue, Target.UNKNOWN, env), expression.getTarget())) {
			throw new PrefilterNonMatchException();
		}
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static boolean FastRegexMatch(String regex, String value) {
		regex = regex.substring(1, regex.length() - 1);
		return value.matches(regex);
	}

	private static void RegexMatch(String regex, Mixed value) throws PrefilterNonMatchException {
		if(!FastRegexMatch(regex, value.val())) {
			throw new PrefilterNonMatchException();
		}
	}

	/**
	 * Use PrefilterMatchers instead.
	 * @deprecated
	 */
	@Deprecated
	public static boolean FastMacroMatch(String key, String expression, Object javaObject, Target t) {
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

	private static void MacroMatch(String key, Mixed expression, Mixed value, Environment env) throws PrefilterNonMatchException {
		Object javaObject = value.val();
		if(value.isInstanceOf(CNumber.TYPE, null, env)) {
			javaObject = ArgumentValidation.getNumber(value, Target.UNKNOWN, env);
		}
		if(!FastMacroMatch(key, expression.val(), javaObject, expression.getTarget())) {
			throw new PrefilterNonMatchException();
		}
	}
}
