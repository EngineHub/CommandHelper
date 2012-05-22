/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.BasicLogic.equals_ic;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;

/**
 *
 * @author Layton
 */
public class ArrayHandling {
    public static String docs(){
        return "This class contains functions that provide a way to manipulate arrays. To create an array, use the <code>array</code> function."
                + " For more detailed information on array usage, see the page on [[CommandHelper/Arrays|arrays]]";
    }
    @api public static class array_size extends AbstractFunction{

        public String getName() {
            return "array_size";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                return new CInt(((CArray)args[0]).size(), t);
            }
            throw new ConfigRuntimeException("Argument 1 of array_size must be an array", ExceptionType.CastException, t);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "int {array} Returns the size of this array as an integer.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Boolean runAsync() {
            return null;
        }
        
    }
    
    @api public static class array_get extends AbstractFunction{

        public String getName() {
            return "array_get";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Construct index = new CSlice(0, -1, t);
            if(args.length == 2){
                index = args[1];
            }
            
            if(env.GetFlag("array_get_alt_mode") == Boolean.TRUE){                
                return new CArrayReference(args[0], args[1], env);
            }
            
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                if(index instanceof CSlice){
                    if(ca.inAssociativeMode()){
                        if(((CSlice)index).getStart() == 0 && ((CSlice)index).getFinish() == -1){
                            //Special exception, we want to clone the whole array
                            CArray na = new CArray(t);
                            na.forceAssociativeMode();
                            for(String key : ca.keySet()){                                
                                try {
                                    na.set(key, ca.get(key, t).clone());
                                } catch (CloneNotSupportedException ex) {
                                    na.set(key, ca.get(key, t));
                                }
                            }
                            return na;
                        }
                        throw new ConfigRuntimeException("Array slices are not allowed with an associative array", ExceptionType.CastException, t);
                    }
                    //It's a range
                    long start = ((CSlice)index).getStart();
                    long finish = ((CSlice)index).getFinish();
                    try{
                        //Convert negative indexes 
                        if(start < 0){
                            start = ca.size() + start;
                        }
                        if(finish < 0){
                            finish = ca.size() + finish;
                        }
                        CArray na = new CArray(t);
                        if(finish < start){
                            //return an empty array in cases where the indexes don't make sense
                            return na;
                        }
                        for(long i = start; i <= finish; i++){
                            try{
                                na.push(ca.get((int)i, t).clone());
                            } catch(CloneNotSupportedException e){
                                na.push(ca.get((int)i, t));
                            }
                        }
                        return na;
                    } catch(NumberFormatException e){
                        throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, t);
                    }
                } else {
                    if(!ca.inAssociativeMode()){
                        int iindex = (int)Static.getInt(args[1]);
                        if(iindex < 0){
                            //negative index, convert to positive index
                            iindex = ca.size() + iindex;
                        }
                        return ca.get(iindex, t);
                    } else {
                        return ca.get(args[1], t);
                    }
                }
            } else if(args[0] instanceof CString){
                if(index instanceof CSlice){
                    ArrayAccess aa = (ArrayAccess)args[0];
                    //It's a range
                    long start = ((CSlice)index).getStart();
                    long finish = ((CSlice)index).getFinish();
                    try{
                        //Convert negative indexes 
                        if(start < 0){
                            start = aa.val().length() + start;
                        }
                        if(finish < 0){
                            finish = aa.val().length() + finish;
                        }
                        CArray na = new CArray(t);
                        if(finish < start){
                            //return an empty array in cases where the indexes don't make sense
                            return new CString("", t);
                        }
                        StringBuilder b = new StringBuilder();
                        String val = aa.val();
                        for(long i = start; i <= finish; i++){
                            b.append(val.charAt((int)i));
                        }
                        return new CString(b.toString(), t);
                    } catch(NumberFormatException e){
                        throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, t);
                    }
                } else {
                    try{                        
                        return new CString(args[0].val().charAt((int)Static.getInt(index)), t);
                    } catch(ConfigRuntimeException e){
                        if(e.getExceptionType() == ExceptionType.CastException){
                            throw new ConfigRuntimeException("Expecting an integer index for the array, but found \"" + index
                                    + "\". (Array is not associative, and cannot accept string keys here.)", ExceptionType.CastException, t);
                        } else {
                            throw e;
                        }
                    } catch(StringIndexOutOfBoundsException e){
                        throw new ConfigRuntimeException("No index at " + args[0].val(), ExceptionType.RangeException, t);
                    }
                }
            } else if(args[0] instanceof ArrayAccess){
                throw new ConfigRuntimeException("Wat. How'd you get here? This isn't supposed to be implemented yet.", t);
            } else{
                throw new ConfigRuntimeException("Argument 1 of array_get must be an array", ExceptionType.CastException, t);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
        }

        public String docs() {
            return "mixed {array, index} Returns the element specified at the index of the array. If the element doesn't exist, an exception is thrown. "
                    + "array_get(array, index). Note also that as of 3.1.2, you can use a more traditional method to access elements in an array: "
                    + "array[index] is the same as array_get(array, index), where array is a variable, or function that is an array. In fact, the compiler"
                    + " does some magic under the covers, and literally converts array[index] into array_get(array, index), so if there is a problem "
                    + "with your code, you will get an error message about a problem with the array_get function, even though you may not be using "
                    + "that function directly.";
        }

        public boolean isRestricted() {
            return false;
        }
        
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public Boolean runAsync() {
            return null;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            if(args[0] instanceof ArrayAccess){
                ArrayAccess aa = (ArrayAccess)args[0];
                if(!aa.canBeAssociative()){
                    if(!(args[1] instanceof CInt) && !(args[1] instanceof CSlice)){
                        throw new ConfigCompileException("Accessing an element as an associative array, when it can only accept integers.", t);
                    }                    
                }
                return null;
            } else {
                throw new ConfigCompileException("Trying to access an element like an array, but it does not support array access.", t);
            }
            
        }                
        
    }
    
    @api public static class array_set extends AbstractFunction{

        public String getName() {
            return "array_set";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                try{
                ((CArray)args[0]).set(args[1], args[2]);
                } catch(IndexOutOfBoundsException e){
                    throw new ConfigRuntimeException("The index " + args[1].val() + " is out of bounds", ExceptionType.IndexOverflowException, t);
                }
                return new CVoid(t);
            }
            throw new ConfigRuntimeException("Argument 1 of array_set must be an array, and argument 2 must be an integer", ExceptionType.CastException, t);        
        }

        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.IndexOverflowException};
        }
        
        public String docs() {
            return "void {array, index, value} Sets the value of the array at the specified index. array_set(array, index, value). Returns void. If"
                    + " the element at the specified index isn't already set, throws an exception. Use array_push to avoid this.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class array_push extends AbstractFunction{

        public String getName() {
            return "array_push";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                if(args.length < 2){
                    throw new ConfigRuntimeException("At least 2 arguments must be provided to array_push", ExceptionType.InsufficientArgumentsException, t);
                }
                for(int i = 1; i < args.length; i++){
                    ((CArray)args[0]).push(args[i]);
                }
                return new CVoid(t);
            }
            throw new ConfigRuntimeException("Argument 1 of array_push must be an array", ExceptionType.CastException, t);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "void {array, value, [value2...]} Pushes the specified value(s) onto the end of the array";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public Boolean runAsync() {
            return null;
        }
        
    }
    @api public static class array_contains extends AbstractFunction {

        public String getName() {
            return "array_contains";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            equals e = new equals();
            if(args[0] instanceof CArray){
                CArray ca = (CArray) args[0];
                for(int i = 0; i < ca.size(); i++){
                    if(((CBoolean)e.exec(t, env, ca.get(i, t), args[1])).getBoolean()){
                        return new CBoolean(true, t);
                    }
                }
                return new CBoolean(false, t);
            } else {
                throw new ConfigRuntimeException("Argument 1 of array_contains must be an array", ExceptionType.CastException, t);
            }
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "boolean {array, testValue} Checks to see if testValue is in array.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        
        public Boolean runAsync() {
            return null;
        }
        
    }
    
    @api public static class array_contains_ic extends AbstractFunction{

        public String getName() {
            return "array_contains_ic";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {array, testValue} Works like array_contains, except the comparison ignores case.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            equals_ic e = new equals_ic();
            if(args[0] instanceof CArray){
                CArray ca = (CArray) args[0];
                for(int i = 0; i < ca.size(); i++){
                    if(((CBoolean)e.exec(t, environment, ca.get(i, t), args[1])).getBoolean()){
                        return new CBoolean(true, t);
                    }
                }
                return new CBoolean(false, t);
            } else {
                throw new ConfigRuntimeException("Argument 1 of array_contains_ic must be an array", ExceptionType.CastException, t);
            }
        }
        
    }
    
    @api public static class array_index_exists extends AbstractFunction{

        public String getName() {
            return "array_index_exists";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {array, index} Checks to see if the specified array has an element at index";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                if(!((CArray)args[0]).inAssociativeMode()){
                    try{
                        int index = (int)Static.getInt(args[1]);                    
                        CArray ca = (CArray)args[0];
                        return new CBoolean(index <= ca.size() - 1, t);
                    } catch(ConfigRuntimeException e){
                        //They sent a key that is a string. Obviously it doesn't exist.
                        return new CBoolean(false, t);
                    }
                } else {
                    CArray ca = (CArray)args[0];
                    return new CBoolean(ca.containsKey(args[1].val()), t);
                }
            } else {
                throw new ConfigRuntimeException("Expecting argument 1 to be an array", ExceptionType.CastException, t);
            }
        }
        
    }
    
    @api public static class array_resize extends AbstractFunction{

        public String getName() {
            return "array_resize";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {array, size, [fill]} Resizes the given array so that it is at least of size size, filling the blank spaces with"
                    + " fill, or null by default. If the size of the array is already at least size, nothing happens; in other words this"
                    + " function can only be used to increase the size of the array.";
                    //+ " If the array is an associative array, the non numeric values are simply copied over.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }


        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray && args[1] instanceof CInt){
                CArray original = (CArray)args[0];
                int size = (int)((CInt)args[1]).getInt();
                Construct fill = new CNull(t);
                if(args.length == 3){
                    fill = args[2];
                }
                for(int i = original.size(); i < size; i++){
                    original.push(fill);
                }
            } else {
                throw new ConfigRuntimeException("Argument 1 must be an array, and argument 2 must be an integer in array_resize", ExceptionType.CastException, t);
            }
            return new CVoid(t);
        }
        
    }
    
    @api public static class range extends AbstractFunction{

        public String getName() {
            return "range";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "array {start, finish, [increment] | finish} Returns an array of numbers from start to (finish - 1)"
                    + " skipping increment integers per count. start defaults to 0, and increment defaults to 1. All inputs"
                    + " must be integers. If the input doesn't make sense, it will reasonably degrade, and return an empty array.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            long start = 0;
            long finish = 0;
            long increment = 1;
            if(args.length == 1){
                finish = Static.getInt(args[0]);
            } else if(args.length == 2){
                start = Static.getInt(args[0]);
                finish = Static.getInt(args[1]);
            } else if(args.length == 3){
                start = Static.getInt(args[0]);
                finish = Static.getInt(args[1]);
                increment = Static.getInt(args[2]);
            }
            if(start < finish && increment < 0 || start > finish && increment > 0  || increment == 0){
                return new CArray(t);
            }
            CArray ret = new CArray(t);
            for(long i = start; (increment > 0?i < finish:i > finish); i = i + increment){
                ret.push(new CInt(i, t));
            }
            return ret;
        }
        
    }
    
    @api public static class array_keys extends AbstractFunction{

        public String getName() {
            return "array_keys";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {array} Returns the keys in this array as a normal array. If the array passed in is already a normal array,"
                    + " the keys will be 0 -> (array_size(array) - 1)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                CArray ca2 = new CArray(t);
                for(String c : ca.keySet()){
                    ca2.push(new CString(c, t));
                }
                return ca2;
            } else {
                throw new ConfigRuntimeException(this.getName() + " expects arg 1 to be an array", ExceptionType.CastException, t);
            }
        }
        
    }
    
    @api public static class array_normalize extends AbstractFunction{

        public String getName() {
            return "array_normalize";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {array} Returns a new normal array, given an associative array. (If the array passed in is not associative, a copy of the "
                    + " array is returned).";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                CArray ca2 = new CArray(t);
                for(String c : ca.keySet()){
                    ca2.push(ca.get(c, t));
                }
                return ca2;
            } else {
                throw new ConfigRuntimeException(this.getName() + " expects arg 1 to be an array", ExceptionType.CastException, t);
            }
        }
        
    }
    
    @api public static class array_merge extends AbstractFunction{

        public String getName() {
            return "array_merge";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "array {array1, array2, [arrayN...]} Merges the specified arrays from left to right, and returns a new array. If the array"
                    + " merged is associative, it will overwrite the keys from left to right, but if the arrays are normal, the keys are ignored,"
                    + " and values are simply pushed.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InsufficientArgumentsException, ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            CArray newArray = new CArray(t);
            if(args.length < 2){
                throw new ConfigRuntimeException("array_merge must be called with at least two parameters", ExceptionType.InsufficientArgumentsException, t);
            }
            for(int i = 0; i < args.length; i++){
                if(args[i] instanceof CArray){
                    CArray cur = (CArray)args[i];
                    if(!cur.inAssociativeMode()){
                        for(int j = 0; j < cur.size(); j++){
                            newArray.push(cur.get(j, t));
                        }
                    } else {
                        for(String key : cur.keySet()){
                            newArray.set(key, cur.get(key, t));
                        }
                    }
                } else {
                    throw new ConfigRuntimeException("All arguments to array_merge must be arrays", ExceptionType.CastException, t);
                }
            }
            return newArray;
        }
        
    }
    
    @api public static class array_remove extends AbstractFunction{

        public String getName() {
            return "array_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "mixed {array, index} Removes an index from an array. If the array is a normal"
                    + " array, all values' indicies are shifted left one. If the array is associative,"
                    + " the index is simply removed. If the index doesn't exist, the array remains"
                    + " unchanged. The value removed is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                return ca.remove(args[1]);
            } else {
                throw new ConfigRuntimeException("Argument 1 of array_remove should be an array", ExceptionType.CastException, t);
            }
        }
        
    }
    
    @api public static class array_implode extends AbstractFunction{

        public String getName() {
            return "array_implode";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "string {array, [glue]} Given an array and glue, to-strings all the elements"
                    + " in the array (just the values, not the keys), and joins them with the glue, defaulting to a space. For instance"
                    + " array_implode(array(1, 2, 3), '-') will return \"1-2-3\".";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(!(args[0] instanceof CArray)){
                throw new ConfigRuntimeException("Expecting argument 1 to be an array", ExceptionType.CastException, t);
            }
            StringBuilder b = new StringBuilder();
            CArray ca = (CArray)args[0];
            String glue = " ";
            if(args.length == 2){
                glue = args[1].val();
            }
            boolean first = true;
            for(String key : ca.keySet()){
                Construct value = ca.get(key, t);
                if(!first){
                    b.append(glue).append(value.val());
                } else {
                    b.append(value.val());
                    first = false;
                }
            }
            return new CString(b.toString(), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api public static class cslice extends AbstractFunction{

        public String getName() {
            return "cslice";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "slice {from, to} Dynamically creates an array slice, which can be used with array_get"
                    + " (or the [bracket notation]) to get a range of elements. cslice(0, 5) is equivalent"
                    + " to 0..5 directly in code, however with this function you can also do cslice(@var, @var),"
                    + " or other more complex expressions, which are not possible in static code.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CSlice(Static.getInt(args[0]), Static.getInt(args[1]), t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }   
    
    @api public static class array_sort extends AbstractFunction{

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(!(args[0] instanceof CArray)){
                throw new ConfigRuntimeException("The first parameter to array_sort must be an array", ExceptionType.CastException, t);
            }
            CArray ca = (CArray)args[0];
            CArray.SortType sortType = CArray.SortType.REGULAR;
            try{
                if(args.length == 2){
                    sortType = CArray.SortType.valueOf(args[1].val().toUpperCase());
                }
            } catch(IllegalArgumentException e){
                throw new ConfigRuntimeException("The sort type must be one of either: REGULAR, NUMERIC, STRING, or STRING_CI",
                        ExceptionType.FormatException, t);
            }
            ca.sort(sortType);
            return ca;
        }

        public String getName() {
            return "array_sort";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "array {array, [sortType]} Sorts an array in place, and also returns a reference to the array. The"
                    + " complexity of this sort algorithm is guaranteed to be no worse than n log n, as it uses merge sort."
                    + " The array is sorted in place, a new array is not explicitly created, so if you sort an array that"
                    + " is passed in as a variable, the contents of that variable will be sorted, even if you don't re-assign"
                    + " the returned array back to the variable. If you really need the old array, you should create a copy of"
                    + " the array first, like so: assign(@sorted, array_sort(@array[])). The sort type may be one of the following:"
                    + " REGULAR, NUMERIC, STRING, STRING_CI. A regular sort sorts the elements without changing types first. A"
                    + " numeric sort always converts numeric values to numbers first (so 001 becomes 1). A string sort compares"
                    + " values as strings, and a string_ci sort is the same as a string sort, but the comparision is case-insensitive."
                    + " If the array contains array values, a CastException is thrown; inner arrays cannot be sorted against each"
                    + " other. If the array is associative, a warning will be raised if the General logging channel is set to verbose,"
                    + " because the array's keys will all be lost in the process. To avoid this warning, and to be more explicit,"
                    + " you can use array_normalize() to normalize the array first. Note that the reason this function is an"
                    + " in place sort instead of explicitely cloning the array is because in most cases, you may not need"
                    + " to actually clone the array, an expensive operation. Due to this, it has slightly different behavior"
                    + " than array_normalize, which could have also been implemented in place.";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
    }
}
