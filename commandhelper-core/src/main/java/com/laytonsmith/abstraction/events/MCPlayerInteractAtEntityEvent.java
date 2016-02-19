package com.laytonsmith.abstraction.events;

import com.laytonsmith.PureUtilities.Vector3D;

public interface MCPlayerInteractAtEntityEvent extends MCPlayerInteractEntityEvent {
	public Vector3D getClickedPosition();
}