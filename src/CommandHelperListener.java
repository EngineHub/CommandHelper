// $Id$
/*
 * CommandHelper
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.io.*;
import com.sk89q.commandhelper.*;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CommandHelperListener extends PluginListener {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    
    /**
     * Sessions.
     */
    private Map<String,CommandHelperSession> sessions =
            new HashMap<String,CommandHelperSession>();
    /**
     * List of global aliases.
     */
    private Map<String,String[]> globalAliases =
            new HashMap<String,String[]>();

    /**
     * Load global aliases.
     */
    public void loadGlobalAliases() {
        globalAliases = CommandHelperSession.readAliases("global-aliases.txt");
    }

    /**
     * Find a global alias. May return null.
     *
     * @param command
     * @return
     */
    public String[] findGlobalAlias(String command) {
        return globalAliases.get(command.toLowerCase());
    }

    /**
     * Get session.
     * 
     * @param player
     * @return
     */
    private CommandHelperSession getSession(Player player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        } else {
            CommandHelperSession session = new CommandHelperSession(player.getName());
            sessions.put(player.getName(), session);
            return session;
        }
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min) {
            throw new InsufficientArgumentsException("Minimum " + min + " arguments");
        } else if (max != -1 && args.length - 1 > max) {
            throw new InsufficientArgumentsException("Maximum " + max + " arguments");
        }
    }

    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        try {
            return runCommand(player, split);
        } catch (InsufficientArgumentsException e) {
            player.sendMessage(Colors.Rose + e.getMessage());
            return true;
        }
    }

    /**
     * Runs commands.
     *
     * @param player
     * @param split
     * @return
     */
    private boolean runCommand(Player player, String[] split) throws InsufficientArgumentsException {
        CommandHelperSession session = getSession(player);

        // Repeat command
        if (split[0].equals("/.")) {
            if (session.getLastCommand() != null) {
                player.sendMessage(Colors.LightGray + session.getLastCommand());
                execCommand(player, session.getLastCommand());
            } else {
                player.sendMessage(Colors.Rose + "No previous command.");
            }
            return true;
        
        // Each group
        } else if (split[0].equalsIgnoreCase("/each")
                && player.canUseCommand("/each")) {
            checkArgs(split, 2, -1, split[0]);

            PlayerFilter filter = SimplePlayerFilter.parse(split[1]);

            // Get arguments
            String[] newSplit = new String[split.length - 2];
            System.arraycopy(split, 2, newSplit, 0, split.length - 2);

            // Perform command
            int affected = 0;
            for (Player pl : filter) {
                affected++;

                // Substitute
                for (int i = 2; i < split.length; i++) {
                    if (split[i].equals("^")) {
                        newSplit[i - 2] = pl.getName();
                    }
                }

                execCommand(player, joinString(newSplit, " "));
            }

            // Tell the user if no users were matched
            if (affected == 0) {
                player.sendMessage(Colors.Rose + "No users were matched.");
            }

            return true;

        // Perform
        } else if (split[0].equalsIgnoreCase("/perform")
                && player.canUseCommand("/perform")) {
            checkArgs(split, 2, -1, split[0]);

            PlayerFilter filter = SimplePlayerFilter.parse(split[1]);

            // Get arguments
            String[] newSplit = new String[split.length - 2];
            System.arraycopy(split, 2, newSplit, 0, split.length - 2);

            // Perform command
            int affected = 0;
            for (Player pl : filter) {
                affected++;

                // Substitute
                for (int i = 2; i < split.length; i++) {
                    if (split[i].equals("^")) {
                        newSplit[i - 2] = pl.getName();
                    }
                }

                execCommand(pl, joinString(newSplit, " "));
            }

            // Tell the user if no users were matched
            if (affected == 0) {
                player.sendMessage(Colors.Rose + "No users were matched.");
            }

            return true;

        // Save alias
        } else if (split[0].equalsIgnoreCase("/alias")
                && player.canUseCommand("/alias")) {
            checkArgs(split, 2, -1, split[0]);

            // Get alias name
            String aliasName = split[1];
            if (aliasName.charAt(0) != '/') {
                aliasName = "/" + aliasName;
            }

            // Get arguments
            String[] newSplit = new String[split.length - 2];
            System.arraycopy(split, 2, newSplit, 0, split.length - 2);

            // Set alias
            String[] commands = new String[]{ joinString(newSplit, " ") };
            getSession(player).setAlias(aliasName, commands);

            player.sendMessage(Colors.Yellow + "Alias " + aliasName + " set.");
            session.saveAliases();
            
            return true;

        // Delete alias
        } else if (split[0].equalsIgnoreCase("/delalias")
                && player.canUseCommand("/alias")) {
            checkArgs(split, 1, 1, split[0]);

            // Get alias name
            String aliasName = split[1];
            if (aliasName.charAt(0) != '/') {
                aliasName = "/" + aliasName;
            }

            getSession(player).removeAlias(aliasName);

            player.sendMessage(Colors.Yellow + "Alias " + aliasName + " removed.");
            session.saveAliases();

            return true;

        // Reload global aliases
        } else if (split[0].equalsIgnoreCase("/reloadaliases")
                && player.canUseCommand("/reloadaliases")) {
            checkArgs(split, 0, 0, split[0]);

            loadGlobalAliases();

            player.sendMessage(Colors.Yellow + "Aliases reloaded.");
            session.saveAliases();

            return true;

        // Catch aliases
        } else {
            session.setLastCommand(joinString(split, " "));

            if (player.canUseCommand(split[0])) {
                String[] commands = getSession(player).findAlias(split[0]);
                String[] arguments = new String[split.length - 1];
                System.arraycopy(split, 1, arguments, 0, split.length - 1);

                if (commands != null) {
                    execCommands(player, commands, arguments, false);
                    return true;
                } else {
                    commands = findGlobalAlias(split[0]);

                    if (commands != null) {
                        execCommands(player, commands, arguments, true);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        sessions.remove(player.getName());
    }

    /**
     * Execute a command.
     *
     * @param cmd
     */
    private void execCommand(Player player, String cmd) {
        player.getUser().a.a(new bg(cmd));
    }

    /**
     * Execute a command.
     *
     * @param cmd
     */
    private void execScriptableCommand(Player player, String cmd) {
        if (cmd.charAt(0) != '@') {
            execCommand(player, cmd);
            return;
        }

        String[] args = cmd.split(" ");
        
        if (args[0].equalsIgnoreCase("@read")) {
            if (args.length >= 2) {
                try {
                    String[] newArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                    FileReader input = new FileReader(joinString(newArgs, " "));
                    BufferedReader reader = new BufferedReader(input);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        player.sendMessage(line);
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "@read: Could not read "
                            + args[1] + ": " + e.getMessage());
                }
            } else {
                logger.log(Level.WARNING, "@read requires 2 arguments");
            }
        } else {
            logger.log(Level.WARNING, "Unknown CommandHelper instruction: "
                    + args[0]);
        }
    }

    /**
     * Execute a command.
     *
     * @param cmd
     */
    private void execCommands(Player player, String[] commands,
            String[] args, boolean scriptable) {
        for (String cmd : commands) {
            String[] parts = cmd.split(" ");
            
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("%[0-9]+")) {
                    int n = Integer.parseInt(parts[i].substring(1)) - 1;
                    if (n < args.length && n >= 0) {
                        parts[i] = args[n];
                    } else {
                        parts[i] = "";
                    }
                }
            }

            cmd = joinString(parts, " ");

            if (scriptable) {
                execScriptableCommand(player, cmd);
            } else {
                execCommand(player, cmd);
            }
        }
    }

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    public static String joinString(String[] str, String delimiter) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }
}