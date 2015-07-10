package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 * Item flags
 *
 * @author jacobwgillespie
 */
@MEnum("ItemFlag")
public enum MCItemFlag {
    HIDE_ATTRIBUTES,
    HIDE_DESTROYS,
    HIDE_ENCHANTS,
    HIDE_PLACED_ON,
    HIDE_POTION_EFFECTS,
    HIDE_UNBREAKABLE
}
