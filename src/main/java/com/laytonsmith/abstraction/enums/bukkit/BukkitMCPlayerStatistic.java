package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPlayerStatistic;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Statistic;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCPlayerStatistic.class,
		forConcreteEnum = Statistic.class
)
public class BukkitMCPlayerStatistic extends EnumConvertor<MCPlayerStatistic, Statistic> {

	private static BukkitMCPlayerStatistic instance;

	public static BukkitMCPlayerStatistic getConvertor() {
		if(instance == null) {
			instance = new BukkitMCPlayerStatistic();
		}
		return instance;
	}
}
