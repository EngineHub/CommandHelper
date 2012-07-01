package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

/**
 *
 * @author layton
 */
public interface MCPlayerChatEvent extends BindableEvent{
    public String getMessage();
    
    public MCPlayer getPlayer();
    
    public List<MCPlayer> getRecipients();
    
    public void setMessage(String message);
    
    public void setRecipients(List<MCPlayer> list);
}
