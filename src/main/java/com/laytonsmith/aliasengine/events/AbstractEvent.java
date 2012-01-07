/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.exceptions.EventException;
import com.laytonsmith.aliasengine.exceptions.PrefilterNonMatchException;
import java.util.HashMap;
import java.util.Map;

/**
 * This helper class implements a few of the common functions in event, and
 * most (all?) Events should extend this class.
 * @author layton
 */
public abstract class AbstractEvent implements Event, Comparable<Event> {
    
    protected AbstractEventMixin mixin;
    protected AbstractEventHandler handler;
    
    protected AbstractEvent(AbstractEventHandler handler){
        this.handler = handler;
    }
    
    public final void setAbstractEventMixin(AbstractEventMixin mixin){
        this.mixin = mixin;
    }
    

    /**
     * If the event needs to run special code when a player binds the event, it
     * can be done here. By default, an UnsupportedOperationException is thrown,
     * but is caught and ignored.
     */
    public void bind() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * If the event needs to run special code at server startup, it can be done
     * here. By default, an UnsupportedOperationException is thrown, but is caught
     * and ignored.
     */
    public void hook() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    /**
     * This function is run when the actual event occurs. By default, the script
     * is simply run with the BoundEvent's environment, however this could be overridden
     * if necessary. It should eventually run the script however.
     * @param s
     * @param b 
     */
    public void execute(Script s, BoundEvent b){     
        s.run(null, b.getEnv(), null);
    }

    /**
     * For sorting and optimizing events, we need a comparison operation. By default
     * it is compared by looking at the event name.
     * @param o
     * @return 
     */
    public int compareTo(Event o) {
        return this.getName().compareTo(o.getName());
    }
    
    /**
     * Since most events are minecraft events, we return true by default.
     * @return 
     */
    public boolean supportsExternal(){
        return true;
    }
    
    /**
     * If it is ok to by default do a simple conversion from a CArray to a
     * Map, this method can do it for you. Likely this is not acceptable,
     * so hard-coding the conversion will be necessary.
     * @param manualObject
     * @return 
     */
    public static Object DoConvert(CArray manualObject){
        Map<String, Construct> map = new HashMap<String, Construct>();
        for(Construct key : manualObject.keySet()){
            map.put(key.val(), manualObject.get(key, 0));
        }
        return map;        
    }
    
    public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException{
        return handler.matches(prefilter, e);
    }
    
    public Map<String, Construct> evaluate(Object e) throws EventException{
        return handler.evaluate(e, this.mixin);
    }
    
    public Object convert(CArray manual){
        return handler.convert(manual);
    }
    
    public void modifyEvent(String key, Construct value, Object event){
        handler.modifyEvent(key, value, event);
    }
    
    /**
     * By default, this function triggers the event by calling the mixin
     * handler. If this is not the desired behavior, this method can be overridden
     * in the actual event (if it's an external event, for instance)
     * @param o 
     */
    public void manualTrigger(Object o){
        mixin.manualTrigger(o);
    }
    
    public void cancel(Object o){
        mixin.cancel(o);
    }
    
    public boolean isCancellable(Object o){
        return mixin.isCancellable(o);
    }
    
    
}
