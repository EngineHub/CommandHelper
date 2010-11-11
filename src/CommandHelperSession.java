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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
 * Command history, etc.
 *
 * @author sk89q
 */
public class CommandHelperSession {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    
    /**
     * Player name.
     */
    private String name;
    /**
     * Last command used.
     */
    private String lastCommand;
    /**
     * List of aliases.
     */
    private Map<String,String[]> aliases =
            new HashMap<String,String[]>();

    /**
     * Construct the instance.
     * 
     * @param name
     */
    public CommandHelperSession(String name) {
        this.name = name;
        loadAliases();
    }

    /**
     * @return the last command
     */
    public String getLastCommand() {
        return lastCommand;
    }

    /**
     * @param lastCommand the last command to set
     */
    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    /**
     * Find an alias. May return null.
     * 
     * @param command
     * @return
     */
    public String[] findAlias(String command) {
        return aliases.get(command.toLowerCase());
    }

    /**
     * Set an alias.
     * 
     * @param command
     * @param split
     * @return
     */
    public void setAlias(String command, String[] commands) {
        aliases.put(command.toLowerCase(), commands);
    }

    /**
     * Remove an alias.
     * 
     * @param command
     */
    public void removeAlias(String command) {
        aliases.remove(command.toLowerCase());
    }

    /**
     * Returns true if the player's name is valid.
     * 
     * @return
     */
    private boolean isValidName() {
        if (name.length() < 1 || name.length() > 40) {
            return false;
        }
        if (name.matches("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]")) {
            return false;
        }
        return true;
    }

    /**
     * Load aliases.
     */
    public void loadAliases() {
        if (!isValidName()) { return; }
        aliases = readAliases("aliases" + File.separator + name + ".txt");
    }

    /**
     * Save aliases.
     */
    public void saveAliases() {
        if (!isValidName()) { return; }
        writeAliases("aliases" + File.separator + name + ".txt", aliases);
    }

    /**
     * Read a file containing cauldron recipes.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Map<String,String[]> readAliases(String path) {
        File file = new File(path);
        FileReader input = null;
        Map<String,List<String>> aliases = new HashMap<String,List<String>>();
        String alias = null;

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank lines
                if (line.length() == 0) {
                    continue;
                }

                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
                    continue;
                }

                // Alias
                if (line.charAt(0) == ':') {
                    alias = line.substring(1).toLowerCase();

                // Alias contents
                } else if (alias == null) {
                    logger.log(Level.WARNING, "Alias command '" + line
                            + "' not under any alias");
                } else {
                    List<String> commands;
                    if (!aliases.containsKey(alias)) {
                        commands = new ArrayList<String>();
                        aliases.put(alias, commands);
                    } else {
                        commands = aliases.get(alias);
                    }
                    commands.add(line);
                }
            }

            Map<String,String[]> outAliases = new HashMap<String,String[]>();

            for (Map.Entry<String,List<String>> entry : aliases.entrySet()) {
                outAliases.put(entry.getKey(), entry.getValue().toArray(new String[]{}));
            }

            return outAliases;
        } catch (FileNotFoundException e) {
            return new HashMap<String,String[]>();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load aliases: "
                    + e.getMessage());
            return new HashMap<String,String[]>();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e2) {
            }
        }
    }

    /**
     * Write aliases to file.
     * 
     * @param path
     * @param aliases
     */
    public static void writeAliases(String path, Map<String,String[]> aliases) {
        File file = new File(path);
        FileWriter output = null;

        // Make parent directory
        String parentPath = file.getParent();
        if (parentPath != null) {
            (new File(parentPath)).mkdirs();
        }

        try {
            output = new FileWriter(file);
            BufferedWriter buff = new BufferedWriter(output);
            buff.write("# Generated automatically\r\n");
            buff.write("# Manual changes will likely be overwritten\r\n");

            for (Map.Entry<String,String[]> entry : aliases.entrySet()) {
                buff.write(":" + entry.getKey() + "\r\n");

                for (String command : entry.getValue()) {
                    buff.write(command + "\r\n");
                }
            }

            buff.close();            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write aliases: "
                    + e.getMessage());
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e2) {
            }
        }
    }
}
