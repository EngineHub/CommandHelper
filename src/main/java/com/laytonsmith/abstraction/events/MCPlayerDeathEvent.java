package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

/**
 *
 * @author layton
 */
public interface MCPlayerDeathEvent extends BindableEvent{

    public List<MCItemStack> getDrops();
    
    public MCEntity getEntity();
    
    public int getDroppedExp();

    public String getDeathMessage();

    public void setDroppedExp(int i);

    public void setDeathMessage(String nval);
    
}
