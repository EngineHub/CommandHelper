package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCBookMeta;

public interface MCPlayerEditBookEvent extends MCPlayerEvent {

	MCBookMeta getNewBookMeta();

	MCBookMeta getPreviousBookMeta();

	void setNewBookMeta(MCBookMeta bookMeta);

	int getSlot();

	boolean isSigning();

	void setSigning(boolean isSigning);
}
