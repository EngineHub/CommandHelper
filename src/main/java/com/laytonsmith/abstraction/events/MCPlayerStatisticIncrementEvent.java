package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

public interface MCPlayerStatisticIncrementEvent extends BindableEvent {

    public EntityType getEntityType();

    public Object getMaterial();

    public CInt getPreviousValue();

    public CInt getNewValue();

    public Statistic getStatistic();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
