package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 */
public abstract class ItemStackPrefilterMatcher<T extends BindableEvent> extends MaterialPrefilterMatcher<T> {

	@Override
	protected MCMaterial getMaterial(T event) {
		MCItemStack itemStack = getItemStack(event);
		if(itemStack == null) {
			return null;
		}
		return itemStack.getType();
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return super.getDocsObject();
	}



	protected abstract MCItemStack getItemStack(T event);
}
