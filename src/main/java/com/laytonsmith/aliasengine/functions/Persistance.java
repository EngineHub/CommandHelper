/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.exceptions.MarshalException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Layton
 */
public class Persistance {
    public static String docs(){
        return "Allows scripts to store data from execution to execution.";
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
                    + " only letters, numbers, underscores.";
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String key = args[0].val();
            String value = null;
            try{
                value = Construct.json_encode(args[1]);
            } catch(MarshalException e){
                throw new ConfigRuntimeException(e.getMessage(), line_num, f);
            }
            for(int i = 0; i < key.length(); i++){
                Character c = key.charAt(i);
                if(c != '_' && !Character.isLetterOrDigit(c)){
                    throw new ConfigRuntimeException("Param 1 in store_value must only contain letters, digits, or underscores.",
                            ExceptionType.FormatException, line_num, f);
                }
            }
            Static.getPersistance().setValue(new String[]{"commandhelper", "function", "storage", key}, value);
            Static.getPersistance().setValue(new String[]{}, null);
            try {
                com.laytonsmith.aliasengine.Static.getPersistance().save();
            } catch (Exception ex) {
                Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {            
            Object o;
            try {
                Object obj = Static.getPersistance().getValue(new String[]{"commandhelper", "function", "storage", args[0].val()});
                if(obj == null){
                    return new CNull(line_num, f);
                }
                o = Construct.json_decode(obj.toString());
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
    
    public static class has_value implements Function{

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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(Static.getPersistance().isKeySet(new String[]{"commandhelper", "function", "storage", args[0].val()}), line_num, f);
        }
        
    }
}
