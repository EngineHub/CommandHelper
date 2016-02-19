package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.events.BindableEvent;

/**
 * @author jacobwgillespie
 */
public interface MCWeatherEvent extends BindableEvent {

    public MCWorld getWorld();

}
