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
		private final UUID player;
		private final long timestamp;

		public MCPreviousInteraction(UUID player, long timestamp) {
			this.player = player;
			this.timestamp = timestamp;
		}

		public UUID getPlayer() {
			return player;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}

}
