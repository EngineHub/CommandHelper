/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.core.functions;

import com.laytonsmith.puls3.core.constructs.CVoid;
import com.laytonsmith.puls3.core.constructs.Construct;
import com.laytonsmith.puls3.core.Env;
import com.laytonsmith.puls3.core.Static;
import com.laytonsmith.puls3.core.api;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.puls3.core.functions.Exceptions.ExceptionType;
import java.io.File;
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
        return "This class provides functions for hooking into Puls3's powerful Performance measuring. To use the functions, you must have"
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
    
    @api public static class enable_performance_logging implements Function{

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

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(!(Boolean)Static.getPreferences().getPreference("allow-profiling")){
                throw new ConfigRuntimeException("allow-profiling is currently off, you must set it to true in your preferences.", ExceptionType.SecurityException, line_num, f);
            }
            PERFORMANCE_LOGGING = Static.getBoolean(args[0]);
            return new CVoid(line_num, f);
        }
        
    }
}
