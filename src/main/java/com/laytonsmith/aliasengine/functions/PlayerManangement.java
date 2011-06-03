/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sun.management.resources.agent;

/**
 *
 * @author Layton
 */
public class PlayerManangement {

    public static String docs() {
        return "This class of functions allow a players to be managed";
    }

    @api
    public static class player implements Function {

        public String getName() {
            return "player";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if (p == null) {
                return new CString("TestPlayer", line_num);
            } else {
                return new CString(p.getName(), line_num);
            }
        }

        public String docs() {
            return "string {} Returns the name of the player running the command";
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

    @api
    public static class all_players implements Function {

        public String getName() {
            return "all_players";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player[] pa = Static.getServer().getOnlinePlayers();
            CString[] sa = new CString[pa.length];
            for (int i = 0; i < pa.length; i++) {
                sa[i] = new CString(pa[i].getName(), line_num);
            }
            return new CArray(line_num, sa);
        }

        public String docs() {
            return "array {} Returns an array of all the player names of all the online players on the server";
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
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class ploc implements Function {

        public String getName() {
            return "ploc";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            p = p.getServer().getPlayer(args[0].val());
            if (p == null) {
                throw new CancelCommandException("The player is not online");
            }
            Location l = p.getLocation();
            return new CArray(line_num, new CInt((int) l.getX(), line_num),
                    new CInt((int) l.getY(), line_num),
                    new CInt((int) l.getZ(), line_num));
        }

        public String docs() {
            return "array {playerName} Returns an array of x, y, z coords of the player";
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
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class set_ploc implements Function {

        public String getName() {
            return "set_ploc";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 4};
        }

        public String docs() {
            return "boolean {player, xyzArray | player, x, y, z} Sets the location of the player to the specified coordinates. If the coordinates"
                    + " are not valid, or the player was otherwise prevented from moving, false is returned, otherwise true.";
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

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String player;
            int x;
            int y;
            int z;
            if (args.length == 2) {
                if (args[1] instanceof CArray) {
                    CArray ca = (CArray) args[1];
                    player = args[0].val();
                    if (ca.size() != 3) {
                        throw new ConfigRuntimeException("Expecting array at parameter 2 of set_ploc to have 3 values");
                    }
                    x = (int) Static.getInt(ca.get(0));
                    y = (int) Static.getInt(ca.get(1));
                    z = (int) Static.getInt(ca.get(2));
                } else {
                    throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_ploc");
                }
            } else {
                player = args[0].val();
                x = (int) Static.getInt(args[1]);
                y = (int) Static.getInt(args[2]);
                z = (int) Static.getInt(args[3]);
            }
            Player m = p.getServer().getPlayer(player);
            return new CBoolean(m.teleport(new Location(p.getWorld(), x, y, z)), line_num);
        }
    }

    @api
    public static class pcursor implements Function {

        public String getName() {
            return "pcursor";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "array {player} Returns an array with the x, y, z coordinates of the block the player has highlighted"
                    + " in their crosshairs.";
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
            return "3.0.2";
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Block b = p.getTargetBlock(null, 200);
            if (b == null) {
                throw new CancelCommandException("No block in sight, or block too far");
            }
            return new CArray(line_num, new CInt(b.getX(), line_num), new CInt(b.getY(), line_num), new CInt(b.getZ(), line_num));
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class kill implements Function {

        public String getName() {
            return "kill";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            p = p.getServer().getPlayer(args[0].val());
            if (p == null) {
                throw new CancelCommandException("The player is online");
            }
            p.setHealth(0);
            return new CVoid(line_num);
        }

        public String docs() {
            return "void {playerName} Kills the specified player";
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
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api
    public static class pgroup implements Function {

        public String getName() {
            return "pgroup";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String[] sa = Static.getPermissionsResolverManager().getGroups(args[0].val());
            Construct[] ca = new Construct[sa.length];
            for (int i = 0; i < sa.length; i++) {
                ca[i] = new CString(sa[i], line_num);
            }
            CArray a = new CArray(line_num, ca);
            return a;
        }

        public String docs() {
            return "array {playerName} Returns an array of the groups a player is in.";
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
            return "3.0.1";
        }

        public Boolean runAsync() {
            return false;
        }
    }

    @api public static class pinfo implements Function {

        public String getName() {
            return "pinfo";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[pName], [value]} Returns various information about the player specified, or the current player if no argument was given."
                    + "If value is set, it should be an integer of one of the following indexes, and only that information for that index"
                    + " will be returned. Otherwise if value is not specified (or is -1), it returns an array of"
                    + " information with the following pieces of information in the specified index: 0 - Player's name; This will return the player's exact name, "
                    + " even if called with a partial match. 1 - Player's location; an array of the player's xyz coordinates 2 - Player's cursor; an array of the "
                    + "location of the player's cursor, or null if the block is out of sight. 3 - Player's IP; Returns the IP address of this player. 4 - Display name; The name that is used when the"
                    + " player's name is displayed on screen typically. 5 - Player's health; Gets the current health of the player, which will be an int"
                    + " from 0-20. 6 - Item in hand; The value returned by this will be similar to the value returned by get_block_at() 7 - "
                    + "World name; Gets the name of the world this player is in. 8 - Is Op; true or false if this player is an op. 9 - Player groups;"
                    + " An array of the permissions groups the player is in.";
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

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String player = "";
            int index = -1;
            if (args.length == 0) {
                player = p.getName();
                index = -1;
            } else if (args.length == 1) {
                player = args[0].val();
                index = -1;
            } else {
                player = args[0].val();
                index = (int) Static.getInt(args[1]);
            }
            p = p.getServer().getPlayer(player);
            if (!p.isOnline()) {
                throw new CancelCommandException("The specified player is not online");
            }
            if (index < -1 || index > 9) {
                throw new ConfigRuntimeException("pinfo expects the index to be between -1 and 8");
            }
            assert index >= -1 && index <= 9;
            ArrayList<Construct> retVals = new ArrayList<Construct>();
            if (index == 0 || index == -1) {
                //Player name
                retVals.add(new CString(p.getName(), line_num));
            }
            if (index == 1 || index == -1) {
                //Player location
                retVals.add(new CArray(line_num, new CInt((long) p.getLocation().getX(), line_num),
                        new CInt((long) p.getLocation().getY(), line_num), new CInt((long) p.getLocation().getZ(), line_num)));
            }
            if (index == 2 || index == -1) {
                //Player cursor
                Block b = p.getTargetBlock(null, 200);
                if (b == null) {
                    retVals.add(new CNull(line_num));
                } else {
                    retVals.add(new CArray(line_num, new CInt(b.getX(), line_num), new CInt(b.getY(), line_num), new CInt(b.getZ(), line_num)));
                }
            }
            if (index == 3 || index == -1) {
                //Player IP
                retVals.add(new CString(p.getAddress().getHostName(), line_num));
            }
            if (index == 4 || index == -1) {
                //Display name
                retVals.add(new CString(p.getDisplayName(), line_num));
            }
            if (index == 5 || index == -1) {
                //Player health
                retVals.add(new CInt((long) p.getHealth(), line_num));
            }
            if (index == 6 || index == -1) {
                //Item in hand
                ItemStack is = p.getItemInHand();
                byte data = 0;
                if(is.getData() != null){
                    data = is.getData().getData();
                }
                retVals.add(new CString(is.getTypeId() + ":" + data, line_num));
            }
            if (index == 7 || index == -1) {
                //World name
                retVals.add(new CString(p.getWorld().getName(), line_num));
            }
            if (index == 8 || index == -1) {
                //Is op
                retVals.add(new CBoolean(p.isOp(), line_num));
            }
            if (index == 9 || index == -1) {
                //Player groups
                String[] sa = Static.getPermissionsResolverManager().getGroups(p.getName());
                Construct[] ca = new Construct[sa.length];
                for (int i = 0; i < sa.length; i++) {
                    ca[i] = new CString(sa[i], line_num);
                }
                CArray a = new CArray(line_num, ca);
                retVals.add(a);
            }
            if (retVals.size() == 1) {
                return retVals.get(0);
            } else {
                CArray ca = new CArray(line_num);
                for (Construct c : retVals) {
                    ca.push(c);
                }
                return ca;
            }
        }
    }

    @api
    public static class pworld implements Function {

        public String getName() {
            return "pworld";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "string {[playerName]} Gets the world of the player specified, or the current player, if playerName isn't specified.";
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

        public Boolean runAsync() {
            return true;
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CString(p.getWorld().getName(), line_num);
        }
    }

    @api
    public static class kick implements Function {

        public String getName() {
            return "kick";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {playerName, [message]} Kicks the specified player, with an optional message. If no message is specified, "
                    + "\"You have been kicked\" is used.";
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

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String message = "You have been kicked";
            if (args.length == 2) {
                message = args[1].val();
            }
            Player ptok = p.getServer().getPlayer(args[0].val());
            if (ptok.isOnline()) {
                ptok.kickPlayer(message);
                return new CVoid(line_num);
            } else {
                throw new CancelCommandException("The specified player does not seem to be online");
            }
        }
    }
}
