package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCNamespacedKey;
import com.laytonsmith.abstraction.MCTagContainer;
import com.laytonsmith.abstraction.enums.MCTagType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class BukkitMCTagContainer implements MCTagContainer {

	PersistentDataContainer pdc;

	public BukkitMCTagContainer(PersistentDataContainer pdc) {
		this.pdc = pdc;
	}

	@Override
	public MCTagContainer newContainer() {
		return new BukkitMCTagContainer(this.pdc.getAdapterContext().newPersistentDataContainer());
	}

	@Override
	public boolean isEmpty() {
		return this.pdc.isEmpty();
	}

	@Override
	public Set<MCNamespacedKey> getKeys() {
		Set<MCNamespacedKey> keys = new HashSet<>();
		for(NamespacedKey key : this.pdc.getKeys()) {
			keys.add(new BukkitMCNamespacedKey(key));
		}
		return keys;
	}

	@Override
	public MCTagType getType(MCNamespacedKey key) {
		NamespacedKey namespacedKey = (NamespacedKey) key.getHandle();
		// Check tag types in order of most frequently used
		if(this.pdc.has(namespacedKey, PersistentDataType.STRING)) {
			return MCTagType.STRING;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.INTEGER)) {
			return MCTagType.INTEGER;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.BYTE)) {
			return MCTagType.BYTE;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.DOUBLE)) {
			return MCTagType.DOUBLE;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.LONG)) {
			return MCTagType.LONG;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.FLOAT)) {
			return MCTagType.FLOAT;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.TAG_CONTAINER)) {
			return MCTagType.TAG_CONTAINER;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.BYTE_ARRAY)) {
			return MCTagType.BYTE_ARRAY;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.SHORT)) {
			return MCTagType.SHORT;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.INTEGER_ARRAY)) {
			return MCTagType.INTEGER_ARRAY;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.LONG_ARRAY)) {
			return MCTagType.LONG_ARRAY;
		} else if(this.pdc.has(namespacedKey, PersistentDataType.TAG_CONTAINER_ARRAY)) {
			return MCTagType.TAG_CONTAINER_ARRAY;
		}
		return null;
	}

	@Override
	public Object get(MCNamespacedKey key, MCTagType type) {
		PersistentDataType bukkitType = GetPersistentDataType(type);
		Object value = this.pdc.get((NamespacedKey) key.getHandle(), bukkitType);
		if(value instanceof PersistentDataContainer) {
			return new BukkitMCTagContainer((PersistentDataContainer) value);
		} else if(value instanceof PersistentDataContainer[] concreteContainers) {
			MCTagContainer[] abstractContainers = new MCTagContainer[concreteContainers.length];
			for(int i = 0; i < concreteContainers.length; i++) {
				abstractContainers[i] = new BukkitMCTagContainer(concreteContainers[i]);
			}
			return abstractContainers;
		}
		return value;
	}

	@Override
	public void set(MCNamespacedKey key, MCTagType type, Object value) {
		PersistentDataType bukkitType = GetPersistentDataType(type);
		if(value instanceof MCTagContainer) {
			value = ((MCTagContainer) value).getHandle();
		} else if(value instanceof MCTagContainer[] abstractContainers) {
			PersistentDataContainer[] concreteContainers = new PersistentDataContainer[abstractContainers.length];
			for(int i = 0; i < abstractContainers.length; i++) {
				concreteContainers[i] = (PersistentDataContainer) abstractContainers[i].getHandle();
			}
			value = concreteContainers;
		}
		this.pdc.set((NamespacedKey) key.getHandle(), bukkitType, value);
	}

	@Override
	public void remove(MCNamespacedKey key) {
		this.pdc.remove((NamespacedKey) key.getHandle());
	}

	private static PersistentDataType GetPersistentDataType(MCTagType type) {
		switch(type) {
			case BYTE:
				return PersistentDataType.BYTE;
			case BYTE_ARRAY:
				return PersistentDataType.BYTE_ARRAY;
			case DOUBLE:
				return PersistentDataType.DOUBLE;
			case FLOAT:
				return PersistentDataType.FLOAT;
			case INTEGER:
				return PersistentDataType.INTEGER;
			case INTEGER_ARRAY:
				return PersistentDataType.INTEGER_ARRAY;
			case LONG:
				return PersistentDataType.LONG;
			case LONG_ARRAY:
				return PersistentDataType.LONG_ARRAY;
			case SHORT:
				return PersistentDataType.SHORT;
			case STRING:
				return PersistentDataType.STRING;
			case TAG_CONTAINER:
				return PersistentDataType.TAG_CONTAINER;
			case TAG_CONTAINER_ARRAY:
				return PersistentDataType.TAG_CONTAINER_ARRAY;
		}
		throw new IllegalArgumentException("Invalid persistent data type: " + type.name());
	}

	@Override
	public Object getHandle() {
		return this.pdc;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MCTagContainer && this.pdc.equals(((MCTagContainer) o).getHandle());
	}

	@Override
	public int hashCode() {
		return this.pdc.hashCode();
	}

	@Override
	public String toString() {
		return this.pdc.toString();
	}
}
