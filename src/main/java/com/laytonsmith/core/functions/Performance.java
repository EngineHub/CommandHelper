

package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
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

        public String getName() {
            return "enable_performance_logging";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "Enables performance logging. The allow-profiling option must be set to true in your preferences file,"
                    + " and play-dirty mode must be active. If allow-profiling is set to false, a SecurityException is thrown."
                    + " The debug filters are used by the performance logger, if you choose to filter through the events."
                    + " See the documenation"
                    + " for more details on performance logging.";
        }
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CBoolean.class, "enabled")
					);
		}

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.SecurityException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            if(!Prefs.AllowProfiling()){
                throw new ConfigRuntimeException("allow-profiling is currently off, you must set it to true in your preferences.", ExceptionType.SecurityException, t);
            }
            PERFORMANCE_LOGGING = args[0].primitive(t).castToBoolean();
            return new CVoid(t);
        }
        
    }
}
