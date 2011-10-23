/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.GenericTree;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.GenericTreeTraversalOrderEnum;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class BoundEvent implements Comparable<BoundEvent> {
    private final String eventName;
    private final String id;
    private final String priority;
    private final Map<String, Construct> prefilter;
    private final String eventObjName;    
    private final Map<String, IVariable> vars;
    private final List<String> custom_names = new ArrayList<String>();
    private final GenericTreeNode<Construct> tree;
    private final org.bukkit.event.Event.Type driver; //For efficiency sake, cache it here
    
    private static int EventID = 0;
    private static int GetUniqueID(){
        return ++EventID;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BoundEvent){
            return this.id.equals(((BoundEvent)obj).id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "(" + eventName + ") " + id;
    }
    
    public BoundEvent(String name, CArray options, CArray prefilter, String eventObjName, 
            Env env, GenericTreeNode<Construct> tree) throws EventException{
        this.eventName = name;
        
        if(options != null && options.contains("id")){
            this.id = options.get("id").val();
            if(this.id.matches(".*?:\\d*?")){
                throw new EventException("The id given may not match the format\"string:number\"");
            }
        } else {
            //Generate a new event id
            id = name + ":" + GetUniqueID();
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
        
        this.vars = new HashMap<String, IVariable>();
        for(IVariable v : vars){
            this.vars.put(v.getName(), v);
        }
        this.tree = tree;      
        
        this.driver = EventList.getEvent(this.eventName).driver();
        this.eventObjName = eventObjName;
        
        for(IVariable v : vars){
            custom_names.add(v.getName());
        }
    }
    
    public String getEventName(){
        return eventName;
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
        LOWEST(5),
        LOW(4),
        NORMAL(3),
        HIGH(2),
        HIGHEST(1),
        MONITOR(1000);
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
           return this.id.compareTo(o.id);
       }
    }
    
    public void trigger(Map<String, Construct> event){
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        for(GenericTreeNode<Construct> node : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            Construct c = node.getData();
            if(c instanceof IVariable){
                IVariable var = ((IVariable)c);
                if(custom_names.contains(var.getName())){
                    try {
                        //Custom variable
                        var.setIval(this.vars.get(var.getName()).ival().clone());
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(BoundEvent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if(var.getName().equals(eventObjName)){
                    //Event object
                    CArray ca = new CArray(0, null);
                    for(String key : event.keySet()){
                        ca.set(new CString(key, 0, null), event.get(key));
                    }
                    var.setIval(ca);
                } else {
                    //Set the default value
                    var.setIval(new CString("", 0, null));
                }
            }
        }
        GenericTreeNode<Construct> superRoot = new GenericTreeNode<Construct>(null);
        superRoot.addChild(tree);
        Script s = Script.GenerateScript(superRoot);
        Event myDriver = EventList.getEvent(this.getDriver(), this.getEventName());
        myDriver.execute(s, this);
    }
}
