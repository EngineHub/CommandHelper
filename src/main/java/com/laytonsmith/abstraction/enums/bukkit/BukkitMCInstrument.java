
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Instrument;

/**
 *
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCInstrument.class,
forConcreteEnum = Instrument.class)
public class BukkitMCInstrument extends EnumConvertor<MCInstrument, Instrument> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument();
		}
		return instance;
	}
}
