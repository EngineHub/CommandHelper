package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import java.util.List;

/**
 *
 */
public interface MCPlayerChatEvent extends MCPlayerEvent{
    public String getMessage();
    
    public void setMessage(String message);
	
	public String getFormat();
	
	public void setFormat(String format);
    
    public List<MCPlayer> getRecipients();
    
    public void setRecipients(List<MCPlayer> list);
}
