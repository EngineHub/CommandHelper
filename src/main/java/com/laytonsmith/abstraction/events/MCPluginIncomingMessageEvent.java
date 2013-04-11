/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCPluginIncomingMessageEvent extends BindableEvent {
	public String getChannel();
	public byte[] getBytes();
	public MCPlayer getPlayer();
}
