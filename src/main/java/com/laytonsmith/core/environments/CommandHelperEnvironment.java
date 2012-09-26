package com.laytonsmith.core.environments;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.events.BoundEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class CommandHelperEnvironment implements Environment.EnvironmentImpl, Cloneable {
	  
    
    private MCCommandSender commandSender = null;
    private IVariableList iVariableList = null;
    private Map<String, Procedure> procs = null;
    private String label = null;
    
    private BoundEvent.ActiveEvent event = null;
    private String command = null;		
    
    
    
    
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
    public CommandHelperEnvironment clone() throws CloneNotSupportedException{	    
        CommandHelperEnvironment clone = (CommandHelperEnvironment)super.clone();
        if(procs != null){
            clone.procs = new HashMap<String, Procedure>(procs);
        } else {
            clone.procs = new HashMap<String, Procedure>();
        }
        if(iVariableList != null){
            clone.iVariableList = (IVariableList) iVariableList.clone();
        }
        return clone;
    }

    public void SetCommand(String command) {
        this.command = command;
    }
    
    public String GetCommand(){
        return this.command;
    }
}
