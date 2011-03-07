/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Layton
 */
public class CoreTestHarness {
    public static void main(String[] args){
        try {
            System.out.println(new File(".").getAbsolutePath());
            AliasCore core = new AliasCore(true, 10, 5, new File("./config.txt"));
            ArrayList<String> cmds = core.alias("/i stone 64", "wraithguard01");
            if (cmds == null) {
                System.out.println("No alias(es) found for that command");
            } else {
                for (String cmd : cmds) {
                    System.out.println(cmd);
                }
            }
        } catch (ConfigCompileException ex) {
            System.err.println(ex);
        }
    }
}
