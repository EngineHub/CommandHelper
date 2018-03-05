package com.laytonsmith.abstraction;

import java.util.List;

public interface MCCommandSender extends AbstractionObject {

	void sendMessage(String string);

	MCServer getServer();

	String getName();

	boolean isOp();

	boolean hasPermission(String perm);

	boolean isPermissionSet(String perm);

	List<String> getGroups();

	boolean inGroup(String groupName);
}
