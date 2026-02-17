package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCNamespacedKey;

public interface MCPlayerAdvancementDoneEvent extends MCPlayerEvent {
	MCNamespacedKey getAdvancementKey();
	String getTitle();
}
