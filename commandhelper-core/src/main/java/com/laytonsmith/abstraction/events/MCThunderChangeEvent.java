package com.laytonsmith.abstraction.events;

/**
 * @author jacobwgillespie
 */
public interface MCThunderChangeEvent extends MCWeatherEvent {

    public boolean toThunderState();

}
