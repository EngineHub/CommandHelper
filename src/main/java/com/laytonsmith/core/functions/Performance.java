

package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.perf4j.StopWatch;

/**
 *

 */
public class Performance {
    public static boolean PERFORMANCE_LOGGING = false;
    public static String docs(){
        return "This class provides functions for hooking into CommandHelper's powerful Performance measuring. To use the functions, you must have"
                + " allow-profiling option set to true in your preferences file.";
    }

    public static void DoLog(File root, StopWatch stopWatch) {
        try {            
            Static.QuickAppend(Static.profilingLogFile(root), "start[" + stopWatch.getStartTime() + "] time[" + stopWatch.getElapsedTime() + "] " 
                    + "tag[" + stopWatch.getTag() + "]\n");
        } catch (IOException ex) {
            Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @api public static class enable_performance_logging extends AbstractFunction{

		@Override
        public String getName() {
            return "enable_performance_logging";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1};
        }

		@Override
        public String docs() {
            return "void {boolean} Enables performance logging. The allow-profiling option must be set to true in your preferences file,"
                    + " and play-dirty mode must be active. If allow-profiling is set to false, a SecurityException is thrown."
                    + " The debug filters are used by the performance logger, if you choose to filter through the events."
                    + " See the documenation"
                    + " for more details on performance logging.";
        }

		@Override
        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CRESecurityException.class};
        }

		@Override
        public boolean isRestricted() {
            return true;
        }
		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public Boolean runAsync() {
            return null;
        }

		@Override
        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            if(!Prefs.AllowProfiling()){
                throw ConfigRuntimeException.BuildException("allow-profiling is currently off, you must set it to true in your preferences.", CRESecurityException.class, t);
            }
            PERFORMANCE_LOGGING = Static.getBoolean(args[0]);
            return CVoid.VOID;
        }
        
    }
}
