/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCPlayerBedLeaveEvent extends MCPlayerEvent {
	public MCBlock getBed();
}
