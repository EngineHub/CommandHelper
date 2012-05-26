/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events.drivers;

import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author EntityReborn
 */
public class EntityEvents {
    public static String docs(){
        return "Contains events related to an entity";
    }
    
    @api
    public static class target_player extends AbstractEvent{

        public String getName() {
            return "target_player";
        }

        public String docs() {
            return "{player: <string match> |"
                    + "join_message: <regex>} This event is called when a living entity ("
                    + "including mobs) is targeted by another entity."
                    + "{player: The player's name | join_message: The default join message}"
                    + "{}"
                    + "{}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public Driver driver(){
            return Driver.TARGET_ENTITY;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
        	if(e instanceof MCEntityTargetEvent){
        		MCEntityTargetEvent ple = (MCEntityTargetEvent) e;
        		 
	        	if (ple.getTarget() instanceof Player) {
	        		return true;
	        	}
        	}
        	return false;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCEntityTargetEvent){
                MCEntityTargetEvent ple = (MCEntityTargetEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                
                String name = ((Player)ple.getTarget()).getName();
                map.put("target", new CString(name, Target.UNKNOWN));
                
                return map;
            } else{
                throw new EventException("Cannot convert e to EntityTargetLivingEntityEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
        	if(event instanceof MCEntityTargetEvent){
        		MCEntityTargetEvent pie = (MCEntityTargetEvent)event;
            
        		if (key.equals("target")) {
        			int id = Static.GetPlayer(value.val(), Target.UNKNOWN).getEntityId();
        			pie.setTarget((Entity)Static.getEntity(id));
        			return true;
        		}
        	}
        	
        	return false;
        }
        
        public BindableEvent convert(CArray manual){
            MCEntityTargetEvent e = EventBuilder.instantiate(MCEntityTargetEvent.class, Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN), manual.get("target").val());
            return e;
        }
        
    }
}