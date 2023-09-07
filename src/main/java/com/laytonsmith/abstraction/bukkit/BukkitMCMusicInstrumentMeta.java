package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMusicInstrumentMeta;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

public class BukkitMCMusicInstrumentMeta extends BukkitMCItemMeta implements MCMusicInstrumentMeta {

	MusicInstrumentMeta mim;

	public BukkitMCMusicInstrumentMeta(MusicInstrumentMeta meta) {
		super(meta);
		this.mim = meta;
	}

	@Override
	public String getInstrument() {
		MusicInstrument instrument = this.mim.getInstrument();
		if(instrument == null) {
			return null;
		}
		return instrument.getKey().toString();
	}

	@Override
	public void setInstrument(String instrument) {
		if(instrument == null) {
			this.mim.setInstrument(null);
		} else {
			NamespacedKey key = NamespacedKey.fromString(instrument);
			if(key == null) {
				this.mim.setInstrument(null);
			} else {
				MusicInstrument musicInstrument = MusicInstrument.getByKey(key);
				this.mim.setInstrument(musicInstrument);
			}
		}
	}
}
