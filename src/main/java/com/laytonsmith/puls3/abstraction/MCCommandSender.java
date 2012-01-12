/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

/**
 *
 * @author layton
 */
public interface MCCommandSender {
    public void sendMessage(String string);

    public MCServer getServer();

    public String getName();
    
    public boolean isOp();
}
