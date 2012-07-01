/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCCommandSender extends AbstractionObject{
    public String getName();

    public MCServer getServer();

    public boolean isOp();
    
    public void sendMessage(String string);

}
