package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLightningStrike;

/**
 * @author jacobwgillespie
 */
public interface MCLightningStrikeEvent extends MCWeatherEvent {

    public MCLightningStrike getLightning();

}