package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.NamespacedKey;

public interface MCPlayerRecipeDiscoverEvent extends BindableEvent {

    public NamespacedKey getRecipe();

    public MCPlayer getPlayer();

    public boolean isCancelled();

    public void setCancelled(boolean cancel);

}
