package com.laytonsmith.abstraction.events;

import java.util.Set;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.CancellableEvent;

public interface MCBroadcastMessageEvent extends BindableEvent, CancellableEvent {
	String getMessage();
	void setMessage(String message);
	Set<MCCommandSender> getRecipients();
	Set<MCPlayer> getPlayerRecipients();
	boolean isCancelled();
}
