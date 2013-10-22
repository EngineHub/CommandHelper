

package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.Common.DateUtils;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.profiler.ProfilePoint;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents an actually bound event. When the script runs bind(), a
 * new BoundEvent is created as a closure. 
 * @author layton
 */
public class BoundEvent implements Comparable<BoundEvent> {

    private final String eventName;
    private final String id;
    private final Priority priority;
    private final Map<String, Construct> prefilter;
    private final String eventObjName;
    private Environment originalEnv;
    private final ParseTree tree; //The code closure for this event
    private final Driver driver; //For efficiency sake, cache it here
    private static int EventID = 0;
    private final Target target;

    /**
     * Returns a unique ID that can be used to identify an event.
     * @return 
     */
    private static int GetUniqueID() {
        synchronized (BoundEvent.class) {
            return ++EventID;
        }
    }

	public Environment getEnvironment() {
		try {
			return originalEnv.clone();
		} catch (CloneNotSupportedException ex) {
			throw new Error(ex);
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
        
        public boolean isHigherPriority(Priority other){
            return other.getId() > this.getId();
        }
        
        public boolean isLowerPriority(Priority other){
            return other.getId() < this.getId();
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
            Environment env, ParseTree tree, Target t) throws EventException {
        this.eventName = name;

        if (options != null && options.containsKey("id")) {
            this.id = options.get("id").val();
            if (this.id.matches(".*?:\\d*?")) {
                throw new EventException("The id given may not match the format\"string:number\"");
            }
        } else {
            //Generate a new event id
            id = name + ":" + GetUniqueID();
        }
        if (options != null && options.containsKey("priority")) {
            try{
            this.priority = Priority.valueOf(options.get("priority").val().toUpperCase());
            } catch(IllegalArgumentException e){
                throw new EventException("Priority must be one of: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR");                
            }
        } else {
            this.priority = Priority.NORMAL;
        }

        this.prefilter = new HashMap<String, Construct>();
        if (prefilter != null) {
            for (String key : prefilter.keySet()) {
                this.prefilter.put(key, prefilter.get(key, Target.UNKNOWN));
            }
        }

        this.originalEnv = env;
        this.tree = tree;
        
        if(EventList.getEvent(this.eventName) == null){
            throw new EventException("No event named \"" + this.eventName + "\" is registered!");
        }
        this.driver = EventList.getEvent(this.eventName).driver();
        this.eventObjName = eventObjName;
        
        this.target = t;

    }

    public int getLineNum(){
        return target.line();
    }
    
    public File getFile(){
        return target.file();
    }
    
    public int getCol(){
        return target.col();
    }
    
    public Target getTarget(){
        return target;
    }
    
    public String getEventName() {
        return eventName;
    }

    public String getEventObjName() {
        return eventObjName;
    }

    public Driver getDriver() {
        return driver;
    }

    public String getId() {
        return id;
    }

    public Map<String, Construct> getPrefilter() {
        return prefilter;
    }

    public Priority getPriority() {
        return priority;
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
    public void trigger(ActiveEvent activeEvent) throws EventException {
        try {
    //        GenericTree<Construct> root = new GenericTree<Construct>();
    //        root.setRoot(tree);
            Environment env = originalEnv.clone();
            CArray ca = new CArray(Target.UNKNOWN);
            for (String key : activeEvent.parsedEvent.keySet()) {
                ca.set(new CString(key, Target.UNKNOWN), activeEvent.parsedEvent.get(key), Target.UNKNOWN);
            }
            if(activeEvent.parsedEvent.containsKey("player")){
                try{
                    MCPlayer p = Static.GetPlayer(activeEvent.parsedEvent.get("player"), Target.UNKNOWN);
                    if(p != null && p.isOnline()){
                        env.getEnv(CommandHelperEnvironment.class).SetPlayer(p);                                        
                    }                    
                } catch(ConfigRuntimeException e){
                    if(!e.getExceptionType().equals(Exceptions.ExceptionType.PlayerOfflineException)){
                        throw e;
                    }
                    //else we just leave the player to be null. It either doesn't matter here,
                    //or the event will add it later, manually.
                }
            }
            env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(eventObjName, ca, Target.UNKNOWN));
            env.getEnv(CommandHelperEnvironment.class).SetEvent(activeEvent);
            activeEvent.addHistory("Triggering bound event: " + this);
            try{
				ProfilePoint p = env.getEnv(GlobalEnv.class).GetProfiler().start("Executing event handler for " + this.getEventName() + " defined at " + this.getTarget(), LogLevel.ERROR);
				try {
					this.execute(env, activeEvent);
				} finally {
					p.stop();
				}
            } catch(ConfigRuntimeException e){
                //We don't know how to handle this, but we need to set the env,
                //then pass it up the chain
                e.setEnv(env);
                throw e;
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BoundEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Used to manually trigger an event, the underlying event is set to null.
     * @param event
     * @throws EventException 
     */
    public void manual_trigger(CArray event) throws EventException{
        try {
            Environment env = originalEnv.clone();
            env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(eventObjName, event, Target.UNKNOWN));
            Map<String, Construct> map = new HashMap<String, Construct>();
            for(String key : event.keySet()){
                map.put(key, event.get(key, Target.UNKNOWN));
            }
            ActiveEvent activeEvent = new ActiveEvent(null);
            activeEvent.setParsedEvent(map);
            activeEvent.setBoundEvent(this);
            env.getEnv(CommandHelperEnvironment.class).SetEvent(activeEvent);
            this.execute(env, activeEvent);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BoundEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void execute(Environment env, ActiveEvent activeEvent) throws EventException{
        ParseTree superRoot = new ParseTree(null);
        superRoot.addChild(tree);
        Script s = Script.GenerateScript(superRoot, PermissionsResolver.GLOBAL_PERMISSION);        
        Event myDriver = this.getEventDriver();
        myDriver.execute(s, this, env, activeEvent);
    }
    
	//TODO: Once ParseTree supports these again, we may bring this back
//    /**
//     * Returns true if this event MUST be synchronous.
//     * @return 
//     */
//    public boolean isSync(){
//	    return tree.isSync();
//    }
//    
//    /**
//     * Returns true if this event MUST be asynchronous.
//     * @return 
//     */
//    public boolean isAsync(){
//	    return tree.isAsync();
//    }
    
    public ParseTree getParseTree(){
	    return tree;
    }
    
    /**
     * Returns the Event driver that knows how to handle this event.
     * @return 
     */
    public Event getEventDriver(){
        return EventList.getEvent(this.getDriver(), this.getEventName());
    }
    
    /**
     * The bound event is essentially an ActiveEvent generator. Because bound events don't change from run to run, it doesn't
     * make sense to store triggered event specific information with the bound event itself. Instead, when the event is triggered,
     * an ActiveEvent is generated, stored in the environment, and then the script is triggered. This ActiveEvent contains both
     * the underlying event (if needed for things like cancellation or other event manipulation) and the BoundEvent object itself
     * (which can be used to get the event id and other information as needed). For convenience, the parsed event information
     * is also cached here.
     */
    public static class ActiveEvent{
        private final BindableEvent underlyingEvent;
        private Map<String, Construct> parsedEvent;
        private BoundEvent boundEvent;
        private Boolean cancelled;
        private BoundEvent consumedAt;
        private final Map<String, BoundEvent> lockedAt;
        private final List<Pair<CClosure, Environment>> whenCancelled;
        private final List<Pair<CClosure, Environment>> whenTriggered;
        
        private final List<String> history;
        
        public ActiveEvent(BindableEvent underlyingEvent){
            this.underlyingEvent = underlyingEvent;
            this.cancelled = null;
            whenCancelled = new ArrayList<Pair<CClosure, Environment>>();
            whenTriggered = new ArrayList<Pair<CClosure, Environment>>();
            lockedAt = new HashMap<String, BoundEvent>();
            history = new ArrayList<String>();
        }
        
        public void addHistory(String history){
            if(Prefs.DebugMode()){
                this.history.add(DateUtils.ParseCalendarNotation("%Y-%M-%D %h:%m.%s - ") + history);
            }
        }
        
        public List<String> getHistory(){
            return history;
        }

        public Map<String, Construct> getParsedEvent() {
            return parsedEvent;
        }

        public BindableEvent getUnderlyingEvent() {
            return underlyingEvent;
        }

        public BoundEvent getBoundEvent() {
            return boundEvent;
        }
        
        public void setBoundEvent(BoundEvent boundEvent){
            this.boundEvent = boundEvent;
        }
        
        public void setParsedEvent(Map<String, Construct> parsedEvent){
            this.parsedEvent = parsedEvent;
        }

        public boolean isCancelled() {
            //if cancelled is not null, return it. If it is null, check with the underlying event.
            //If it isn't null, that means we have manually set it somewhere, so that takes precedence;
            //indeed, it may not make sense to ask the event, as it may not be cancellable in the first
            //place, but we can still return regardless.
            if(cancelled != null){
                return cancelled;
            } else {
                if(boundEvent.getEventDriver().isCancellable(underlyingEvent)){
                    return boundEvent.getEventDriver().isCancelled(underlyingEvent);
                } else {
                    return false;
                }
            }
        }

        public void setCancelled(boolean cancelled) {
            this.addHistory("Setting cancelled flag to " + cancelled + " " + boundEvent);
            this.cancelled = cancelled;
            try {
                boundEvent.getEventDriver().cancel(underlyingEvent, cancelled);
            } catch (EventException ex) {
                //Ignore this exception. This is thrown if the event isn't cancellable.
                //Who cares.
            }
        }         
        
        public Event getEventDriver(){
            return this.boundEvent.getEventDriver();
        }

        public boolean isCancellable() {
            return boundEvent.getEventDriver().isCancellable(this.underlyingEvent);
        }
        
        public void consume(){
            this.addHistory("Consuming event" + boundEvent);
            if(consumedAt == null){
                consumedAt = boundEvent;
            }
        }
        
        public boolean canReceive(){
            if(consumedAt == null){
                return true;
            }
            return consumedAt.getPriority().isLowerPriority(boundEvent.getPriority());
        }
        
        public boolean isConsumed(){
            return consumedAt != null;
        }
        
        public Priority consumedAt(){
            return consumedAt.getPriority();
        }
        
        public void lock(String parameter){
            this.addHistory("Locking " + (parameter==null?"the whole event":parameter) + " " + boundEvent);
            if(lockedAt.containsKey(null)){
                return; //Everything is already locked
            }
            if(parameter == null && !lockedAt.containsKey(null)){
                lockedAt.put(null, boundEvent); //Everything is locked now
            } else if(!lockedAt.containsKey(parameter)) {
                lockedAt.put(parameter, boundEvent);
            }
        }
        
        public boolean isLocked(String parameter){           
            Priority param = lockedAt.get(parameter)==null?null:lockedAt.get(parameter).getPriority();
            Priority global = lockedAt.get(parameter)==null?null:lockedAt.get(null).getPriority();
            if(param == null && global == null){
                return false;
            } else if(param == null){
                return global.isHigherPriority(boundEvent.getPriority());
            } else if(global == null){
                return param.isHigherPriority(boundEvent.getPriority());
            } else {
                if(param.isHigherPriority(global)){
                    return param.isHigherPriority(boundEvent.getPriority());
                } else {
                    return global.isHigherPriority(boundEvent.getPriority());
                }
            }
        }
        
        public Priority lockedAt(String parameter){            
            Priority param = lockedAt.get(parameter)==null?null:lockedAt.get(parameter).getPriority();
            Priority global = lockedAt.get(parameter)==null?null:lockedAt.get(null).getPriority();
            if(param == null && global == null){
                return null; //It's not locked
            } else if(param == null){
                return global; //It's not parameter locked, but it is globally locked
            } else if(global == null){
                return param; //It's not globally locked, but it is parameter locked
            } else {
                //It's both. The higher priority one wins.
                if(param.isHigherPriority(global)){
                    return param;
                } else {
                    return global;
                }
            }
        }
        
        public void addWhenTriggered(CClosure tree){
            this.addHistory("Adding a whenTriggered callback. " + boundEvent);
            try {
                whenTriggered.add(new Pair<CClosure, Environment>(tree, boundEvent.originalEnv.clone()));
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(BoundEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public void addWhenCancelled(CClosure tree){
            this.addHistory("Adding a whenCancelled callback. " + boundEvent);
            try {
                whenCancelled.add(new Pair<CClosure, Environment>(tree, boundEvent.originalEnv.clone()));
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(BoundEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public void executeTriggered(){
//            for(Pair<CClosure, Env> pair : whenTriggered){
//                MethodScriptCompiler.execute(pair.fst, pair.snd, null, null);
//            }            
        }
        
        public void executeCancelled(){
//            for(Pair<CClosure, Env> pair : whenCancelled){
//                MethodScriptCompiler.execute(pair.fst, pair.snd, null, null);
//            }            
        }
    }
}
