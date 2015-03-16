

package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

import java.util.Map;

/**
 *
 * 
 */
public final class Prefilters {
    
    private Prefilters(){}
    
    public enum PrefilterType{
        /**
         * Item matches are fuzzy matches for item notation. Red wool and black wool
         * will match. Essentially, this match ignores the item's data value when
         * comparing.
         */
		ITEM_MATCH,
		/**
		 * Checks if indexes 'x', 'y', 'z' and 'world' (or 0, 1, 2, 3) of a location array match.
		 * The location is matched via block matching, for instance if the array's x parameter is 1, 1.3 will match.
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
         * Math match parses numbers out and checks to see if the numbers
         * are equivalent. i.e. 1.0 does equal 1.
         */
        MATH_MATCH,
        /**
         * Regexes allow for more complex matching. A full blown regular expression
         * is accepted as the argument.
         */
        REGEX,
        /**
         * An expression allows for more complex numerical matching. Similar to a regex,
         * but designed for numerical values. This requires WorldEdit in plugins, lib,
		 * or in the server root to function.
         */
        EXPRESSION,
        /**
         * A macro expression allows for either an exact string match, or a regular expression,
         * or an expression. It is parsed according to the format of the prefilter. In
         * general, this should be used most often for things that are not definitively
         * another type, so as to give scripts more flexibility.
         */
        MACRO
    }
    
    public static void match(Map<String, Construct> map, String key,
            String actualValue, PrefilterType type) throws PrefilterNonMatchException{
        match(map, key, new CString(actualValue, Target.UNKNOWN), type);
    }
    
    public static void match(Map<String, Construct> map, String key,
            int actualValue, PrefilterType type) throws PrefilterNonMatchException{
        match(map, key, new CInt(actualValue, Target.UNKNOWN), type);
    }
    
    public static void match(Map<String, Construct> map, String key,
            double actualValue, PrefilterType type) throws PrefilterNonMatchException{
        match(map, key, new CDouble(actualValue, Target.UNKNOWN), type);
    }
    
	public static void match(Map<String, Construct> map, String key,
			boolean actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, CBoolean.get(actualValue), type);
	}
    
	public static void match(Map<String, Construct> map, String key,
			MCLocation actualValue, PrefilterType type) throws PrefilterNonMatchException {
		match(map, key, ObjectGenerator.GetGenerator().location(actualValue, false), type);
	}
    
    /**
     * Given a prototype and the actual user provided value, determines if it matches.
     * If it doesn't, it throws an exception. If the value is not provided, or it does
     * match, it returns void, which means that the test passed, and the event matches.
     */
    public static void match(Map<String, Construct> map, String key,
            Construct actualValue, PrefilterType type) throws PrefilterNonMatchException{
        if(map.containsKey(key)){
            switch(type){
                case ITEM_MATCH:
                    ItemMatch(map.get(key), actualValue);
                    break;
                case STRING_MATCH:
                    StringMatch(map.get(key).val(), actualValue.val());
                    break;
                case MATH_MATCH:
                    MathMatch(map.get(key), actualValue);
                    break;
                case EXPRESSION:
                    ExpressionMatch(MathReplace(key, map.get(key), actualValue), actualValue);
                    break;
                case REGEX:
                    RegexMatch(map.get(key), actualValue);
                    break;
                case MACRO:
                    MacroMatch(key, map.get(key), actualValue);
					break;
				case BOOLEAN_MATCH:
					BooleanMatch(map.get(key), actualValue);
					break;
				case LOCATION_MATCH:
					LocationMatch(map.get(key), actualValue);
					break;
            }
        }
    }
    
    private static void ItemMatch(Construct item1, Construct item2) throws PrefilterNonMatchException{
        String i1 = item1.val();
        String i2 = item2.val();
        if(item1.val().contains(":")){
            String[] split = item1.val().split(":");
            i1 = split[0].trim();
        }
        if(item2.val().contains(":")){
            String[] split = item2.val().split(":");
            i2 = split[0].trim();
        }
        if(!i1.trim().equalsIgnoreCase(i2.trim())){
            throw new PrefilterNonMatchException();
        }
    }

	private static void BooleanMatch(Construct bool1, Construct bool2) throws PrefilterNonMatchException {
		if (Static.getBoolean(bool1) != Static.getBoolean(bool2)) {
			throw new PrefilterNonMatchException();
		}
	}

	private static void LocationMatch(Construct location1, Construct location2) throws PrefilterNonMatchException {
		MCLocation l1 = ObjectGenerator.GetGenerator().location(location1, null, location1.getTarget());
		MCLocation l2 = ObjectGenerator.GetGenerator().location(location2, null, Target.UNKNOWN);
		if ((!l1.getWorld().equals(l2.getWorld())) || (l1.getBlockX() != l2.getBlockX()) || (l1.getBlockY() != l2.getBlockY()) || (l1.getBlockZ() != l2.getBlockZ())) {
			throw new PrefilterNonMatchException();
		}
	}

    private static void StringMatch(String string1, String string2) throws PrefilterNonMatchException{
        if(!string1.equals(string2)){
            throw new PrefilterNonMatchException();
        }
    }
    
    private static void MathMatch(Construct one, Construct two) throws PrefilterNonMatchException{
        try{
            double dOne = Static.getNumber(one, Target.UNKNOWN);
            double dTwo = Static.getNumber(two, Target.UNKNOWN);
            if(dOne != dTwo){
                throw new PrefilterNonMatchException();
            }
        } catch(ConfigRuntimeException e){
            throw new PrefilterNonMatchException();
        }
    }
    
    private static void ExpressionMatch(Construct expression, Construct dvalue) throws PrefilterNonMatchException{
        if(expression.val().matches("\\(.*\\)")){
            String exp = expression.val().substring(1, expression.val().length() - 1);
            boolean inequalityMode = false;
            if(exp.contains("<") || exp.contains(">") || exp.contains("==")){
                inequalityMode = true;
            }
			String eClass = "com.sk89q.worldedit.internal.expression.Expression";
			String errClass = "com.sk89q.worldedit.internal.expression.ExpressionException";
			Class eClazz, errClazz;
			try {
				eClazz = Class.forName(eClass);
				errClazz = Class.forName(errClass);
			} catch (ClassNotFoundException cnf) {
				throw new ConfigRuntimeException("You are missing a required dependency: " + eClass,
						ExceptionType.PluginInternalException, expression.getTarget(), cnf);
			}
			try {
				Object e = ReflectionUtils.invokeMethod(eClazz, null, "compile",
						new Class[]{String.class}, new Object[]{exp});
				double val = (double) ReflectionUtils.invokeMethod(eClazz, e, "evaluate");
				if (inequalityMode) {
					if (val == 0) {
						throw new PrefilterNonMatchException();
					}
				} else {
					if (val != Static.getDouble(dvalue, Target.UNKNOWN)) {
						throw new PrefilterNonMatchException();
					}
				}
			} catch (ReflectionUtils.ReflectionException rex) {
				if (rex.getCause().getClass().isAssignableFrom(errClazz)) {
					throw new ConfigRuntimeException("Your expression was invalidly formatted",
							ExceptionType.PluginInternalException, expression.getTarget(), rex.getCause());
				} else {
					throw new ConfigRuntimeException(rex.getMessage(), ExceptionType.PluginInternalException,
							expression.getTarget(), rex.getCause());
				}
			}
        } else {
            throw new ConfigRuntimeException("Prefilter expecting expression type, and \"" 
                    + expression.val() + "\" does not follow expression format. "
                    + "(Did you surround it in parenthesis?)", 
                    ExceptionType.FormatException, expression.getTarget());
        }
    }
    
    private static void RegexMatch(Construct expression, Construct value) throws PrefilterNonMatchException{
        if(expression.val().matches("/.*/")){
            String exp = expression.val().substring(1, expression.val().length() - 1);
            if(!value.val().matches(exp)){
                throw new PrefilterNonMatchException();
            }
        } else {
            throw new ConfigRuntimeException("Prefilter expecting regex type, and \"" 
                    + expression.val() + "\" does not follow regex format", ExceptionType.FormatException, expression.getTarget());
        }
    }
    
    private static void MacroMatch(String key, Construct expression, Construct value) throws PrefilterNonMatchException{
        if(expression.val().matches("\\(.*\\)")){
            ExpressionMatch(MathReplace(key, expression, value), value);
        } else if(expression.val().matches("/.*/")){
            RegexMatch(expression, value);
        } else {
            StringMatch(expression.val(), value.val());
        }
    }
    
    private static Construct MathReplace(String key, Construct expression, Construct value){
        return new CString(expression.val().replaceAll(key, value.val()), expression.getTarget());
    }
}
