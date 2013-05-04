package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.WrappedItem;
import com.laytonsmith.annotations.testing.AbstractConstructor;
import org.bukkit.Note;

/**
 *
 * @author Layton
 */
public class BukkitMCNote implements MCNote {

	@WrappedItem Note n;

	public BukkitMCNote(int octave, MCTone tone, boolean sharp) throws IllegalArgumentException {
		n = new Note(octave, Note.Tone.valueOf(tone.name()), sharp);
	}

	@AbstractConstructor
	public BukkitMCNote(AbstractionObject a) {
		this.n = a.getHandle();
	}

	public Note getHandle() {
		return n;
	}

	@Override
	public String toString() {
		return n.toString();
	}
	
}
