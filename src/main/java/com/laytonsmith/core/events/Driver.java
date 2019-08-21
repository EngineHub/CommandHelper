package com.laytonsmith.core.events;

import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.Static;

/**
 * This class is an enum class that represents all the types of events that CH is aware of. The reason an enum is
 * required, is because events can more easily be sorted and found this way.
 *
 */
public enum Driver {

	/**
	 * Block events
	 */
	PISTON_EXTEND,
	PISTON_RETRACT,
	BLOCK_BREAK,
	BLOCK_BURN,
	BLOCK_DISPENSE,
	BLOCK_GROW,
	BLOCK_IGNITE,
	BLOCK_PLACE,
	BLOCK_FROM_TO,
	SIGN_CHANGED,
	REDSTONE_CHANGED,
	NOTE_PLAY,
	BLOCK_FADE,
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
	ENTITY_INTERACT,
	ENTITY_REGAIN_HEALTH,
	ENTITY_PORTAL_TRAVEL,
	HANGING_BREAK,
	ITEM_DROP,
	ITEM_PICKUP,
	ITEM_DESPAWN,
	ITEM_SPAWN,
	POTION_SPLASH,
	PROJECTILE_HIT,
	PROJECTILE_LAUNCH,
	TARGET_ENTITY,
	ENTITY_TOGGLE_GLIDE,
	FIREWORK_EXPLODE,
	/**
	 * Inventory events
	 */
	INVENTORY_CLICK,
	INVENTORY_CLOSE,
	INVENTORY_DRAG,
	INVENTORY_OPEN,
	ITEM_ENCHANT,
	ITEM_HELD,
	ITEM_SWAP,
	ITEM_PRE_CRAFT,
	ITEM_PRE_ENCHANT,
	/**
	 * Player events
	 */
	BOOK_EDITED,
	EXP_CHANGE,
	GAMEMODE_CHANGE,
	PLAYER_ENTER_BED,
	PLAYER_LEAVE_BED,
	PLAYER_CHAT,
	PLAYER_COMMAND,
	PLAYER_CONSUME,
	PLAYER_DEATH,
	PLAYER_FISH,
	PLAYER_INTERACT,
	PLAYER_INTERACT_ENTITY,
	PLAYER_INTERACT_AT_ENTITY,
	PLAYER_JOIN,
	PLAYER_KICK,
	PLAYER_LOGIN,
	PLAYER_MOVE,
	PLAYER_PORTAL_TRAVEL,
	PLAYER_QUIT,
	PLAYER_SPAWN,
	PLAYER_TOGGLE_FLIGHT,
	PLAYER_TOGGLE_SNEAK,
	PLAYER_TOGGLE_SPRINT,
	PLAYER_TELEPORT,
	TAB_COMPLETE,
	WORLD_CHANGED,
	FOOD_LEVEL_CHANGED,
	RESOURCE_PACK_STATUS,
	/**
	 * Plugin events
	 */
	PLUGIN_MESSAGE_RECEIVED,
	/**
	 * Server events
	 */
	SERVER_COMMAND,
	SERVER_PING,
	BROADCAST_MESSAGE,
	/**
	 * Vehicle events
	 */
	VEHICLE_COLLIDE,
	VEHICLE_ENTER,
	VEHICLE_LEAVE,
	VEHICLE_MOVE,
	VEHICLE_DESTROY,
	/**
	 * Weather events
	 */
	LIGHTNING_STRIKE,
	THUNDER_CHANGE,
	WEATHER_CHANGE,
	/**
	 * World events
	 */
	TREE_GROW,
	WORLD_LOAD,
	WORLD_UNLOAD,
	WORLD_SAVE,
	/**
	 * Cmdline events
	 */
	CMDLINE_PROMPT_INPUT,
	SHUTDOWN,
	/**
	 * Extension events, used by events fired from the extension system.
	 */
	EXTENSION;

	MCVersion version;

	Driver() {
		version = MCVersion.MC1_0;
	}

	Driver(MCVersion added) {
		version = added;
	}

	public boolean existsInCurrent() {
		return Static.getServer().getMinecraftVersion().ordinal() >= version.ordinal();
	}
}
