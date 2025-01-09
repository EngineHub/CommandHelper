package com.laytonsmith.core;

import com.laytonsmith.annotations.MEnum;

/**
 * A utility class that contains various easing algorithms.
 */
public final class Easings {

	private Easings() {
	}

	@MEnum("com.methodscript.EasingType")
	public static enum EasingType {
		EASE_IN_SINE,
		EASE_OUT_SINE,
		EASE_IN_OUT_SINE,
		EASE_IN_CUBIC,
		EASE_OUT_CUBIC,
		EASE_IN_OUT_CUBIC,
		EASE_IN_QUINT,
		EASE_OUT_QUINT,
		EASE_IN_OUT_QUINT,
		EASE_IN_CIRC,
		EASE_OUT_CIRC,
		EASE_IN_OUT_CIRC,
		EASE_IN_ELASTIC,
		EASE_OUT_ELASTIC,
		EASE_IN_OUT_ELASTIC,
		EASE_IN_QUAD,
		EASE_OUT_QUAD,
		EASE_IN_OUT_QUAD,
		EASE_IN_QUART,
		EASE_OUT_QUART,
		EASE_IN_OUT_QUART,
		EASE_IN_EXPO,
		EASE_OUT_EXPO,
		EASE_IN_OUT_EXPO,
		EASE_IN_BACK,
		EASE_OUT_BACK,
		EASE_IN_OUT_BACK,
		EASE_IN_BOUNCE,
		EASE_OUT_BOUNCE,
		EASE_IN_OUT_BOUNCE,
		LINEAR,
	}

	private static final double C1 = 1.70158;
	private static final double C2 = C1 * 1.525;
	private static final double C3 = C1 + 1;
	private static final double C4 = (2 * Math.PI) / 3;
	private static final double C5 = (2 * Math.PI) / 4.5;
	private static final double D1 = 2.75;
	private static final double N1 = 7.5625;

	/**
	 * Returns the resultant percentage of travel given the overall time percentage, based on the given easing type. For
	 * instance, for {@code LINEAR} easings, the total distance is the same as the input, but for others, the value will
	 * differ based on the specific easing selected.
	 *
	 * @param type The easing type to use. Based on the easings listed at https://easings.net/
	 * @param x The overall time percentage, a value between 0 and 1. If the value is outside the range, it is clamped
	 * to 0 or 1.
	 * @return The resultant percentage of travel.
	 */
	public static double GetEasing(EasingType type, double x) {
		if(x < 0) {
			x = 0;
		} else if(x > 1) {
			x = 1;
		}

		switch(type) {
			case EASE_IN_SINE:
				return 1 - Math.cos((x * Math.PI) / 2);
			case EASE_OUT_SINE:
				return Math.sin((x * Math.PI) / 2);
			case EASE_IN_OUT_SINE:
				return -(Math.cos(Math.PI * x) - 1) / 2;
			case EASE_IN_CUBIC:
				return x * x * x;
			case EASE_OUT_CUBIC:
				return 1 - Math.pow(1 - x, 3);
			case EASE_IN_OUT_CUBIC:
				return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
			case EASE_IN_QUINT:
				return x * x * x * x * x;
			case EASE_OUT_QUINT:
				return 1 - Math.pow(1 - x, 5);
			case EASE_IN_OUT_QUINT:
				return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;
			case EASE_IN_CIRC:
				return 1 - Math.sqrt(1 - Math.pow(x, 2));
			case EASE_OUT_CIRC:
				return Math.sqrt(1 - Math.pow(x - 1, 2));
			case EASE_IN_OUT_CIRC:
				return x < 0.5
						? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
						: (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
			case EASE_IN_ELASTIC:
				return (x - 0.000001) < 0.0
						? 0
						: (x + 0.000001) > 1.0
								? 1
								: -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * C4);
			case EASE_OUT_ELASTIC:
				return (x - 0.000001) < 0.0
						? 0
						: (x + 0.000001) > 1.0
								? 1
								: Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * C4) + 1;
			case EASE_IN_OUT_ELASTIC:
				return (x - 0.000001) < 0.0
						? 0
						: (x + 0.000001) > 1.0
								? 1
								: x < 0.5
										? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * C5)) / 2
										: (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * C5)) / 2 + 1;
			case EASE_IN_QUAD:
				return x * x;
			case EASE_OUT_QUAD:
				return 1 - (1 - x) * (1 - x);
			case EASE_IN_OUT_QUAD:
				return x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
			case EASE_IN_QUART:
				return x * x * x * x;
			case EASE_OUT_QUART:
				return 1 - Math.pow(1 - x, 4);
			case EASE_IN_OUT_QUART:
				return x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2;
			case EASE_IN_EXPO:
				return (x - 0.000001) < 0.0 ? 0 : Math.pow(2, 10 * x - 10);
			case EASE_OUT_EXPO:
				return (x + 0.000001) > 1.0 ? 1 : 1 - Math.pow(2, -10 * x);
			case EASE_IN_OUT_EXPO:
				return (x - 0.000001) < 0.0
						? 0
						: (x + 0.000001) > 1.0
								? 1
								: x < 0.5 ? Math.pow(2, 20 * x - 10) / 2
										: (2 - Math.pow(2, -20 * x + 10)) / 2;
			case EASE_IN_BACK:
				return C3 * x * x * x - C1 * x * x;
			case EASE_OUT_BACK:
				return 1 + C3 * Math.pow(x - 1, 3) + C1 * Math.pow(x - 1, 2);
			case EASE_IN_OUT_BACK:
				return x < 0.5
						? (Math.pow(2 * x, 2) * ((C2 + 1) * 2 * x - C2)) / 2
						: (Math.pow(2 * x - 2, 2) * ((C2 + 1) * (x * 2 - 2) + C2) + 2) / 2;
			case EASE_IN_BOUNCE:
				return 1 - GetEasing(EasingType.EASE_OUT_BOUNCE, 1 - x);
			case EASE_OUT_BOUNCE:
				if(x < 1 / D1) {
					return N1 * x * x;
				} else if(x < 2 / D1) {
					return N1 * (x -= 1.5 / D1) * x + 0.75;
				} else if(x < 2.5 / D1) {
					return N1 * (x -= 2.25 / D1) * x + 0.9375;
				} else {
					return N1 * (x -= 2.625 / D1) * x + 0.984375;
				}
			case EASE_IN_OUT_BOUNCE:
				return x < 0.5
						? (1 - GetEasing(EasingType.EASE_OUT_BOUNCE, 1 - 2 * x)) / 2
						: (1 + GetEasing(EasingType.EASE_OUT_BOUNCE, 2 * x - 1)) / 2;
			case LINEAR:
				return x;
		}
		throw new RuntimeException("Missing easing implementation.");
	}
}
