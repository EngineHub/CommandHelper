package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import java.util.List;

public interface MCPlayerChatEvent extends MCPlayerEvent {

	String getMessage();

	void setMessage(String message);

	String getFormat();

	void setFormat(String format);

	List<MCPlayer> getRecipients();

	void setRecipients(List<MCPlayer> list);
}
