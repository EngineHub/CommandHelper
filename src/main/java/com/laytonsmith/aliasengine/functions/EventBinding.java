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
import com.laytonsmith.aliasengine.events.BoundEvent;
import com.laytonsmith.aliasengine.events.EventHandler;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.io.File;
import java.util.List;
import org.bukkit.command.CommandSender;
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
            return "void {[eventID]}";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
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
    
}
