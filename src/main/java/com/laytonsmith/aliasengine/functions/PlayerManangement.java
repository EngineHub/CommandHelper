/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CDouble;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(p instanceof Player){
                return new CString(((Player)p).getName(), line_num, f);            
            } else if(p instanceof ConsoleCommandSender){
                return new CString("~console", line_num, f);
            } else {
                return new CNull(line_num, f);
            }
        }

        public String docs() {
            return "string {} Returns the name of the player running the command. If the command is being run from the console, then the string '~console'"
                    + " is returned. If the command is coming from elsewhere, null is returned, and the behavior is undefined. Note that most functions"
                    + " won't support the user '~console' (they'll throw a PlayerOfflineException), but you can use this to determine where a command is being run from.";
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

    @api
    public static class all_players implements Function {

        public String getName() {
            return "all_players";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player[] pa = Static.getServer().getOnlinePlayers();
            CString[] sa = new CString[pa.length];
            for (int i = 0; i < pa.length; i++) {
                sa[i] = new CString(pa[i].getName(), line_num, f);
            }
            return new CArray(line_num, f, sa);
        }

        public String docs() {
            return "array {} Returns an array of all the player names of all the online players on the server";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
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
            return new Integer[]{0, 1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            if(p instanceof Player){
                m = (Player)p;
            }
            if (args.length == 1) {
                m = Static.getServer().getPlayer(args[0].val());
                if (m == null || !m.isOnline()) {
                    throw new ConfigRuntimeException("The player is not online", 
                            ExceptionType.PlayerOfflineException, line_num, f);
                }
            }
            if(m == null){
                throw new ConfigRuntimeException("Player was not specified", ExceptionType.PlayerOfflineException, line_num, f);
            }
            Location l = m.getLocation();
            return new CArray(line_num, f, 
                    new CDouble(l.getX(), line_num, f),
                    new CDouble(l.getY() - 1, line_num, f),
                    new CDouble(l.getZ(), line_num, f));
        }

        public String docs() {
            return "array {[playerName]} Returns an array of x, y, z coords of the player specified, or the player running the command otherwise. Note that the y coordinate is"
                    + " in relation to the player's head, not their feet.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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
            return new Integer[]{1, 2, 3, 4};
        }

        public String docs() {
            return "boolean {[player], xyzArray | [player], x, y, z} Sets the location of the player to the specified coordinates. If the coordinates"
                    + " are not valid, or the player was otherwise prevented from moving, false is returned, otherwise true. If player is omitted, "
                    + " the current player is used. Note that 1 is automatically added to the y component, which means that sending a player to"
                    + " x, y, z coordinates shown with F3 will work as expected, instead of getting them stuck inside the floor. ";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.LengthException, ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String player = null;
            double x;
            double y;
            double z;
            Player m = null;
            Location l = null;
            if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    CArray ca = (CArray) args[0];
                    l = Static.GetLocation(ca, (p instanceof Player?((Player)p).getWorld():null), line_num, f);                                       
                    x = Static.getNumber(ca.get(0, line_num));
                    y = Static.getNumber(ca.get(1, line_num));
                    z = Static.getNumber(ca.get(2, line_num));
                    if(p instanceof Player){
                        m = ((Player)p);
                    }

                } else {
                    throw new ConfigRuntimeException("Expecting an array at parameter 1 of set_ploc", 
                            ExceptionType.CastException, line_num, f);
                }
            } else if (args.length == 2) {
                if (args[1] instanceof CArray) {
                    CArray ca = (CArray) args[1];
                    player = args[0].val();
                    l = Static.GetLocation(ca, Static.getServer().getPlayer(player).getWorld(), line_num, f);
                    x = l.getX();
                    y = l.getY();
                    z = l.getZ();
                } else {
                    throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_ploc", 
                            ExceptionType.CastException, line_num, f);
                }
            } else if (args.length == 3) {
                if(p instanceof Player){
                    m = (Player)p;
                }
                x = Static.getNumber(args[0]);
                y = Static.getNumber(args[1]);
                z = Static.getNumber(args[2]);
                l = m.getLocation();
            } else {
                player = args[0].val();
                x = Static.getNumber(args[1]);
                y = Static.getNumber(args[2]);
                z = Static.getNumber(args[3]);
                l = new Location(Static.getServer().getPlayer(player).getWorld(), x, y, z);
            }
            if (m == null && player != null) {
                m = Static.getServer().getPlayer(player);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("That player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CBoolean(m.teleport(new Location(l.getWorld(), x, y + 1, z, m.getLocation().getYaw(), m.getLocation().getPitch())), line_num, f);
        }
    }

    @api
    public static class pcursor implements Function {

        public String getName() {
            return "pcursor";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "array {[player]} Returns an array with the x, y, z coordinates of the block the player has highlighted"
                    + " in their crosshairs. If player is omitted, the current player is used. If the block is too far, a"
                    + " RangeException is thrown.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            if (args.length == 0) {
                if(p instanceof Player){
                    m = (Player)p;
                }
            } else {
                m = Static.getServer().getPlayer(args[0].val());
                if (m == null || !m.isOnline()) {
                    throw new ConfigRuntimeException("That player is not online", 
                            ExceptionType.PlayerOfflineException, line_num, f);
                }
            }
            if(m != null){
                Block b = m.getTargetBlock(null, 200);
                if (b == null) {
                    throw new ConfigRuntimeException("No block in sight, or block too far", 
                            ExceptionType.RangeException, line_num, f);
                }
                return new CArray(line_num, f, new CInt(b.getX(), line_num, f), new CInt(b.getY(), line_num, f), new CInt(b.getZ(), line_num, f));
            } else {
                throw new ConfigRuntimeException("Player was not specified", ExceptionType.PlayerOfflineException, line_num, f); 
            }
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
            return new Integer[]{0, 1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            if (args.length == 1) {
                m = Static.getServer().getPlayer(args[0].val());
            } else {
                if(p instanceof Player){
                    m = (Player)p;
                }
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            m.setHealth(0);
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "void {[playerName]} Kills the specified player, or the current player if it is omitted";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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
            return new Integer[]{0, 1};
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            if (args.length == 0) {
                if(p instanceof Player){
                    m = (Player)p;
                }
            } else {
                m = Static.getServer().getPlayer(args[0].val());
            }
            
            if(m == null){
                throw new ConfigRuntimeException("Player was not specified, or is offline", ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("That player is not online.", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            String[] sa = Static.getPermissionsResolverManager().getGroups(m.getName());
            Construct[] ca = new Construct[sa.length];
            for (int i = 0; i < sa.length; i++) {
                ca[i] = new CString(sa[i], line_num, f);
            }
            CArray a = new CArray(line_num, f, ca);
            return a;
        }

        public String docs() {
            return "array {[playerName]} Returns an array of the groups a player is in. If playerName is omitted, the current player is used.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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
    public static class pinfo implements Function {

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
                    + " information with the following pieces of information in the specified index: "
                    + "<ul><li>0 - Player's name; This will return the player's exact name, "
                    + " even if called with a partial match.</li><li>1 - Player's location; an array of the player's xyz coordinates</li><li>2 - Player's cursor; an array of the "
                    + "location of the player's cursor, or null if the block is out of sight.</li><li>3 - Player's IP; Returns the IP address of this player.</li><li>4 - Display name; The name that is used when the"
                    + " player's name is displayed on screen typically. </li><li>5 - Player's health; Gets the current health of the player, which will be an int"
                    + " from 0-20.</li><li>6 - Item in hand; The value returned by this will be similar to the value returned by get_block_at()</li><li>7 - "
                    + "World name; Gets the name of the world this player is in.</li><li>8 - Is Op; true or false if this player is an op.</li><li>9 - Player groups;"
                    + " An array of the permissions groups the player is in.</li></ul>";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
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

        public Construct exec(int line_num, File f, CommandSender m, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String player = "";
            int index = -1;
            if (args.length == 0) {
                player = (m instanceof Player?((Player)m).getName():null);
                index = -1;
            } else if (args.length == 1) {
                player = args[0].val();
                index = -1;
            } else {
                player = args[0].val();
                index = (int) Static.getInt(args[1]);
            }
            if(player == null){
                throw new ConfigRuntimeException("Player was not specified", ExceptionType.PlayerOfflineException, line_num, f);
            }
            Player p = Static.getServer().getPlayer(player);
            if (p == null || !p.isOnline()) {
                throw new ConfigRuntimeException("The specified player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (index < -1 || index > 9) {
                throw new ConfigRuntimeException("pinfo expects the index to be between -1 and 8", 
                        ExceptionType.RangeException, line_num, f);
            }
            assert index >= -1 && index <= 9;
            ArrayList<Construct> retVals = new ArrayList<Construct>();
            if (index == 0 || index == -1) {
                //Player name 
                retVals.add(new CString(p.getName(), line_num, f));
            }
            if (index == 1 || index == -1) {
                //Player location
                retVals.add(new CArray(line_num, f, new CDouble(p.getLocation().getX(), line_num, f),
                        new CDouble(p.getLocation().getY() - 1, line_num, f), new CDouble(p.getLocation().getZ(), line_num, f)));
            }
            if (index == 2 || index == -1) {
                //Player cursor
                Block b = p.getTargetBlock(null, 200);
                if (b == null) {
                    retVals.add(new CNull(line_num, f));
                } else {
                    retVals.add(new CArray(line_num, f, new CInt(b.getX(), line_num, f), new CInt(b.getY(), line_num, f), new CInt(b.getZ(), line_num, f)));
                }
            }
            if (index == 3 || index == -1) {
                //Player IP
                retVals.add(new CString(p.getAddress().getHostName(), line_num, f));
            }
            if (index == 4 || index == -1) {
                //Display name
                retVals.add(new CString(p.getDisplayName(), line_num, f));
            }
            if (index == 5 || index == -1) {
                //Player health
                retVals.add(new CInt((long) p.getHealth(), line_num, f));
            }
            if (index == 6 || index == -1) {
                //Item in hand
                ItemStack is = p.getItemInHand();
                byte data = 0;
                if (is.getData() != null) {
                    data = is.getData().getData();
                }
                retVals.add(new CString(is.getTypeId() + ":" + data, line_num, f));
            }
            if (index == 7 || index == -1) {
                //World name
                retVals.add(new CString(p.getWorld().getName(), line_num, f));
            }
            if (index == 8 || index == -1) {
                //Is op
                retVals.add(new CBoolean(p.isOp(), line_num, f));
            }
            if (index == 9 || index == -1) {
                //Player groups
                String[] sa = Static.getPermissionsResolverManager().getGroups(p.getName());
                Construct[] ca = new Construct[sa.length];
                for (int i = 0; i < sa.length; i++) {
                    ca[i] = new CString(sa[i], line_num, f);
                }
                CArray a = new CArray(line_num, f, ca);
                retVals.add(a);
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

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player m = null;
            if (args.length == 0) {
                if(p instanceof Player){
                    m = (Player)p;
                }
            } else {
                m = Static.getServer().getPlayer(args[0].val());
                if (m == null || !m.isOnline()) {
                    throw new ConfigRuntimeException("That player is not online", 
                            ExceptionType.PlayerOfflineException, line_num, f);
                }
            }
            return new CString(m.getWorld().getName(), line_num, f);
        }
    }

    @api
    public static class kick implements Function {

        public String getName() {
            return "kick";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "void {[playerName], [message]} Kicks the specified player, with an optional message. If no message is specified, "
                    + "\"You have been kicked\" is used. If no player is specified, the current player is used, with the default message.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String message = "You have been kicked";
            Player m = null;
            if (args.length == 0) {
                if(p instanceof Player){
                    m = (Player)p;
                }
            }
            if (args.length >= 1) {
                m = Static.getServer().getPlayer(args[0].val());
            }
            if (args.length >= 2) {
                message = args[1].val();
            }
            Player ptok = m;
            if (ptok != null && ptok.isOnline()) {
                ptok.kickPlayer(message);
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("The specified player does not seem to be online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
        }
    }

    @api
    public static class set_display_name implements Function {

        public String getName() {
            return "set_display_name";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {playerName, newDisplayName | newDisplayName} Sets a player's display name. If the second usage is used,"
                    + " it sets the display name of the player running the command. See reset_display_name also. playerName, as well"
                    + " as all CommandHelper commands expect the player's real name, not their display name.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player player = null;
            String name;
            if (args.length == 1) {
                if(p instanceof Player){
                    player = (Player)p;
                }
                name = args[0].val();
            } else {
                player = p.getServer().getPlayer(args[0].val());
                name = args[1].val();
            }
            if (player == null || !player.isOnline()) {
                throw new ConfigRuntimeException("That player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            player.setDisplayName(name);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class reset_display_name implements Function {

        public String getName() {
            return "reset_display_name";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[playerName]} Resets a player's display name to their real name. If playerName isn't specified, defaults to the"
                    + " player running the command.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Player player = null;
            if (args.length == 0) {
                if(p instanceof Player){
                    player = (Player)p;
                }
            } else {
                player = p.getServer().getPlayer(args[0].val());
            }
            if (player == null || !player.isOnline()) {
                throw new ConfigRuntimeException("That player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            player.setDisplayName(player.getName());
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pfacing implements Function {

        public String getName() {
            return "pfacing";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2, 3};
        }

        public String docs() {
            return "mixed {F | yaw, pitch | player, F | player, yaw, pitch | player | &lt;none&gt;} Sets the direction the player is facing. When using the first variation, expects an integer 0-3, which will"
                    + " set the direction the player faces using their existing pitch (up and down) but sets their yaw (left and right) to one of the"
                    + " cardinal directions, as follows: 0 - West, 1 - South, 2 - East, 3 - North, which corresponds to the directions given by F when"
                    + " viewed with F3. In the second variation, specific yaw and pitches can be provided. If the player is not specified, the current player"
                    + " is used. If just the player is specified, that player's yaw and pitch are returned as an array, or if no arguments are given, the"
                    + " player running the command's yaw and pitch are returned as an array. The function returns void when setting the values. (Note that while this"
                    + " function looks like it has ambiguous arguments, players cannot be named numbers.) A note on numbers: The values returned by the getter will always be"
                    + " as such: pitch will always be a number between 90 and -90, with -90 being the player looking up, and 90 being the player looking down. Yaw will"
                    + " always be a number between 0 and 359.9~. When using it as a setter, pitch must be a number between -90 and 90, and yaw may be any number."
                    + " If the number given is not between 0 and 359.9~, it will be normalized first. 0 is dead west, 90 is north, etc.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.RangeException, ExceptionType.CastException};
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
            return "3.1.3";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            //Getter
            if (args.length == 0 || args.length == 1) {
                Location l = null;
                if (args.length == 0) {
                    if(p instanceof Player){
                        l = ((Player)p).getLocation();
                    }
                } else if (args.length == 1) {
                    //if it's a number, we are setting F. Otherwise, it's a getter for the player specified.
                    try {
                        Integer.parseInt(args[0].val());
                    } catch (NumberFormatException e) {
                        Player p2 = p.getServer().getPlayer(args[0].val());
                        if (p2 == null || !p2.isOnline()) {
                            throw new ConfigRuntimeException("The specified player is offline", 
                                    ExceptionType.PlayerOfflineException, line_num, f);
                        } else {
                            l = p2.getLocation();
                        }
                    }
                }
                if (l != null) {
                    float yaw = l.getYaw();
                    float pitch = l.getPitch();
                    //normalize yaw
                    if (yaw < 0) {
                        yaw = (((yaw) % 360) + 360);
                    }
                    return new CArray(line_num, f, new CDouble(yaw, line_num, f), new CDouble(pitch, line_num, f));
                }
            }
            //Setter
            Player toSet = null;
            float yaw = 0;
            float pitch = 0;
            if (args.length == 1) {
                //We are setting F for this player
                if(p instanceof Player){
                    toSet = (Player)p;
                    pitch = toSet.getLocation().getPitch();
                }
                int g = (int) Static.getInt(args[0]);
                if (g < 0 || g > 3) {
                    throw new ConfigRuntimeException("The F specifed must be from 0 to 3", 
                            ExceptionType.RangeException, line_num, f);
                }
                yaw = g * 90;
            } else if (args.length == 2) {
                //Either we are setting this player's pitch and yaw, or we are setting the specified player's F.
                //Check to see if args[0] is a number
                try {
                    Float.parseFloat(args[0].val());
                    //It's the yaw, pitch variation
                    if(p instanceof Player){
                        toSet = (Player)p;
                    }
                    yaw = (float) Static.getNumber(args[0]);
                    pitch = (float) Static.getNumber(args[1]);
                } catch (NumberFormatException e) {
                    //It's the player, F variation
                    toSet = p.getServer().getPlayer(args[0].val());
                    pitch = toSet.getLocation().getPitch();
                    int g = (int) Static.getInt(args[1]);
                    if (g < 0 || g > 3) {
                        throw new ConfigRuntimeException("The F specifed must be from 0 to 3", 
                                ExceptionType.RangeException, line_num, f);
                    }
                    yaw = g * 90;
                }
            } else if (args.length == 3) {
                //It's the player, yaw, pitch variation
                toSet = p.getServer().getPlayer(args[0].val());
                yaw = (float) Static.getNumber(args[1]);
                pitch = (float) Static.getNumber(args[2]);
            }

            //Error check our data
            if (toSet == null || !toSet.isOnline()) {
                throw new ConfigRuntimeException("The specified player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (pitch > 90 || pitch < -90) {
                throw new ConfigRuntimeException("pitch must be between -90 and 90", 
                        ExceptionType.RangeException, line_num, f);
            }
            Location l = toSet.getLocation().clone();
            l.setPitch(pitch);
            l.setYaw(yaw);
            toSet.teleport(l);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pinv implements Function {

        public String getName() {
            return "pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[player], [index]} Gets the inventory information for the specified player, or the current player if none specified. If the index is specified, only the slot "
                    + " given will be returned, but in general, the return format is: array(array(data, qty), array(data, qty), ...) where data is the x:y value of the block (or just the"
                    + " value if it's an item), and"
                    + " qty is the number of items. The index of the array in the array is 0 - 35, which corresponds to the slot in the players inventory. To access armor"
                    + " slots, you must specify the index. (100 - 103). The quick bar is 0 - 8. If index is null, the item in the player's hand is returned, regardless"
                    + " of what slot is selected. If there is no item at the slot specified, null is returned.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException, ExceptionType.RangeException};
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
            return "3.1.3";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            int index = -1;
            boolean all = false;
            Player m = null;
            if (args.length == 0) {
                all = true;
                if(p instanceof Player){
                    m = (Player)p;
                }
            } else if (args.length == 1) {
                all = true;
                m = p.getServer().getPlayer(args[0].val());
            } else if (args.length == 2) {
                if (args[1] instanceof CNull) {
                    index = -1;
                } else {
                    index = (int) Static.getInt(args[1]);
                }
                all = false;
                m = p.getServer().getPlayer(args[0].val());
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified player is not online", 
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (!all) {
                if ((index < 0 || index > 35) && (index < 100 || index > 103) && index != -1) {
                    throw new ConfigRuntimeException("The index specified must be between 0-35, or 100-103", 
                            ExceptionType.RangeException, line_num, f);
                }
            }
            PlayerInventory inv = m.getInventory();
            if (!all) {
                String value = "";
                int qty = 0;
                if (index == -1) {
                    ItemStack is = m.getItemInHand();
                    if (is.getTypeId() != 0) {
                        return new CArray(line_num, f,
                                new CString(is.getTypeId() + (is.getData() == null ? "" : ":" + is.getData().getData()), line_num, f),
                                new CInt(is.getAmount(), line_num, f));
                    } else {
                        return new CNull(line_num, f);
                    }
                }
                if (index >= 100 && index <= 103) {
                    qty = 1;
                    switch (index) {
                        case 100:
                            value = Integer.toString(inv.getBoots().getTypeId());
                            break;
                        case 101:
                            value = Integer.toString(inv.getLeggings().getTypeId());
                            break;
                        case 102:
                            value = Integer.toString(inv.getChestplate().getTypeId());
                            break;
                        case 103:
                            value = Integer.toString(inv.getHelmet().getTypeId());
                            break;
                    }
                    if (value.equals("0")) {
                        value = null;
                        qty = 0;
                    }
                } else {
                    if (inv.getItem(index).getTypeId() == 0) {
                        value = null;
                    } else {
                        value = inv.getItem(index).getTypeId() + (inv.getItem(index).getData() == null ? "" : ":" + inv.getItem(index).getData().getData());
                    }
                    qty = inv.getItem(index).getAmount();
                }
                if (value == null) {
                    return new CNull(line_num, f);
                } else {
                    Construct cvalue = null;
                    cvalue = new CString(value, line_num, f);
                    return new CArray(line_num, f, cvalue, new CInt(qty, line_num, f));
                }
            } else {
                CArray ca = new CArray(line_num, f);
                for (int i = 0; i < 36; i++) {
                    ItemStack is = inv.getItem(i);
                    if (is != null && is.getTypeId() != 0) {
                        ca.push(new CArray(line_num, f,
                                new CString(is.getTypeId() + (is.getData() == null ? "" : ":" + is.getData().getData()), line_num, f),
                                new CInt(is.getAmount(), line_num, f)));
                    } else {
                        ca.push(new CNull(line_num, f));
                    }
                }
                return ca;
            }
        }
    }

    @api public static class set_pinv implements Function {

        public String getName() {
            return "set_pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3, 4, 5};
        }

        public String docs() {
            return "void {[player], slot, item_id, [qty], [damage]} Sets the index of the slot to the specified item_id, with the specified qty,"
                    + " or 1 by default. If the qty of armor indexes is greater than 1, it is silently ignored, and only 1 is added."
                    + " item_id follows the same notation for items used elsewhere. Damage defaults to 0, and is a percentage from 0-100, of"
                    + " how damaged an item is.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, CommandSender p, Construct... args) throws ConfigRuntimeException {
            Player m = null;
            if(p instanceof Player){
                m = (Player)p;
            }
            int slot = 0;
            int offset = 0;
            int qty = 1;
            short damage = 0;
            if(args[0].val().matches("\\d*")){
                //We're using the slot as arg 1
                if(Static.isNull(args[0])){
                    slot = -1;
                } else {
                    slot = (int)Static.getInt(args[0]);
                }
            } else {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                if(Static.isNull(args[1])){
                    slot = -1;
                } else {
                    slot = (int)Static.getInt(args[1]);
                }
                offset = 1;
            }
            if(args.length > 2 + offset){
                qty = (int)Static.getInt(args[2 + offset]);
            }            
            qty = Static.Normalize(qty, 0, Integer.MAX_VALUE);
            ItemStack is = Static.ParseItemNotation(this.getName(), args[1 + offset].val(), qty, line_num, f);
            if(args.length > 3 + offset){
                damage = (short)Static.getInt(args[3 + offset]);
            }
            damage = (short)java.lang.Math.max(0, java.lang.Math.min(100, damage));
            short max = is.getType().getMaxDurability();
            is.setDurability((short)((max * damage) / 100));
            
            if(is.getTypeId() == 0){
                qty = 0; //Giving the player air crashes their client, so just remove the item
                is.setTypeId(1);
            }
            
            if(qty == 0){
                is.setAmount(0);
                if(slot == -1){
                    m.setItemInHand(null);
                } else {
                    m.getInventory().setItem(slot, null);
                }
            } else {
                if(slot == -1){
                    m.setItemInHand(is);
                } else {
                    m.getInventory().setItem(slot, is);
                }
            }
            return new CVoid(line_num, f);
        }
    }
    
    
}
