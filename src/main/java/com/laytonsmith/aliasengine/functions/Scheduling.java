/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;

/**
 *
 * @author Layton
 */
public class Scheduling {
    public static String docs(){
        return "This class contains methods for dealing with time and server scheduling.";
    }
    @api public static class time implements Function{

        public String getName() {
            return "time";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "int {} Returns the current unix time stamp, in milliseconds. The resolution of this is not guaranteed to be extremely accurate. If "
                    + "you need extreme accuracy, use nano_time()";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CInt(System.currentTimeMillis(), line_num, f);
        }
        
    }
    
    @api public static class nano_time implements Function{

        public String getName() {
            return "nano_time";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "int {} Returns an arbitrary number based on the most accurate clock available on this system. Only useful when compared to other calls"
                    + " to nano_time(). The return is in nano seconds. See the Java API on System.nanoTime() for more information on the usage of this function.";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CInt(System.nanoTime(), line_num, f);
        }
        
    }
    
    public static class sleep implements Function {

        public String getName() {
            return "sleep";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {seconds} Sleeps the script for the specified number of seconds, up to the maximum time limit defined in the preferences file."
                    + " Seconds may be a double value, so 0.5 would be half a second."
                    + " PLEASE NOTE: Sleep times are NOT very accurate, and should not be relied on for preciseness.";
        }
        
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (Thread.currentThread().getName().equals("Server thread")) {
                throw new ConfigRuntimeException("sleep() cannot be run in the main server thread", 
                        null, line_num, f);
            }
            Construct x = args[0];
            double time = Static.getNumber(x);
            Integer i = (Integer) (Static.getPreferences().getPreference("max-sleep-time"));
            if (i > time || i <= 0) {
                try {
                    Thread.sleep((int)(time * 1000));
                } catch (InterruptedException ex) {
                }
            } else {
                throw new ConfigRuntimeException("The value passed to sleep must be less than the server defined value of " + i + " seconds or less.", 
                        ExceptionType.RangeException, line_num, f);
            }
            return new CVoid(line_num, f);
        }

        public Boolean runAsync() {
            //Because we stop the thread
            return true;
        }
    }
}
