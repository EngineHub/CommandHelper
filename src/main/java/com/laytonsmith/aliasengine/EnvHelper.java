/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.IVariableList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class EnvHelper {
    /**
     * Given the environment, this function returns the CommandSender in the
     * environment, which can possibly be null.
     * @param env
     * @return 
     */
    public static CommandSender GetCommandSender(Map<String, Object> env){
        if(env.containsKey("user")){
            Object userObject = env.get("user");
            if(userObject == null){
                return null;
            }
            if(userObject instanceof CommandSender){
                return ((CommandSender)userObject);
            }
            throw new ConfigRuntimeException("Expecting environment variable \"user\" to be an instance of CommandSender", 0, null);
        } else {
            throw new ConfigRuntimeException("Expecting environment to contain \"user\", but no such key was found", 0, null);
        }
    }
    
    /**
     * Sets the CommandSender in this environment
     * @param env 
     */
    public static void SetCommandSender(Map<String, Object> env, CommandSender cs){
        env.put("user", cs);
    }
    
    /**
     * Given the environment, this function returns the Player in the
     * environment, which can possibly be null. It is also possible the
     * environment contains a CommandSender object instead, which will
     * cause an exception.
     * @param env
     * @return 
     */
    public static Player GetPlayer(Map<String, Object> env){
        if(env.containsKey("user")){
            Object userObject = env.get("user");
            if(userObject == null){
                return null;
            }
            if(userObject instanceof Player){
                return ((Player)userObject);
            }
            throw new ConfigRuntimeException("Expecting environment variable \"user\" to be an instance of Player", 0, null);
        } else {
            throw new ConfigRuntimeException("Expecting environment to contain \"user\", but no such key was found", 0, null);
        }
    }
    
    /**
     * Sets the Player in this environment
     * @param env 
     */
    public static void SetPlayer(Map<String, Object> env, Player p){
        env.put("user", p);
    }
    
    /**
     * This function will return the variable list in this environment.
     * If the environment doesn't contain a variable list, an empty one
     * is created, and stored in the environment.
     * @param env
     * @return 
     */
    public static IVariableList GetVarList(Map<String, Object> env){
        IVariableList varList = null;
        if(!env.containsKey("varList")){
            varList = new IVariableList();
            env.put("varList", varList);
        } else {
            varList = ((IVariableList)env.get("varList"));
        }
        return varList;
    }
    
    /**
     * Returns the Map of known procedures in this environment. If the list
     * of procedures is currently empty, a new one is created and stored in
     * the environment.
     * @param env
     * @return 
     */
    public static Map<String, Procedure> GetProcs(Map<String, Object> env){
        Map<String, Procedure> procs = null;
        if(!env.containsKey("knownProcs")){
            procs = new HashMap<String, Procedure>();
            env.put("knownProcs", procs);
        } else {
            procs = ((Map<String, Procedure>)env.get("knownProcs"));
        }
        return procs;
    }
}
