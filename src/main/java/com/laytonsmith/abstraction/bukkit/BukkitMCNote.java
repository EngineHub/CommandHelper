
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.enums.MCTone;
import org.bukkit.Note;

/**
 *
 * 
 */
public class BukkitMCNote implements AbstractionObject, MCNote {
	Note n;
	public BukkitMCNote(int octave, MCTone tone, boolean sharp) throws IllegalArgumentException {
		n = new Note(octave, Note.Tone.valueOf(tone.name()), sharp);
	}

	@Override
	public Object getHandle() {
		return n;
	}

	@Override
	public String toString() {
		return n.toString();
	}

}
