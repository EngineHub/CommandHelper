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

/**
 * TODO: Remove bukkit references
 * @author Layton
 */
public class Debug {

@api
    public static class debug extends AbstractFunction {

        public String docs() {
            return "void {message} Manually logs a timestamped message to the debug log and the console, if debug-mode is set to true in the preferences";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if (Prefs.DebugMode()) {
                try {
                    Static.LogDebug(args[0].val());
                } catch (IOException ex) {
                    throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
                }
            }
            return new CVoid(t);
        }

        public String getName() {
            return "debug";
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
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.IOException};
        }
    }

//    public static void DoLog(Event.Type filter, int verbosity, String message) {
//        synchronized (EVENT_LOGGING_FILTER) {
//            if (EVENT_LOGGING && EVENT_LOGGING_FILTER.contains(filter) && EVENT_LOGGING_LEVEL >= verbosity) {
//                try {
//                    Static.LogDebug(message);
//                } catch (IOException ex) {
//                    Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//    }
//
//    public static boolean IsFiltered(Plugin plugin) {
//        if (EVENT_PLUGIN_FILTER.isEmpty()) {
//            return true;
//        } else {
//            return EVENT_PLUGIN_FILTER.contains(plugin.getClass().getSimpleName().toUpperCase());
//        }
//    }

    //    public static boolean EVENT_LOGGING = false;
//    public static int EVENT_LOGGING_LEVEL = 1;
//    public static final Set<Event.Type> EVENT_LOGGING_FILTER = new HashSet<Event.Type>();
//    public static final Set<String> EVENT_PLUGIN_FILTER = new HashSet<String>();
    public static boolean LOG_TO_SCREEN = false;

//    @api
//    public static class dump_listeners extends AbstractFunction {
//
//        public String getName() {
//            return "dump_listeners";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1, 2};
//        }
//
//        public String docs() {
//            return " {[typeFilter], [verboseLevel]} Send null as the typeFilter to see possibilities. VerboseLevel can be 1-4";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.SecurityException};
//        }
//
//        public boolean isRestricted() {
//            return true;
//        }
//
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public CHVersion since() {
//            return "0.0.0";
//        }
//
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//            if (!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//                throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getName() + ", enable it in your preferences.", ExceptionType.SecurityException, t);
//            }
//            StringBuilder b = new StringBuilder("\n");
//            if (args.length >= 1 && args[0] instanceof CNull) {
//                b.append("You can sort the listeners further by specifying one of the options:\n");
//                for (Event.Type t : Event.Type.values()) {
//                    b.append(t.name()).append("\n");
//                }
//                return new CString(b.toString(), 0, null);
//            }
//            int verbosity = 1;
//            if (args.length == 2) {
//                verbosity = (int) Static.getInt(args[1]);
//            }
//            try {
//                SimplePluginManager pm = (SimplePluginManager) AliasCore.parent.getServer().getPluginManager();
//                Field fListener = SimplePluginManager.class.getDeclaredField("listeners");
//                //set it to public
//                fListener.setAccessible(true);
//                EnumMap<Event.Type, SortedSet<RegisteredListener>> listeners =
//                        (EnumMap<Event.Type, SortedSet<RegisteredListener>>) fListener.get(pm);
//
//                if (args.length >= 1) {
//                    for (RegisteredListener l : listeners.get(Event.Type.valueOf(args[0].val().toUpperCase()))) {
//                        b.append(Build(l, verbosity));
//                    }
//                } else {
//                    for (Event.Type type : listeners.keySet()) {
//                        b.append("Type: ").append(type.name()).append("\n");
//                        for (RegisteredListener l : listeners.get(type)) {
//                            b.append(Build(l, verbosity));
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return new CString(b.toString(), 0, null);
//        }
//
//        public String Build(RegisteredListener l, int verbosity) {
//            StringBuilder b = new StringBuilder();
//            switch (Static.Normalize(verbosity, 1, 5)) {
//                case 1:
//                    b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//                    break;
//                case 2:
//                    b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getSimpleName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//                    break;
//                case 3:
//                    b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getCanonicalName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//                    break;
//                case 4:
//                    b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getCanonicalName()).append("\n\t").append("; Priority: ").append(l.getPriority().toString()).append("\n");
//                    break;
//                case 5:
//                    b.append("Plugin: ").append(l.getPlugin().getClass().getSimpleName()).append(":").append(l.getListener().getClass().getCanonicalName()).append("; Priority: ").append(l.getPriority().toString()).append("\n");
//                    b.append("\tMethods defined in listener that override ");
//                    try {
//                        Class<? extends Listener> parent = (Class<? extends Listener>) l.getListener().getClass().getSuperclass();
//                        while (parent.getSuperclass() != null && parent.getSuperclass().equals(Listener.class)) {
//                            parent = (Class<? extends Listener>) parent.getSuperclass();
//                        }
//                        b.append(parent.getSimpleName()).append(":\n");
//                        Set<Method> parentSet = new HashSet(Arrays.asList(parent.getDeclaredMethods()));
//                        for (Method m : l.getListener().getClass().getDeclaredMethods()) {
//                            for (Method pm : parentSet) {
//                                if (pm.getName().equals(m.getName()) && Arrays.equals(pm.getParameterTypes(), m.getParameterTypes())) {
//                                    b.append("\t\t").append(m.getReturnType().getSimpleName()).append(" ").append(m.getName()).append("(").append(Static.strJoin(m.getParameterTypes(), ", ")).append(");\n");
//                                }
//                            }
//                        }
//                    } catch (NoClassDefFoundError e) {
//                        b.append("Could not get methods for ").append(l.getListener().getClass());
//                    }
//                    break;
//            }
//            return b.toString();
//        }
//
//        public String BuildClassList(Class[] list) {
//            StringBuilder b = new StringBuilder();
//            ArrayList<String> l = new ArrayList<String>();
//            for (Class c : list) {
//                try {
//                    l.add(c.getSimpleName());
//                } catch (NoClassDefFoundError e) {
//                }
//            }
//            return Static.strJoin(list, ", ");
//        }
//    }

    public static String docs() {
        return "Provides methods for viewing data about both CommandHelper and the other plugins in your server. Though not meant to"
                + " be called by normal scripts, these methods are available everywhere other methods are available. Note that for"
                + " some of these functions to even work, play-dirty mode must set to on. These are most useful in conjuction with"
                + " interpreter mode.";
    }

//    @api
//    public static class debug_log_events extends AbstractFunction {
//
//        public String getName() {
//            return "debug_log_events";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{1, 2, 3};
//        }
//
//        public String docs() {
//            return "void {boolean, [level, [logToScreen]]} Turns the event logging on or off. Event logging may be useful in determining the problem if CommandHelper isn't"
//                    + " able to receive events, you can track what's actually happening. play-dirty mode must be enabled for this to work properly however."
//                    + " This feature may also be useful in diagnosing other problems with other plugins as well. Level varies from 1-5, and shows more"
//                    + " information as it increases. You must also set at least one filter with the set_debug_event_filter function before anything"
//                    + " will happen. logToScreen defaults to false. This should only be turned on when you are testing, or have very strict filters set.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.SecurityException};
//        }
//
//        public boolean isRestricted() {
//            return true;
//        }
//
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public CHVersion since() {
//            return CHVersion.V3_3_0;
//        }
//
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//            if (!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//                throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getName() + ", enable it in your preferences.", ExceptionType.SecurityException, t);
//            }
//            boolean on = Static.getBoolean(args[0]);
//            int level = 1;
//            if(args.length >= 2){
//                level = Static.Normalize((int) Static.getInt(args[1]), 1, 5);
//            }
//            Debug.EVENT_LOGGING = on;
//            Debug.EVENT_LOGGING_LEVEL = level;
//            if(args.length >= 3){
//                Debug.LOG_TO_SCREEN = Static.getBoolean(args[2]);
//            }
//            return new CVoid(t);
//        }
//    }

//    @api
//    public static class set_debug_event_filter extends AbstractFunction {
//
//        public String getName() {
//            return "set_debug_event_filter";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{1};
//        }
//
//        public String docs() {
//            return "void {array} Logs the specified event types as they occur, assuming that logging is currently enabled. For a list of"
//                    + " available filters, you can run dump_listeners(null). As these events occur, they will be logged according to the logging level.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.SecurityException};
//        }
//
//        public boolean isRestricted() {
//            return true;
//        }
//
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public CHVersion since() {
//            return CHVersion.V3_3_0;
//        }
//
//        public Boolean runAsync() {
//            return true;
//        }
//
//        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//            if (!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//                throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getName() + ", enable it in your preferences.", ExceptionType.SecurityException, t);
//            }
//            Set<Event.Type> set = new HashSet<Event.Type>();
//            if (args[0] instanceof CString) {
//                if (args[0].val().equals("*")) {
//                    for (Event.Type t : Event.Type.values()) {
//                        set.add(t);
//                    }
//                } else {
//                    try {
//                        Event.Type t = Event.Type.valueOf(args[0].val().toUpperCase());
//                        set.add(t);
//                    } catch (IllegalArgumentException e) {
//                        throw new ConfigRuntimeException(args[0].val() + " is not a valid filter type. The filter log has not been changed.", ExceptionType.FormatException, t);
//                    }
//                }
//            } else if (args[0] instanceof CArray) {
//                for (String c : ((CArray) args[0]).keySet()) {
//                    try {
//                        set.add(Event.Type.valueOf(((CArray) args[0]).get(c, t).val().toUpperCase()));
//                    } catch (IllegalArgumentException e) {
//                        throw new ConfigRuntimeException(c + " is not a valid filter type. The filter log has not been changed.", ExceptionType.FormatException, t);
//                    }
//                }
//            } else {
//                throw new ConfigRuntimeException("The parameter specified to " + this.getName() + " must be an array (or a single string). The filter array has not been changed.", ExceptionType.CastException, t);
//            }
//            synchronized (EVENT_LOGGING_FILTER) {
//                EVENT_LOGGING_FILTER.clear();
//                for (Event.Type t : set) {
//                    EVENT_LOGGING_FILTER.add(t);
//                }
//            }
//            return new CVoid(t);
//        }
//    }

//    @api
//    public static class set_debug_plugin_filter extends AbstractFunction {
//
//        public String getName() {
//            return "set_debug_plugin_filter";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{1};
//        }
//
//        public String docs() {
//            return "void {array} Often times you just are interested in the events a particular plugin is outputting. If the plugin filter"
//                    + " is empty, all plugins are reported (assuming their event types are not filtered out) otherwise, only the ones in"
//                    + " the list are logged. The name of the plugin is the field \"Called from Plugin: \" in the output, not the name"
//                    + " it may be commonly referred to as.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.SecurityException};
//        }
//
//        public boolean isRestricted() {
//            return true;
//        }
//
//        public boolean preResolveVariables() {
//            return true;
//        }
//
//        public CHVersion since() {
//            return CHVersion.V3_3_0;
//        }
//
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//            if (!(Boolean) Static.getPreferences().getPreference("allow-debug-logging")) {
//                throw new ConfigRuntimeException("allow-debug-logging is currently set to false. To use " + this.getName() + ", enable it in your preferences.", ExceptionType.SecurityException, t);
//            }
//            if (args[0] instanceof CString) {
//                EVENT_PLUGIN_FILTER.clear();
//                EVENT_PLUGIN_FILTER.add(args[0].val().toUpperCase());
//            } else if (args[0] instanceof CArray) {
//                for (String c : ((CArray) args[0]).keySet()) {
//                    EVENT_PLUGIN_FILTER.add(((CArray) args[0]).get(c, t).val().toUpperCase());
//                }
//            } else {
//                throw new ConfigRuntimeException(this.getName() + " expects the argument to be a single string, or an array of strings.", ExceptionType.CastException, t);
//            }
//            return new CVoid(t);
//        }
//    }
}
