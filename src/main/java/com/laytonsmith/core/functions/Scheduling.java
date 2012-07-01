/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Layton
 */
public class Scheduling {
    
    
    @api public static class clear_task extends AbstractFunction{

        public String docs() {
            return "void {[id]} Stops the interval or timeout that is specified. The id can be gotten by"
                    + " storing the integer returned from either set_timeout or set_interval."
                    + " An invalid id is simply ignored. Also note that you can cancel an interval"
                    + " (and technically a timeout, though this is pointless) from within the interval"
                    + " by using the cancel function. This clear_task function is more useful for set_timeout, where"
                    + " you may queue up some task to happen in the far future, yet have some trigger to"
                    + " prevent it from happening. ID is optional, but only if called from within a set_interval or set_timeout"
                    + " closure, in which case it defaults to the id of that particular task.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args.length == 0 && environment.GetCustom("timeout-id") != null){
                StaticLayer.ClearFutureRunnable((Integer)environment.GetCustom("timeout-id"));
            } else if(args.length == 1){
                StaticLayer.ClearFutureRunnable((int)Static.getInt(args[0]));
            } else {
                throw new ConfigRuntimeException("No id was passed to clear_task, and it's not running inside a task either.", ExceptionType.InsufficientArgumentsException, t);
            }
            return new CVoid(t);
        }

        public String getName() {
            return "clear_task";
        }

        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.InsufficientArgumentsException};
        }
        
    }
    
    @api public static class nano_time extends AbstractFunction{

        public String docs() {
            return "int {} Returns an arbitrary number based on the most accurate clock available on this system. Only useful when compared to other calls"
                    + " to nano_time(). The return is in nano seconds. See the Java API on System.nanoTime() for more information on the usage of this function.";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CInt(System.nanoTime(), t);
        }

        public String getName() {
            return "nano_time";
        }
        
        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {}
        
    }
    @api public static class set_interval extends AbstractFunction{

        public String docs() {
            return "int {timeInMS, [initialDelayInMS,] closure} Sets a task to run every so often. This works similarly to set_timeout,"
                    + " except the task will automatically re-register itself to run again. Note that the resolution"
                    + " of the time is in ms, however, the server will only have a resolution of up to 50 ms, meaning"
                    + " that a time of 1-50ms is essentially the same as 50ms. The inital delay defaults to the same"
                    + " thing as timeInMS, that is, there will be a pause between registration and initial firing. However,"
                    + " this can be set to 0 (or some other number) to adjust how long of a delay there is before it begins.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            long time = Static.getInt(args[0]);
            int offset = 0;
            long delay = time;
            if(args.length == 3){
                offset = 1;
                delay = Static.getInt(args[1]);
            }            
            if(!(args[1 + offset] instanceof CClosure)){
                throw new ConfigRuntimeException(getName() + " expects a closure to be sent as the second argument", ExceptionType.CastException, t);
            }
            final CClosure c = (CClosure) args[1 + offset];     
            final AtomicInteger ret = new AtomicInteger(-1);
            
            ret.set(StaticLayer.SetFutureRepeater(time, delay, new Runnable(){
               public void run(){
                   c.getEnv().SetCustom("timeout-id", ret.get());
                   try{
                       c.execute(null);
                   } catch(ConfigRuntimeException e){
                       ConfigRuntimeException.React(e);
                   } catch(CancelCommandException e){
                       //Ok
                   } catch(ProgramFlowManipulationException e){
                       ConfigRuntimeException.DoWarning("Using a program flow manipulation construct improperly! " + e.getClass().getSimpleName());
                   }
               } 
            }));
            return new CInt(ret.get(), t);
        }

        public String getName() {
            return "set_interval";
        }

        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }
        
    }
    
    @api public static class set_timeout extends AbstractFunction{

        public String docs() {
            return "int {timeInMS, closure} Sets a task to run in the specified number of ms in the future."
                    + " The task will only run once. Note that the resolution"
                    + " of the time is in ms, however, the server will only have a resolution of up to 50 ms, meaning"
                    + " that a time of 1-50ms is essentially the same as 50ms.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            long time = Static.getInt(args[0]);
            if(!(args[1] instanceof CClosure)){
                throw new ConfigRuntimeException(getName() + " expects a closure to be sent as the second argument", ExceptionType.CastException, t);
            }
            final CClosure c = (CClosure) args[1];     
            final AtomicInteger ret = new AtomicInteger(-1);
            ret.set(StaticLayer.SetFutureRunnable(time, new Runnable(){
               public void run(){
                   c.getEnv().SetCustom("timeout-id", ret.get());
                   try{
                       c.execute(null);
                   } catch(ConfigRuntimeException e){
                       ConfigRuntimeException.React(e);
                   } catch(CancelCommandException e){
                       //Ok
                   } catch(ProgramFlowManipulationException e){
                       ConfigRuntimeException.DoWarning("Using a program flow manipulation construct improperly! " + e.getClass().getSimpleName());
                   }
               } 
            }));
            return new CInt(ret.get(), t);
        }

        public String getName() {
            return "set_timeout";
        }

        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }
        
    }
    
    public static class sleep extends AbstractFunction {

        public String docs() {
            return "void {seconds} Sleeps the script for the specified number of seconds, up to the maximum time limit defined in the preferences file."
                    + " Seconds may be a double value, so 0.5 would be half a second."
                    + " PLEASE NOTE: Sleep times are NOT very accurate, and should not be relied on for preciseness.";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
//            if (Thread.currentThread().getName().equals("Server thread")) {
//                throw new ConfigRuntimeException("sleep() cannot be run in the main server thread", 
//                        null, t);
//            }
//            Construct x = args[0];
//            double time = Static.getNumber(x);
//            Integer i = (Integer) (Prefs.);
//            if (i > time || i <= 0) {
//                try {
//                    Thread.sleep((int)(time * 1000));
//                } catch (InterruptedException ex) {
//                }
//            } else {
//                throw new ConfigRuntimeException("The value passed to sleep must be less than the server defined value of " + i + " seconds or less.", 
//                        ExceptionType.RangeException, t);
//            }
            return new CVoid(t);
        }

        public String getName() {
            return "sleep";
        }
        
        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            //Because we stop the thread
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public void varList(IVariableList varList) {
        }
    }
    
    @api public static class time extends AbstractFunction{

        public String docs() {
            return "int {} Returns the current unix time stamp, in milliseconds. The resolution of this is not guaranteed to be extremely accurate. If "
                    + "you need extreme accuracy, use nano_time()";
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CInt(System.currentTimeMillis(), t);
        }

        public String getName() {
            return "time";
        }
        
        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_1_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public void varList(IVariableList varList) {}
        
    }
    
    public static void ClearScheduledRunners(){
        StaticLayer.ClearAllRunnables();
    }
    
    public static String docs(){
        return "This class contains methods for dealing with time and server scheduling.";
    }
}
