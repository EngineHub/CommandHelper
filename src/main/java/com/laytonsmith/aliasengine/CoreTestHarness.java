/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import java.io.File;

/**
 *
 * @author Layton
 */
public class CoreTestHarness {
    public static void start(String config, String prefs){
        try {
            if(config == null){
                config = "CommandHelper/config.txt";
            }
            if(prefs == null){
                prefs = "CommandHelper/preferences.txt";
            }
            AliasCore core = new AliasCore(new File(config), new File(prefs), null);
        } catch (ConfigCompileException ex) {
            System.err.println(ex);
        }
    }
}
