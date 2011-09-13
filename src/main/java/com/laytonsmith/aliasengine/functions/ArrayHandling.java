/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.BasicLogic._equals;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Layton
 */
public class ArrayHandling {
    public static String docs(){
        return "This class contains functions that provide a way to manipulate arrays. To create an array, use the <code>array</code> function.";
    }
    @api public static class array_size implements Function{

        public String getName() {
            return "array_size";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
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
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                return ((CArray)args[0]).get((int)Static.getInt(args[1]), line_num);
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray && args[1] instanceof CInt){
                try{
                ((CArray)args[0]).set((int)((CInt)args[1]).getInt(), args[2]);
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
            return new Integer[]{2};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CArray){
                ((CArray)args[0]).push(args[1]);
                return new CVoid(line_num, f);
            }
            throw new ConfigRuntimeException("Argument 1 of array_push must be an array", ExceptionType.CastException, line_num, f);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public String docs() {
            return "void {array, value} Pushes the specified value onto the end of the array";
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            _equals e = new _equals();
            if(args[0] instanceof CArray){
                CArray ca = (CArray) args[0];
                for(int i = 0; i < ca.size(); i++){
                    if(((CBoolean)e.exec(line_num, f, p, ca.get(i, line_num), args[1])).getBoolean()){
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
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
}
