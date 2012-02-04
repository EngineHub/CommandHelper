/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCGameMode;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.api;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;


/**
 *
 * @author Layton
 */
public class PlayerManagement {

    public static String docs() {
        return "This class of functions allow a MCPlayers to be managed";
    }

    @api
    public static class player implements Function {

        public String getName() {
            return "player";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            if(p == null){
                return new CNull(line_num, f);
            }
            
            if (args.length == 1) {
                p = Static.getServer().getPlayer(args[0].val());
            }
            
            if (p.instanceofPlayer()) {
                return new CString(((MCPlayer) p).getName(), line_num, f);
            } else if (p.instanceofMCConsoleCommandSender()) {
                return new CString("~console", line_num, f);
            } else {
                return new CNull(line_num, f);
            }
        }

        public String docs() {
            return "string {[MCPlayerName]} Returns the full name of the partial Player name specified or the Player running the command otherwise. If the command is being run from"
                    + " the console, then the string '~console' is returned. If the command is coming from elsewhere, null is returned, and the behavior is undefined."
                    + " Note that most functions won't support the user '~console' (they'll throw a PlayerOfflineException), but you can use this to determine"
                    + " where a command is being run from.";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer[] pa = Static.getServer().getOnlinePlayers();
            CString[] sa = new CString[pa.length];
            for (int i = 0; i < pa.length; i++) {
                sa[i] = new CString(pa[i].getName(), line_num, f);
            }
            return new CArray(line_num, f, sa);
        }

        public String docs() {
            return "array {} Returns an array of all the MCPlayer names of all the online MCPlayers on the server";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.getServer().getPlayer(args[0].val());
                if (m == null || !m.isOnline()) {
                    throw new ConfigRuntimeException("The MCPlayer is not online",
                            ExceptionType.PlayerOfflineException, line_num, f);
                }
            }
            if (m == null) {
                throw new ConfigRuntimeException("MCPlayer was not specified", ExceptionType.PlayerOfflineException, line_num, f);
            }
            MCLocation l = m.getLocation();
            MCWorld w = m.getWorld();
            return new CArray(line_num, f,
                    new CDouble(l.getX(), line_num, f),
                    new CDouble(l.getY() - 1, line_num, f),
                    new CDouble(l.getZ(), line_num, f),
                    new CString(w.getName(), line_num, f));
        }

        public String docs() {
            return "array {[MCPlayerName]} Returns an array of x, y, z coords of the MCPlayer specified, or the MCPlayer running the command otherwise. Note that the y coordinate is"
                    + " in relation to the block the MCPlayer is standing on. The array returned will also include the MCPlayer's world in index 3 of the array.";
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
            return "boolean {[MCPlayer], locationArray | [MCPlayer], x, y, z} Sets the location of the MCPlayer to the specified coordinates. If the coordinates"
                    + " are not valid, or the MCPlayer was otherwise prevented from moving, false is returned, otherwise true. If MCPlayer is omitted, "
                    + " the current MCPlayer is used. Note that 1 is automatically added to the y component, which means that sending a MCPlayer to"
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            String MCPlayer = null;
            double x;
            double y;
            double z;
            MCPlayer m = null;
            MCLocation l = null;
            if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    CArray ca = (CArray) args[0];
                    l = ObjectGenerator.GetGenerator().location(ca, (p instanceof MCPlayer ? ((MCPlayer) p).getWorld() : null), line_num, f);
                    x = Static.getNumber(ca.get(0, line_num, f));
                    y = Static.getNumber(ca.get(1, line_num, f));
                    z = Static.getNumber(ca.get(2, line_num, f));
                    if (p instanceof MCPlayer) {
                        m = ((MCPlayer) p);
                    }

                } else {
                    throw new ConfigRuntimeException("Expecting an array at parameter 1 of set_ploc",
                            ExceptionType.CastException, line_num, f);
                }
            } else if (args.length == 2) {
                if (args[1] instanceof CArray) {
                    CArray ca = (CArray) args[1];
                    MCPlayer = args[0].val();
                    l = ObjectGenerator.GetGenerator().location(ca, Static.getServer().getPlayer(MCPlayer).getWorld(), line_num, f);
                    x = l.getX();
                    y = l.getY();
                    z = l.getZ();
                } else {
                    throw new ConfigRuntimeException("Expecting parameter 2 to be an array in set_ploc",
                            ExceptionType.CastException, line_num, f);
                }
            } else if (args.length == 3) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
                x = Static.getNumber(args[0]);
                y = Static.getNumber(args[1]);
                z = Static.getNumber(args[2]);
                l = m.getLocation();
            } else {
                MCPlayer = args[0].val();
                x = Static.getNumber(args[1]);
                y = Static.getNumber(args[2]);
                z = Static.getNumber(args[3]);
                l = StaticLayer.GetLocation(Static.getServer().getPlayer(MCPlayer).getWorld(), x, y, z, 0, 0);
            }
            if (m == null && MCPlayer != null) {
                m = Static.getServer().getPlayer(MCPlayer);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("That MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CBoolean(m.teleport(StaticLayer.GetLocation(l.getWorld(), x, y + 1, z, m.getLocation().getYaw(), m.getLocation().getPitch())), line_num, f);
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
            return "array {[MCPlayer]} Returns an array with the (x, y, z, world) coordinates of the block the MCPlayer has highlighted"
                    + " in their crosshairs. If MCPlayer is omitted, the current MCPlayer is used. If the block is too far, a"
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else {
                m = Static.getServer().getPlayer(args[0].val());
                if (m == null || !m.isOnline()) {
                    throw new ConfigRuntimeException("That MCPlayer is not online",
                            ExceptionType.PlayerOfflineException, line_num, f);
                }
            }
            if (m != null) {
                MCBlock b = m.getTargetBlock(null, 200);
                if (b == null) {
                    throw new ConfigRuntimeException("No block in sight, or block too far",
                            ExceptionType.RangeException, line_num, f);
                }
                return new CArray(line_num, f, new CInt(b.getX(), line_num, f),
                        new CInt(b.getY(), line_num, f),
                        new CInt(b.getZ(), line_num, f),
                        new CString(b.getWorld().getName(), line_num, f));
            } else {
                throw new ConfigRuntimeException("MCPlayer was not specified", ExceptionType.PlayerOfflineException, line_num, f);
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 1) {
                m = Static.getServer().getPlayer(args[0].val());
            } else {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            m.setHealth(0);
            return new CVoid(line_num, f);
        }

        public String docs() {
            return "void {[MCPlayerName]} Kills the specified MCPlayer, or the current MCPlayer if it is omitted";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else {
                m = Static.getServer().getPlayer(args[0].val());
            }

            if (m == null) {
                throw new ConfigRuntimeException("MCPlayer was not specified, or is offline", ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("That MCPlayer is not online.",
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
            return "array {[MCPlayerName]} Returns an array of the groups a MCPlayer is in. If MCPlayerName is omitted, the current MCPlayer is used.";
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
            return "mixed {[pName], [value]} Returns various information about the MCPlayer specified, or the current MCPlayer if no argument was given."
                    + "If value is set, it should be an integer of one of the following indexes, and only that information for that index"
                    + " will be returned. Otherwise if value is not specified (or is -1), it returns an array of"
                    + " information with the following pieces of information in the specified index: "
                    + "<ul><li>0 - MCPlayer's name; This will return the MCPlayer's exact name, "
                    + " even if called with a partial match.</li><li>1 - MCPlayer's location; an array of the MCPlayer's xyz coordinates</li><li>2 - MCPlayer's cursor; an array of the "
                    + "location of the MCPlayer's cursor, or null if the block is out of sight.</li><li>3 - MCPlayer's IP; Returns the IP address of this MCPlayer.</li><li>4 - Display name; The name that is used when the"
                    + " MCPlayer's name is displayed on screen typically. </li><li>5 - MCPlayer's health; Gets the current health of the MCPlayer, which will be an int"
                    + " from 0-20.</li><li>6 - Item in hand; The value returned by this will be similar to the value returned by get_block_at()</li><li>7 - "
                    + "World name; Gets the name of the world this MCPlayer is in.</li><li>8 - Is Op; true or false if this MCPlayer is an op.</li><li>9 - MCPlayer groups;"
                    + " An array of the permissions groups the MCPlayer is in.</li><li>10 - The MCPlayer's hostname (or IP if a hostname can't be found)</li>"
                    + " <li>11 - Is sneaking?</li></ul>";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender m = env.GetCommandSender();
            String MCPlayer = "";
            int index = -1;
            if (args.length == 0) {
                MCPlayer = (m instanceof MCPlayer ? ((MCPlayer) m).getName() : null);
                index = -1;
            } else if (args.length == 1) {
                MCPlayer = args[0].val();
                index = -1;
            } else {
                MCPlayer = args[0].val();
                index = (int) Static.getInt(args[1]);
            }
            if (MCPlayer == null) {
                throw new ConfigRuntimeException("MCPlayer was not specified", ExceptionType.PlayerOfflineException, line_num, f);
            }
            MCPlayer p = Static.getServer().getPlayer(MCPlayer);
            if (p == null || !p.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (index < -1 || index > 11) {
                throw new ConfigRuntimeException("pinfo expects the index to be between -1 and 11",
                        ExceptionType.RangeException, line_num, f);
            }
            assert index >= -1 && index <= 11;
            ArrayList<Construct> retVals = new ArrayList<Construct>();
            if (index == 0 || index == -1) {
                //MCPlayer name 
                retVals.add(new CString(p.getName(), line_num, f));
            }
            if (index == 1 || index == -1) {
                //MCPlayer location
                retVals.add(new CArray(line_num, f, new CDouble(p.getLocation().getX(), line_num, f),
                        new CDouble(p.getLocation().getY() - 1, line_num, f), new CDouble(p.getLocation().getZ(), line_num, f)));
            }
            if (index == 2 || index == -1) {
                //MCPlayer cursor
                MCBlock b = p.getTargetBlock(null, 200);
                if (b == null) {
                    retVals.add(new CNull(line_num, f));
                } else {
                    retVals.add(new CArray(line_num, f, new CInt(b.getX(), line_num, f), new CInt(b.getY(), line_num, f), new CInt(b.getZ(), line_num, f)));
                }
            }
            if (index == 3 || index == -1) {
                //MCPlayer IP                
                retVals.add(new CString(p.getAddress().getAddress().getHostAddress(), line_num, f));
            }
            if (index == 4 || index == -1) {
                //Display name
                retVals.add(new CString(p.getDisplayName(), line_num, f));
            }
            if (index == 5 || index == -1) {
                //MCPlayer health
                retVals.add(new CInt((long) p.getHealth(), line_num, f));
            }
            if (index == 6 || index == -1) {
                //Item in hand
                MCItemStack is = p.getItemInHand();
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
                //MCPlayer groups
                String[] sa = Static.getPermissionsResolverManager().getGroups(p.getName());
                Construct[] ca = new Construct[sa.length];
                for (int i = 0; i < sa.length; i++) {
                    ca[i] = new CString(sa[i], line_num, f);
                }
                CArray a = new CArray(line_num, f, ca);
                retVals.add(a);
            }
            if (index == 10 || index == -1) {
                retVals.add(new CString(p.getAddress().getHostName(), line_num, f));
            }
            if(index == 11 || index == -1){
                retVals.add(new CBoolean(p.isSneaking(), line_num, f));
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
            return "string {[MCPlayerName]} Gets the world of the MCPlayer specified, or the current MCPlayer, if MCPlayerName isn't specified.";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else {
                m = Static.getServer().getPlayer(args[0].val());
                if (m == null || !m.isOnline()) {
                    throw new ConfigRuntimeException("That MCPlayer is not online",
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
            return "void {[MCPlayerName], [message]} Kicks the specified MCPlayer, with an optional message. If no message is specified, "
                    + "\"You have been kicked\" is used. If no MCPlayer is specified, the current MCPlayer is used, with the default message.";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            String message = "You have been kicked";
            MCPlayer m = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            }
            if (args.length >= 1) {
                m = Static.getServer().getPlayer(args[0].val());
            }
            if (args.length >= 2) {
                message = args[1].val();
            }
            MCPlayer ptok = m;
            if (ptok != null && ptok.isOnline()) {
                ptok.kickPlayer(message);
                return new CVoid(line_num, f);
            } else {
                throw new ConfigRuntimeException("The specified MCPlayer does not seem to be online",
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
            return "void {MCPlayerName, newDisplayName | newDisplayName} Sets a MCPlayer's display name. If the second usage is used,"
                    + " it sets the display name of the MCPlayer running the command. See reset_display_name also. MCPlayerName, as well"
                    + " as all CommandHelper commands expect the MCPlayer's real name, not their display name.";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer MCPlayer = null;
            String name;
            if (args.length == 1) {
                if (p instanceof MCPlayer) {
                    MCPlayer = (MCPlayer) p;
                }
                name = args[0].val();
            } else {
                MCPlayer = p.getServer().getPlayer(args[0].val());
                name = args[1].val();
            }
            if (MCPlayer == null || !MCPlayer.isOnline()) {
                throw new ConfigRuntimeException("That MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            MCPlayer.setDisplayName(name);
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
            return "void {[MCPlayerName]} Resets a MCPlayer's display name to their real name. If MCPlayerName isn't specified, defaults to the"
                    + " MCPlayer running the command.";
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer MCPlayer = null;
            if (args.length == 0) {
                if (p instanceof MCPlayer) {
                    MCPlayer = (MCPlayer) p;
                }
            } else {
                MCPlayer = p.getServer().getPlayer(args[0].val());
            }
            if (MCPlayer == null || !MCPlayer.isOnline()) {
                throw new ConfigRuntimeException("That MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            MCPlayer.setDisplayName(MCPlayer.getName());
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
            return "mixed {F | yaw, pitch | MCPlayer, F | MCPlayer, yaw, pitch | MCPlayer | &lt;none&gt;} Sets the direction the MCPlayer is facing. When using the first variation, expects an integer 0-3, which will"
                    + " set the direction the MCPlayer faces using their existing pitch (up and down) but sets their yaw (left and right) to one of the"
                    + " cardinal directions, as follows: 0 - West, 1 - South, 2 - East, 3 - North, which corresponds to the directions given by F when"
                    + " viewed with F3. In the second variation, specific yaw and pitches can be provided. If the MCPlayer is not specified, the current MCPlayer"
                    + " is used. If just the MCPlayer is specified, that MCPlayer's yaw and pitch are returned as an array, or if no arguments are given, the"
                    + " MCPlayer running the command's yaw and pitch are returned as an array. The function returns void when setting the values. (Note that while this"
                    + " function looks like it has ambiguous arguments, MCPlayers cannot be named numbers.) A note on numbers: The values returned by the getter will always be"
                    + " as such: pitch will always be a number between 90 and -90, with -90 being the MCPlayer looking up, and 90 being the MCPlayer looking down. Yaw will"
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            //Getter
            if (args.length == 0 || args.length == 1) {
                MCLocation l = null;
                if (args.length == 0) {
                    if (p instanceof MCPlayer) {
                        l = ((MCPlayer) p).getLocation();
                    }
                } else if (args.length == 1) {
                    //if it's a number, we are setting F. Otherwise, it's a getter for the MCPlayer specified.
                    try {
                        Integer.parseInt(args[0].val());
                    } catch (NumberFormatException e) {
                        MCPlayer p2 = p.getServer().getPlayer(args[0].val());
                        if (p2 == null || !p2.isOnline()) {
                            throw new ConfigRuntimeException("The specified MCPlayer is offline",
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
            MCPlayer toSet = null;
            float yaw = 0;
            float pitch = 0;
            if (args.length == 1) {
                //We are setting F for this MCPlayer
                if (p instanceof MCPlayer) {
                    toSet = (MCPlayer) p;
                    pitch = toSet.getLocation().getPitch();
                }
                int g = (int) Static.getInt(args[0]);
                if (g < 0 || g > 3) {
                    throw new ConfigRuntimeException("The F specifed must be from 0 to 3",
                            ExceptionType.RangeException, line_num, f);
                }
                yaw = g * 90;
            } else if (args.length == 2) {
                //Either we are setting this MCPlayer's pitch and yaw, or we are setting the specified MCPlayer's F.
                //Check to see if args[0] is a number
                try {
                    Float.parseFloat(args[0].val());
                    //It's the yaw, pitch variation
                    if (p instanceof MCPlayer) {
                        toSet = (MCPlayer) p;
                    }
                    yaw = (float) Static.getNumber(args[0]);
                    pitch = (float) Static.getNumber(args[1]);
                } catch (NumberFormatException e) {
                    //It's the MCPlayer, F variation
                    toSet = Static.getServer().getPlayer(args[0].val());
                    pitch = toSet.getLocation().getPitch();
                    int g = (int) Static.getInt(args[1]);
                    if (g < 0 || g > 3) {
                        throw new ConfigRuntimeException("The F specifed must be from 0 to 3",
                                ExceptionType.RangeException, line_num, f);
                    }
                    yaw = g * 90;
                }
            } else if (args.length == 3) {
                //It's the MCPlayer, yaw, pitch variation
                toSet = Static.getServer().getPlayer(args[0].val());
                yaw = (float) Static.getNumber(args[1]);
                pitch = (float) Static.getNumber(args[2]);
            }

            //Error check our data
            if (toSet == null || !toSet.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (pitch > 90 || pitch < -90) {
                throw new ConfigRuntimeException("pitch must be between -90 and 90",
                        ExceptionType.RangeException, line_num, f);
            }
            MCLocation l = toSet.getLocation().clone();
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
            return "mixed {[MCPlayer], [index]} Gets the inventory information for the specified MCPlayer, or the current MCPlayer if none specified. If the index is specified, only the slot "
                    + " given will be returned, but in general, the return format is: array(array(data, qty, enchantArray, enchantLevel), array(data, qty, enchantArray, enchantLevel), ...)"
                    + " where data is the x:y value of the block (or just the"
                    + " value if it's an item, and y is the damage value for tools), and"
                    + " qty is the number of items. EnchantArray and enchantLevel are an array of enchantments (and their corresponding level)"
                    + " applied to the item. It will always be an array, but it may be empty, or contain just one element."
                    + " The index of the array in the array is 0 - 35, 100 - 103, which corresponds to the slot in the MCPlayers inventory. To access armor"
                    + " slots, you may also specify the index. (100 - 103). The quick bar is 0 - 8. If index is null, the item in the MCPlayer's hand is returned, regardless"
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            int index = -1;
            boolean all = false;
            MCPlayer m = null;
            if (args.length == 0) {
                all = true;
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
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
                throw new ConfigRuntimeException("The specified MCPlayer is not online",
                        ExceptionType.PlayerOfflineException, line_num, f);
            }
            if (!all) {
                if ((index < 0 || index > 35) && (index < 100 || index > 103) && index != -1) {
                    throw new ConfigRuntimeException("The index specified must be between 0-35, or 100-103",
                            ExceptionType.RangeException, line_num, f);
                }
            }
            MCInventory inv = m.getInventory();
            if (!all) {
                String value = "";
                int qty = 0;
                if (index == -1) {
                    MCItemStack is = m.getItemInHand();
                    return getInvSlot(is, line_num, f, env);
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
                        value = Static.ParseItemNotation(inv.getItem(index));
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
                    MCItemStack is = inv.getItem(i);
                    ca.push(getInvSlot(is, line_num, f, env));
                }
                ca.set(100, getInvSlot(inv.getBoots(), line_num, f, env));
                ca.set(101, getInvSlot(inv.getLeggings(), line_num, f, env));
                ca.set(102, getInvSlot(inv.getChestplate(), line_num, f, env));
                ca.set(103, getInvSlot(inv.getHelmet(), line_num, f, env));
                return ca;
            }
        }

        private Construct getInvSlot(MCItemStack is, int line_num, File f, Env env) {
            if (is != null && is.getTypeId() != 0) {
                CArray enchants = new CArray(line_num, f);
                CArray levels = new CArray(line_num, f);
                for (Map.Entry<MCEnchantment, Integer> entry : is.getEnchantments().entrySet()) {
                    MCEnchantment e = entry.getKey();
                    Integer l = entry.getValue();
                    enchants.push(new CString(e.getName(), line_num, f));
                    levels.push(new CInt(l, line_num, f));
                }
                return new CArray(line_num, f,
                        new CString(Static.ParseItemNotation(is), line_num, f),
                        new CInt(is.getAmount(), line_num, f), enchants, levels);
            } else {
                return new CNull(line_num, f);
            }
        }
    }

    @api
    public static class set_pinv implements Function {

        public String getName() {
            return "set_pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3, 4, 5, 7};
        }

        public String docs() {
            return "void {[MCPlayer], slot, item_id, [qty], [damage], [enchantArray, levelArray] | [MCPlayer], pinvArray} Sets the index of the slot to the specified item_id, with the specified qty,"
                    + " or 1 by default. If the qty of armor indexes is greater than 1, it is silently ignored, and only 1 is added."
                    + " item_id follows the same notation for items used elsewhere. Damage defaults to 0, and is a percentage from 0-100, of"
                    + " how damaged an item is. If slot is null, it defaults to the item in hand. The item_id notation gives a shortcut"
                    + " to setting damage values, for instance, set_pinv(null, '35:15') will give the MCPlayer black wool. The \"15\""
                    + " here is an unscaled damage value. This is the same thing as set_pinv(null, 35, 1, 100). 100 is a scaled damage"
                    + " value. When using the second signature, the pinvArray should be an array similar to the array returned by pinv()."
                    + " enchantArray and levelArray may also be specified, which is a shortcut to using enchant_inv. In addition, pinvArray"
                    + " may contain instead of 2 elements, 4 elements, of which the last two are the enchantArray and levelArray, per item.";

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
            return "3.2.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            int slot = 0;
            int offset = 0;
            int qty = 1;
            short damage = -1;
            if (args.length == 1 && args[0] instanceof CArray || args.length == 2 && args[1] instanceof CArray) {
                //we are using the set_pinv(pinv()) method
                CArray ca = null;
                if (args.length == 1) {
                    ca = (CArray) args[0];
                }
                if (args.length == 2) {
                    m = Static.GetPlayer(args[0].val(), line_num, f);
                    ca = (CArray) args[1];
                }

                for (String key : ca.keySet()) {
                    int i = 0;
                    if (Integer.valueOf(key) != null) {
                        i = Integer.parseInt(key);
                    } else {
                        continue; //Ignore this key
                    }
                    if (!ca.contains(key)) {
                        continue; //Ignore this key too
                    }
                    Construct item = ca.get(key);
                    if (item instanceof CNull) {
                        this.exec(line_num, f, env, new CString(m.getName(), line_num, f),
                                new CInt(i, line_num, f),
                                new CInt(0, line_num, f));
                    } else {
                        if (item instanceof CArray && (((CArray) item).size() == 2) || ((CArray) item).size() == 4) {
                            CArray citem = (CArray) item;
                            Construct enchantArray = new CArray(line_num, f);
                            Construct levelArray = new CArray(line_num, f);
                            if (citem.size() == 4) {
                                enchantArray = citem.get(2, line_num, f);
                                levelArray = citem.get(3, line_num, f);
                            }
                            this.exec(line_num, f, env, new CString(m.getName(), line_num, f),
                                    new CInt(i, line_num, f),
                                    new CString(citem.get(0, line_num, f).val(), line_num, f),
                                    new CInt(Static.getInt(citem.get(1, line_num, f)), line_num, f),
                                    enchantArray, levelArray);
                        } else {
                            throw new ConfigRuntimeException("Expecting internal values of the array to be 2 or 4 element arrays", ExceptionType.CastException, line_num, f);
                        }
                    }
                }
                return new CVoid(line_num, f);
            }
            //else we are using the first method
            if (args[0].val().matches("\\d*(:\\d*)?") || Static.isNull(args[0])) {
                //We're using the slot as arg 1
                if (Static.isNull(args[0])) {
                    slot = -1;
                } else {
                    slot = (int) Static.getInt(args[0]);
                }
            } else {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                if (Static.isNull(args[1])) {
                    slot = -1;
                } else {
                    slot = (int) Static.getInt(args[1]);
                }
                offset = 1;
            }
            if (slot < -1 || slot > 35 && slot < 100 || slot > 103) {
                throw new ConfigRuntimeException("Slot number must be from 0-35 or 100-103", ExceptionType.RangeException, line_num, f);
            }
            if (args.length > 2 + offset) {
                qty = (int) Static.getInt(args[2 + offset]);
            }
            qty = Static.Normalize(qty, 0, Integer.MAX_VALUE);
            MCItemStack is = Static.ParseItemNotation(this.getName(), args[1 + offset].val(), qty, line_num, f);
            if (args.length > 3 + offset) {
                damage = (short) Static.getInt(args[3 + offset]);
            }


            if (damage != -1) {
                damage = (short) java.lang.Math.max(0, java.lang.Math.min(100, damage));
                short max = is.getType().getMaxDurability();
                is.setDurability((short) ((max * damage) / 100));
            }

            if (is.getTypeId() == 0) {
                qty = 0; //Giving the MCPlayer air crashes their client, so just remove the item
                is.setTypeId(1);
            }

            if (qty == 0) {
                is = null;
            }
            if (slot == -1) {
                m.setItemInHand(is);
            } else {
                if (slot == 103) {
                    m.getInventory().setHelmet(is);
                } else if (slot == 102) {
                    m.getInventory().setChestplate(is);
                } else if (slot == 101) {
                    m.getInventory().setLeggings(is);
                } else if (slot == 100) {
                    m.getInventory().setBoots(is);
                } else {
                    m.getInventory().setItem(slot, is);
                }
            }
            if (args.length > 4 + offset) {
                //We want to enchant this item also
                Enchantments.enchant_inv ei = new Enchantments.enchant_inv();
                ei.exec(line_num, f, env, new CString(m.getName(), line_num, f),
                        new CInt(slot, line_num, f),
                        args[4 + offset],
                        args[5 + offset]);
            }
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pmode implements Function {

        public String getName() {
            return "pmode";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "string {[MCPlayer]} Returns the MCPlayer's game mode. It will be one of \"CREATIVE\" or \"SURVIVAL\".";
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
            return "3.1.3";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.getServer().getPlayer(args[0].val());
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is offline", ExceptionType.PlayerOfflineException, line_num, f);
            }
            String mode = m.getGameMode().name();
            return new CString(mode, line_num, f);
        }
    }

    @api
    public static class set_pmode implements Function {

        public String getName() {
            return "set_pmode";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], mode} Sets the MCPlayer's game mode. mode must be either \"CREATIVE\" or \"SURVIVAL\""
                    + " (case doesn't matter)";
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
            return "3.1.3";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            String mode = "";
            MCGameMode gm;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.getServer().getPlayer(args[0].val());
                mode = args[1].val();
            } else {
                mode = args[0].val();
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("That MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }

            try {
                gm = MCGameMode.valueOf(mode.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ConfigRuntimeException("Mode must be either 'CREATIVE' or 'SURVIVAL'", ExceptionType.FormatException, line_num, f);
            }
            m.setGameMode(gm);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pexp implements Function {

        public String getName() {
            return "pexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[MCPlayer]} Gets the experience of a MCPlayer within this level, as a percentage, from 0 to 99. (100 would be next level,"
                    + " therefore, 0.)";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CInt((int) (m.getExp() * 100), line_num, f);
        }
    }

    @api
    public static class set_pexp implements Function {

        public String getName() {
            return "set_pexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], xp} Sets the experience of a MCPlayer within the current level, as a percentage, from 0 to 100.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int xp = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                xp = (int) Static.getInt(args[1]);
            } else {
                xp = (int) Static.getInt(args[0]);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            m.setExp(((float) xp) / 100.0F);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class give_pexp implements Function {

        public String getName() {
            return "give_pexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], exp} Gives the MCPlayer the specified amount of xp.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
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
            return true;
        }

        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = environment.GetCommandSender();
            MCPlayer m = null;
            int xp = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                xp = (int) Static.getInt(args[1]);
            } else {
                xp = (int) Static.getInt(args[0]);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }

            m.giveExp(xp);

            return new CVoid(line_num, f);
        }
    }

    @api
    public static class plevel implements Function {

        public String getName() {
            return "plevel";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[MCPlayer]} Gets the MCPlayer's level.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CInt(m.getLevel(), line_num, f);
        }
    }

    @api
    public static class set_plevel implements Function {

        public String getName() {
            return "set_plevel";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], level} Sets the level of a MCPlayer.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int level = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                level = (int) Static.getInt(args[1]);
            } else {
                level = (int) Static.getInt(args[0]);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            m.setLevel(level);
//            float portion = m.getExp();
//            m.setLevel(0);
//            m.setExp(0);
//            m.setTotalExperience(0);
//            m.giveExp(7 + (level * 7 >> 1));
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class ptexp implements Function {

        public String getName() {
            return "ptexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[MCPlayer]} Gets the total experience of a MCPlayer.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CInt(m.getTotalExperience(), line_num, f);
        }
    }

    @api
    public static class set_ptexp implements Function {

        public String getName() {
            return "set_ptexp";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], xp} Sets the total experience of a MCPlayer.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int xp = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                xp = (int) Static.getInt(args[1]);
            } else {
                xp = (int) Static.getInt(args[0]);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            m.setTotalExperience(xp);
//            m.setLevel(0);
//            m.setExp(0);
//            m.setTotalExperience(0);
//            m.giveExp(xp);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pfood implements Function {

        public String getName() {
            return "pfood";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[MCPlayer]} Returns the MCPlayer's current food level.";
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
            return "3.1.3";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CInt(m.getFoodLevel(), line_num, f);
        }
    }

    @api
    public static class set_pfood implements Function {

        public String getName() {
            return "set_pfood";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], level} Sets the MCPlayer's food level. This is an integer from 0-?";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
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

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            int level = 0;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                level = (int) Static.getInt(args[1]);
            } else {
                level = (int) Static.getInt(args[0]);
            }
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("The specified MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            m.setFoodLevel(level);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class set_peffect implements Function {

        public String getName() {
            return "set_peffect";
        }

        public Integer[] numArgs() {
            return new Integer[]{3, 4};
        }

        public String docs() {
            return "void {MCPlayer, potionID, strength, [seconds]} Not all potions work of course, but effect is 1-19. Seconds defaults to 30."
                    + " If the potionID is out of range, a RangeException is thrown, because out of range potion effects"
                    + " cause the client to crash, fairly hardcore.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException,
            ExceptionType.RangeException};
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
            return "0.0.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCPlayer m = Static.GetPlayer(args[0].val(), line_num, f);
            if (m == null || !m.isOnline()) {
                throw new ConfigRuntimeException("That player is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            int effect = (int) Static.getInt(args[1]);
            //To work around a bug in bukkit/vanilla, if the effect is invalid, throw an exception
            //otherwise the client crashes, and requires deletion of
            //player data to fix.
            if(effect < 1 || effect > 19){
                throw new ConfigRuntimeException("Invalid effect ID recieved, ignoring", ExceptionType.RangeException, line_num, f);
            }
            int strength = (int) Static.getInt(args[2]);
            int seconds = 30;
            if (args.length == 4) {
                seconds = (int) Static.getInt(args[3]);
            }
            m.addEffect(effect, strength, seconds);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class set_phealth implements Function {

        public String getName() {
            return "set_phealth";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[MCPlayer], health} Sets the MCPlayer's health. health should be an integer from 0-20.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException, ExceptionType.PlayerOfflineException};
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
            return "3.2.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            int health = 0;
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                health = (int) Static.getInt(args[1]);
            } else {
                health = (int) Static.getInt(args[0]);
            }
            if (health < 0 || health > 20) {
                throw new ConfigRuntimeException("Health must be between 0 and 20", ExceptionType.RangeException, line_num, f);
            }
            m.setHealth(health);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class ponline implements Function {

        public String getName() {
            return "ponline";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {MCPlayer} Returns whether or not the specified MCPlayer is online. Note"
                    + " that the name must match exactly, but it will not throw a PlayerOfflineException"
                    + " if the MCPlayer is not online, or if the MCPlayer doesn't even exist.";
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
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            return new CBoolean(Static.getServer().getOfflinePlayer(args[0].val()).isOnline(), line_num, f);
        }
    }

    @api
    public static class pwhitelisted implements Function {

        public String getName() {
            return "pwhitelisted";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {MCPlayer} Returns whether or not this MCPlayer is whitelisted. Note that"
                    + " this will work with offline MCPlayers, but the name must be exact.";
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
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            return new CBoolean(pl.isWhitelisted(), line_num, f);
        }
    }

    @api
    public static class set_pwhitelisted implements Function {

        public String getName() {
            return "set_pwhitelisted";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {MCPlayer, isWhitelisted} Sets the whitelist flag of the specified MCPlayer. Note that"
                    + " this will work with offline MCPlayers, but the name must be exact.";
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
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            boolean whitelist = Static.getBoolean(args[1]);
            pl.setWhitelisted(whitelist);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pbanned implements Function {

        public String getName() {
            return "pbanned";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "boolean {MCPlayer} Returns whether or not this MCPlayer is banned. Note that"
                    + " this will work with offline MCPlayers, but the name must be exact. At this"
                    + " time, this function only works with the vanilla ban system. If you use"
                    + " a third party ban system, you should instead run the command for that"
                    + " plugin instead.";
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
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            return new CBoolean(pl.isBanned(), line_num, f);
        }
    }

    @api
    public static class set_pbanned implements Function {

        public String getName() {
            return "set_pbanned";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {MCPlayer, isBanned} Sets the ban flag of the specified MCPlayer. Note that"
                    + " this will work with offline MCPlayers, but the name must be exact. At this"
                    + " time, this function only works with the vanilla ban system. If you use"
                    + " a third party ban system, you should instead run the command for that"
                    + " plugin instead.";
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
            return "3.3.0";
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(int line_num, File f, Env env, Construct... args) throws ConfigRuntimeException {
            MCOfflinePlayer pl = Static.getServer().getOfflinePlayer(args[0].val());
            boolean ban = Static.getBoolean(args[1]);
            pl.setBanned(ban);
            return new CVoid(line_num, f);
        }
    }

    @api
    public static class pisop implements Function {

        public String getName() {
            return "pisop";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "boolean {[MCPlayer]} Returns whether or not the specified MCPlayer (or the current"
                    + " MCPlayer if not specified) is op";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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
            MCPlayer m = environment.GetPlayer();
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), line_num, f);
            }
            if (m == null) {
                throw new ConfigRuntimeException("That MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            return new CBoolean(m.isOp(), line_num, f);
        }
    }
    
    @api public static class set_compass_target implements Function{

        public String getName() {
            return "set_compass_target";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "array {[MCPlayer], locationArray} Sets the MCPlayer's compass target, and returns the old location.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
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
            MCPlayer m = null;
            MCLocation l;
            if(args.length == 1){
                l = ObjectGenerator.GetGenerator().location(args[0], null, line_num, f);
            } else {
                m = Static.GetPlayer(args[0].val(), line_num, f);
                l = ObjectGenerator.GetGenerator().location(args[1], null, line_num, f);
            }
            if (m == null) {
                throw new ConfigRuntimeException("That MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            MCLocation old = m.getCompassTarget();
            m.setCompassTarget(l);
            return ObjectGenerator.GetGenerator().location(old);
        }
        
    }
    
    @api public static class get_compass_target implements Function{

        public String getName() {
            return "get_compass_target";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "array {[MCPlayer]} Gets the compass target of the specified MCPlayer";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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
            MCPlayer m = environment.GetPlayer();
            if(args.length == 1){
                m = Static.GetPlayer(args[0].val(), line_num, f);
            }
            if (m == null) {
                throw new ConfigRuntimeException("That MCPlayer is not online", ExceptionType.PlayerOfflineException, line_num, f);
            }
            return ObjectGenerator.GetGenerator().location(m.getCompassTarget());
        }
        
    }
    
    @api public static class ponfire implements Function{

        public String getName() {
            return "ponfire";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "int {[player]} Returns the number of ticks remaining that this player will"
                    + " be on fire for. If the player is not on fire, 0 is returned, which incidentally"
                    + " is false.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
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
            MCPlayer p = environment.GetPlayer();
            if(args.length == 1){
                p = Static.GetPlayer(args[0]);
            }
            int left = p.getRemainingFireTicks();
            return new CInt(left, line_num, f);
        }
        
    }
    
    @api public static class set_ponfire implements Function{

        public String getName() {
            return "set_ponfire";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[player], ticks} Sets the player on fire for the specified number of"
                    + " ticks. If a boolean is given for ticks, false is 0, and true is 20.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
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
            MCPlayer p = environment.GetPlayer();
            Construct ticks;
            if(args.length == 2){
                p = Static.GetPlayer(args[0]);
                ticks = args[1];
            } else {
                ticks = args[0];
            }
            int tick = 0;
            if(ticks instanceof CBoolean){
                boolean value = ((CBoolean)ticks).getBoolean();
                if(value){
                    tick = 20;
                }
            } else {
                tick = (int) Static.getInt(ticks);
            }
            p.setRemainingFireTicks(tick);
            return new CVoid(line_num, f);
        }
        
    }
    
//    @api public static class phas_item implements Function{
//
//        public String getName() {
//            return "phas_item";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{1, 2};
//        }
//
//        public String docs() {
//            return "int {[player], itemId} Returns the quantity of the specified item"
//                    + " that the player is carrying. This counts across all slots in"
//                    + " inventory. Recall that 0 is false, and anything else is true,"
//                    + " so this can be used to get the total, or just see if they have"
//                    + " the item.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.FormatException};
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
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            return "3.3.0";
//        }
//        
//    }
//    
//    @api public static class pitem_slot implements Function{
//
//        public String getName() {
//            return "pitem_slot";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{1, 2};
//        }
//
//        public String docs() {
//            return "array {[player], itemID} Given an item id, returns the slot numbers"
//                    + " that the matching item has at least one item in.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{};
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
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            return "3.3.0";
//        }
//        
//    }
//    
//    @api public static class pgive_item implements Function{
//
//        public String getName() {
//            return "pgive_item";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{2, 3};
//        }
//
//        public String docs() {
//            return "int {[player], itemID, qty} Gives a player the specified item * qty."
//                    + " Unlike set_pinv(), this does not specify a slot. The qty is distributed"
//                    + " in the player's inventory, first filling up slots that have the same item"
//                    + " type, up to the max stack size, then fills up empty slots, until either"
//                    + " the entire inventory is filled, or the entire amount has been given."
//                    + " The number of items actually given is returned, which will be less than"
//                    + " or equal to the quantity provided.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{};
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
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            return "3.3.0";
//        }
//        
//    }
//    
//    @api public static class ptake_item implements Function{
//
//        public String getName() {
//            return "ptake_item";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{2, 3};
//        }
//
//        public String docs() {
//            return "int {[player], itemID, qty} Works in reverse of pgive_item(), but"
//                    + " returns the number of items actually taken, which will be"
//                    + " from 0 to qty.";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{};
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
//        public Boolean runAsync() {
//            return false;
//        }
//
//        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            return "3.3.0";
//        }
//        
//    }
//    
//    @api
//    public static class pinv_consolidate implements Function {
//        
//        public String getName() {
//            return "pinv_consolidate";
//        }
//        
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1};
//        }
//        
//        public String docs() {
//            return "void {[player]} Consolidates a player's inventory as much as possible."
//                    + " There is no guarantee anything will happen after this function"
//                    + " is called, and there is no way to specify details about how"
//                    + " consolidation occurs, however, the following heuristics are followed:"
//                    + " The hotbar items will not be moved from the hotbar, unless there are"
//                    + " two+ slots that have the same item. Items in the main inventory area"
//                    + " will be moved closer to the bottom of the main inventory. No empty slots"
//                    + " will be filled in the hotbar.";
//        }
//        
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{};
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
//        public Boolean runAsync() {
//            return false;
//        }
//        
//        public Construct exec(int line_num, File f, Env environment, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not yet supported");
//        }
//        
//        public String since() {
//            return "3.3.0";
//        }
//    }
    
    
    
}
