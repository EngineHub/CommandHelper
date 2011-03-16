package com.laytonsmith.aliasengine;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

/**
 * An Item is a block or crafted item that holds information about the type and state of the Material
 * that it consists of.
 * @author <a href="mailto:sunkid@iminurnetz.com">sunkid</a>
 *
 */
public class Item {
	protected Material material;
	protected MaterialData data;

	public Item(Block block) {
		this.material = block.getState().getType();
		this.data = block.getState().getData();
	}

	public Item(Material material) {
		this(material, material.getNewData((byte) 0));
	}

	public Item(String item) throws InstantiationException {
		if (item == null)
			throw new InstantiationException("Item specification cannot be null");

		String[] words = item.split(":");

		if (words.length > 2)
			throw new InstantiationException("Incorrect Item specification");

		Material m = MaterialUtils.getMaterial(words[0]);
		if (m == null)
			throw new InstantiationException("Cannot parse material information from '" + words[0] + "'");

		this.material = m;

		if (words.length == 2) {
			this.data = MaterialUtils.getData(material, words[1]);
		} else {
			this.data = material.getNewData((byte) 0);
		}
	}

	public Item(Material material, MaterialData data) {
		this.material = material;
		this.data = data;
	}

	public MaterialData getData() {
		return data;
	}

	public Material getMaterial() {
		return material;
	}

	public void setData(MaterialData data) {
		this.data = data;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public boolean isBlock() {
		return getMaterial().isBlock();
	}
}
