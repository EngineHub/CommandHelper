package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;

import java.util.UUID;

public interface MCInteraction extends MCEntity {
	double getWidth();
	void setWidth(double width);
	double getHeight();
	void setHeight(double height);
	boolean isResponsive();
	void setResponsive(boolean response);
	MCPreviousInteraction getLastAttack();
	MCPreviousInteraction getLastInteraction();

	class MCPreviousInteraction {
		private final UUID uuid;
		private final long timestamp;

		public MCPreviousInteraction(UUID uuid, long timestamp) {
			this.uuid = uuid;
			this.timestamp = timestamp;
		}

		public UUID getUuid() {
			return uuid;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}

}
