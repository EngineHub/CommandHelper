/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events;

/**
 * This class is an enum class that represents all the types of events that CH is aware of. The
 * reason an enum is required, is because events can more easily be sorted and found this way.
 * @author layton
 */
public enum Driver {
	BLOCK_BREAK,
	BLOCK_PLACE,
    ENTITY_DAMAGE_PLAYER,    
    PLAYER_CHAT, 
    PLAYER_COMMAND,     
    PLAYER_DEATH, 
    PLAYER_INTERACT,
    PLAYER_JOIN, 
    PLAYER_LOGIN, 
    PLAYER_PRELOGIN,
    PLAYER_QUIT,
    PLAYER_SPAWN,
    SIGN_CHANGED,
    TARGET_ENTITY, 
    WORLD_CHANGED,  
}
