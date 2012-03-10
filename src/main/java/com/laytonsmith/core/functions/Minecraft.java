/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Layton
 */
public class Minecraft {

    public static String docs() {
        return "These functions provide a hook into game functionality.";
    }
    private static final SortedMap<String, Construct> DataValueLookup = new TreeMap<String, Construct>();
    private static final SortedMap<String, Construct> DataNameLookup = new TreeMap<String, Construct>();
    static{
        Properties p1 = new Properties();
        try {
            p1.load(Minecraft.class.getResourceAsStream("/data_values.txt"));
            Enumeration e = p1.propertyNames();
            while(e.hasMoreElements()){
                String name = e.nextElement().toString();
                DataValueLookup.put(name, new CString(p1.getProperty(name).toString(), 0, null));
            }
        } catch (IOException ex) {
            Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Properties p2 = new Properties();
        try{
            p2.load(Minecraft.class.getResourceAsStream("/data_names.txt"));
            Enumeration e = p2.propertyNames();
            while(e.hasMoreElements()){
                String name = e.nextElement().toString();
                DataNameLookup.put(name, new CString(p2.getProperty(name).toString(), 0, null));
            }
        } catch(IOException ex){
            Logger.getLogger(Minecraft.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @api
    public static class data_values implements Function {

        public String getName() {
            return "data_values";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (args[0] instanceof CInt) {
                return new CInt(Static.getInt(args[0]), line_num, f);
            } else {
                String c = args[0].val();
                int number = StaticLayer.LookupItemId(c);
                if(number != -1){
                    return new CInt(number, line_num, f);
                }
                String changed = c;
                if(changed.contains(":")){
                    //Split on that, and reverse. Change wool:red to redwool
                    String split[] = changed.split(":");
                    if(split.length == 2){
                        changed = split[1] + split[0];
                    }
                }
                //Remove anything that isn't a letter or a number
                changed = changed.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                //Do a lookup in the DataLookup table
                if(DataValueLookup.containsKey(changed)){
                    String split[] = DataValueLookup.get(changed).toString().split(":");
                    if(split[1].equals("0")){
                        return new CInt(split[0], line_num, f);
                    }
                    return new CString(split[0] + ":" + split[1], line_num, f);
                }
                return new CNull(line_num, f);
            }
        }

        public String docs() {
            return "int {var1} Does a lookup to return the data value of a name. For instance, returns 1 for 'stone'. If an integer is given,"
                    + " simply returns that number. If the data value cannot be found, null is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
    }
    
    @api public static class data_name implements Function{

        public String getName() {
            return "data_name";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {int | itemArray} Performs the reverse functionality as data_values. Given 1, returns 'Stone'. Note that the enum value"
                    + " given in bukkit's Material class is what is returned as a fallback, if the id doesn't match a value in the internally maintained list."
                    + " If a completely invalid argument is passed"
                    + " in, null is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            int i = -1;
            int i2 = -1;
            if(args[0] instanceof CString){
                //We also accept item notation
                if(args[0].val().contains(":")){
                    String[] split = args[0].val().split(":");
                    try{
                        i = Integer.parseInt(split[0]);
                        i2 = Integer.parseInt(split[1]);
                    } catch(NumberFormatException e){}
                }
            } else if(args[0] instanceof CArray){
                MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], line_num, f);
                i = is.getTypeId();
                i2 = (int)is.getData().getData();
            }
            if(i == -1){
                i = (int)Static.getInt(args[0]);
            }  
            if(i2 == -1){
                i2 = 0;
            }
            if(DataNameLookup.containsKey(i + "_" + i2)){
                return DataNameLookup.get(i + "_" + i2);
            } else if(DataNameLookup.containsKey(i + "_0")){
                return DataNameLookup.get(i + "_0");
            } else {
                try{
                    return new CString(StaticLayer.LookupMaterialName(i), line_num, f);
                } catch(NullPointerException e){
                    return new CNull(line_num, f);
                }
            }
        }
        
    }
    
    @api
    public static class max_stack_size implements Function{

        public String getName() {
            return "max_stack_size";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "integer {itemType | itemArray} Given an item type, returns"
                    + " the maximum allowed stack size. This method will accept either"
                    + " a single data value (i.e. 278) or an item array like is returned"
                    + " from pinv(). Additionally, if a single value, it can also be in"
                    + " the old item notation (i.e. '35:11'), though for the purposes of this"
                    + " function, the data is unneccesary.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return false;
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args[0] instanceof CArray){
                MCItemStack is = ObjectGenerator.GetGenerator().item(args[0], line_num, f);
                return new CInt(is.getType().getMaxStackSize(), line_num, f);
            } else {
                String item = args[0].val();
                if(item.contains(":")){
                    String[] split = item.split(":");
                    item = split[0];
                }
                try{
                    int iitem = Integer.parseInt(item);
                    int max = StaticLayer.GetItemStack(iitem, 1).getType().getMaxStackSize();
                    return new CInt(max, line_num, f);
                } catch(NumberFormatException e){
                    
                }
            }
            throw new ConfigRuntimeException("Improper value passed to max_stack. Expecting a number, or an item array, but received \"" + args[0].val() + "\"", ExceptionType.CastException, line_num, f);
        }

        public String since() {
            return "3.3.0";
        }
        
    }

    @api
    public static class get_worlds implements Function {

        public String getName() {
            return "get_worlds";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "array {} Returns the names of the worlds available in this server";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            List<MCWorld> worlds = env.GetCommandSender().getServer().getWorlds();
            CArray c = new CArray(line_num, f);
            for (MCWorld w : worlds) {
                c.push(new CString(w.getName(), line_num, f));
            }
            return c;
        }
    }

    @api
    public static class spawn_mob implements Function {

        public String getName() {
            return "spawn_mob";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

        public String docs() {
            return "array {mobType, [qty], [location]} Spawns qty mob of one of the following types at location. qty defaults to 1, and location defaults"
                    + " to the location of the player. mobType can be one of: BLAZE, CAVESPIDER, CHICKEN, COW, CREEPER, ENDERDRAGON, ENDERMAN, GHAST,"
                    + " IRONGOLEM, MAGMACUBE, MOOSHROOM, OCELOT, PIG, PIGZOMBIE, SHEEP, SILVERFISH, SKELETON, SLIME, SPIDER, SPIDERJOCKEY, SQUID, VILLAGER, WOLF, ZOMBIE. Spelling matters, but capitalization doesn't. At this"
                    + " time, the function is limited to spawning a maximum of 50 at a time. Further, SHEEP can be spawned as any color, by specifying"
                    + " SHEEP:COLOR, where COLOR is any of the dye colors: BLACK RED GREEN BROWN BLUE PURPLE CYAN SILVER GRAY PINK LIME YELLOW LIGHT_BLUE MAGENTA ORANGE WHITE. COLOR defaults to white if not"
                    + " specified. An array of the entity IDs spawned is returned. OCELOT can also take a subtype, like sheep, and may be one of: WILD_OCELOT, BLACK_CAT, RED_CAT, or SIAMESE_CAT"
                    + " <small>GIANTs can also be spawned, if you are running craftbukkit. This is an experimental feature. Only one GIANT can be spawned at a time</small>";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.FormatException};
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
            return "3.1.2";
        }

        public Boolean runAsync() {
            return false;
        }

        

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String mob = args[0].val();
            String secondary = "";
            if (mob.contains(":")) {
                secondary = mob.substring(mob.indexOf(":") + 1);
                mob = mob.substring(0, mob.indexOf(":"));
            }
            int qty = 1;
            if (args.length > 1) {
                qty = (int) Static.getInt(args[1]);
            }
            if (qty > 50) {
                throw new ConfigRuntimeException("A bit excessive, don't you think? Let's scale that back some, huh?",
                        ExceptionType.RangeException, line_num, f);
            }
            MCLocation l = null;
            if (env.GetCommandSender() instanceof MCPlayer) {
                l = env.GetPlayer().getLocation();
            }
            if (args.length > 2) {
                if (args[2] instanceof CArray) {
                    CArray ca = (CArray) args[2];
                    l = ObjectGenerator.GetGenerator().location(ca, (l != null?l.getWorld():null), line_num, f);
                } else {
                    throw new ConfigRuntimeException("Expected argument 3 to spawn_mob to be an array",
                            ExceptionType.CastException, line_num, f);
                }
            }
            if(l.getWorld() != null){
                return l.getWorld().spawnMob(mob, secondary, qty, l, line_num, f);
            } else {
                throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, line_num, f);
            }
        }
    }
    
    @api public static class tame_mob implements Function{

        public String getName() {
            return "tame_mob";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], entityID} Tames the entity specified to the player. Wolves and ocelots are supported. Offline players"
                    + " are supported, but this means that partial matches are NOT supported. You must type the players name exactly. Setting"
                    + " the player to null will untame the mob. If the entity doesn't exist, nothing happens.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.UntameableMobException, ExceptionType.CastException};
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
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            String player = environment.GetPlayer().getName();
            Construct entityID = null;
            if(args.length == 2){
                if(args[0] instanceof CNull){
                    player = null;
                } else {
                    player = args[0].val();
                }
                entityID = args[1];
            } else {
                entityID = args[0];
            }
            int id = (int) Static.getInt(entityID);
            MCEntity e = Static.getEntity(id);
            if(e == null){
                return new CVoid(line_num, f);
            } else if(e.isTameable()){                
                MCTameable t = e.getMCTameable();
                if(player != null){
                    t.setOwner(Static.getServer().getOfflinePlayer(player));
                } else {
                    t.setOwner(null);
                }
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableMobException, line_num, f);
            }
        }
        
    }
    
    @api public static class get_mob_owner implements Function{

        public String getName() {
            return "get_mob_owner";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {entityID} Returns the owner's name, or null if the mob is unowned. An UntameableMobException is thrown if"
                    + " mob isn't tameable to begin with.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.UntameableMobException, ExceptionType.CastException};
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
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            int id = (int)Static.getInt(args[0]);
            MCEntity e = Static.getEntity(id);
            if(e == null){
                return new CNull(line_num, f);
            } else if(e instanceof MCTameable){
                MCAnimalTamer at = ((MCTameable)e).getOwner();
                if(at instanceof HumanEntity){
                    return new CString(((HumanEntity)at).getName(), line_num, f);
                } else if(at instanceof OfflinePlayer){
                    return new CString(((OfflinePlayer)at).getName(), line_num, f);
                } else {
                    return new CNull(line_num, f);
                }
            } else {
                throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableMobException, line_num, f);
            }
        }
        
    }
    
    @api public static class is_tameable implements Function{

        public String getName() {
            return "is_tameable";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {entityID} Returns true or false if the specified entity is tameable";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
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
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            int id = (int)Static.getInt(args[0]);
            MCEntity e = Static.getEntity(id);
            boolean ret = false;
            if(e == null){
                ret = false;
            } else if(e instanceof MCTameable){
                ret = true;
            } else {
                ret = false;
            }
            return new CBoolean(ret, line_num, f);
        }
        
    }
    
    @api public static class make_effect implements Function{

        public String getName() {
            return "make_effect";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "void {xyzArray, effect, [radius]} Plays the specified effect (sound effect) at the given location, for all players within"
                    + " the radius (or 64 by default). The effect can be one of the following:"
                    + " BOW_FIRE, CLICK1, CLICK2, DOOR_TOGGLE, EXTINGUISH.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.3";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCLocation l = ObjectGenerator.GetGenerator().location(args[0], (env.GetCommandSender() instanceof MCPlayer?env.GetPlayer().getWorld():null), line_num, f);
            MCEffect e = null;
            try{
                e = MCEffect.valueOf(args[1].val().toUpperCase());
                if(e.equals(MCEffect.RECORD_PLAY) || e.equals(MCEffect.SMOKE) || e.equals(MCEffect.STEP_SOUND)){
                    throw new IllegalArgumentException();
                }
            } catch(IllegalArgumentException ex){
                throw new ConfigRuntimeException("The effect type " + args[1].val() + " is not valid", ExceptionType.FormatException, line_num, f);
            }
            int data = 0;
            int radius = 64;
            if(args.length == 3){
                radius = (int) Static.getInt(args[2]);
            }
            l.getWorld().playEffect(l, e, data, radius);
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class set_entity_health implements Function{

        public String getName() {
            return "set_entity_health";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {entityID, healthPercent} Sets the specified entity's health (0 kills it), or ignores this call if the entityID doesn't exist or isn't"
                    + "a LivingEntity.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
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
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            MCEntity e = Static.getEntity((int)Static.getInt(args[0]));
            if(e instanceof MCLivingEntity){
                int health = (int)((double)Static.getInt(args[1])/100.0*(double)((LivingEntity)e).getMaxHealth());
                if(health != 0){
                    ((MCLivingEntity)e).setHealth(health);
                } else {
                    ((MCLivingEntity)e).damage(9001); //His power level is over 9000!
                }
            }
            return new CVoid(line_num, f);
        }
        
    }
    
    @api public static class get_entity_health implements Function{

        public String getName() {
            return "get_entity_health";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "int {entityID} Returns the entity's health, as a percentage. If the specified entity doesn't exist, or is not"
                    + " a LivingEntity, a format exception is thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
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
            return false;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            MCEntity e = Static.getEntity((int)Static.getInt(args[0]));
            if(e instanceof MCLivingEntity){
                int h = (int)(((double)((MCLivingEntity)e).getHealth()/(double)((MCLivingEntity)e).getMaxHealth())*100);
                return new CInt(h, line_num, f);
            } else {
                throw new ConfigRuntimeException("Not a valid entity id", ExceptionType.FormatException, line_num, f);
            }
        }
        
    }

    @api
    public static class get_server_info implements Function {

        public String getName() {
            return "get_server_info";
        }

        public Integer[] numArgs() {
            return new Integer[]{0,1};
        }

        public String docs() {
            return "mixed {[value]} Returns various information about server."
                    + "If value is set, it should be an integer of one of the following indexes, and only that information for that index"
                    + " will be returned. Otherwise if value is not specified (or is -1), it returns an array of"
                    + " information with the following pieces of information in the specified index: "
                    + "<ul><li>0 - Server name; the name of the server in server.properties. "
                    + "</li><li>1 - API version; The bukkit api version that is implemented in this build.</li><li>2 - Bukkit version; The version of craftbukkit your using.  "
                    + "</li><li>3 - Allow flight; If true, minecrafts inbuild anti fly check is enabled.</li><li>4 - Allow nether; is true, nether is enabled"
                    + "</li><li>5 - Allow end; if true, end is enabled"
                    + "</li><li>6 - World container; The path to the world container.</li><li>7 - "
                    + "Max player limit; returns the player limit.</li><li>8 - Operators; An array of operators on the server.</li>"
                    + "</ul>";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.4.0";
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCServer server = env.GetCommandSender().getServer();


            int index = -1;
            if (args.length == 0) {
                index = -1;
            } else if (args.length == 1) {
                index = (int) Static.getInt(args[0]);
            }

            if (index < -1 || index > 8) {
                throw new ConfigRuntimeException("get_server_info expects the index to be between -1 and 10",
                        ExceptionType.RangeException, line_num, f);
            }

            assert index >= -1 && index <= 8;
            ArrayList<Construct> retVals = new ArrayList<Construct>();

            if (index == 0 || index == -1) {
                //Server name
                retVals.add(new CString(server.getServerName(), line_num, f));
            }

            if (index == 1 || index == -1) {
                //Server Version
                retVals.add(new CString(server.getVersion(), line_num, f));
            }
            if (index == 2 || index == -1) {
                //Bukkit Version
                retVals.add(new CString(server.getModVersion(), line_num, f));
            }
            if (index == 3 || index == -1) {
                //Allow flight
                retVals.add(new CBoolean(server.getAllowFlight(), line_num, f));
            }
            if (index == 4 || index == -1) {
                //Allow nether
                retVals.add(new CBoolean(server.getAllowNether(), line_num, f));
            }
            if (index == 5 || index == -1) {
                //Allow end
                retVals.add(new CBoolean(server.getAllowEnd(), line_num, f));
            }
            if (index == 6 || index == -1) {
                //World container
                retVals.add(new CString(server.getWorldContainer(), line_num, f));
            }
            if (index == 7 || index == -1) {
                //Max player limit
                retVals.add(new CInt(server.getMaxPlayers(), line_num, f));
            }
            if (index == 8 || index == -1) {
                //Array of op's
                CArray co = new CArray(line_num, f);
                List<MCOfflinePlayer> so = server.getOperators();
                for(MCOfflinePlayer o : so) {
                    if(o == null) {
                        continue;
                    }
                    CString os = new CString(o.getName(), line_num, f);
                    co.push(os);
                }
                retVals.add(co);
            }

            if (retVals.size() == 1) {
                return retVals.get(0);
            } else {
                CArray ca = new CArray(line_num, f);
                for (Construct c : retVals) {
                    ca.push(c);
                }
                return ca;
            }
        }
    }

    @api
    public static class get_banned_players implements Function {

        public String getName() {
            return "get_banned_players";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "Array {} An array of players banned on the server.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.4.0";
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCServer server = env.GetCommandSender().getServer();

            CArray co = new CArray(line_num, f);
            List<MCOfflinePlayer> so = server.getBannedPlayers();
            for(MCOfflinePlayer o : so) {
                if(o == null) {
                    continue;
                }
                CString os = new CString(o.getName(), line_num, f);
                co.push(os);
            }
            return co;
        }
    }

    @api
    public static class get_whitelisted_players implements Function {

        public String getName() {
            return "get_whitelisted_players";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "Array {} An array of players whitelisted on the server.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
        }

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.4.0";
        }

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCServer server = env.GetCommandSender().getServer();

            CArray co = new CArray(line_num, f);
            List<MCOfflinePlayer> so = server.getWhitelistedPlayers();
            for(MCOfflinePlayer o : so) {
                if(o == null) {
                    continue;
                }
                CString os = new CString(o.getName(), line_num, f);
                co.push(os);
            }
            return co;
        }
    }
}
