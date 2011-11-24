/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CInt;
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
import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.material.MaterialData;

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
            return "void {mobType, [qty], [location]} Spawns qty mob of one of the following types at location. qty defaults to 1, and location defaults"
                    + " to the location of the player. mobType can be one of: CHICKEN, COW, CREEPER, GHAST,"
                    + " PIG, PIGZOMBIE, SHEEP, SKELETON, SLIME, SPIDER, SQUID, WOLF, ZOMBIE, CAVESPIDER,"
                    + " ENDERMAN, SILVERFISH, BLAZE, VILLAGER, ENDERDRAGON. Spelling matters, but capitalization doesn't. At this"
                    + " time, the function is limited to spawning a maximum of 50 at a time. Further, SHEEP can be spawned as any color, by specifying"
                    + " SHEEP:COLOR, where COLOR is any of the dye colors: BLACK RED GREEN BROWN BLUE PURPLE CYAN SILVER GRAY PINK LIME YELLOW LIGHT_BLUE MAGENTA ORANGE WHITE. COLOR defaults to white if not"
                    + " specified.";
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
            BLAZE, ENDERDRAGON
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
            try {
                switch (MOBS.valueOf(mob.toUpperCase())) {
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
                }
            } catch (IllegalArgumentException e) {
                throw new ConfigRuntimeException("No mob of type " + mob + " exists",
                        ExceptionType.FormatException, line_num, f);
            }
            if (l.getWorld() != null) {
                for (int i = 0; i < qty; i++) {
                    Entity e = l.getWorld().spawn(l, mobType);
                    if (e instanceof Sheep) {
                        Sheep s = (Sheep) e;
                        try {
                            s.setColor(DyeColor.valueOf(sheepColor.toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            throw new ConfigRuntimeException(sheepColor.toUpperCase() + " is not a valid color",
                                    ExceptionType.FormatException, line_num, f);
                        }
                    }
                }
            } else {
                throw new ConfigRuntimeException("World was not specified", ExceptionType.InvalidWorldException, line_num, f);
            }
            return new CVoid(line_num, f);
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
}
