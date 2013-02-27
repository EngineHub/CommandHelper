package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;
import java.util.List;

/**
 *
 * @author lsmith
 */
@typename("ItemStack")
public class MItemStack extends MObject {
	public int type;
	public int data;
	public int qty;
	public List<MEnchantment> enchants;
	public MItemMeta meta;
}
