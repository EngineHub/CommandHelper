/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Layton
 */
public class Main {
    static List<String> doctypes = new ArrayList<String>(Arrays.asList(new String[]{"html", "wiki", "text"}));
    public static void main(String [] args){
        if(args.length == 0){
            CoreTestHarness.start(null);
        }
        List l = Arrays.asList(args);
        if(l.contains("-help") || l.contains("-h") || l.contains("--help") || l.contains("/?")){
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
                    + "--copyright - Displays the copyright notice and exits");
        }
        if(l.contains("--version")){
            System.out.println("Whoops, no version information yet.");
            return;
        }
        if(l.contains("--copyright")){
            System.out.println("CommandHelper\n" +
                                "Copyright (C) 2010 sk89q <http://www.sk89q.com> and \n" +
                                "wraithguard01 <http://www.laytonsmith.com>\n" + 
                                "\n" +
                                "This program is free software: you can redistribute it and/or modify\n" +
                                "it under the terms of the GNU General Public License as published by\n" +
                                "the Free Software Foundation, either version 3 of the License, or\n" +
                                "(at your option) any later version.\n" +
                                "\n" +
                                "This program is distributed in the hope that it will be useful,\n" +
                                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n" +
                                "GNU General Public License for more details.\n" +
                                "\n" +
                                "You should have received a copy of the GNU General Public License\n" +
                                "along with this program. If not, see <http://www.gnu.org/licenses/>.\n");
            return;
        }
        for(int i = 0; i < l.size(); i++){
            String s = l.get(i).toString();
            if(s.matches("--docs")){
                //Documentation generator
                String type = (i <= l.size() - 1?l.get(i + 1).toString().toLowerCase():null);
                if(type == null){
                    type = "html";
                }
                if(!doctypes.contains(type)){
                    System.out.println("The type of documentation must be one of the following: " + doctypes.toString());
                    return;
                }
                System.out.println("Creating " + type + " documentation.");
                DocGen.start(type);
            } else if(s.matches("--test-compile")){
                String file = (i <= l.size() - 1?l.get(i + 1).toString().toLowerCase():null);                
                CoreTestHarness.start(file);
                return;
            }
        }
    }
}
