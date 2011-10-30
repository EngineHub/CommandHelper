/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
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
public class Env implements Cloneable{
    
    /**
     * This is the underlying map of variables
     */
    private Map<String, Object> env = new HashMap<String, Object>();   
    
    /*
     * The constructor has relatively little todo, most things are lazy
     * initialized, but sometimes it's more convenient to pre-initialize
     * certain things
     */
    public Env(){
       
    }
    
    /**
     * Use this if you would like to stick a custom variable in the environment.
     * It should be discouraged to use this for more than one shot transfers. Typically,
     * an setter and getter should be made to wrap the element.
     * @param name
     * @param var 
     */
    public void SetCustom(String name, Object var){
        if(!env.containsKey("custom")){
            env.put("custom", new HashMap<String, Object>());
        }
        ((Map<String, Object>)env.get("custom")).put(name, var);        
    }
    
    public Object GetCustom(String name){
        if(!env.containsKey("custom")){
            env.put("custom", new HashMap<String, Object>());
        }
        return ((Map<String, Object>)env.get("custom")).get(name);
    }
    /**
     * Given the environment, this function returns the CommandSender in the
     * environment, which can possibly be null.
     * @param env
     * @return 
     */
    public CommandSender GetCommandSender(){
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
    public void SetCommandSender(CommandSender cs){
        env.put("user", cs);
    }
    
    /**
     * Given the environment, this function returns the Player in the
     * environment, which can possibly be null. It is also possible the
     * environment contains a CommandSender object instead, which will
     * cause null to be returned.
     * @param env
     * @return 
     */
    public Player GetPlayer(){
        if(env.containsKey("user")){
            Object userObject = env.get("user");
            if(userObject == null){
                return null;
            }
            if(userObject instanceof Player){
                return ((Player)userObject);
            }
            return null;
        } else {
            throw new ConfigRuntimeException("Expecting environment to contain \"user\", but no such key was found", 0, null);
        }
    }
    
    /**
     * Sets the Player in this environment
     * @param env 
     */
    public void SetPlayer(Player p){
        env.put("user", p);
    }
    
    /**
     * This function will return the variable list in this environment.
     * If the environment doesn't contain a variable list, an empty one
     * is created, and stored in the environment.
     * @param env
     * @return 
     */
    public IVariableList GetVarList(){
        IVariableList varList = null;
        if(!env.containsKey("varList")){
            varList = new IVariableList();
            env.put("varList", varList);
        } else {
            varList = ((IVariableList)env.get("varList"));
        }
        return varList;
    }
    
    public void SetVarList(IVariableList varList){
        env.put("varList", varList);
    }
    
    /**
     * Returns the Map of known procedures in this environment. If the list
     * of procedures is currently empty, a new one is created and stored in
     * the environment.
     * @param env
     * @return 
     */
    public Map<String, Procedure> GetProcs(){
        Map<String, Procedure> procs = null;
        if(!env.containsKey("knownProcs")){
            procs = new HashMap<String, Procedure>();
            env.put("knownProcs", procs);
        } else {
            procs = ((Map<String, Procedure>)env.get("knownProcs"));
        }
        return procs;
    }
    
    public void SetProcs(Map<String, Procedure> procs){
        env.put("knownProcs", procs);
    }
    
    public String GetLabel(){
        return (String)env.get("label");
    }
    
    public void SetLabel(String label){
        env.put("label", label);
    }
    
    public void SetScript(Script s){
        env.put("script", s);
    }
    
    public Script GetScript(){
        return (Script)env.get("script");
    }        
    
    @Override
    public Env clone(){
        Env clone = new Env();
        clone.env = new HashMap<String, Object>(this.env);
        return clone;
    }
}
