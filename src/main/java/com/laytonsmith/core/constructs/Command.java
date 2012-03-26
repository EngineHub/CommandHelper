/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

/**
 *
 * @author layton
 */
public class Command extends Construct implements Cloneable {
    
    public Command(String name, Target t) {
        super(name, ConstructType.COMMAND, t);
    }

    @Override
    public Command clone() throws CloneNotSupportedException{
        return (Command) super.clone();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
