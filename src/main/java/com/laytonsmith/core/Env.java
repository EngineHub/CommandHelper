/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.events.BoundEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * The Env class contains the operating environment for a particular function.
 * @author Layton
 */
public final class Env implements Cloneable{
    
    /**
     * This is the underlying map of variables
     */
    private Map<String, Object> custom = new HashMap<String, Object>();   
    private Map<String, Boolean> flags = new HashMap<String, Boolean>();
    private MCCommandSender commandSender = null;
    private IVariableList iVariableList = null;
    private Map<String, Procedure> procs = null;
    private String label = null;
    private Script script = null;
    private BoundEvent.ActiveEvent event = null;
    private String command = null;
    
    /*
     * The constructor has relatively little to do, most things are lazy
     * initialized, but sometimes it's more convenient to pre-initialize
     * certain things
     */
    public Env(){
       
    }
    
    /**
     * Sets the value of a flag
     * @param name
     * @param value 
     */
    public void SetFlag(String name, boolean value){
        flags.put(name, value);
    }
    
    /**
     * Returns the value of a flag. Null if unset.
     * @param name
     * @return 
     */
    public Boolean GetFlag(String name){
        if(!flags.containsKey(name)){
            return null;
        } else {
            return flags.get(name);
        }
    }
    
    /**
     * Clears the value of a flag from the flag list, causing further calls to GetFlag(name) to return null.
     * @param name 
     */
    public void ClearFlag(String name){
        flags.remove(name);
    }
    
    /**
     * Use this if you would like to stick a custom variable in the environment.
     * It should be discouraged to use this for more than one shot transfers. Typically,
     * an setter and getter should be made to wrap the element.
     * @param name
     * @param var 
     */
    public void SetCustom(String name, Object var){
        if(!custom.containsKey("custom")){
            custom.put("custom", new HashMap<String, Object>());
        }
        ((Map<String, Object>)custom.get("custom")).put(name, var);        
    }
    
    /**
     * Returns the custom value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * @param name
     * @return 
     */
    public Object GetCustom(String name){
        if(!custom.containsKey("custom")){
            custom.put("custom", new HashMap<String, Object>());
        }
        return ((Map<String, Object>)custom.get("custom")).get(name);
    }
    /**
     * Given the environment, this function returns the CommandSender in the
     * environment, which can possibly be null.
     * @param env
     * @return 
     */
    public MCCommandSender GetCommandSender(){
        return commandSender;
    }
    
    /**
     * Sets the CommandSender in this environment
     * @param env 
     */
    public void SetCommandSender(MCCommandSender cs){
        commandSender = cs;
    }
    
    /**
     * Given the environment, this function returns the Player in the
     * environment, which can possibly be null. It is also possible the
     * environment contains a CommandSender object instead, which will
     * cause null to be returned.
     * @param env
     * @return 
     */
    public MCPlayer GetPlayer(){
        if(commandSender instanceof MCPlayer){
            return (MCPlayer)commandSender;
        } else {
            return null;
        }
    }
    
    /**
     * Sets the Player in this environment
     * @param env 
     */
    public void SetPlayer(MCPlayer p){
        commandSender = p;
    }
    
    /**
     * This function will return the variable list in this environment.
     * If the environment doesn't contain a variable list, an empty one
     * is created, and stored in the environment.
     * @param env
     * @return 
     */
    public IVariableList GetVarList(){
        if(iVariableList == null){
            iVariableList = new IVariableList();
        }
        return iVariableList;
    }
    
    public void SetVarList(IVariableList varList){
        iVariableList = varList;
    }
    
    /**
     * Returns the Map of known procedures in this environment. If the list
     * of procedures is currently empty, a new one is created and stored in
     * the environment.
     * @param env
     * @return 
     */
    public Map<String, Procedure> GetProcs(){
        if(procs == null){
            procs = new HashMap<String, Procedure>();
        }
        return procs;
    }
    
    public void SetProcs(Map<String, Procedure> procs){
        this.procs = procs;
    }
    
    public String GetLabel(){
        return label;
    }
    
    public void SetLabel(String label){
        this.label = label;
    }
    
    public void SetScript(Script s){
        this.script = s;
    }
    
    public Script GetScript(){
        return script;
    }        
    
    public void SetEvent(BoundEvent.ActiveEvent e){
        event = e;
    }
    
    /**
     * Returns the active event, or null if not in scope.
     * @return 
     */
    public BoundEvent.ActiveEvent GetEvent(){
        return event;
    }
    
    @Override
    public Env clone() throws CloneNotSupportedException{
        Env clone = new Env();
        clone.custom = new HashMap<String, Object>(this.custom);
        clone.commandSender = commandSender;
        clone.event = event;
        if(flags != null){
            clone.flags = new HashMap<String, Boolean>(flags);
        } else {
            clone.flags = new HashMap<String, Boolean>();
        }
        clone.label = label;
        if(procs != null){
            clone.procs = new HashMap<String, Procedure>(procs);
        } else {
            clone.procs = new HashMap<String, Procedure>();
        }
        clone.script = script;
        if(iVariableList != null){
            clone.iVariableList = (IVariableList) iVariableList.clone();
        }
        clone.command = command;
        return clone;
    }

    public void SetCommand(String command) {
        this.command = command;
    }
    
    public String GetCommand(){
        return this.command;
    }
}
