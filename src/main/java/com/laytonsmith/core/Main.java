/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.Util;
import com.laytonsmith.tools.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Layton
 */
public class Main {

    static List<String> doctypes = new ArrayList<String>(Arrays.asList(new String[]{"html", "wiki", "text"}));

    public static void main(String[] args) throws Exception {
        try {
            if(args.length == 0){
                args = new String[]{"--help"};
            }
            ArgumentParser argParser = ArgumentParser.GetParser()
                    .addFlag('h', "help", "Shows this screen, then exits")
                    .addFlag("manager", "Launcher the built in interactive data manager, which will allow command line access to the full persistance database.")
                    .addFlag("interpreter", "Lauches the minimal cmdline interpreter. Note that many things don't work properly, and this feature is mostly experimental"
                    + " at this time.")
                    .addArgument("mslp", ArgumentParser.Type.STRING, "Creates an MSLP file based on the directory specified.", "path/to/folder", false)
                    .addFlag('v', "version", "Prints the version of CommandHelper, and exits.")
                    .addFlag("copyright", "Prints the copyright and exits.")
                    .addFlag("print-db", "Prints out the built in database in a human readable form, then exits.")
                    .addArgument("docs", ArgumentParser.Type.STRING, "Prints documentation for the functions that CommandHelper knows about, then exits.\n" +
                            "'type' can be one of the following: " + doctypes.toString() + ". Defaults to 'html'.", "[type]", false)
                    .addFlag("verify", "Compiles all the files in the system, simply checking for compile errors, then exits.")
                    .addFlag("install-cmdline", "Installs MethodScript to your system, so that commandline scripts work. (Currently only unix is supported.)")
                    .addFlag("uninstall-cmdline", "Uninstalls the MethodScript interpreter from your system.")
                    .addArgument("syntax", ArgumentParser.Type.ARRAY_OF_STRINGS, "Generates the syntax highlighter for the specified editor (if available).\n"
                    + "Don't specify a type to see the available options.", "type", false)
            ;
            ArgumentParser.ArgumentParserResults switches = argParser.match(args);            
            
            Prefs.init(new File("CommandHelper/preferences.txt"));
            if(switches.isFlagSet("manager")){
                Manager.start();
                System.exit(0);
            }
            if(switches.isFlagSet("interpreter")){
                Interpreter.start(switches.getStringListArgument());
                System.exit(0);
            }
            
            if(switches.isFlagSet("install-cmdline")){
                Interpreter.install();
                System.exit(0);
            }
            
            if(switches.isFlagSet("uninstall-cmdline")){
                Interpreter.uninstall();
                System.exit(0);
            }
            
            String mslp = switches.getStringArgument("mslp");
            if(mslp != null){
                if(mslp.isEmpty()){
                    System.out.println("Usage: --mslp path/to/folder");
                    System.exit(0);
                }
                MSLPMaker.start(mslp);
                System.exit(0);
            }
            //We don't want to have any loose arguments, they aren't using it correctly.
            if(switches.isFlagSet("help") || !switches.getStringArgument().isEmpty()){
                System.out.println(argParser.getBuiltDescription());
                System.exit(0);
            }

            if (switches.isFlagSet("version")) {
                System.out.println("You are running CommandHelper version " + loadSelfVersion());
                System.exit(0);
            }
            if (switches.isFlagSet("copyright")) {
                System.out.println("The MIT License (MIT)\n" +
                                    "\n" +
                                    "Copyright (c) 2012 Layton Smith, sk89q, Deaygo, \n" +
                                    "t3hk0d3, zml2008, EntityReborn, and albatrossen\n" +
                                    "\n" +
                                    "Permission is hereby granted, free of charge, to any person obtaining a copy of \n" +
                                    "this software and associated documentation files (the \"Software\"), to deal in \n" +
                                    "the Software without restriction, including without limitation the rights to \n" +
                                    "use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of \n" +
                                    "the Software, and to permit persons to whom the Software is furnished to do so, \n" +
                                    "subject to the following conditions:\n" +
                                    "\n" +
                                    "The above copyright notice and this permission notice shall be included in all \n" +
                                    "copies or substantial portions of the Software.\n" +
                                    "\n" +
                                    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR \n" +
                                    "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS \n" +
                                    "FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR \n" +
                                    "COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER \n" +
                                    "IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN \n" +
                                    "CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
                System.exit(0);
            }
            if (switches.isFlagSet("print-db")) {
                new SerializedPersistance(new File("CommandHelper/persistance.ser")).printValues(System.out);
                System.exit(0);
            }
            
            String docs = switches.getStringArgument("docs");
            if (docs != null) {
                //Documentation generator
                if(docs.isEmpty()){
                    docs = "html";
                }
                if (!doctypes.contains(docs)) {
                    System.out.println("The type of documentation must be one of the following: " + doctypes.toString());
                    return;
                }
                System.err.print("Creating " + docs + " documentation...");
                DocGen.functions(docs, api.Platforms.INTERPRETER_JAVA);
                System.err.println("Done.");
                System.exit(0);
            } 
            if (switches.isFlagSet("verify")) {
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
            }
            List<String> syntax = switches.getStringListArgument("syntax");
            if(syntax != null){
                String type = (syntax.size()>=1?syntax.get(0):null);
                String theme = (syntax.size()>=2?syntax.get(1):null);
                System.out.println(SyntaxHighlighters.generate(type, theme));
                System.exit(0);
            }
        } catch (NoClassDefFoundError error) {
            System.err.println(getNoClassDefFoundErrorMessage(error));
        }
    }
    
    public static String getNoClassDefFoundErrorMessage(NoClassDefFoundError error){
        String ret = "The main class requires craftbukkit or bukkit to be included in order to run. If you are seeing"
                    + " this message, you have two options. First, it seems you have renamed your craftbukkit jar, or"
                    + " you are altogether not using craftbukkit. If this is the case, you can download craftbukkit and place"
                    + " it in the correct directory (one above this one) or you can download bukkit, rename it to bukkit.jar,"
                    + " and put it in the CommandHelper directory.";
        if(Prefs.DebugMode()){
            ret += " If you're dying for more details, here:\n";
            ret += Util.GetStacktrace(error);
        }
        return ret;
    }
    
    private static String loadSelfVersion() throws Exception{
        String version = null;

        File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!file.exists()) {
            throw new Exception(new FileNotFoundException(String.format("%s does not exist", file.getPath())));
        }
        try {
            Yaml yaml = new Yaml();
            Object obj = null;
            if(file.isFile()){
                JarFile jar = new JarFile(file);
                JarEntry entry = jar.getJarEntry("plugin.yml");

                if (entry == null) {
                    throw new Exception(new FileNotFoundException("Jar does not contain plugin.yml"));
                }

                InputStream stream = jar.getInputStream(entry);
                obj = yaml.load(stream);                

                stream.close();
                jar.close();
            } else {
                InputStream stream = new FileInputStream(new File(file, "plugin.yml"));
                obj = yaml.load(stream);
                stream.close();
            }
            if(!(obj instanceof HashMap)){
                throw new Exception("Invalid plugin.yml supplied");
            } else {
                version = (String)((HashMap)obj).get("version");
            }
        } catch (IOException ex) {
            throw new Exception(ex);
        } catch (Exception ex) {
            throw new Exception(ex);
        }
        return version;
    }
}
