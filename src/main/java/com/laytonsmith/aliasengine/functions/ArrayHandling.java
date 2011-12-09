/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CArrayReference;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.BasicLogic.equals;
import com.laytonsmith.aliasengine.functions.BasicLogic.equals_ic;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;

/**
 *
 * @author Layton
 */
public class ArrayHandling {
    public static String docs(){
        return "This class contains functions that provide a way to manipulate arrays. To create an array, use the <code>array</code> function."
                + " For more detailed information on array usage, see the page on [[CommandHelper/Arrays|arrays]]";
    }
    @api public static class array_size implements Function{

        public String getName() {
            return "array_size";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                return new CInt(((CArray)args[0]).size(), line_num, f);
            }
            throw new ConfigRuntimeException("Argument 1 of array_size must be an array", ExceptionType.CastException, line_num, f);
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

        public String since() {
            return "3.0.1";
        }

        public Boolean runAsync() {
            return null;
        }
        
    }
    
    @api public static class array_get implements Function{

        public String getName() {
            return "array_get";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String index = "0..-1";
            if(args.length == 2){
                index = args[1].val();
            }
            if(env.GetFlag("array_get_alt_mode") == Boolean.TRUE){                
                return new CArrayReference(args[0], new CString(index, line_num, f), env);
            }
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                if(index.contains("..")){
                    if(ca.inAssociativeMode()){
                        if(index.equals("0..-1")){
                            //Special exception, we want to clone the whole array
                            CArray na = new CArray(line_num, f);
                            na.forceAssociativeMode();
                            for(Construct key : ca.keySet()){                                
                                try {
                                    na.set(key, ca.get(key, line_num).clone());
                                } catch (CloneNotSupportedException ex) {
                                    na.set(key, ca.get(key, line_num));
                                }
                            }
                            return na;
                        }
                        throw new ConfigRuntimeException("Array slices are not allowed with an associative array", ExceptionType.CastException, line_num, f);
                    }
                    //It's a range
                    int start = 0;
                    int finish = 0;
                    String[] split = index.split("\\.\\.");
                    try{
                        if(split[0].isEmpty()){
                            start = 0;
                        } else {
                            start = Integer.parseInt(split[0]);
                        }
                        if(split.length == 1 || split[1].isEmpty()){
                            finish = ca.size() - 1;
                        } else {
                            finish = Integer.parseInt(split[1]);
                        }
                        //Convert negative indexes 
                        if(start < 0){
                            start = ca.size() + start;
                        }
                        if(finish < 0){
                            finish = ca.size() + finish;
                        }
                        CArray na = new CArray(line_num, f);
                        if(finish < start){
                            //return an empty array in cases where the indexes don't make sense
                            return na;
                        }
                        for(int i = start; i <= finish; i++){
                            try{
                                na.push(ca.get(i, line_num).clone());
                            } catch(CloneNotSupportedException e){
                                na.push(ca.get(i, line_num));
                            }
                        }
                        return na;
                    } catch(NumberFormatException e){
                        throw new ConfigRuntimeException("Ranges must be integer numbers, i.e., [0..5]", ExceptionType.CastException, line_num, f);
                    }
                } else {
                    if(!ca.inAssociativeMode()){
                        int iindex = (int)Static.getInt(args[1]);
                        if(iindex < 0){
                            //negative index, convert to positive index
                            iindex = ca.size() + iindex;
                        }
                        return ca.get(iindex, line_num);
                    } else {
                        return ca.get(args[1], line_num);
                    }
                }
            } else{
                throw new ConfigRuntimeException("Argument 1 of array_get must be an array", ExceptionType.CastException, line_num, f);
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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        
        public Boolean runAsync() {
            return null;
        }
        
    }
    
    @api public static class array_set implements Function{

        public String getName() {
            return "array_set";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                try{
                ((CArray)args[0]).set(args[1], args[2]);
                } catch(IndexOutOfBoundsException e){
                    throw new ConfigRuntimeException("The index " + args[1].val() + " is out of bounds", ExceptionType.IndexOverflowException, line_num, f);
                }
                return new CVoid(line_num, f);
            }
            throw new ConfigRuntimeException("Argument 1 of array_set must be an array, and argument 2 must be an integer", ExceptionType.CastException, line_num, f);        
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
        public String since() {
            return "3.0.1";
        }
        
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class array_push implements Function{

        public String getName() {
            return "array_push";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                if(args.length < 2){
                    throw new ConfigRuntimeException("At least 2 arguments must be provided to array_push", ExceptionType.InsufficientArgumentsException, line_num, f);
                }
                for(int i = 1; i < args.length; i++){
                    ((CArray)args[0]).push(args[i]);
                }
                return new CVoid(line_num, f);
            }
            throw new ConfigRuntimeException("Argument 1 of array_push must be an array", ExceptionType.CastException, line_num, f);
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
        public String since() {
            return "3.0.1";
        }
        
        public Boolean runAsync() {
            return null;
        }
        
    }
    @api public static class array_contains implements Function {

        public String getName() {
            return "array_contains";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            equals e = new equals();
            if(args[0] instanceof CArray){
                CArray ca = (CArray) args[0];
                for(int i = 0; i < ca.size(); i++){
                    if(((CBoolean)e.exec(line_num, f, env, ca.get(i, line_num), args[1])).getBoolean()){
                        return new CBoolean(true, line_num, f);
                    }
                }
                return new CBoolean(false, line_num, f);
            } else {
                throw new ConfigRuntimeException("Argument 1 of array_contains must be an array", ExceptionType.CastException, line_num, f);
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
        public String since() {
            return "3.0.1";
        }
        
        public Boolean runAsync() {
            return null;
        }
        
    }
    
    @api public static class array_contains_ic implements Function{

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

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            equals_ic e = new equals_ic();
            if(args[0] instanceof CArray){
                CArray ca = (CArray) args[0];
                for(int i = 0; i < ca.size(); i++){
                    if(((CBoolean)e.exec(line_num, f, environment, ca.get(i, line_num), args[1])).getBoolean()){
                        return new CBoolean(true, line_num, f);
                    }
                }
                return new CBoolean(false, line_num, f);
            } else {
                throw new ConfigRuntimeException("Argument 1 of array_contains_ic must be an array", ExceptionType.CastException, line_num, f);
            }
        }
        
    }
    
    @api public static class array_index_exists implements Function{

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

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                int index = (int)Static.getInt(args[1]);
                CArray ca = (CArray)args[0];
                return new CBoolean(index <= ca.size() - 1, line_num, f);
            } else {
                throw new ConfigRuntimeException("Expecting argument 1 to be an array", ExceptionType.CastException, line_num, f);
            }
        }
        
    }
    
    @api public static class array_resize implements Function{

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

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray && args[1] instanceof CInt){
                CArray original = (CArray)args[0];
                int size = (int)((CInt)args[1]).getInt();
                Construct fill = new CNull(line_num, f);
                if(args.length == 3){
                    fill = args[2];
                }
                for(int i = original.size(); i < size; i++){
                    original.push(fill);
                }
            } else {
                throw new ConfigRuntimeException("Argument 1 must be an array, and argument 2 must be an integer in array_resize", ExceptionType.CastException, line_num, f);
            }
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class range implements Function{

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

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
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
                return new CArray(line_num, f);
            }
            CArray ret = new CArray(line_num, f);
            for(long i = start; (increment > 0?i < finish:i > finish); i = i + increment){
                ret.push(new CInt(i, line_num, f));
            }
            return ret;
        }
        
    }
    
    @api public static class array_keys implements Function{

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

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                CArray ca2 = new CArray(line_num, f);
                for(Construct c : ca.keySet()){
                    ca2.push(c);
                }
                return ca2;
            } else {
                throw new ConfigRuntimeException(this.getName() + " expects arg 1 to be an array", ExceptionType.CastException, line_num, f);
            }
        }
        
    }
    
    @api public static class array_normalize implements Function{

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

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                CArray ca2 = new CArray(line_num, f);
                for(Construct c : ca.keySet()){
                    ca2.push(ca.get(c, line_num));
                }
                return ca2;
            } else {
                throw new ConfigRuntimeException(this.getName() + " expects arg 1 to be an array", ExceptionType.CastException, line_num, f);
            }
        }
        
    }
    
    @api public static class array_merge implements Function{

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

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            CArray newArray = new CArray(line_num, f);
            if(args.length < 2){
                throw new ConfigRuntimeException("array_merge must be called with at least two parameters", ExceptionType.InsufficientArgumentsException, line_num, f);
            }
            for(int i = 0; i < args.length; i++){
                if(args[i] instanceof CArray){
                    CArray cur = (CArray)args[i];
                    if(!cur.inAssociativeMode()){
                        for(int j = 0; j < cur.size(); j++){
                            newArray.push(cur.get(j, line_num));
                        }
                    } else {
                        for(Construct key : cur.keySet()){
                            newArray.set(key, cur.get(key, line_num));
                        }
                    }
                } else {
                    throw new ConfigRuntimeException("All arguments to array_merge must be arrays", ExceptionType.CastException, line_num, f);
                }
            }
            return newArray;
        }
        
    }
    
    @api public static class array_remove implements Function{

        public String getName() {
            return "array_remove";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {array, index} Removes an index from an array. If the array is a normal"
                    + " array, all values' indicies are shifted left one. If the array is associative,"
                    + " the index is simply removed. If the index doesn't exist, the array remains"
                    + " unchanged.";
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

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                CArray ca = (CArray)args[0];
                ca.remove(args[1]);
            } else {
                throw new ConfigRuntimeException("Argument 1 of array_remove should be an array", ExceptionType.CastException, line_num, f);
            }
            return new CVoid(line_num, f);
        }
        
    }
    
//    @api public static class array_entry implements Function{
//
//        public String getName() {
//            return "array_entry";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{2};
//        }
//
//        public String docs() {
//            return "CEntry {key, value} This function is used internally by the compiler, and while possible, shouldn't be used directly.";
//        }
//
//        public ExceptionType[] thrown() {
//            return null;
//        }
//
//        public boolean isRestricted() {
//            return false;
//        }
//
//        public void varList(IVariableList varList) {}
//
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public String since() {
//            return "3.3.0";
//        }
//
//        public Boolean runAsync() {
//            return null;
//        }
//
//        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
//            return new CEntry(args[0], args[1], line_num, f);
//        }
//        
//    }
}
