/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

/**
 *
 * @author Layton
 */
public class Minecraft {

    public static String docs() {
        return "These functions provide a hook into game functionality.";
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
                return new CInt(new MaterialData(Material.matchMaterial(c)).getItemTypeId(), line_num, f);
            }
        }

        public String docs() {
            return "int {var1} Does a lookup to return the data value of a name. For instance, returns 1 for 'stone'. If an integer is given,"
                    + " simply returns that number";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
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
            return "string {int} Performs the reverse functionality as data_values. Given 1, returns 'STONE'. Note that the enum value"
                    + " given in bukkit's Material class is what is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException};
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
            int i = (int)Static.getInt(args[0]);
            Material m = Material.getMaterial(i);
            return new CString(m.toString(), line_num, f);
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
            List<World> worlds = env.GetCommandSender().getServer().getWorlds();
            CArray c = new CArray(line_num, f);
            for (World w : worlds) {
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
                    + " MAGMACUBE, MOOSHROOM, PIG, PIGZOMBIE, SHEEP, SILVERFISH, SKELETON, SLIME, SPIDER, SPIDERJOCKEY, SQUID, VILLAGER, WOLF, ZOMBIE. Spelling matters, but capitalization doesn't. At this"
                    + " time, the function is limited to spawning a maximum of 50 at a time. Further, SHEEP can be spawned as any color, by specifying"
                    + " SHEEP:COLOR, where COLOR is any of the dye colors: BLACK RED GREEN BROWN BLUE PURPLE CYAN SILVER GRAY PINK LIME YELLOW LIGHT_BLUE MAGENTA ORANGE WHITE. COLOR defaults to white if not"
                    + " specified. An array of the entity IDs spawned is returned."
                    + ""
                    + " <p><small>GIANTs can also be spawned, if you are running craftbukkit. This is an experimental feature. Only one GIANT can be spawned at a time</small></p>";
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

        enum MOBS {

            CHICKEN, COW, CREEPER, GHAST, PIG, PIGZOMBIE, SHEEP, SKELETON, SLIME, 
            SPIDER, SQUID, WOLF, ZOMBIE, CAVESPIDER, ENDERMAN, SILVERFISH, VILLAGER,
            BLAZE, ENDERDRAGON, MAGMACUBE, MOOSHROOM, SPIDERJOCKEY, GIANT, SNOWGOLEM
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String mob = args[0].val();
            String sheepColor = "WHITE";
            if (mob.toUpperCase().startsWith("SHEEP:")) {
                sheepColor = mob.substring(6);
                mob = "SHEEP";
            }
            int qty = 1;
            if (args.length > 1) {
                qty = (int) Static.getInt(args[1]);
            }
            if (qty > 50) {
                throw new ConfigRuntimeException("A bit excessive, don't you think? Let's scale that back some, huh?",
                        ExceptionType.RangeException, line_num, f);
            }
            Location l = null;
            if (env.GetCommandSender() instanceof Player) {
                l = env.GetPlayer().getLocation();
            }
            if (args.length > 2) {
                if (args[2] instanceof CArray) {
                    CArray ca = (CArray) args[2];
                    l = Static.GetLocation(ca, (l != null?l.getWorld():null), line_num, f);
                } else {
                    throw new ConfigRuntimeException("Expected argument 3 to spawn_mob to be an array",
                            ExceptionType.CastException, line_num, f);
                }
            }
            Class mobType = null;
            CArray ids = new CArray(line_num, f);
            try {
                switch (MOBS.valueOf(mob.toUpperCase().replaceAll(" ", ""))) {
                    case CHICKEN:
                        mobType = Chicken.class;
                        break;
                    case COW:
                        mobType = Cow.class;
                        break;
                    case CREEPER:
                        mobType = Creeper.class;
                        break;
                    case GHAST:
                        mobType = Ghast.class;
                        break;
                    case PIG:
                        mobType = Pig.class;
                        break;
                    case PIGZOMBIE:
                        mobType = PigZombie.class;
                        break;
                    case SHEEP:
                        mobType = Sheep.class;
                        break;
                    case SKELETON:
                        mobType = Skeleton.class;
                        break;
                    case SLIME:
                        mobType = Slime.class;
                        break;
                    case SPIDER:
                        mobType = Spider.class;
                        break;
                    case SQUID:
                        mobType = Squid.class;
                        break;
                    case WOLF:
                        mobType = Wolf.class;
                        break;
                    case ZOMBIE:
                        mobType = Zombie.class;
                        break;
                    case CAVESPIDER:
                        mobType = CaveSpider.class;
                        break;
                    case ENDERMAN:
                        mobType = Enderman.class;
                        break;
                    case SILVERFISH:
                        mobType = Silverfish.class;
                        break;
                    case BLAZE:
                        mobType = Blaze.class;
                        break;
                    case VILLAGER:
                        mobType = Villager.class;
                        break;
                    case ENDERDRAGON:
                        mobType = EnderDragon.class;
                        break;
                    case MAGMACUBE:
                        mobType = MagmaCube.class;
                        break;
                    case MOOSHROOM:
                        mobType = MushroomCow.class;
                        break;
                    case SPIDERJOCKEY:
                        mobType = Spider.class;
                        break;
                    case GIANT:
                        double x = l.getX();
                        double y = l.getY();
                        double z = l.getZ();
                        float pitch = l.getPitch();
                        float yaw = l.getYaw();
                        net.minecraft.server.Entity giant = new net.minecraft.server.EntityGiantZombie(((CraftWorld)l.getWorld()).getHandle());
                        giant.setLocation(x, y, z, pitch, yaw);
                        ((CraftWorld)l.getWorld()).getHandle().addEntity(giant, SpawnReason.CUSTOM);
                        return new CVoid(line_num, f);
                    case SNOWGOLEM:
                        mobType = Snowman.class;
                        break;
                }
            } catch (IllegalArgumentException e) {
                throw new ConfigRuntimeException("No mob of type " + mob + " exists",
                        ExceptionType.FormatException, line_num, f);
            }
            if (l.getWorld() != null) {
                for (int i = 0; i < qty; i++) {
                    Entity e = l.getWorld().spawn(l, mobType);
                    if(MOBS.valueOf(mob.toUpperCase()) == MOBS.SPIDERJOCKEY){
                        Spider s = (Spider) e;
                        Skeleton sk = (Skeleton) l.getWorld().spawn(l, Skeleton.class);
                        s.setPassenger(sk);
                    }
                    if (e instanceof Sheep) {
                        Sheep s = (Sheep) e;
                        try {
                            s.setColor(DyeColor.valueOf(sheepColor.toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            throw new ConfigRuntimeException(sheepColor.toUpperCase() + " is not a valid color",
                                    ExceptionType.FormatException, line_num, f);
                        }
                    }
                    ids.push(new CInt(e.getEntityId(), line_num, f));
                }
                return ids;
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
            return "void {[player], entityID} Tames the entity specified to the player. Currently only wolves are supported. Offline players"
                    + " are supported, but this means that partial matches are NOT supported. You must type the players name exactly. Setting"
                    + " the player to null will untame the mob. If the entity doesn't exist, nothing happens.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.UntameableModException, ExceptionType.CastException};
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
            Entity e = Static.getEntity(id);
            if(e == null){
                return new CVoid(line_num, f);
            } else if(e instanceof Tameable){                
                Tameable t = (Tameable) e;
                if(player != null){
                    t.setOwner(Static.getServer().getOfflinePlayer(player));
                } else {
                    t.setOwner(null);
                }
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableModException, line_num, f);
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
            return new ExceptionType[]{ExceptionType.UntameableModException, ExceptionType.CastException};
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
            Entity e = Static.getEntity(id);
            if(e == null){
                return new CNull(line_num, f);
            } else if(e instanceof Tameable){
                AnimalTamer at = ((Tameable)e).getOwner();
                if(at instanceof HumanEntity){
                    return new CString(((HumanEntity)at).getName(), line_num, f);
                } else if(at instanceof OfflinePlayer){
                    return new CString(((OfflinePlayer)at).getName(), line_num, f);
                } else {
                    return new CNull(line_num, f);
                }
            } else {
                throw new ConfigRuntimeException("The specified entity is not tameable", ExceptionType.UntameableModException, line_num, f);
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
            Entity e = Static.getEntity(id);
            boolean ret = false;
            if(e == null){
                ret = false;
            } else if(e instanceof Tameable){
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
            Location l = Static.GetLocation(args[0], (env.GetCommandSender() instanceof Player?env.GetPlayer().getWorld():null), line_num, f);
            Effect e = null;
            try{
                e = Effect.valueOf(args[1].val().toUpperCase());
                if(e.equals(Effect.RECORD_PLAY) || e.equals(Effect.SMOKE) || e.equals(Effect.STEP_SOUND)){
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
            Entity e = Static.getEntity((int)Static.getInt(args[0]));
            if(e instanceof LivingEntity){
                int health = (int)((double)Static.getInt(args[1])/100.0*(double)((LivingEntity)e).getMaxHealth());
                if(health != 0){
                    ((LivingEntity)e).setHealth(health);
                } else {
                    ((LivingEntity)e).damage(9001); //His power level is over 9000!
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
            Entity e = Static.getEntity((int)Static.getInt(args[0]));
            if(e instanceof LivingEntity){
                int h = (int)(((double)((LivingEntity)e).getHealth()/(double)((LivingEntity)e).getMaxHealth())*100);
                return new CInt(h, line_num, f);
            } else {
                throw new ConfigRuntimeException("Not a valid entity id", ExceptionType.FormatException, line_num, f);
            }
        }
        
    }
}
