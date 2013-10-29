

package com.laytonsmith.core.events;

/**
 * This class is an enum class that represents all the types of events that CH is aware of. The
 * reason an enum is required, is because events can more easily be sorted and found this way.
 * @author layton
 */
public enum Driver {

	/**
	 * Block events
	 */
	BLOCK_BREAK,
	BLOCK_BURN,
	BLOCK_DISPENSE,
	BLOCK_GROW,
	BLOCK_IGNITE,
	BLOCK_PLACE,
	SIGN_CHANGED,

	/**
	 * Entity events
	 */
	CREATURE_SPAWN,
	ENTITY_CHANGE_BLOCK,
	ENTITY_DAMAGE,
	ENTITY_DAMAGE_PLAYER,
	ENTITY_DEATH,
	ENTITY_ENTER_PORTAL,
	ENTITY_EXPLODE,
	HANGING_BREAK,
	ITEM_DROP,
	ITEM_PICKUP,
	ITEM_SPAWN,
	POTION_SPLASH,
	PROJECTILE_HIT,
	PROJECTILE_LAUNCH,
	TARGET_ENTITY,

	/**
	 * Inventory events
	 */
	INVENTORY_CLICK,
	INVENTORY_CLOSE,
	INVENTORY_DRAG,
	INVENTORY_OPEN,
	ITEM_ENCHANT,
	ITEM_HELD,
	ITEM_PRE_CRAFT,
	ITEM_PRE_ENCHANT,

	/**
	 * Player events
	 */
	BOOK_EDITED,
	EXP_CHANGE,
	GAMEMODE_CHANGE,
	PLAYER_BED_EVENT,
	PLAYER_CHAT,
	PLAYER_COMMAND,
	PLAYER_CONSUME,
	PLAYER_DEATH,
	PLAYER_FISH,
	PLAYER_INTERACT,
	PLAYER_INTERACT_ENTITY,
	PLAYER_JOIN,
	PLAYER_KICK,
	PLAYER_LOGIN,
	PLAYER_MOVE,
	PLAYER_PORTAL_TRAVEL,
	PLAYER_PRELOGIN,
	PLAYER_QUIT,
	PLAYER_SPAWN,
	PLAYER_TELEPORT,
	TAB_COMPLETE,
	WORLD_CHANGED,

	/**
	 * Plugin events
	 */
	PLUGIN_MESSAGE_RECEIVED,

	/**
	 * Server events
	 */
	SERVER_COMMAND,
	SERVER_PING,

	/**
	 * Vehicle events
	 */
	VEHICLE_COLLIDE,
	VEHICLE_ENTER,
	VEHICLE_LEAVE,
	VEHICLE_MOVE,

	/**
	 * Extension events, used by events fired from the extension system.
	 */
	EXTENSION
}
