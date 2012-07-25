
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCCommandSender extends AbstractionObject{
    public void sendMessage(String string);

    public MCServer getServer();

    public String getName();
    
    public boolean isOp();

}
