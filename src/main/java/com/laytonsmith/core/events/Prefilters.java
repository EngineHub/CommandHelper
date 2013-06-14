

package com.laytonsmith.core.events;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import java.util.Map;

/**
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
         * String matches are just exact string matches
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
         * but designed for numerical values.
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
    
    public static void match(Map<String, Mixed> map, String key,
            String actualValue, PrefilterType type) throws PrefilterNonMatchException{
        match(map, key, new CString(actualValue, Target.UNKNOWN), type);
    }
    
    public static void match(Map<String, Mixed> map, String key,
            int actualValue, PrefilterType type) throws PrefilterNonMatchException{
        match(map, key, new CInt(actualValue, Target.UNKNOWN), type);
    }
    
    public static void match(Map<String, Mixed> map, String key,
            double actualValue, PrefilterType type) throws PrefilterNonMatchException{
        match(map, key, new CDouble(actualValue, Target.UNKNOWN), type);
    }
    
    /**
     * Given a prototype and the actual user provided value, determines if it matches.
     * If it doesn't, it throws an exception. If the value is not provided, or it does
     * match, it returns void, which means that the test passed, and the event matches.
     */
    public static void match(Map<String, Mixed> map, String key,
            Mixed actualValue, PrefilterType type) throws PrefilterNonMatchException{
		Target t = actualValue.getTarget();
        if(map.containsKey(key)){
            switch(type){
                case ITEM_MATCH:
                    ItemMatch(map.get(key), actualValue);
                    break;
                case STRING_MATCH:
                    StringMatch(map.get(key).val(), actualValue.val());
                    break;
                case MATH_MATCH:
                    MathMatch(map.get(key).primitive(t).castToDouble(t), actualValue.primitive(t).castToDouble(t));
                    break;
                case EXPRESSION:
                    ExpressionMatch(MathReplace(key, map.get(key), actualValue), actualValue.primitive(t).castToDouble(t));
                    break;
                case REGEX:
                    RegexMatch(map.get(key), actualValue);
                    break;
                case MACRO:
                    MacroMatch(key, map.get(key), actualValue.primitive(t), t);
            }
        }
    }
    
    private static void ItemMatch(Mixed item1, Mixed item2) throws PrefilterNonMatchException{
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
    
    private static void StringMatch(String string1, String string2) throws PrefilterNonMatchException{
        if(!string1.equals(string2)){
            throw new PrefilterNonMatchException();
        }
    }
    
    private static void MathMatch(double one, double two) throws PrefilterNonMatchException{
        try{
            double dOne = one;
            double dTwo = two;
            if(dOne != dTwo){
                throw new PrefilterNonMatchException();
            }
        } catch(ConfigRuntimeException e){
            throw new PrefilterNonMatchException();
        }
    }
    
    private static void ExpressionMatch(Mixed expression, double dvalue) throws PrefilterNonMatchException{
        if(expression.val().matches("\\(.*\\)")){
            String exp = expression.val().substring(1, expression.val().length() - 1);
            boolean inequalityMode = false;
            if(exp.contains("<") || exp.contains(">") || exp.contains("==")){
                inequalityMode = true;
            }
            try{
                double val = Expression.compile(exp).evaluate();
                if(inequalityMode){
                    if(val == 0){
                        throw new PrefilterNonMatchException();
                    }
                } else {
                    if(val != dvalue){
                        throw new PrefilterNonMatchException();
                    }
                }
            } catch(ExpressionException e){
                throw new ConfigRuntimeException("Your expression is invalidly formatted", 
                        ExceptionType.FormatException, expression.getTarget());
            }
        } else {
            throw new ConfigRuntimeException("Prefilter expecting expression type, and \"" 
                    + expression.val() + "\" does not follow expression format. "
                    + "(Did you surround it in parenthesis?)", 
                    ExceptionType.FormatException, expression.getTarget());
        }
    }
    
    private static void RegexMatch(Mixed expression, Mixed value) throws PrefilterNonMatchException{
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
    
    private static void MacroMatch(String key, Mixed expression, CPrimitive value, Target t) throws PrefilterNonMatchException{
        if(expression.val().matches("\\(.*\\)")){
            ExpressionMatch(MathReplace(key, expression, value), value.castToDouble(t));
        } else if(expression.val().matches("/.*/")){
            RegexMatch(expression, value);
        } else {
            StringMatch(expression.val(), value.val());
        }
    }
    
    private static Construct MathReplace(String key, Mixed expression, Mixed value){
        return new CString(expression.val().replaceAll(key, value.val()), expression.getTarget());
    }
}
