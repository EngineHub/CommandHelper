package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.HashMap;

public class BukkitMCSound extends MCSound<Sound> {

	public BukkitMCSound(MCVanillaSound vanillaSound, Sound sound) {
		super(vanillaSound, sound);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaSound.UNKNOWN ? concreteName() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		Sound concrete = getConcrete();
		if(concrete == null) {
			return "null";
		}
		return concrete.name();
	}

	public static BukkitMCSound valueOfConcrete(Sound test) {
		for(MCSound t : mappings.values()) {
			if(((BukkitMCSound) t).getConcrete().equals(test)) {
				return (BukkitMCSound) t;
			}
		}
		return (BukkitMCSound) NULL;
	}

	public static BukkitMCSound valueOfConcrete(String test) {
		try {
			return valueOfConcrete(Sound.valueOf(test));
		} catch (IllegalArgumentException iae) {
			return (BukkitMCSound) NULL;
		}
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		mappings = new HashMap<>();
		NULL = new BukkitMCSound(MCVanillaSound.UNKNOWN, null);
		ArrayList<Sound> counted = new ArrayList<>();
		for(MCVanillaSound v : MCVanillaSound.values()) {
			if(v.existsInCurrent()) {
				Sound sound = getBukkitType(v);
				if(sound == null) {
					CHLog.GetLogger().e(CHLog.Tags.RUNTIME, "Could not find a matching sound for " + v.name()
							+ ". This is an error, please report this to the bug tracker.", Target.UNKNOWN);
					continue;
				}
				BukkitMCSound wrapper = new BukkitMCSound(v, sound);
				mappings.put(v.name(), wrapper);
				counted.add(sound);
			}
		}
		for(Sound s : Sound.values()) {
			if(!counted.contains(s)) {
				mappings.put(s.name(), new BukkitMCSound(MCVanillaSound.UNKNOWN, s));
			}
		}
	}

	private static Sound getBukkitType(MCVanillaSound v) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
			switch(v){
				case AMBIENCE_CAVE:
					return Sound.AMBIENT_CAVE;
				case AMBIENCE_RAIN:
					return Sound.WEATHER_RAIN;
				case AMBIENCE_THUNDER:
					return Sound.ENTITY_LIGHTNING_THUNDER;
				case ANVIL_BREAK:
					return Sound.BLOCK_ANVIL_DESTROY;
				case ANVIL_LAND:
					return Sound.BLOCK_ANVIL_LAND;
				case ANVIL_USE:
					return Sound.BLOCK_ANVIL_USE;
				case ARROW_HIT:
					return Sound.ENTITY_ARROW_HIT;
				case BURP:
					return Sound.ENTITY_PLAYER_BURP;
				case CHEST_CLOSE:
					return Sound.BLOCK_CHEST_CLOSE;
				case CHEST_OPEN:
					return Sound.BLOCK_CHEST_OPEN;
				case CLICK:
					return Sound.UI_BUTTON_CLICK;
				case DOOR_CLOSE:
					return Sound.BLOCK_WOODEN_DOOR_CLOSE;
				case DOOR_OPEN:
					return Sound.BLOCK_WOODEN_DOOR_OPEN;
				case DRINK:
					return Sound.ENTITY_GENERIC_DRINK;
				case EAT:
					return Sound.ENTITY_GENERIC_EAT;
				case EXPLODE:
					return Sound.ENTITY_GENERIC_EXPLODE;
				case FALL_BIG:
					return Sound.ENTITY_GENERIC_BIG_FALL;
				case FALL_SMALL:
					return Sound.ENTITY_GENERIC_SMALL_FALL;
				case FIRE:
					return Sound.BLOCK_FIRE_AMBIENT;
				case FIRE_IGNITE:
					return Sound.ITEM_FLINTANDSTEEL_USE;
				case FIZZ:
					return Sound.ENTITY_GENERIC_EXTINGUISH_FIRE;
				case FUSE:
					return Sound.ENTITY_TNT_PRIMED;
				case GLASS:
					return Sound.BLOCK_GLASS_BREAK;
				case HURT_FLESH:
					return Sound.ENTITY_GENERIC_HURT;
				case ITEM_BREAK:
					return Sound.ENTITY_ITEM_BREAK;
				case ITEM_PICKUP:
					return Sound.ENTITY_ITEM_PICKUP;
				case LAVA:
					return Sound.BLOCK_LAVA_AMBIENT;
				case LAVA_POP:
					return Sound.BLOCK_LAVA_POP;
				case LEVEL_UP:
					return Sound.ENTITY_PLAYER_LEVELUP;
				case MINECART_BASE:
					return Sound.ENTITY_MINECART_RIDING;
				case MINECART_INSIDE:
					return Sound.ENTITY_MINECART_INSIDE;
				case NOTE_BASS:
					return Sound.BLOCK_NOTE_BASS;
				case NOTE_PIANO:
					return Sound.BLOCK_NOTE_HARP;
				case NOTE_BASS_DRUM:
					return Sound.BLOCK_NOTE_BASEDRUM;
				case NOTE_STICKS:
					return Sound.BLOCK_NOTE_HAT;
				case NOTE_BASS_GUITAR:
					return Sound.BLOCK_NOTE_BASS;
				case NOTE_SNARE_DRUM:
					return Sound.BLOCK_NOTE_SNARE;
				case NOTE_PLING:
					return Sound.BLOCK_NOTE_PLING;
				case ORB_PICKUP:
					return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
				case PISTON_EXTEND:
					return Sound.BLOCK_PISTON_EXTEND;
				case PISTON_RETRACT:
					return Sound.BLOCK_PISTON_CONTRACT;
				case PORTAL:
					return Sound.BLOCK_PORTAL_AMBIENT;
				case PORTAL_TRAVEL:
					return Sound.BLOCK_PORTAL_TRAVEL;
				case PORTAL_TRIGGER:
					return Sound.BLOCK_PORTAL_TRIGGER;
				case SHOOT_ARROW:
					return Sound.ENTITY_ARROW_SHOOT;
				case SPLASH:
					return Sound.ENTITY_BOBBER_SPLASH;
				case SPLASH2:
					return Sound.ENTITY_GENERIC_SPLASH;
				case STEP_GRASS:
					return Sound.BLOCK_GRASS_STEP;
				case STEP_GRAVEL:
					return Sound.BLOCK_GRAVEL_STEP;
				case STEP_LADDER:
					return Sound.BLOCK_LADDER_STEP;
				case STEP_SAND:
					return Sound.BLOCK_SAND_STEP;
				case STEP_SNOW:
					return Sound.BLOCK_SNOW_STEP;
				case STEP_STONE:
					return Sound.BLOCK_STONE_STEP;
				case STEP_WOOD:
					return Sound.BLOCK_WOOD_STEP;
				case STEP_WOOL:
					return Sound.BLOCK_CLOTH_STEP;
				case SUCCESSFUL_HIT:
					return Sound.ENTITY_ARROW_HIT_PLAYER;
				case SWIM:
					return Sound.ENTITY_GENERIC_SWIM;
				case WATER:
					return Sound.BLOCK_WATER_AMBIENT;
				case WOOD_CLICK:
					return Sound.BLOCK_WOOD_BUTTON_CLICK_ON;

				// Mob sounds
				case BAT_DEATH:
					return Sound.ENTITY_BAT_DEATH;
				case BAT_HURT:
					return Sound.ENTITY_BAT_HURT;
				case BAT_IDLE:
					return Sound.ENTITY_BAT_AMBIENT;
				case BAT_LOOP:
					return Sound.ENTITY_BAT_LOOP;
				case BAT_TAKEOFF:
					return Sound.ENTITY_BAT_TAKEOFF;
				case BLAZE_BREATH:
					return Sound.ENTITY_BLAZE_AMBIENT;
				case BLAZE_DEATH:
					return Sound.ENTITY_BLAZE_DEATH;
				case BLAZE_HIT:
					return Sound.ENTITY_BLAZE_HURT;
				case CAT_HISS:
					return Sound.ENTITY_CAT_HISS;
				case CAT_HIT:
					return Sound.ENTITY_CAT_HURT;
				case CAT_MEOW:
					return Sound.ENTITY_CAT_AMBIENT;
				case CAT_PURR:
					return Sound.ENTITY_CAT_PURR;
				case CAT_PURREOW:
					return Sound.ENTITY_CAT_PURREOW;
				case CHICKEN_IDLE:
					return Sound.ENTITY_CHICKEN_AMBIENT;
				case CHICKEN_HURT:
					return Sound.ENTITY_CHICKEN_HURT;
				case CHICKEN_EGG_POP:
					return Sound.ENTITY_CHICKEN_EGG;
				case CHICKEN_WALK:
					return Sound.ENTITY_CHICKEN_STEP;
				case COW_IDLE:
					return Sound.ENTITY_COW_AMBIENT;
				case COW_HURT:
					return Sound.ENTITY_COW_HURT;
				case COW_WALK:
					return Sound.ENTITY_COW_STEP;
				case CREEPER_HISS:
					return Sound.ENTITY_CREEPER_HURT;
				case CREEPER_DEATH:
					return Sound.ENTITY_CREEPER_DEATH;
				case ENDERDRAGON_DEATH:
					return Sound.ENTITY_ENDERDRAGON_DEATH;
				case ENDERDRAGON_GROWL:
					return Sound.ENTITY_ENDERDRAGON_GROWL;
				case ENDERDRAGON_HIT:
					return Sound.ENTITY_ENDERDRAGON_HURT;
				case ENDERDRAGON_WINGS:
					return Sound.ENTITY_ENDERDRAGON_FLAP;
				case ENDERMAN_DEATH:
					return Sound.ENTITY_ENDERMEN_DEATH;
				case ENDERMAN_HIT:
					return Sound.ENTITY_ENDERMEN_HURT;
				case ENDERMAN_IDLE:
					return Sound.ENTITY_ENDERMEN_AMBIENT;
				case ENDERMAN_TELEPORT:
					return Sound.ENTITY_ENDERMEN_TELEPORT;
				case ENDERMAN_SCREAM:
					return Sound.ENTITY_ENDERMEN_SCREAM;
				case ENDERMAN_STARE:
					return Sound.ENTITY_ENDERMEN_STARE;
				case GHAST_SCREAM:
					return Sound.ENTITY_GHAST_HURT;
				case GHAST_SCREAM2:
					return Sound.ENTITY_GHAST_SCREAM;
				case GHAST_CHARGE:
					return Sound.ENTITY_GHAST_WARN;
				case GHAST_DEATH:
					return Sound.ENTITY_GHAST_DEATH;
				case GHAST_FIREBALL:
					return Sound.ENTITY_GHAST_SHOOT;
				case GHAST_MOAN:
					return Sound.ENTITY_GHAST_AMBIENT;
				case HORSE_DEATH:
					return Sound.ENTITY_HORSE_DEATH;
				case HORSE_SKELETON_HIT:
					return Sound.ENTITY_SKELETON_HORSE_HURT;
				case IRONGOLEM_DEATH:
					return Sound.ENTITY_IRONGOLEM_DEATH;
				case IRONGOLEM_HIT:
					return Sound.ENTITY_IRONGOLEM_HURT;
				case IRONGOLEM_THROW:
					return Sound.ENTITY_IRONGOLEM_ATTACK;
				case IRONGOLEM_WALK:
					return Sound.ENTITY_IRONGOLEM_STEP;
				case MAGMACUBE_WALK:
					return Sound.ENTITY_SMALL_MAGMACUBE_SQUISH;
				case MAGMACUBE_WALK2:
					return Sound.ENTITY_MAGMACUBE_SQUISH;
				case MAGMACUBE_JUMP:
					return Sound.ENTITY_MAGMACUBE_JUMP;
				case PIG_IDLE:
					return Sound.ENTITY_PIG_AMBIENT;
				case PIG_DEATH:
					return Sound.ENTITY_PIG_DEATH;
				case PIG_WALK:
					return Sound.ENTITY_PIG_STEP;
				case SHEEP_IDLE:
					return Sound.ENTITY_SHEEP_AMBIENT;
				case SHEEP_SHEAR:
					return Sound.ENTITY_SHEEP_SHEAR;
				case SHEEP_WALK:
					return Sound.ENTITY_SHEEP_STEP;
				case SILVERFISH_HIT:
					return Sound.ENTITY_SILVERFISH_HURT;
				case SILVERFISH_KILL:
					return Sound.ENTITY_SILVERFISH_DEATH;
				case SILVERFISH_IDLE:
					return Sound.ENTITY_SILVERFISH_AMBIENT;
				case SILVERFISH_WALK:
					return Sound.ENTITY_SILVERFISH_STEP;
				case SKELETON_IDLE:
					return Sound.ENTITY_SKELETON_AMBIENT;
				case SKELETON_DEATH:
					return Sound.ENTITY_SKELETON_DEATH;
				case SKELETON_HURT:
					return Sound.ENTITY_SKELETON_HURT;
				case SKELETON_WALK:
					return Sound.ENTITY_SKELETON_STEP;
				case SLIME_ATTACK:
					return Sound.ENTITY_SLIME_ATTACK;
				case SLIME_WALK:
					return Sound.ENTITY_SMALL_SLIME_HURT;
				case SLIME_WALK2:
					return Sound.ENTITY_SLIME_SQUISH;
				case SPIDER_IDLE:
					return Sound.ENTITY_SPIDER_AMBIENT;
				case SPIDER_DEATH:
					return Sound.ENTITY_SPIDER_DEATH;
				case SPIDER_WALK:
					return Sound.ENTITY_SPIDER_STEP;
				case WITHER_DEATH:
					return Sound.ENTITY_WITHER_DEATH;
				case WITHER_HURT:
					return Sound.ENTITY_WITHER_HURT;
				case WITHER_IDLE:
					return Sound.ENTITY_WITHER_AMBIENT;
				case WITHER_SHOOT:
					return Sound.ENTITY_WITHER_SHOOT;
				case WITHER_SPAWN:
					return Sound.ENTITY_WITHER_SPAWN;
				case WOLF_BARK:
					return Sound.ENTITY_WOLF_AMBIENT;
				case WOLF_DEATH:
					return Sound.ENTITY_WOLF_DEATH;
				case WOLF_GROWL:
					return Sound.ENTITY_WOLF_GROWL;
				case WOLF_HOWL:
					return Sound.ENTITY_WOLF_HOWL;
				case WOLF_HURT:
					return Sound.ENTITY_WOLF_HURT;
				case WOLF_PANT:
					return Sound.ENTITY_WOLF_PANT;
				case WOLF_SHAKE:
					return Sound.ENTITY_WOLF_SHAKE;
				case WOLF_WALK:
					return Sound.ENTITY_WOLF_STEP;
				case WOLF_WHINE:
					return Sound.ENTITY_WOLF_WHINE;
				case ZOMBIE_METAL:
					return Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR;
				case ZOMBIE_WALK:
					return Sound.ENTITY_ZOMBIE_STEP;
				case ZOMBIE_WOOD:
					return Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD;
				case ZOMBIE_WOODBREAK:
					return Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD;
				case ZOMBIE_IDLE:
					return Sound.ENTITY_ZOMBIE_AMBIENT;
				case ZOMBIE_DEATH:
					return Sound.ENTITY_ZOMBIE_DEATH;
				case ZOMBIE_HURT:
					return Sound.ENTITY_ZOMBIE_HURT;
				case ZOMBIE_INFECT:
					return Sound.ENTITY_ZOMBIE_INFECT;
				case ZOMBIE_UNFECT:
					return Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED;
				case ZOMBIE_REMEDY:
					return Sound.ENTITY_ZOMBIE_VILLAGER_CURE;
				case ZOMBIE_PIG_IDLE:
					return Sound.ENTITY_ZOMBIE_PIG_AMBIENT;
				case ZOMBIE_PIG_ANGRY:
					return Sound.ENTITY_ZOMBIE_PIG_ANGRY;
				case ZOMBIE_PIG_DEATH:
					return Sound.ENTITY_ZOMBIE_PIG_DEATH;
				case ZOMBIE_PIG_HURT:
					return Sound.ENTITY_ZOMBIE_PIG_HURT;

				// Dig Sounds
				case DIG_WOOL:
					return Sound.BLOCK_CLOTH_BREAK;
				case DIG_GRASS:
					return Sound.BLOCK_GRASS_BREAK;
				case DIG_GRAVEL:
					return Sound.BLOCK_GRAVEL_BREAK;
				case DIG_SAND:
					return Sound.BLOCK_SAND_BREAK;
				case DIG_SNOW:
					return Sound.BLOCK_SNOW_BREAK;
				case DIG_STONE:
					return Sound.BLOCK_STONE_BREAK;
				case DIG_WOOD:
					return Sound.BLOCK_WOOD_BREAK;

				// Fireworks
				case FIREWORK_BLAST:
					return Sound.ENTITY_FIREWORK_BLAST;
				case FIREWORK_BLAST2:
					return Sound.ENTITY_FIREWORK_BLAST_FAR;
				case FIREWORK_LARGE_BLAST:
					return Sound.ENTITY_FIREWORK_LARGE_BLAST;
				case FIREWORK_LARGE_BLAST2:
					return Sound.ENTITY_FIREWORK_LARGE_BLAST_FAR;
				case FIREWORK_TWINKLE:
					return Sound.ENTITY_FIREWORK_TWINKLE;
				case FIREWORK_TWINKLE2:
					return Sound.ENTITY_FIREWORK_TWINKLE_FAR;
				case FIREWORK_LAUNCH:
					return Sound.ENTITY_FIREWORK_LAUNCH;

				// Horses
				case HORSE_ANGRY:
					return Sound.ENTITY_HORSE_ANGRY;
				case HORSE_ARMOR:
					return Sound.ENTITY_HORSE_ARMOR;
				case HORSE_BREATHE:
					return Sound.ENTITY_HORSE_BREATHE;
				case HORSE_GALLOP:
					return Sound.ENTITY_HORSE_GALLOP;
				case HORSE_HIT:
					return Sound.ENTITY_HORSE_HURT;
				case HORSE_IDLE:
					return Sound.ENTITY_HORSE_AMBIENT;
				case HORSE_JUMP:
					return Sound.ENTITY_HORSE_JUMP;
				case HORSE_LAND:
					return Sound.ENTITY_HORSE_LAND;
				case HORSE_SADDLE:
					return Sound.ENTITY_HORSE_SADDLE;
				case HORSE_SOFT:
					return Sound.ENTITY_HORSE_STEP;
				case HORSE_WOOD:
					return Sound.ENTITY_HORSE_STEP_WOOD;
				case DONKEY_ANGRY:
					return Sound.ENTITY_DONKEY_ANGRY;
				case DONKEY_DEATH:
					return Sound.ENTITY_DONKEY_DEATH;
				case DONKEY_HIT:
					return Sound.ENTITY_DONKEY_HURT;
				case DONKEY_IDLE:
					return Sound.ENTITY_DONKEY_AMBIENT;
				case HORSE_SKELETON_DEATH:
					return Sound.ENTITY_SKELETON_HORSE_DEATH;
				case HORSE_SKELETON_IDLE:
					return Sound.ENTITY_SKELETON_HORSE_AMBIENT;
				case HORSE_ZOMBIE_DEATH:
					return Sound.ENTITY_ZOMBIE_HORSE_DEATH;
				case HORSE_ZOMBIE_HIT:
					return Sound.ENTITY_ZOMBIE_HORSE_HURT;
				case HORSE_ZOMBIE_IDLE:
					return Sound.ENTITY_ZOMBIE_HORSE_AMBIENT;

				// Villager
				case VILLAGER_DEATH:
					return Sound.ENTITY_VILLAGER_DEATH;
				case VILLAGER_HAGGLE:
					return Sound.ENTITY_VILLAGER_TRADING;
				case VILLAGER_HIT:
					return Sound.ENTITY_VILLAGER_HURT;
				case VILLAGER_IDLE:
					return Sound.ENTITY_VILLAGER_AMBIENT;
				case VILLAGER_NO:
					return Sound.ENTITY_VILLAGER_NO;
				case VILLAGER_YES:
					return Sound.ENTITY_VILLAGER_YES;
			}
		}
		try {
			return Sound.valueOf(v.name());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
}
