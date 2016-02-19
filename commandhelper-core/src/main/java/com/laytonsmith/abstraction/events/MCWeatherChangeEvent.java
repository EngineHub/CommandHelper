package com.laytonsmith.abstraction.events;

/**
 * @author jacobwgillespie
 */
public interface MCWeatherChangeEvent extends MCWeatherEvent {

    public boolean toWeatherState();

}
