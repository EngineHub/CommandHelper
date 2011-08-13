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
public class Command extends Construct implements Cloneable {
    public static final long serialVersionUID = 1L;
    
    public Command(String name, int line_num, File file) {
        super(name, ConstructType.COMMAND, line_num, file);
    }

    @Override
    public Command clone() throws CloneNotSupportedException{
        return (Command) super.clone();
    }
}
