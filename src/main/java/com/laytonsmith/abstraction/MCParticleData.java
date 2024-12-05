package com.laytonsmith.abstraction;

public class MCParticleData {

	public static class DustTransition {
		MCColor from;
		MCColor to;

		public DustTransition(MCColor from, MCColor to) {
			this.from = from;
			this.to = to;
		}

		public MCColor from() {
			return this.from;
		}

		public MCColor to() {
			return this.to;
		}
	}

	public static class Trail {
		MCLocation location;
		MCColor color;
		int duration;

		public Trail(MCLocation location, MCColor color, int duration) {
			this.location = location;
			this.color = color;
			this.duration = duration;
		}

		public MCLocation location() {
			return this.location;
		}

		public MCColor color() {
			return this.color;
		}

		public int duration() {
			return this.duration;
		}
	}

	public static class VibrationBlockDestination {
		MCLocation location;
		int arrivalTime;

		public VibrationBlockDestination(MCLocation location, int arrivalTime) {
			this.location = location;
			this.arrivalTime = arrivalTime;
		}

		public MCLocation location() {
			return this.location;
		}

		public int arrivalTime() {
			return this.arrivalTime;
		}
	}

	public static class VibrationEntityDestination {
		MCEntity entity;
		int arrivalTime;

		public VibrationEntityDestination(MCEntity entity, int arrivalTime) {
			this.entity = entity;
			this.arrivalTime = arrivalTime;
		}

		public MCEntity entity() {
			return this.entity;
		}

		public int arrivalTime() {
			return this.arrivalTime;
		}
	}
}
