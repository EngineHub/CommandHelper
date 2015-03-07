
package com.laytonsmith.abstraction;

import java.util.List;

/**
 *
 * 
 */
public interface MCCommandSender extends AbstractionObject{
    public void sendMessage(String string);

    public MCServer getServer();

    public String getName();
    
    public boolean isOp();

	public boolean hasPermission(String perm);

	public boolean isPermissionSet(String perm);

	public List<String> getGroups();

	public boolean inGroup(String groupName);
}
