package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MDynamicEnum("com.commandhelper.PatternShape")
public abstract class MCPatternShape<Concrete> extends DynamicEnum<MCPatternShape.MCVanillaPatternShape, Concrete> {

	protected static final Map<String, MCPatternShape> MAP = new HashMap<>();

	public MCPatternShape(MCVanillaPatternShape mcVanillaPatternShape, Concrete concrete) {
		super(mcVanillaPatternShape, concrete);
	}

	public static MCPatternShape valueOf(String test) throws IllegalArgumentException {
		MCPatternShape shape = MAP.get(test);
		if(shape == null) {
			throw new IllegalArgumentException("Unknown pattern shape: " + test);
		}
		return shape;
	}

	/**
	 * @return Names of available patterns
	 */
	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaPatternShape t : MCVanillaPatternShape.values()) {
				if(t.existsIn(MCVersion.CURRENT)) {
					dummy.add(t.name());
				}
			}
			return dummy;
		}
		return MAP.keySet();
	}

	/**
	 * @return Our own MCPatternShape list
	 */
	public static List<MCPatternShape> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCPatternShape> dummy = new ArrayList<>();
			for(final MCPatternShape.MCVanillaPatternShape p : MCPatternShape.MCVanillaPatternShape.values()) {
				if(!p.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCPatternShape<>(p, null) {
					@Override
					public String name() {
						return p.name();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaPatternShape {
		BASE,
		BORDER,
		BRICKS,
		CIRCLE_MIDDLE,
		CREEPER,
		CROSS,
		CURLY_BORDER,
		DIAGONAL_LEFT,
		DIAGONAL_LEFT_MIRROR,
		DIAGONAL_RIGHT,
		DIAGONAL_RIGHT_MIRROR,
		FLOW(MCVersion.MC1_21),
		FLOWER,
		GLOBE,
		GRADIENT,
		GRADIENT_UP,
		GUSTER(MCVersion.MC1_21),
		HALF_HORIZONTAL,
		HALF_HORIZONTAL_MIRROR,
		HALF_VERTICAL,
		HALF_VERTICAL_MIRROR,
		MOJANG,
		PIGLIN,
		RHOMBUS_MIDDLE,
		SKULL,
		SQUARE_BOTTOM_LEFT,
		SQUARE_BOTTOM_RIGHT,
		SQUARE_TOP_LEFT,
		SQUARE_TOP_RIGHT,
		STRAIGHT_CROSS,
		STRIPE_BOTTOM,
		STRIPE_CENTER,
		STRIPE_DOWNLEFT,
		STRIPE_DOWNRIGHT,
		STRIPE_LEFT,
		STRIPE_MIDDLE,
		STRIPE_RIGHT,
		STRIPE_SMALL,
		STRIPE_TOP,
		TRIANGLE_BOTTOM,
		TRIANGLE_TOP,
		TRIANGLES_BOTTOM,
		TRIANGLES_TOP,
		UNKNOWN(MCVersion.NEVER);

		final MCVersion added;

		MCVanillaPatternShape() {
			this.added = MCVersion.MC1_8;
		}

		MCVanillaPatternShape(MCVersion version) {
			this.added = version;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(added);
		}
	}
}
