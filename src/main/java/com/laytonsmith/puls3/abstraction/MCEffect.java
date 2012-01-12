/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

/**
 *
 * @author layton
 */
public enum MCEffect {
    BOW_FIRE(1002),
    CLICK1(1001),
    CLICK2(1000),
    DOOR_TOGGLE(1003),
    EXTINGUISH(1004),
    RECORD_PLAY(1005),
    GHAST_SHRIEK(1007),
    GHAST_SHOOT(1008),
    BLAZE_SHOOT(1009),
    SMOKE(2000),
    STEP_SOUND(2001),
    POTION_BREAK(2002),
    ENDER_SIGNAL(2003),
    MOBSPAWNER_FLAMES(2004);

    private final int id;

    MCEffect(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
