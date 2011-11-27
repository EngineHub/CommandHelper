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
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an actually bound event. When the script runs bind(), a
 * new BoundEvent is created as a closure. 
 * @author layton
 */
public class BoundEvent implements Comparable<BoundEvent> {

    private final String eventName;
    private final String id;
    private final String priority;
    private final Map<String, Construct> prefilter;
    private final String eventObjName;
    private final Env env;
    private final GenericTreeNode<Construct> tree; //The code closure for this event
    private final org.bukkit.event.Event.Type driver; //For efficiency sake, cache it here
    private static int EventID = 0;

    /**
     * Returns a unique ID that can be used to identify an event.
     * @return 
     */
    private static int GetUniqueID() {
        synchronized (BoundEvent.class) {
            return ++EventID;
        }
    }

    /**
     * Event priorities. This is sorted and events are run in a particular order.
     */
    public enum Priority {
        LOWEST(5),
        LOW(4),
        NORMAL(3),
        HIGH(2),
        HIGHEST(1),
        MONITOR(1000);
        private final int id;

        private Priority(int i) {
            this.id = i;
        }

        public int getId() {
            return this.id;
        }
    }

    /**
     * Compares two event's IDs, and if they are the same, they should
     * be the actual same event. Since only one event of a given ID exists,
     * technically == should work on these events.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundEvent) {
            return this.id.equals(((BoundEvent) obj).id);
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

    /**
     * Constructs a new BoundEvent.
     * @param name The name of the event
     * @param options The options for this event. Contains the priority and assigned id, possibly
     * @param prefilter The prefilter provided by the user
     * @param eventObjName The name of the variable that should be assigned the event object
     * @param env The script's environment
     * @param tree The closure of the BoundEvent
     * @throws EventException If the priority or id are improperly specified
     */
    public BoundEvent(String name, CArray options, CArray prefilter, String eventObjName,
            Env env, GenericTreeNode<Construct> tree) throws EventException {
        this.eventName = name;

        if (options != null && options.contains("id")) {
            this.id = options.get("id").val();
            if (this.id.matches(".*?:\\d*?")) {
                throw new EventException("The id given may not match the format\"string:number\"");
            }
        } else {
            //Generate a new event id
            id = name + ":" + GetUniqueID();
        }
        if (options != null && options.contains("priority")) {
            this.priority = options.get("priority").val().toUpperCase();
        } else {
            this.priority = "NORMAL";
        }
        if (!(this.priority.equals("LOWEST")
                || this.priority.equals("LOW")
                || this.priority.equals("NORMAL")
                || this.priority.equals("HIGH")
                || this.priority.equals("HIGHEST")
                || this.priority.equals("MONITOR"))) {
            throw new EventException("Priority must be one of: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR");
        }

        this.prefilter = new HashMap<String, Construct>();
        if (prefilter != null) {
            for (Construct key : prefilter.keySet()) {
                String k = key.val();
                this.prefilter.put(k, prefilter.get(key, 0));
            }
        }

        this.env = env;
        this.tree = tree;

        this.driver = EventList.getEvent(this.eventName).driver();
        this.eventObjName = eventObjName;

    }

    public String getEventName() {
        return eventName;
    }

    public String getEventObjName() {
        return eventObjName;
    }

    public org.bukkit.event.Event.Type getDriver() {
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

    public Env getEnv() {
        return env;
    }

    /**
     * Events are sorted by priority
     * @param o
     * @return 
     */
    public int compareTo(BoundEvent o) {
        if (this.getPriority().getId() < o.getPriority().getId()) {
            return -1;
        } else if (this.getPriority().getId() > o.getPriority().getId()) {
            return 1;
        } else {
            return this.id.compareTo(o.id);
        }
    }

    /**
     * When the event actually occurs, this should be run, after translating the
     * original event object (of whatever type it may be) into a standard map, which
     * contains the event object data. It is converted into a CArray here, and then
     * the script is executed with the driver's execute function.
     * @param event 
     */
    public void trigger(Map<String, Construct> event) throws EventException {
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        CArray ca = new CArray(0, null);
        for (String key : event.keySet()) {
            ca.set(new CString(key, 0, null), event.get(key));
        }
        env.GetVarList().set(new IVariable(eventObjName, ca, 0, null));
        env.SetEvent(this);

        GenericTreeNode<Construct> superRoot = new GenericTreeNode<Construct>(null);
        superRoot.addChild(tree);
        Script s = Script.GenerateScript(superRoot, env);
        Event myDriver = EventList.getEvent(this.getDriver(), this.getEventName());
        myDriver.execute(s, this);
    }
}
