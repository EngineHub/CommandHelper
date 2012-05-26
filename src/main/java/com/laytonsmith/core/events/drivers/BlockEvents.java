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
            return "{player: <string match> | 1: <macro> | 2: <macro> | "
            		+ "3: <macro> | 4: <macro> } "
            		+ "This event is called when a player changes a sign. "
                    + "Cancelling the event cancels any edits completely."
                    + "{player: The player's name | 1: The first line of the sign | 2: "
                    + "The second line of the sign | 3: The third line of the sign | 4: "
                    + "The fourth line of the sign | block: An array with keys 'X', 'Y', 'Z' and 'world' "
                    + "for the physical location of the sign | text: An array with keys 0 thru 3 defining "
                    + "every line on the sign}"
                    + "{1|2|3|4|text}"
                    + "{player|1|2|3|4|block|text}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public Driver driver(){
            return Driver.SIGN_CHANGED;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
        	if(e instanceof MCSignChangeEvent){
                MCSignChangeEvent ple = (MCSignChangeEvent) e;
                
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }
                
                //Prefilters.match(prefilter, "1", ple.getLine(0), Prefilters.PrefilterType.MACRO);
                //Prefilters.match(prefilter, "2", ple.getLine(1), Prefilters.PrefilterType.MACRO);
                //Prefilters.match(prefilter, "3", ple.getLine(2), Prefilters.PrefilterType.MACRO);
                //Prefilters.match(prefilter, "4", ple.getLine(3), Prefilters.PrefilterType.MACRO);
                
                return true;
            }
            return false;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCSignChangeEvent){
                MCSignChangeEvent ple = (MCSignChangeEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                map.put("player", new CString(ple.getPlayer().getName(), Target.UNKNOWN));

                map.put("text", ple.getLines());
                
                CArray blk = new CArray(Target.UNKNOWN);
                blk.set("X", new CInt(ple.getBlock().getX(), Target.UNKNOWN));
                blk.set("Y", new CInt(ple.getBlock().getX(), Target.UNKNOWN));
                blk.set("Z", new CInt(ple.getBlock().getX(), Target.UNKNOWN));
                blk.set("world", new CString(ple.getBlock().getWorld().getName(), Target.UNKNOWN));
                map.put("location", blk);
                
                return map;
            } else{
                throw new EventException("Cannot convert e to MCSignChangeEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if(event instanceof MCSignChangeEvent){
                MCSignChangeEvent pje = (MCSignChangeEvent)event;
                
                if(key.equals("text")) {
                	if (!(value instanceof CArray)){
                		return false;
                	}
                	
                	CArray val = (CArray)value;
                	if (val.size() != 4) {
                		return false;
                	}
                	
                	String[] lines = {};
                	
                	for (int i=0; i<4; i++) {
                		lines[i] = val.get(i).toString();
                	}
                	
                	pje.setLines(lines);
                	
                	return true;
                }
                
                int index;
                
                if(key.equals("1")){
                	index = 0;
                } else if(key.equals("2")){
                	index = 1;
                } else if(key.equals("3")){
                	index = 2;
                } else if(key.equals("4")){
                	index = 3;
                } else {
                	return false;
                }
                
                if(value instanceof CNull){
                    pje.setLine(index, "");
                    return pje.getLine(index).toString() == "";
                } else {
                	pje.setLine(index, value.val());
                    return pje.getLine(index).toString() == value.val();
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
