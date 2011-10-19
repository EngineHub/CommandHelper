/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.events.BoundEvent;
import com.laytonsmith.aliasengine.events.EventHandler;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.exceptions.EventException;
import java.io.File;
import java.util.List;
import org.bukkit.command.CommandSender;

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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            return new CVoid(line_num, f);
        }
        
        public Construct execs(Construct name, Construct options, Construct prefilter, 
                Construct event_obj, GenericTreeNode<Construct> tree, List<IVariable> vars, int line_num, File f){
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
                        ((IVariable)event_obj).getName(), vars, tree);
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            return EventHandler.DumpEvents();
        }
        
    }
    
}
