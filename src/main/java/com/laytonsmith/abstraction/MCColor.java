package com.laytonsmith.abstraction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface MCColor {

	/**
	 * White, or (0xFF,0xFF,0xFF) in (R,G,B)
	 */
	MCColor WHITE = StaticLayer.GetConvertor().GetColor(0xFF, 0xFF, 0xFF);

	/**
	 * Silver, or (0xC0,0xC0,0xC0) in (R,G,B)
	 */
	 MCColor SILVER = StaticLayer.GetConvertor().GetColor(0xC0, 0xC0, 0xC0);

	/**
	 * Gray, or (0x80,0x80,0x80) in (R,G,B)
	 */
	 MCColor GRAY = StaticLayer.GetConvertor().GetColor(0x80, 0x80, 0x80);

	/**
	 * Black, or (0x00,0x00,0x00) in (R,G,B)
	 */
	 MCColor BLACK = StaticLayer.GetConvertor().GetColor(0x00, 0x00, 0x00);

	/**
	 * Red, or (0xFF,0x00,0x00) in (R,G,B)
	 */
	 MCColor RED = StaticLayer.GetConvertor().GetColor(0xFF, 0x00, 0x00);

	/**
	 * Maroon, or (0x80,0x00,0x00) in (R,G,B)
	 */
	 MCColor MAROON = StaticLayer.GetConvertor().GetColor(0x80, 0x00, 0x00);

	/**
	 * Yellow, or (0xFF,0xFF,0x00) in (R,G,B)
	 */
	 MCColor YELLOW = StaticLayer.GetConvertor().GetColor(0xFF, 0xFF, 0x00);

	/**
	 * Olive, or (0x80,0x80,0x00) in (R,G,B)
	 */
	 MCColor OLIVE = StaticLayer.GetConvertor().GetColor(0x80, 0x80, 0x00);

	/**
	 * Lime, or (0x00,0xFF,0x00) in (R,G,B)
	 */
	 MCColor LIME = StaticLayer.GetConvertor().GetColor(0x00, 0xFF, 0x00);

	/**
	 * Green, or (0x00,0x80,0x00) in (R,G,B)
	 */
	 MCColor GREEN = StaticLayer.GetConvertor().GetColor(0x00, 0x80, 0x00);

	/**
	 * Aqua, or (0x00,0xFF,0xFF) in (R,G,B)
	 */
	 MCColor AQUA = StaticLayer.GetConvertor().GetColor(0x00, 0xFF, 0xFF);

	/**
	 * Teal, or (0x00,0x80,0x80) in (R,G,B)
	 */
	 MCColor TEAL = StaticLayer.GetConvertor().GetColor(0x00, 0x80, 0x80);

	/**
	 * Blue, or (0x00,0x00,0xFF) in (R,G,B)
	 */
	 MCColor BLUE = StaticLayer.GetConvertor().GetColor(0x00, 0x00, 0xFF);

	/**
	 * Navy, or (0x00,0x00,0x80) in (R,G,B)
	 */
	 MCColor NAVY = StaticLayer.GetConvertor().GetColor(0x00, 0x00, 0x80);

	/**
	 * Fuchsia, or (0xFF,0x00,0xFF) in (R,G,B)
	 */
	 MCColor FUCHSIA = StaticLayer.GetConvertor().GetColor(0xFF, 0x00, 0xFF);

	/**
	 * Purple, or (0x80,0x00,0x80) in (R,G,B)
	 */
	 MCColor PURPLE = StaticLayer.GetConvertor().GetColor(0x80, 0x00, 0x80);

	/**
	 * Orange, or (0xFF,0xA5,0x00) in (R,G,B)
	 */
	MCColor ORANGE = StaticLayer.GetConvertor().GetColor(0xFF, 0xA5, 0x00);

	/**
	 * A mapping of string color name values to the standard MCColor objects.
	 * Note that the map is immutable, so any attempts to write to it will fail.
	 */
	Map<String, MCColor> STANDARD_COLORS = Internal.buildColors();

	int getRed();
	int getGreen();
	int getBlue();

	/**
	 * Returns a NEW instance of a color, given the specified RGB values.
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	MCColor build(int red, int green, int blue);

	class Internal {
		private static Map<String, MCColor> buildColors(){
			Map<String, MCColor> map = new HashMap<>();
			map.put("AQUA", AQUA);
			map.put("BLACK", BLACK);
			map.put("BLUE", BLUE);
			map.put("FUCHSIA", FUCHSIA);
			map.put("GRAY", GRAY);
			map.put("GREY", GRAY);
			map.put("GREEN", GREEN);
			map.put("LIME", LIME);
			map.put("MAROON", MAROON);
			map.put("NAVY", NAVY);
			map.put("OLIVE", OLIVE);
			map.put("ORANGE", ORANGE);
			map.put("PURPLE", PURPLE);
			map.put("RED", RED);
			map.put("SILVER", SILVER);
			map.put("TEAL", TEAL);
			map.put("WHITE", WHITE);
			map.put("YELLOW", YELLOW);
			return Collections.unmodifiableMap(map);
		}
	}

}
