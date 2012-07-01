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

    public void addDrop(MCItemStack is);
    
    public void clearDrops();
    
    public String getDeathMessage();

    public int getDroppedExp();

    public List<MCItemStack> getDrops();

    public MCEntity getEntity();

    public void setDeathMessage(String nval);
    
    public void setDroppedExp(int i);
    
}
