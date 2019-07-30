package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCMerchant;

public interface MCTrader extends MCAgeable, MCInventoryHolder {

	MCMerchant asMerchant();
}
