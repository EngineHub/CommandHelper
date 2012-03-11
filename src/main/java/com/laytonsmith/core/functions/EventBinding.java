/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.BoundEvent.Priority;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class EventBinding {
    public static String docs(){
        return "This class of functions provide methods to hook deep into the server's event architecture";
    }
    
    @api public static class bind implements Function{

        public String getName() {
            return "bind";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "string {event_name, options, prefilter, event_obj, [custom_params], &lt;code&gt;} Binds some functionality to an event, so that"
                    + " when said event occurs, the event handler will fire. Returns the id of this event, so it can be unregistered"
                    + " later, if need be.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return false;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return new CVoid(line_num, f);
        }
        
        public Construct execs(Construct name, Construct options, Construct prefilter, 
                Construct event_obj, GenericTreeNode<Construct> tree, Env env, int line_num, File f){
            //Check to see if our arguments are correct
            if(!(options instanceof CNull || options instanceof CArray)){
                throw new ConfigRuntimeException("The options must be an array or null", ExceptionType.CastException, line_num, f);
            }
            if(!(prefilter instanceof CNull || prefilter instanceof CArray)){
                throw new ConfigRuntimeException("The prefilters must be an array or null", ExceptionType.CastException, line_num, f);                
            }
            if(!(event_obj instanceof IVariable)){                
                throw new ConfigRuntimeException("The event object must be an IVariable", ExceptionType.CastException, line_num, f);                
            }
            CString id;
            if(options instanceof CNull){
                options = null;
            }
            if(prefilter instanceof CNull){
                prefilter = null;
            }
            try {
                BoundEvent be = new BoundEvent(name.val(), (CArray)options, (CArray)prefilter, 
                        ((IVariable)event_obj).getName(), env, tree, line_num, f);
                EventUtils.RegisterEvent(be);
                id = new CString(be.getId(), line_num, f);
            } catch (EventException ex) {
                throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.BindException, line_num, f);
            }
            
            return id;
        }
        
    }
    
    @api public static class dump_events implements Function{

        public String getName() {
            return "dump_events";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "array {} Returns an array of all the events currently registered on the server. Mostly meant for debugging,"
                    + " however it would be possible to parse this response to cherry pick events to unregister.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return EventUtils.DumpEvents();
        }
        
    }
    
    @api public static class unbind implements Function{

        public String getName() {
            return "unbind";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[eventID]} Unbinds an event, which causes it to not run anymore. If called from within an event handler, eventID is"
                    + " optional, and defaults to the current event id.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            String id = null;
            if(args.length == 1){
                //We are cancelling an arbitrary event
                id = args[0].val();
            } else {
                //We are cancelling this event. If we are not in an event, throw an exception
                if(environment.GetEvent() == null){
                    throw new ConfigRuntimeException("No event ID specified, and not running inside an event", ExceptionType.BindException, line_num, f);
                }
                id = environment.GetEvent().getBoundEvent().getId();
            }
            EventUtils.UnregisterEvent(id);
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class cancel implements Function{

        public String getName() {
            return "cancel";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[state]} Cancels the event (if applicable). If the event is not cancellable, or is already cancelled, nothing happens."
                    + " If called from outside an event handler, a BindException is thrown. By default, state is true, but you can"
                    + " uncancel an event (if possible) by calling cancel(false).";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            boolean cancelled = true;
            if(args.length == 1){
                cancelled = Static.getBoolean(args[0]);
            }            
            
            BoundEvent.ActiveEvent original = environment.GetEvent();
            if(original == null){
                throw new ConfigRuntimeException("cancel cannot be called outside an event handler", ExceptionType.BindException, line_num, f);
            }
            if(original.getUnderlyingEvent() != null && original.isCancellable()){
                original.setCancelled(cancelled);
            }
            environment.GetEvent().setCancelled(true);
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class is_cancelled implements Function{

        public String getName() {
            return "is_cancelled";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "boolean {} Returns whether or not the underlying event is cancelled or not. If the event is not cancellable in the first place,"
                    + " false is returned. If called from outside an event, a BindException is thrown";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            BoundEvent.ActiveEvent original = environment.GetEvent();
            if(original == null){
                throw new ConfigRuntimeException("is_cancelled cannot be called outside an event handler", ExceptionType.BindException, line_num, f);
            }
            boolean result = false;
            if(original.getUnderlyingEvent() != null && original.isCancellable()){
                result = original.isCancelled();
            }
            return new CBoolean(result, line_num, f);
        }
        
    }
    
    @api public static class trigger implements Function{

        public String getName() {
            return "trigger";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {eventName, eventObject, [serverWide]} Manually triggers bound events. The event object passed to this function is "
                    + " sent directly as-is to the bound events. Check the documentation for each event to see what is required."
                    + " No checks will be done on the data here, but it is not recommended to fail to send all parameters required."
                    + " If serverWide is true, the event is triggered directly in the server, unless it is a CommandHelper specific"
                    + " event, in which case, serverWide is irrelevant. Defaults to false, which means that only CommandHelper code"
                    + " will receive the event.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            CArray obj = null;
            if(args[1] instanceof CNull){
                obj = new CArray(line_num, f);
            } else if(args[1] instanceof CArray){
                obj = (CArray)args[1];
            } else {
                throw new ConfigRuntimeException("The eventObject must be null, or an array", ExceptionType.CastException, line_num, f);
            }
            boolean serverWide = false;
            if(args.length == 3){
                serverWide = Static.getBoolean(args[2]);
            }
            EventUtils.ManualTrigger(args[0].val(), obj, serverWide);
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class modify_event implements Function{

        public String getName() {
            return "modify_event";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "boolean {parameter, value, [throwOnFailure]} Modifies the underlying event object, if applicable."
                    + " The documentation for each event will explain what parameters can be modified,"
                    + " and what their expected values are. If an invalid parameter name is passed in,"
                    + " nothing will happen. If this function is called from outside an event"
                    + " handler, a BindException is thrown. Note that modifying the underlying event"
                    + " will NOT update the event object passed in to the event handler. The function returns"
                    + " whether or not the parameter was updated successfully. It could fail to modify the"
                    + " event if a higher priority handler has locked this parameter, or if updating the underlying"
                    + " event failed. If throwOnFailure is true, instead of returning false, it will throw"
                    + " a BindException. The default for throwOnFailure is false. If a monitor level handler"
                    + " even attempts to modify an event, an exception will be thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            String parameter = args[0].val();
            Construct value = args[1];
            boolean throwOnFailure = false;
            if(args.length == 3){
                throwOnFailure = Static.getBoolean(args[3]);
            }
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException(this.getName() + " must be called from within an event handler", ExceptionType.BindException, line_num, f);
            }
            Event e = environment.GetEvent().getEventDriver();
            if(environment.GetEvent().getBoundEvent().getPriority().equals(Priority.MONITOR)){
                throw new ConfigRuntimeException("Monitor level handlers may not modify an event!", ExceptionType.BindException, line_num, f);
            }
            ActiveEvent active = environment.GetEvent();
            boolean success = false;
            if(!active.isLocked(parameter)){
                try{
                    success = e.modifyEvent(parameter, value, environment.GetEvent().getUnderlyingEvent());
                } catch(ConfigRuntimeException ex){
                    ex.setFile(f);
                    ex.setLineNum(line_num);
                    throw ex;
                }
            } else {
                success = false;
            }
            if(throwOnFailure && !success){
                throw new ConfigRuntimeException("Event parameter is already locked!", ExceptionType.BindException, line_num, f);
            }
            return new CBoolean(success, line_num, f);
        }
        
    }
    
    @api public static class lock implements Function{

        public String getName() {
            return "lock";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "void {<none> | parameterArray | parameter, [parameter...]} Locks the specified event parameter(s), or all of them,"
                    + " if specified with no arguments. Locked parameters become read only for lower priority event handlers.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException("lock must be called from within an event handler", ExceptionType.BindException, line_num, f);
            }
            
            BoundEvent.ActiveEvent e = environment.GetEvent();
            Priority p = e.getBoundEvent().getPriority();
            List<String> params = new ArrayList<String>();
            if(args.length == 0){
                e.lock(null);
            } else {                
                if(args[0] instanceof CArray){
                    CArray ca = (CArray)args[1];
                    for(int i = 0; i < ca.size(); i++){
                        params.add(ca.get(i, line_num, f).val());
                    }
                } else {
                    for(int i = 0; i < args.length; i++){
                        params.add(args[i].val());
                    }
                }
            }
            for(String param : params){
                e.lock(param);
            }
            return new CVoid(line_num, f);
        }

        public String since() {
            return "3.3.0";
        }
        
    }
    
    @api public static class is_locked implements Function{

        public String getName() {
            return "is_locked";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {parameter} Returns whether or not a call to modify_event() would fail, based on"
                    + " the parameter being locked by a higher priority handler. If this returns false, it"
                    + " is still not a guarantee that the event would be successfully modified, just that"
                    + " it isn't locked.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException("is_locked may only be called from inside an event handler", ExceptionType.BindException, line_num, f);
            }
            boolean locked = environment.GetEvent().isLocked(args[0].val());
            return new CBoolean(locked, line_num, f);
        }

        public String since() {
            return "3.3.0";
        }
        
    }
    
    @api public static class consume implements Function{

        public String getName() {
            return "consume";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "void {} Consumes an event, so that lower priority handlers don't even"
                    + " recieve the event. Monitor level handlers will still recieve it, however,"
                    + " and they can check to see if the event was consumed.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException("consume may only be called from an event handler!", ExceptionType.BindException, line_num, f);
            }
            environment.GetEvent().consume();
            return new CVoid(line_num, f);
        }

        public String since() {
            return "3.3.0";
        }
        
    }
    
    @api public static class is_consumed implements Function{

        public String getName() {
            return "is_consumed";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "boolean {} Returns whether or not this event has been consumed. Usually only useful"
                    + " for Monitor level handlers, it could also be used for highly robust code,"
                    + " as an equal priority handler could have consumed the event, but this handler"
                    + " would still recieve it.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException("is_consumed must be called from within an event handler", ExceptionType.BindException, line_num, f);
            }
            return new CBoolean(environment.GetEvent().isConsumed(), line_num, f);
        }

        public String since() {
            return "3.3.0";
        }
        
    }
  
//    @api public static class when_triggered implements Function{
//        
//    }
    
//    @api public static class when_cancelled implements Function{
//        
//    }
    
    @api public static class event_meta implements Function{

        public String getName() {
            return "event_meta";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "array {} Returns meta information about the activity in regards to this event. This"
                    + " is meant as a debug tool.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.BindException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException("event_meta must be called from within an event handler!", ExceptionType.BindException, line_num, f);
            }
            CArray history = new CArray(line_num, f);
            for(String entry : environment.GetEvent().getHistory()){
                history.push(new CString(entry, line_num, f));
            }
            return history;
        }

        public String since() {
            return "3.3.0";
        }
        
    }
    
}
