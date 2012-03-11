/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import java.util.HashMap;
import java.util.Map;

/**
 * This helper class implements a few of the common functions in event, and
 * most (all?) Events should extend this class.
 * @author layton
 */
public abstract class AbstractEvent implements Event, Comparable<Event> {
    
    private EventMixinInterface mixin;
//    protected EventHandlerInterface handler;
//    
//    protected AbstractEvent(EventHandlerInterface handler){
//        this.handler = handler;
//    }
//    
    public final void setAbstractEventMixin(EventMixinInterface mixin){
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
     * This function is run when the actual event occurs.
     * @param s
     * @param b 
     */
    public final void execute(Script s, BoundEvent b, Env env, BoundEvent.ActiveEvent activeEvent) throws ConfigRuntimeException{          
        try{
            preExecution(env, activeEvent);
        } catch(UnsupportedOperationException e){
            //Ignore. This particular event doesn't need to customize
        }
        s.run(null, env, null);
        try{
            this.postExecution(env, activeEvent);
        } catch(UnsupportedOperationException e){
            //Ignore.
        }
    }
    
    public void preExecution(Env env, BoundEvent.ActiveEvent activeEvent){
        throw new UnsupportedOperationException();
    }
    
    public void postExecution(Env env, BoundEvent.ActiveEvent activeEvent){
        throw new UnsupportedOperationException();
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
        for(String key : manualObject.keySet()){
            map.put(key, manualObject.get(key, 0, null));
        }
        return map;        
    }
    
    public Map<String, Construct> evaluate_helper(BindableEvent e) throws EventException{
        return mixin.evaluate_helper(e);
    }
    
    /**
     * By default, this function triggers the event by calling the mixin
     * handler. If this is not the desired behavior, this method can be overridden
     * in the actual event (if it's an external event, for instance)
     * @param o 
     */
    public void manualTrigger(BindableEvent o){
        mixin.manualTrigger(o);
    }
    
    public void cancel(BindableEvent o, boolean state){
        mixin.cancel(o, state);
    }
    
    public boolean isCancellable(BindableEvent o){
        return mixin.isCancellable(o);
    }
    
    
}
