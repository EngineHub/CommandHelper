package com.laytonsmith.core.environments;

import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.core.events.BoundEvent;

/**
 *
 * @author lsmith
 */
public class CommandHelperEnvironment implements Environment.EnvironmentImpl, Cloneable {
	  
    
    private MCCommandSender commandSender = null;
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
        return clone;
    }

    public void SetCommand(String command) {
        this.command = command;
    }
    
    public String GetCommand(){
        return this.command;
    }

	public void SetBlockCommandSender(MCBlockCommandSender bcs) {
		this.commandSender = bcs;
	}
	
	public MCBlockCommandSender GetBlockCommandSender(){
		if(this.commandSender instanceof MCBlockCommandSender){
			return (MCBlockCommandSender)commandSender;
		} else {
			return null;
		}
	}
}
