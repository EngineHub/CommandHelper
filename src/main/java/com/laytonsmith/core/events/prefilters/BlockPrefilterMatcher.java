package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 */
public abstract class BlockPrefilterMatcher<T extends BindableEvent> extends MaterialPrefilterMatcher<T> {

	@Override
	protected MCMaterial getMaterial(T event) {
		MCBlock block = getBlock(event);
		if(block.isEmpty()) {
			return null;
		}
		return block.getType();
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return super.getDocsObject();
	}

	protected abstract MCBlock getBlock(T event);
}
