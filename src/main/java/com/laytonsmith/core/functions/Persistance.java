/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.Arrays;
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
        return "Allows scripts to store data from execution to execution. See the guide on [[CommandHelper/Persistance|persistance]] for more information."
                + " In all the functions, you may send multiple arguments for the key, which will automatically"
                + " be concatenated with a period (the namespace separator). No magic happens here, you can"
                + " put periods yourself, or combine manually namespaced values or automatically namespaced values"
                + " with no side effects.";
    }
    
    @api public static class store_value extends AbstractFunction{

        public String getName() {
            return "store_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "void {[namespace, ...,] key, value} Allows you to store a value, which can then be retrieved later. key must be a string containing"
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

        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {            
            String key = GetNamespace(args, args.length - 1, getName(), t);
            String value = null;
            try{
                value = Construct.json_encode(args[args.length - 1], t);
            } catch(MarshalException e){
                throw new ConfigRuntimeException(e.getMessage(), t);
            }
            char pc = '.';
            for(int i = 0; i < key.length(); i++){
                Character c = key.charAt(i);
                if(i != 0){
                    pc = key.charAt(i - 1);
                }
                if((i == 0 || i == key.length() - 1 || pc == '.') && c == '.'){
                    throw new ConfigRuntimeException("Periods may only be used as seperators between namespaces.", ExceptionType.FormatException, t);
                }
                if(c != '_' && c != '.' && !Character.isLetterOrDigit(c)){
                    throw new ConfigRuntimeException("Param 1 in store_value must only contain letters, digits, underscores, or dots, (which denote namespaces).",
                            ExceptionType.FormatException, t);
                }
            }
            CHLog.Log(CHLog.Tags.PERSISTANCE, "Storing: " + key + " -> " + value, t);
            Static.getPersistance().setValue(new String[]{"storage", key}, value);
            try {
                Static.getPersistance().save();
            } catch (Exception ex) {
                Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
                throw new ConfigRuntimeException(ex.getMessage(), null, t, ex);
            }
            return new CVoid(t);
        }
        
        public Boolean runAsync(){
            //Because we do IO
            return true;
        }
        
    }
    
    @api public static class get_value extends AbstractFunction{

        public String getName() {
            return "get_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "Mixed {[namespace, ...,] key} Returns a stored value stored with store_value. If the key doesn't exist in storage, null"
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

        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {            
            Object o;
            String namespace = GetNamespace(args, null, getName(), t);
            CHLog.Log(CHLog.Tags.PERSISTANCE, "Getting value: " + namespace, t);
            try {
                Object obj = Static.getPersistance().getValue(new String[]{"storage", namespace});
                if(obj == null){
                    return new CNull(t);
                }
                o = Construct.json_decode(obj.toString(), t);
            } catch (MarshalException ex) {
                throw new ConfigRuntimeException(ex.getMessage(), t);
            }
            try{
                return (Construct)o;
            } catch(ClassCastException e){
                return new CNull(t);
            }
        }
        public Boolean runAsync(){
            //Because we do IO
            return true;
        }
        
    }
    
    @api public static class get_values extends AbstractFunction{

        public String getName() {
            return "get_values";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "array {name[, space, ...]} Returns all the values in a particular namespace"
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

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            com.laytonsmith.PureUtilities.Persistance p = Static.getPersistance();
            List<String> keyChain = new ArrayList<String>();
            keyChain.add("storage");
            String namespace = GetNamespace(args, null, getName(), t);
            CHLog.Log(CHLog.Tags.PERSISTANCE, "Getting all values from " + namespace, t);
            keyChain.addAll(Arrays.asList(namespace.split("\\.")));
            List<Map.Entry<String, Object>> list = p.getNamespaceValues(keyChain.toArray(new String[]{}));
            CArray ca = new CArray(t);
            CHLog.Log(CHLog.Tags.PERSISTANCE, CHLog.Level.DEBUG, list.size() + " value(s) are being returned", t);
            for(Map.Entry<String, Object> e : list){
                try {
                    String key = ((String)e.getKey()).replaceFirst("storage\\.", ""); //Get that junk out of here
                    ca.set(new CString(key, t), 
                            Construct.json_decode(e.getValue().toString(), t));
                } catch (MarshalException ex) {
                    Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ca;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api public static class has_value extends AbstractFunction{

        public String getName() {
            return "has_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "boolean {[namespace, ...,] key} Returns whether or not there is data stored at the specified key in the Persistance database.";
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

        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(Static.getPersistance().isKeySet(new String[]{"storage", GetNamespace(args, null, getName(), t)}), t);
        }
        
    }
    
    @api public static class clear_value extends AbstractFunction{

        public String getName() {
            return "clear_value";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "void {[namespace, ...,] key} Completely removes a value from storage. Calling has_value(key) after this call will return false.";
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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            String namespace = GetNamespace(args, null, getName(), t);
            CHLog.Log(CHLog.Tags.PERSISTANCE, "Clearing value: " + namespace, t);
            Static.getPersistance().setValue(new String[]{"storage", namespace}, null);
            return new CVoid(t);
        }
        
    }
    
    /**
     * Generates the namespace for this value, given an array of constructs. 
     * If the entire list of arguments isn't supposed to be part of the namespace,
     * the value to be excluded may be specified.
     * @param args
     * @param exclude
     * @return 
     */
    private static String GetNamespace(Construct [] args, Integer exclude, String name, Target t){
        if(exclude != null && args.length < 2 || exclude == null && args.length < 1){
            throw new ConfigRuntimeException(name + " was not provided with enough arguments. Check the documentation, and try again.", ExceptionType.InsufficientArgumentsException, t);
        }
        boolean first = true;
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < args.length; i++){
            if(exclude != null && exclude == i){
                continue;
            }
            if(!first){
                b.append(".");
            }
            first = false;
            b.append(args[i].val());
        }
        return b.toString();
    }
}
