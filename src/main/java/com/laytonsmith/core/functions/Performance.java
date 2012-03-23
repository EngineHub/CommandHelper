/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.perf4j.StopWatch;

/**
 *
 * @author Layton
 */
public class Performance {
    public static boolean PERFORMANCE_LOGGING = false;
    public static String docs(){
        return "This class provides functions for hooking into CommandHelper's powerful Performance measuring. To use the functions, you must have"
                + " allow-profiling option set to true in your preferences file.";
    }

    public static void DoLog(StopWatch stopWatch) {
        try {            
            Static.QuickAppend(Static.profilingLogFile(), "start[" + stopWatch.getStartTime() + "] time[" + stopWatch.getElapsedTime() + "] " 
                    + "tag[" + stopWatch.getTag() + "]\n");
        } catch (IOException ex) {
            Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @api public static class enable_performance_logging extends AbstractFunction{

        public String getName() {
            return "enable_performance_logging";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {boolean} Enables performance logging. The allow-profiling option must be set to true in your preferences file,"
                    + " and play-dirty mode must be active. If allow-profiling is set to false, a SecurityException is thrown."
                    + " The debug filters are used by the performance logger, if you choose to filter through the events."
                    + " See the documenation"
                    + " for more details on performance logging.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.SecurityException};
        }

        public boolean isRestricted() {
            return true;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(!Prefs.AllowProfiling()){
                throw new ConfigRuntimeException("allow-profiling is currently off, you must set it to true in your preferences.", ExceptionType.SecurityException, t);
            }
            PERFORMANCE_LOGGING = Static.getBoolean(args[0]);
            return new CVoid(t);
        }
        
    }
}
