/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class World {
    @api public static class get_spawn extends AbstractFunction{

        public String docs() {
            return "array {[world]} Returns a location array for the specified world, or the current player's world, if not specified.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            String world;
            if(args.length == 1){
                world = args[0].val();
            } else {
                world = environment.GetPlayer().getWorld().getName();
            }
            return ObjectGenerator.GetGenerator().location(Static.getServer().getWorld(world).getSpawnLocation());
        }

        public String getName() {
            return "get_spawn";
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
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
        }
        
    }
    
    @api public static class get_world_time extends AbstractFunction{

        public String docs() {
            return "int {[world]} Returns the time of the specified world, as an integer from"
                    + " 0 to 24000-1";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCWorld w = null;
            if(environment.GetPlayer() != null){
                w = environment.GetPlayer().getWorld();
            }
            if(args.length == 1){
                w = Static.getServer().getWorld(args[0].val());                
            }
            if(w == null){
                throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
            }
            return new CInt(w.getTime(), t);
        }

        public String getName() {
            return "get_world_time";
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
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException};
        }
        
    }
    
    @api public static class refresh_chunk extends AbstractFunction{

        public String docs() {
            return "void {[world], x, z | [world], locationArray} Resends the chunk to all clients, using the specified world, or the current"
                    + " players world if not provided.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = environment.GetPlayer();
            MCWorld world;
            int x;
            int z;
            if(args.length == 1){
                //Location array provided                
                MCLocation l = ObjectGenerator.GetGenerator().location(args[0], m!=null?m.getWorld():null, t);
                world = l.getWorld();
                x = l.getBlockX();
                z = l.getBlockZ();
            } else if(args.length == 2) {
                //Either location array and world provided, or x and z. Test for array at pos 2
                if(args[1] instanceof CArray){
                    world = Static.getServer().getWorld(args[0].val());
                    MCLocation l = ObjectGenerator.GetGenerator().location(args[1], null, t);
                    x = l.getBlockX();
                    z = l.getBlockZ();
                } else {
                    if(m == null){
                        throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
                    }
                    world = m.getWorld();
                    x = (int)Static.getInt(args[0]);
                    z = (int)Static.getInt(args[1]);
                }
            } else {
                //world, x and z provided
                world = Static.getServer().getWorld(args[0].val());
                x = (int)Static.getInt(args[1]);
                z = (int)Static.getInt(args[2]);
            }
            world.refreshChunk(x, z);
            return new CVoid(t);
        }

        public String getName() {
            return "refresh_chunk";
        }

        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
        }
        
    }
    
    @api public static class set_world_time extends AbstractFunction{

        public String docs() {
            StringBuilder doc = new StringBuilder();
            synchronized(World.class){
                doc.append("void {[world], time} Sets the time of a given world. Should be a number from 0 to"
                        + " 24000, if not, it is modulo scaled. Alternatively, common time notation (9:30pm, 4:00 am)"
                        + " is acceptable, and convenient english mappings also exist:"
                        );
                doc.append("<ul>");
                for(String key : TimeLookup.keySet()){
                    doc.append("<li>").append(key).append(" = ").append(TimeLookup.get(key)).append("</li>\n");
                }
                doc.append("</ul>");
            }
            return doc.toString();
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCWorld w = null;
            if(environment.GetPlayer() != null){
                w = environment.GetPlayer().getWorld();
            }
            if(args.length == 2){
                w = Static.getServer().getWorld(args[0].val());                
            }
            if(w == null){
                throw new ConfigRuntimeException("No world specified", ExceptionType.InvalidWorldException, t);
            }
            long time = 0;
            String stime = (args.length == 1?args[0]:args[1]).val().toLowerCase();
            if(TimeLookup.containsKey(stime.replaceAll("[^a-z]", ""))){
                stime = TimeLookup.get(stime.replaceAll("[^a-z]", "")).val();
            }
            if(stime.matches("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$")){
                Pattern p = Pattern.compile("^([\\d]+)[:.]([\\d]+)[ ]*?(?:([pa])\\.*m\\.*){0,1}$");
                Matcher m = p.matcher(stime);
                m.find();
                int hour = Integer.parseInt(m.group(1));
                int minute = Integer.parseInt(m.group(2));
                String offset = "a";
                if(m.group(3) != null){
                    offset = m.group(3);
                }
                if(offset.equals("p")){
                    hour += 12;
                }
                if(hour == 24) hour = 0;
                if(hour > 24){
                    throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
                }
                if(minute > 59){
                    throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);                    
                }
                hour -= 6;
                hour = hour % 24;
                long ttime = hour * 1000;
                ttime += ((minute / 60.0) * 1000);
                stime = Long.toString(ttime);
            }
            try{
                Long.valueOf(stime);
            } catch(NumberFormatException e){
                throw new ConfigRuntimeException("Invalid time provided", ExceptionType.FormatException, t);
            }
            time = Long.parseLong(stime);
            w.setTime(time);
            return new CVoid(t);
        }

        public String getName() {
            return "set_world_time";
        }

        public boolean isRestricted() {
            return true;
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException};
        }
        
    }
    private static final SortedMap<String, Construct> TimeLookup = new TreeMap<String, Construct>();
    
    static{
        synchronized(World.class){
            Properties p = new Properties();
            try {
                p.load(Minecraft.class.getResourceAsStream("/time_names.txt"));
                Enumeration e = p.propertyNames();
                while(e.hasMoreElements()){
                    String name = e.nextElement().toString();
                    TimeLookup.put(name, new CString(p.getProperty(name).toString(), Target.UNKNOWN));
                }
            } catch (IOException ex) {
                Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static String docs(){
        return "Provides functions for manipulating a world";
    }
}
