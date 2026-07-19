package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.Effect;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCEffect.class,
		forConcreteEnum = Effect.class
)
public class BukkitMCEffect extends EnumConvertor<MCEffect, Effect> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEffect instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEffect getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCEffect();
		}
		return instance;
	}

	@Override
	protected Effect getConcreteEnumCustom(MCEffect abstracted) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC26_2)
				&& ((BukkitMCServer) Static.getServer()).isPaper()) {
			switch(abstracted) {
				case BOW_FIRE:
					return Effect.DISPENSER_PROJECTILE_LAUNCH;
				case CLICK1:
					return Effect.DISPENSER_FAIL;
				case CLICK2:
					return Effect.DISPENSER_DISPENSE;
				case DRAGON_BREATH:
					return Effect.ENDER_DRAGON_BREATH;
				case ENDERDRAGON_GROWL:
					return Effect.ENDER_DRAGON_GROWL;
				case ENDERDRAGON_SHOOT:
					return Effect.ENDER_DRAGON_SHOOT;
				case PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE:
					return Effect.BRUSH_BLOCK_COMPLETE;
				case PARTICLES_EGG_CRACK:
					return Effect.EGG_CRACK;
				case PARTICLES_SCULK_CHARGE:
					return Effect.SCULK_CHARGE;
				case PARTICLES_SCULK_SHRIEK:
					return Effect.SCULK_SHRIEK;
				case SHOOT_WHITE_SMOKE:
					return Effect.WHITE_SMOKE_SHOOT;
				case SMOKE:
					return Effect.SMOKE_SHOOT;
				case SOUND_STOP_JUKEBOX_SONG:
					return Effect.RECORD_STOP;
				case SOUND_WITH_CHARGE_SHOT:
					return Effect.WIND_CHARGE_SHOOT;
				case VILLAGER_PLANT_GROW:
					return Effect.BEE_GROWTH;
				case ZOMBIE_CONVERTED_VILLAGER:
					return Effect.ZOMBIE_CONVERTED_TO_VILLAGER;
			}
		}
		return super.getConcreteEnumCustom(abstracted);
	}

	@Override
	protected MCEffect getAbstractedEnumCustom(Effect concrete) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC26_2)
				&& ((BukkitMCServer) Static.getServer()).isPaper()) {
			switch(concrete) {
				case DISPENSER_PROJECTILE_LAUNCH:
					return MCEffect.BOW_FIRE;
				case DISPENSER_FAIL:
					return MCEffect.CLICK1;
				case DISPENSER_DISPENSE:
					return MCEffect.CLICK2;
				case ENDER_DRAGON_BREATH:
					return MCEffect.DRAGON_BREATH;
				case ENDER_DRAGON_GROWL:
					return MCEffect.ENDERDRAGON_GROWL;
				case ENDER_DRAGON_SHOOT:
					return MCEffect.ENDERDRAGON_SHOOT;
				case BRUSH_BLOCK_COMPLETE:
					return MCEffect.PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE;
				case EGG_CRACK:
					return MCEffect.PARTICLES_EGG_CRACK;
				case SCULK_CHARGE:
					return MCEffect.PARTICLES_SCULK_CHARGE;
				case SCULK_SHRIEK:
					return MCEffect.PARTICLES_SCULK_SHRIEK;
				case WHITE_SMOKE_SHOOT:
					return MCEffect.SHOOT_WHITE_SMOKE;
				case SMOKE_SHOOT:
					return MCEffect.SMOKE;
				case RECORD_STOP:
					return MCEffect.SOUND_STOP_JUKEBOX_SONG;
				case WIND_CHARGE_SHOOT:
					return MCEffect.SOUND_WITH_CHARGE_SHOT;
				case ZOMBIE_CONVERTED_TO_VILLAGER:
					return MCEffect.ZOMBIE_CONVERTED_VILLAGER;
			}
		}
		return super.getAbstractedEnumCustom(concrete);
	}

}
