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
//            AliasCore core = new AliasCore(true, 10, 5, new File("./config.txt"));
//            boolean cmds = core.alias("/ping a message", null, null);
//            if (!cmds) {
//                System.out.println("No alias(es) found for that command");
//            }
            AliasConfig ac = new AliasConfig("/hey = msg('message')");
            System.out.println("yay!");
        } catch (ConfigCompileException ex) {
            System.err.println(ex);
        }
    }
}
