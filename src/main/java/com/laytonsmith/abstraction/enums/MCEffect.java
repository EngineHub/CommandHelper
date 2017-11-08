
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 *
 * 
 */
@MEnum("Effect")
public enum MCEffect {
	/**
	 * VISUAL
	 */
	DRAGON_BREATH(2006),
	END_GATEWAY_SPAWN(3000),
	ENDER_SIGNAL(2003),
	MOBSPAWNER_FLAMES(2004),
	POTION_BREAK(2002),
	SMOKE(2000),
	VILLAGER_PLANT_GROW(2005),

	/**
	 * SOUND
	 */
	ANVIL_BREAK(1029),
	ANVIL_LAND(1031),
	ANVIL_USE(1030),
	BAT_TAKEOFF(1025),
	BLAZE_SHOOT(1018),
	BOW_FIRE(1002),
	BREWING_STAND_BREW(1035),
	CHORUS_FLOWER_DEATH(1034),
	CHORUS_FLOWER_GROW(1033),
	CLICK1(1001),
	CLICK2(1000),
	DOOR_CLOSE(1012),
	DOOR_TOGGLE(1006),
	ENDERDRAGON_GROWL(3001),
	ENDERDRAGON_SHOOT(1017),
	ENDEREYE_LAUNCH(1003),
	EXTINGUISH(1009),
	FENCE_GATE_CLOSE(1014),
	FENCE_GATE_TOGGLE(1008),
	FIREWORK_SHOOT(1004),
	GHAST_SHOOT(1016),
	GHAST_SHRIEK(1015),
	IRON_DOOR_CLOSE(1011),
	IRON_DOOR_TOGGLE(1005),
	IRON_TRAPDOOR_CLOSE(1036),
	IRON_TRAPDOOR_TOGGLE(1037),
	PORTAL_TRAVEL(1032),
	RECORD_PLAY(1005),
	STEP_SOUND(2001),
	TRAPDOOR_CLOSE(1013),
	TRAPDOOR_TOGGLE(1007),
	ZOMBIE_CHEW_WOODEN_DOOR(1019),
	ZOMBIE_CHEW_IRON_DOOR(1020),
	ZOMBIE_DESTROY_DOOR(1012),
	WITHER_BREAK_BLOCK(1022),
	WITHER_SHOOT(1024),
	ZOMBIE_CONVERTED_VILLAGER(1027),
	ZOMBIE_INFECT(1026),

	/**
	 * PARTICLE
	 * Deprecated in favor of the Particle API
	 */
	FIREWORKS_SPARK("fireworksSpark"),
	CRIT("crit"),
	MAGIC_CRIT("magicCrit"),
	POTION_SWIRL("mobSpell"),
	POTION_SWIRL_TRANSPARENT("mobSpellAmbient"),
	SPELL("spell"),
	INSTANT_SPELL("instantSpell"),
	WITCH_MAGIC("witchMagic"),
	NOTE("note"),
	PORTAL("portal"),
	FLYING_GLYPH("enchantmenttable"),
	FLAME("flame"),
	LAVA_POP("lava"),
	FOOTSTEP("footstep"),
	SPLASH("splash"),
	PARTICLE_SMOKE("smoke"),
	EXPLOSION_HUGE("hugeexplosion"),
	EXPLOSION_LARGE("largeexplode"),
	EXPLOSION("explode"),
	VOID_FOG("depthsuspend"),
	SMALL_SMOKE("townaura"),
	CLOUD("cloud"),
	COLOURED_DUST("reddust"),
	SNOWBALL_BREAK("snowballpoof"),
	WATERDRIP("dripWater"),
	LAVADRIP("dripLava"),
	SNOW_SHOVEL("snowshovel"),
	SLIME("slime"),
	HEART("heart"),
	VILLAGER_THUNDERCLOUD("angryVillager"),
	HAPPY_VILLAGER("happyVillager"),
	LARGE_SMOKE("largesmoke"),
	//ITEM_BREAK("iconcrack"), // crashes clients without correct data
	TILE_BREAK("blockcrack"),
	TILE_DUST("blockdust")
	;

    private final int id;
	private final String particleName;

    private MCEffect(int id) {
        this.id = id;
		particleName = null;
    }

	private MCEffect(String particleName){
		this.id = 0;
		this.particleName = particleName;
	}

    public int getId() {
        return this.id;
    }

	public String getName(){
		return particleName;
	}
}
