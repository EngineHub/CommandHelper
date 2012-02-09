/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class Persistance {
    public static String docs(){
        return "Allows scripts to store data from execution to execution. See the guide on [[CommandHelper/Persistance|persistance]] for more information.";
    }
    
    @api public static class store_value implements Function{

        public String getName() {
            return "store_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {key, value} Allows you to store a value, which can then be retrieved later. key must be a string containing"
                    + " only letters, numbers, underscores. Periods may also be used, but they form a namespace, and have special meaning."
                    + " (See get_values())";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String key = args[0].val();
            String value = null;
            try{
                value = Construct.json_encode(args[1], line_num, f);
            } catch(MarshalException e){
                throw new ConfigRuntimeException(e.getMessage(), line_num, f);
            }
            char pc = '.';
            for(int i = 0; i < key.length(); i++){
                Character c = key.charAt(i);
                if((i == 0 || i == key.length() - 1 || pc == '.') && c == '.'){
                    throw new ConfigRuntimeException("Periods may only be used as seperators between namespaces.", ExceptionType.FormatException, line_num, f);
                }
                if(c != '_' && c != '.' && !Character.isLetterOrDigit(c)){
                    throw new ConfigRuntimeException("Param 1 in store_value must only contain letters, digits, or underscores.",
                            ExceptionType.FormatException, line_num, f);
                }
            }
            Static.getPersistance().setValue(new String[]{"storage", key}, value);
            try {
                Static.getPersistance().save();
            } catch (Exception ex) {
                Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
                throw new ConfigRuntimeException(ex.getMessage(), null, line_num, f, ex);
            }
            return new CVoid(line_num, f);
        }
        
        public Boolean runAsync(){
            //Because we do IO
            return true;
        }
        
    }
    
    @api public static class get_value implements Function{

        public String getName() {
            return "get_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Mixed {key} Returns a stored value stored with store_value. If the key doesn't exist in storage, null"
                    + " is returned. On a more detailed note: If the value stored in the persistance database is not actually a construct,"
                    + " then null is also returned.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {            
            Object o;
            try {
                Object obj = Static.getPersistance().getValue(new String[]{"storage", args[0].val()});
                if(obj == null){
                    return new CNull(line_num, f);
                }
                o = Construct.json_decode(obj.toString(), line_num, f);
            } catch (MarshalException ex) {
                throw new ConfigRuntimeException(ex.getMessage(), line_num, f);
            }
            try{
                return (Construct)o;
            } catch(ClassCastException e){
                return new CNull(line_num, f);
            }
        }
        public Boolean runAsync(){
            //Because we do IO
            return true;
        }
        
    }
    
    @api public static class get_values implements Function{

        public String getName() {
            return "get_values";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {name.space} Returns all the values in a particular namespace"
                    + " as an associative"
                    + " array(key: value, key: value). Only full namespace matches are considered,"
                    + " so if the key 'users.data.username.hi' existed in the database, and you tried"
                    + " get_values('users.data.user'), nothing would be returned. The last segment in"
                    + " a key is also considered a namespace, so 'users.data.username.hi' would return"
                    + " a single value (in this case).";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            com.laytonsmith.PureUtilities.Persistance p = Static.getPersistance();
            List<Map.Entry<String, Object>> list = p.getNamespaceValues(args[0].val().split("\\."));
            CArray ca = new CArray(line_num, f);
            for(Map.Entry<String, Object> e : list){
                try {
                    ca.set(new CString((String)e.getKey(), line_num, f), 
                            Construct.json_decode(e.getValue().toString(), line_num, f));
                } catch (MarshalException ex) {
                    Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ca;
        }

        public String since() {
            return "3.3.0";
        }
        
    }
    
    @api public static class has_value implements Function{

        public String getName() {
            return "has_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {key} Returns whether or not there is data stored at the specified key in the Persistance database.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.2";
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(Static.getPersistance().isKeySet(new String[]{"storage", args[0].val()}), line_num, f);
        }
        
    }
    
    @api public static class clear_value implements Function{

        public String getName() {
            return "clear_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {key} Completely removes a value from storage. Calling has_value(key) after this call will return false.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
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
            Static.getPersistance().setValue(new String[]{"storage", args[0].val()}, null);
            return new CVoid(line_num, f);
        }
        
    }
}
