package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

public interface MCPlayerStatisticIncrementEvent extends BindableEvent {

	EntityType getEntityType();

	Object getMaterial();

	CInt getPreviousValue();

	CInt getNewValue();

	Statistic getStatistic();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancel);

}
