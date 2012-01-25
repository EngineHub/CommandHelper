/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.tools.DocGen;
import com.laytonsmith.tools.Manager;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.tools.Interpreter;
import com.laytonsmith.tools.SyntaxHighlighters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author Layton
 */
public class Main {

    static List<String> doctypes = new ArrayList<String>(Arrays.asList(new String[]{"html", "wiki", "text"}));

    public static void main(String[] args) throws Exception {
        System.err.println("Running with arguments: " + Arrays.asList(args));
        try {
            Static.getPreferences().init(new File("CommandHelper/preferences.txt"));
            List l = Arrays.asList(args);
            if (args.length == 0) {
                l.add("-help");
            }
            if(l.contains("--manager")){
                Manager.start();
                System.exit(0);
            }
            if(l.contains("--interpreter")){
                Interpreter.start();
                System.exit(0);
            }
            if (l.contains("-help") || l.contains("-h") || l.contains("--help") || l.contains("/?")) {
                System.out.println("CommandHelper can be run as a standalone jar with the command:\n\n"
                        + "     java -jar CommandHelper.jar <options>\n\n"
                        + "where options can be one of the following:\n\n"
                        + "--version - Prints the version and exits\n"
                        + "--help - Displays this message and exits\n"
                        + "--docs [type] - Creates documentation for the functions that CommandHelper knows about.\n"
                        + "     'type' can be one of the following: " + doctypes.toString() + ". Defaults to 'html'.\n"
                        + "--test-compile [file] - Attempts to compile the config file, but does not actually start up. Any\n"
                        + "     compile errors or warnings can be shown this way, without actually running the program.\n"
                        + "     The location of the config file to test can be given, so that you don't actually have to\n"
                        + "     edit your live config file. If you don't provide a file, the default config file is used.\n"
                        + "     Regardless, the program will look in ./CommandHelper/ for the file.\n"
                        + "--copyright - Displays the copyright notice and exits\n"
                        + "--print-db - Prints out the built in database in a human readable form, then exits.\n"
                        + "--manager - Launcher the built in data manager\n"
                        + "--interpreter - Lauches the minimal cmdline interpreter\n"
                        + "--syntax [type] - Generates the syntax highlighter for the specified editor (if available).\n"
                        + "     Don't specify a type to see the available options.\n"
                        );
            }
            if (l.contains("--version")) {
                PluginDescriptionFile me = loadSelf();
                System.out.println("You are running CommandHelper version " + me.getVersion());
                return;
            }
            if (l.contains("--copyright")) {
                System.out.println("CommandHelper/CommandHelper\n"
                        + "Copyright (C) 2010-2011 sk89q <http://www.sk89q.com> and \n"
                        + "wraithguard01 <http://www.laytonsmith.com>\n"
                        + "\n"
                        + "This program is free software: you can redistribute it and/or modify\n"
                        + "it under the terms of the GNU General Public License as published by\n"
                        + "the Free Software Foundation, either version 3 of the License, or\n"
                        + "(at your option) any later version.\n"
                        + "\n"
                        + "This program is distributed in the hope that it will be useful,\n"
                        + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n"
                        + "GNU General Public License for more details.\n"
                        + "\n"
                        + "You should have received a copy of the GNU General Public License\n"
                        + "along with this program. If not, see <http://www.gnu.org/licenses/>.\n");
                return;
            }
            if (l.contains("--print-db")) {
                new SerializedPersistance(new File("CommandHelper/persistance.ser"), null).printValues(System.out);
                return;
            }
            for (int i = 0; i < l.size(); i++) {
                String s = l.get(i).toString();
                if (s.matches("--docs")) {
                    //Documentation generator
                    String type = (i + 1 <= l.size() - 1 ? l.get(i + 1).toString().toLowerCase() : null);
                    if (type == null) {
                        type = "html";
                    }
                    if (!doctypes.contains(type)) {
                        System.out.println("The type of documentation must be one of the following: " + doctypes.toString());
                        return;
                    }
                    System.out.println("Creating " + type + " documentation.");
                    DocGen.functions(type);
                } else if (s.matches("--test-compile")) {
                    System.out.println("This functionality is not currently implemented!");
//                    File f = new File(".");
//                    for (File a : f.listFiles()) {
//                        if (a.getName().equals("CommandHelper.jar")) {
//                            //We are in the plugins folder
//                            f = new File("CommandHelper/bukkit.jar");
//                            if (!f.exists()) {
//                                System.out.println("In order to run the --test-compile command, you must include the latest build of bukkit (not craftbukkit)"
//                                        + " in the CommandHelper folder. You MUST rename it to bukkit.jar. See the wiki for more information.");
//                                System.exit(1);
//                            }
//                            break;
//                        }
//                    }
//                    String file = (i + 1 <= l.size() - 1 ? l.get(i + 1).toString().toLowerCase() : null);
//                    
//                    return;
                } else if(s.matches("--syntax")){
                    String type = (i + 1 <= l.size() - 1 ? l.get(i + 1).toString().toLowerCase() : null);
                    String theme = (i + 2 <= l.size() - 1 ? l.get(i + 2).toString().toLowerCase() : null);
                    System.out.println(SyntaxHighlighters.generate(type, theme));
                }
            }
        } catch (NoClassDefFoundError error) {
            System.err.println("The main class requires craftbukkit or bukkit to be included in order to run. If you are seeing"
                    + " this message, you have two options. First, it seems you have renamed your craftbukkit jar, or"
                    + " you are altogether not using craftbukkit. If this is the case, you can download craftbukkit and place"
                    + " it in the correct directory (one above this one) or you can download bukkit, rename it to bukkit.jar,"
                    + " and put it in the CommandHelper directory. If you're dying for more details, here:");
            error.printStackTrace();
        }
    }
    
    private static PluginDescriptionFile loadSelf() throws Exception{
        PluginDescriptionFile description = null;
        System.out.println(new File(".").getAbsolutePath());
        File file = new File("./target/commandhelper-3.1.2-ShadedBundle.jar");
        if (!file.exists()) {
            throw new InvalidPluginException(new FileNotFoundException(String.format("%s does not exist", file.getPath())));
        }
        try {
            JarFile jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidPluginException(new FileNotFoundException("Jar does not contain plugin.yml"));
            }

            InputStream stream = jar.getInputStream(entry);
            description = new PluginDescriptionFile(stream);

            stream.close();
            jar.close();
        } catch (IOException ex) {
            throw new InvalidPluginException(ex);
        } catch (Exception ex) {
            throw new InvalidPluginException(ex);
        }
        return description;
    }
}
