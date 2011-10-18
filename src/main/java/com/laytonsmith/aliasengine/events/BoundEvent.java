/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.exceptions.EventException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author layton
 */
public class BoundEvent implements Comparable<BoundEvent> {
    String id;
    String priority;
    Map<String, Construct> prefilter;
    String eventObjName;
    List<IVariable> vars;
    GenericTreeNode<Construct> tree;
    org.bukkit.event.Event.Type driver; //For efficiency sake, cache it here
    
    public BoundEvent(String name, CArray options, CArray prefilter, List<IVariable> vars,
            GenericTreeNode<Construct> tree) throws EventException{
        this.eventObjName = name;
        
        if(options != null && options.contains("id")){
            this.id = options.get("id").val();
            if(this.id.matches(".*?:\\d*?")){
                throw new EventException("The id given may not match the format\"string:number\"");
            }
        } else {
            //Generate a new event id
            id = name + ":" + EventHandler.GetUniqueID();
        }
        if(options != null && options.contains("priority")){
            this.priority = options.get("priority").val().toUpperCase();
        } else {
            this.priority = "NORMAL";
        }
        if(!(
                this.priority.equals("LOWEST") ||
                this.priority.equals("LOW") ||
                this.priority.equals("NORMAL") ||
                this.priority.equals("HIGH") ||
                this.priority.equals("HIGHEST") ||
                this.priority.equals("MONITOR")
                )){
            throw new EventException("Priority must be one of: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR");
        }
        
        this.prefilter = new HashMap<String, Construct>();
        if(prefilter != null){
            for(Construct key : prefilter.keySet()){
                String k = key.val();
                this.prefilter.put(k, prefilter.get(key, 0));
            }
        }
        
        this.vars = vars;
        this.tree = tree;      
        
        this.driver = EventList.getEvent(this.eventObjName).driver();
    }

    public String getEventObjName() {
        return eventObjName;
    }
    
    public org.bukkit.event.Event.Type getDriver(){
        return driver;
    }

    public String getId() {
        return id;
    }

    public Map<String, Construct> getPrefilter() {
        return prefilter;
    }

    public Priority getPriority() {
        return Priority.valueOf(priority);
    }
    public enum Priority{
        LOWEST(1),
        LOW(2),
        NORMAL(3),
        HIGH(4),
        HIGHEST(5),
        MONITOR(6);
        private final int id;
        private Priority(int i){
            this.id = i;
        }
        public int getId(){
            return this.id;
        }
    }
    public int compareTo(BoundEvent o) {
       if(this.getPriority().getId() < o.getPriority().getId()){
           return -1;
       } else if(this.getPriority().getId() > o.getPriority().getId()){
           return 1;
       } else {
           return 0;
       }
    }
}
