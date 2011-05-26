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
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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

        public boolean runAsync() {
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

        public boolean runAsync() {
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

        public boolean runAsync() {
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

        public boolean runAsync() {
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
                    x = (int)Static.getInt(ca.get(0));
                    y = (int)Static.getInt(ca.get(1));
                    z = (int)Static.getInt(ca.get(2));
                } else {
                    throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_ploc");
                }
            } else {
                player = args[0].val();
                x = (int)Static.getInt(args[1]);
                y = (int)Static.getInt(args[2]);
                z = (int)Static.getInt(args[3]);
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

        public boolean runAsync() {
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

        public boolean runAsync() {
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
            String[] sa = com.laytonsmith.aliasengine.Static.getPermissionsResolverManager().getGroups(args[0].val());
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

        public boolean runAsync() {
            return false;
        }
    }
}
