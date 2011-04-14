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
    public static void start(String[] args){
        try {
            AliasCore core = new AliasCore(true, 10, 5, new File("plugins/CommandHelper/config.txt"), null);
            boolean cmds = core.alias("/i 2", null, null);
            if (!cmds) {
                System.out.println("No alias(es) found for that command");
            }
        } catch (ConfigCompileException ex) {
            System.err.println(ex);
        }
    }
}
