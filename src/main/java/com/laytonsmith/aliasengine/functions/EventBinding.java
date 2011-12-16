/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.events.BoundEvent;
import com.laytonsmith.aliasengine.events.Event;
import com.laytonsmith.aliasengine.events.EventHandler;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.io.File;
import org.bukkit.event.Cancellable;

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
                        ((IVariable)event_obj).getName(), env, tree);
                EventHandler.RegisterEvent(be);
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
            return EventHandler.DumpEvents();
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
            EventHandler.UnregisterEvent(id);
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class cancel implements Function{

        public String getName() {
            return "cancel";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "void {} Cancels the event (if applicable). If the event is not cancellable, or is already cancelled, nothing happens."
                    + " If called from outside an event handler, a BindException is thrown.";
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
            BoundEvent.ActiveEvent original = environment.GetEvent();
            if(original == null){
                throw new ConfigRuntimeException("is_cancelled cannot be called outside an event handler", ExceptionType.BindException, line_num, f);
            }
            if(original.getUnderlyingEvent() != null && original.getUnderlyingEvent() instanceof Cancellable){
                ((Cancellable)original.getUnderlyingEvent()).setCancelled(true);
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
            if(original.getUnderlyingEvent() != null && original.getUnderlyingEvent() instanceof Cancellable){
                result = ((Cancellable)original.getUnderlyingEvent()).isCancelled();
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
            EventHandler.ManualTrigger(args[0].val(), obj, serverWide);
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
            return "void {parameter, value} Modifies the underlying event object, if applicable."
                    + " The documentation for each event will explain what parameters can be modified,"
                    + " and what their expected values are. If an invalid parameter name is passed in,"
                    + " nothing will happen. If this function is called from outside an event"
                    + " handler, a BindException is thrown. Note that modifying the underlying event"
                    + " will NOT update the event object passed in to the event handler.";
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
            if(environment.GetEvent() == null){
                throw new ConfigRuntimeException(this.getName() + " must be called from within an event handler", ExceptionType.BindException, line_num, f);
            }
            Event e = environment.GetEvent().getEventDriver();
            e.modifyEvent(parameter, value, environment.GetEvent().getUnderlyingEvent());
            return new CVoid(line_num, f);
        }
        
    }
    
}
