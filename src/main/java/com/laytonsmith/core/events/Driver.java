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
	PLAYER_PRELOGIN,
	PLAYER_LOGIN,
    PLAYER_JOIN,    
    PLAYER_INTERACT, 
    PLAYER_SPAWN,     
    PLAYER_DEATH, 
    PLAYER_QUIT,
    PLAYER_CHAT, 
    PLAYER_COMMAND, 
    WORLD_CHANGED,
    SIGN_CHANGED,
    BLOCK_BREAK,
    BLOCK_PLACE,
    TARGET_ENTITY,  
}
