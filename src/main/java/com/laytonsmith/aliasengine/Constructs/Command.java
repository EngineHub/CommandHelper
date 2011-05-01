/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

/**
 *
 * @author layton
 */
public class Command extends Construct {

    public Command(String name, int line_num) {
        super(TType.COMMAND, name, ConstructType.COMMAND, line_num);
    }

    @Override
    public String toString() {
        return "command:" + value;
    }
}
