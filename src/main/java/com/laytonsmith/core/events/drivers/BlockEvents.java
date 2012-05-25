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

/**
 *
 * @author EntityReborn
 */
public class BlockEvents {
    public static String docs(){
        return "Contains events related to a block";
    }
    
    @api
    public static class sign_changed extends AbstractEvent{

        public String getName() {
            return "sign_changed";
        }

        public String docs() {
            return "{player: <string match> |"
                    + "join_message: <regex>} This event is called when a player logs in. "
                    + "Setting join_message to null causes it to not be displayed at all. Cancelling "
                    + "the event does not prevent them from logging in. Instead, you should just kick() them."
                    + "{player: The player's name | join_message: The default join message}"
                    + "{join_message}"
                    + "{player|join_message}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public Driver driver(){
            return Driver.SIGN_CHANGED;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if(e instanceof MCPlayerJoinEvent){
                MCSignChangeEvent ple = (MCSignChangeEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player_name").val())){
                        return false;
                    }
                }                
                return true;
            }
            return false;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCSignChangeEvent){
                MCSignChangeEvent ple = (MCSignChangeEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                map.put("player", new CString(ple.getPlayer().getName(), Target.UNKNOWN));
                //map.put("block", (MCBlock)ple.getBlock());
                map.put("lineone", ple.getLine(0));
                map.put("linetwo", ple.getLine(1));
                map.put("linethree", ple.getLine(2));
                map.put("linefour", ple.getLine(3));
                return map;
            } else{
                throw new EventException("Cannot convert e to MCSignChangeEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if(event instanceof MCSignChangeEvent){
                MCSignChangeEvent pje = (MCSignChangeEvent)event;
                int index;
                if(key.equals("lineone")){
                	index = 0;
                } else if(key.equals("linetwo")){
                	index = 1;
                } else if(key.equals("linethree")){
                	index = 2;
                } else if(key.equals("linefour")){
                	index = 3;
                } else {
                	return false;
                }
                
                if(value instanceof CNull){
                    pje.setLine(index, "");
                    return pje.getLine(index).toString() == "";
                } else {
                	pje.setLine(0, value.val());
                    return pje.getLine(0).toString() == value.val();
                }
            }
            return false;
        }
        
        public BindableEvent convert(CArray manual){
            MCSignChangeEvent e = EventBuilder.instantiate(MCSignChangeEvent.class, Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN), manual.get("block").val());
            return e;
        }   
    }
}
