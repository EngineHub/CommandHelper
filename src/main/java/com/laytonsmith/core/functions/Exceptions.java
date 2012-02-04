/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.api;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class Exceptions {
    public static String docs(){
        return "This class contains functions related to Exception handling in MScript";
    }
    public enum ExceptionType{
        /**
         * This exception is thrown if a value cannot be cast into an appropriate type. Functions that require
         * a numeric value, for instance, would throw this if the string "hi" were passed in.
         */
        CastException,
        /**
         * This exception is thrown if a value is requested from an array that is above the highest index of the array,
         * or a negative number.
         */
        IndexOverflowException,
        /**
         * This exception is thrown if a function expected a numeric value to be in a particular range, and it wasn't
         */
        RangeException,
        /**
         * This exception is thrown if a function expected the length of something to be a particular value, but it was not.
         */
        LengthException,
        /**
         * This exception is thrown if the user running the command does not have permission to run the function
         */
        InsufficientPermissionException,
        /**
         * This exception is thrown if a function expected an online player, but that player was offline, or the
         * command is being run from somewhere not in game, and the function was trying to use the current player.
         */
        PlayerOfflineException, 
        /**
         * Some var arg functions may require at least a certain number of arguments to be passed to the function
         */
        InsufficientArgumentsException, 
        /**
         * This exception is thrown if a function expected a string to be formatted in a particular way, but it could not interpret the 
         * given value.
         */
        FormatException,
        /**
         * This exception is thrown if a procedure is used without being defined, or if a procedure name does not follow proper naming
         * conventions.
         */
        InvalidProcedureException, 
        /**
         * This exception is thrown if there is a problem with an include. This is thrown if there is
         * a compile error in the included script.
         */
        IncludeException,
        /**
         * This exception is thrown if a script tries to read or write to a location of the filesystem that is not allowed.
         */
        SecurityException, 
        /**
         * This exception is thrown if a file cannot be read or written to.
         */
        IOException, 
        /**
         * This exception is thrown if a function uses an external plugin, and that plugin is not loaded, 
         * or otherwise unusable.
         */
        InvalidPluginException,
        /**
         * This exception is thrown when a plugin is loaded, but a call to the plugin failed, usually
         * for some reason specific to the plugin. Check the error message for more details about this
         * error.
         */
        PluginInternalException,
        /**
         * If a function requests a world, and the world given doesn't exist, this is thrown
         */
        InvalidWorldException,
        /**
         * This exception is thrown if an error occurs when trying to bind() an event, or if a event framework
         * related error occurs.
         */
        BindException,
        /**
         * If an enchantment is added to an item that isn't supported, this is thrown.
         */
        EnchantmentException,
        /**
         * If an untameable mob is attempted to be tamed, this exception is thrown
         */
        UntameableMobException,
    }
    @api public static class _try implements Function{      
        
        public String getName() {
            
            return "try";
        }

        public Integer[] numArgs() {
            return new Integer[]{3,4};
        }

        public String docs() {
            return "void {tryCode, varName, catchCode, [exceptionTypes]} This function works similar to a try-catch block in most languages. If the code in"
                    + " tryCode throws an exception, instead of killing the whole script, it stops running, and begins running the catchCode."
                    + " var should be an ivariable, and it is set to an array containing the following information about the exception:"
                    + " 0 - The class of the exception; 1 - The message generated by the exception; 2 - The file the exception was generated from; 3 - The line the exception"
                    + " occured on. If exceptionTypes is provided, it should be an array of exception types, or a single string that this try function is interested in."
                    + " If the exception type matches one of the values listed, the exception will be caught, otherwise, the exception will continue up the stack."
                    + " If exceptionTypes is missing, it will catch all exceptions."
                    + " PLEASE NOTE! This function will not catch exceptions thrown by CommandHelper, only built in exceptions. "
                    + " Please see the wiki for more information about what possible exceptions can be thrown and where.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return false;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return null;
        }
        public Construct execs(int line_num, File f, Env env, Script that, GenericTreeNode<Construct> tryCode,
                GenericTreeNode<Construct> varName, GenericTreeNode<Construct> catchCode, GenericTreeNode<Construct> types) throws CancelCommandException{
            Construct pivar = that.eval(varName, env);
            IVariable ivar;
            if(pivar instanceof IVariable){
                ivar = (IVariable)pivar;
            } else {
                throw new ConfigRuntimeException("Expected argument 2 to be an IVariable", ExceptionType.CastException, line_num, f);
            }
            List<String> interest = new ArrayList<String>();
            if(types != null){
            Construct ptypes = that.eval(types, env);
                if(ptypes instanceof CString){
                    interest.add(ptypes.val());
                } else if(ptypes instanceof CArray){
                    CArray ca = (CArray)ptypes;
                    for(int i = 0; i < ca.size(); i++){
                        interest.add(ca.get(i, line_num, f).val());
                    }
                } else {
                    throw new ConfigRuntimeException("Expected argument 4 to be a string, or an array of strings.", 
                            ExceptionType.CastException, line_num, f);
                }
            }
            
            for(String in : interest){
                try{
                    ExceptionType.valueOf(in);
                } catch(IllegalArgumentException e){
                    throw new ConfigRuntimeException("Invalid exception type passed to try():" + in, 
                            ExceptionType.FormatException, line_num, f);
                }
            }
            
            try{
                that.eval(tryCode, env);
            } catch (ConfigRuntimeException e){
                if((Boolean)Static.getPreferences().getPreference("debug-mode")){
                    System.out.println("[CommandHelper]: Exception thrown -> " + e.getMessage() + " :: " + e.getExceptionType() + ":" + e.getFile() + ":" + e.getLineNum());
                }
                if(e.getExceptionType() != null  && (interest.isEmpty() || interest.contains(e.getExceptionType().toString()))){
                    CArray ex = new CArray(line_num, f);
                    ex.push(new CString(e.getExceptionType().toString(), line_num, f));
                    ex.push(new CString(e.getMessage(), line_num, f));
                    ex.push(new CString((e.getFile()!=null?e.getFile().getAbsolutePath():"null"), line_num, f));
                    ex.push(new CInt(e.getLineNum(), line_num, f));
                    ivar.setIval(ex);
                    env.GetVarList().set(ivar);
                    that.eval(catchCode, env);
                } else {
                    throw e;
                }
            }
            
            
            return new CVoid(line_num, f);
        }
        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    @api public static class _throw implements Function{

        public String getName() {
            return "throw";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "nothing {exceptionType, msg} This function causes an exception to be thrown. If the exception type is null,"
                    + " it will be uncatchable. Otherwise, exceptionType may be any valid exception type.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.FormatException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            try{
                ExceptionType c = null;
                if(!(args[0] instanceof CNull)){
                    c = ExceptionType.valueOf(args[0].val());
                }
                throw new ConfigRuntimeException(args[1].val(), c, line_num, f);
            } catch(IllegalArgumentException e){
                throw new ConfigRuntimeException("Expected a valid exception type", ExceptionType.FormatException, line_num, f);
            }
        }
        
    }
}
