
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 *
 * 
 */
@MEnum("Effect")
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
	/**
     * Sound of zombies chewing on wooden doors.
     */
    ZOMBIE_CHEW_WOODEN_DOOR(1010),
    /**
     * Sound of zombies chewing on iron doors.
     */
    ZOMBIE_CHEW_IRON_DOOR(1011),
    /**
     * Sound of zombies destroying a door.
     */
    ZOMBIE_DESTROY_DOOR(1012),
    MOBSPAWNER_FLAMES(2004),
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
	ITEM_BREAK("iconcrack"),
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
