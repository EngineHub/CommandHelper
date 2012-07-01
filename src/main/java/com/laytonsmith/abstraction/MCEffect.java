/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public enum MCEffect {
    BLAZE_SHOOT(1009),
    BOW_FIRE(1002),
    CLICK1(1001),
    CLICK2(1000),
    DOOR_TOGGLE(1003),
    ENDER_SIGNAL(2003),
    EXTINGUISH(1004),
    GHAST_SHOOT(1008),
    GHAST_SHRIEK(1007),
    MOBSPAWNER_FLAMES(2004),
    POTION_BREAK(2002),
    RECORD_PLAY(1005),
    SMOKE(2000),
    STEP_SOUND(2001);

    private final int id;

    MCEffect(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
