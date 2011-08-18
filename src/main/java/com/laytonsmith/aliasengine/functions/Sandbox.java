/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.SimplePluginManager;

/**
 * @author Layton
 */
public class Sandbox {
    public static String docs(){
        return "This class is for functions that are experimental. They don't actually get added"
                + " to the documentation, and are subject to removal at any point in time, nor are they"
                + " likely to have good documentation.";
    }
    @api public static class plugin_cmd implements Function{

        public String getName() {
            return "__plugin_cmd__";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {plugin, cmd} ";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "0.0.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
            Object o = Static.getAliasCore().parent.getServer().getPluginManager();
            if(o instanceof SimplePluginManager){
                SimplePluginManager spm = (SimplePluginManager)o;
                try {
                    Method m = spm.getClass().getDeclaredMethod("getEventListeners", Event.Type.class);
                    m.setAccessible(true);
                    SortedSet<RegisteredListener> ss = (SortedSet<RegisteredListener>) m.invoke(spm, Event.Type.PLAYER_COMMAND_PREPROCESS);
                    for(RegisteredListener l : ss){
                        if(l.getPlugin().getDescription().getName().equals(args[0].val())){
                            PluginCommand.class.getDeclaredMethods();
                            Constructor c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                            c.setAccessible(true);
                            Command com = (Command)c.newInstance(l.getPlugin().getDescription().getName(), l.getPlugin()); 
                            List<String> argList = Arrays.asList(args[1].val().split(" "));
//                            com.execute(p, argList.get(0).substring(1), argList.subList(1, argList.size()).toArray(new String[]{}));
//                            l.callEvent(new Event() {});
//                            break;
                            l.getPlugin().onCommand(p, com, argList.get(0).substring(1), argList.subList(1, argList.size()).toArray(new String[]{}));
                        }
                    }
                } catch (InstantiationException ex) {
                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(Sandbox.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class pdrop implements Function{

        public String getName() {
            return "item_drop";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "void {[player/LocationArray], item, [qty]} Drops the specified item at the specified quantity at the specified player's feet (or "
                    + " at an arbitrary Location, if an array is given),"
                    + " like the vanilla /give command. player defaults to the current player, and qty defaults to 1. item follows the"
                    + " same type[:data] format used elsewhere.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {

            Location l = null;
            int qty = 1;
            ItemStack is = null;
            boolean natural = false;
            if(args.length == 1){
                //It is just the item
                is = Static.ParseItemNotation(this.getName(), args[0].val(), qty, line_num, f);
                natural = true;
            } else if(args.length == 2){
                //If args[0] starts with a number, it's the (item, qty) version, otherwise it's
                //(player, item)
                if(args[0].val().matches("\\d.*")){
                    qty = (int)Static.getInt(args[1]);
                    is = Static.ParseItemNotation(this.getName(), args[0].val(), qty, line_num, f);
                    natural = true;
                } else {
                    if(args[0] instanceof CArray){
                        l = Static.GetLocation(args[0], p.getWorld(), line_num, f);
                        natural = false;
                    } else {
                        l = Static.GetPlayer(args[0].val(), line_num, f).getLocation();
                        natural = true;
                    }
                    is = Static.ParseItemNotation(this.getName(), args[1].val(), qty, line_num, f);
                    
                }
            } else if(args.length == 3){
                //We are specifying all 3
                if(args[0] instanceof CArray){
                    l = Static.GetLocation(args[0], p.getWorld(), line_num, f);
                    natural = false;
                } else {
                    l = Static.GetPlayer(args[0].val(), line_num, f).getLocation();
                    natural = true;
                }
                qty = (int)Static.getInt(args[2]);
                is = Static.ParseItemNotation(this.getName(), args[1].val(), qty, line_num, f);
            }      
            if(natural){
                l.getWorld().dropItemNaturally(l, is);
            } else {
                l.getWorld().dropItem(l, is);
            }

            return new CVoid(line_num, f);
        }
        
    }
}
