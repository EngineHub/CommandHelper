package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MVector3D;

public interface MCPlayerInteractAtEntityEvent extends MCPlayerInteractEntityEvent {
	public MVector3D getClickedPosition();
}