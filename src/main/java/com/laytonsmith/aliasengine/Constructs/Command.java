/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

import java.io.File;

/**
 *
 * @author layton
 */
public class Command extends Construct {

    public Command(String name, int line_num, File file) {
        super(name, ConstructType.COMMAND, line_num, file);
    }

    @Override
    public String toString() {
        return "command:" + value;
    }
}
