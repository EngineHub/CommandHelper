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
    public static void start(String path){
        try {
            if(path == null){
                path = "CommandHelper/config.txt";
            }
            AliasCore core = new AliasCore(true, 10, 5, new File(path), null);
        } catch (ConfigCompileException ex) {
            System.err.println(ex);
        }
    }
}
