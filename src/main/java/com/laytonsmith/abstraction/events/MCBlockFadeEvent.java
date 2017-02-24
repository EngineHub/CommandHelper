package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlockState;

/**
 * Created by Junhyeong Lim on 2017-02-25.
 */
public interface MCBlockFadeEvent extends MCBlockEvent {
    MCBlockState getNewState();
}
