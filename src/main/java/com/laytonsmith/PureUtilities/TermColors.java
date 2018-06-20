package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
// Variables can be set through DisableColors(), but should be handled as if they were final.
@SuppressWarnings("checkstyle:staticvariablename")
public final class TermColors {

	private TermColors() {
	}

	public enum SYS {

		WINDOWS,
		UNIX
	}
	public static final SYS SYSTEM;

	static {
		String os = System.getProperty("os.name");
		if(os.contains("Windows")) {
			SYSTEM = SYS.WINDOWS;
		} else {
			SYSTEM = SYS.UNIX;
		}
	}

	public static void cls() {
		if(SYSTEM.equals(SYS.WINDOWS)) {
			//Fuck you windows.
			for(int i = 0; i < 50; i++) {
				StreamUtils.GetSystemOut().println();
			}
		} else {
			StreamUtils.GetSystemOut().print("\u001b[2J");
			StreamUtils.GetSystemOut().flush();
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private @interface TermColor {
	}

	/*
	 * Standard foreground colors
	 */
	@TermColor
	public static String RED = color(Color.RED);
	@TermColor
	public static String GREEN = color(Color.GREEN);
	@TermColor
	public static String BLUE = color(Color.BLUE);
	@TermColor
	public static String YELLOW = color(Color.YELLOW);
	@TermColor
	public static String CYAN = color(Color.CYAN);
	@TermColor
	public static String MAGENTA = color(Color.MAGENTA);
	@TermColor
	public static String BLACK = color(Color.BLACK);
	@TermColor
	public static String WHITE = color(Color.WHITE);

	/*
	 * Bright foreground colors
	 */
	@TermColor
	public static String BRIGHT_RED = color(Color.RED, true, true, true);
	@TermColor
	public static String BRIGHT_GREEN = color(Color.GREEN, true, true, true);
	@TermColor
	public static String BRIGHT_BLUE = color(Color.BLUE, true, true, true);
	@TermColor
	public static String BRIGHT_YELLOW = color(Color.YELLOW, true, true, true);
	@TermColor
	public static String BRIGHT_CYAN = color(Color.CYAN, true, true, true);
	@TermColor
	public static String BRIGHT_MAGENTA = color(Color.MAGENTA, true, true, true);
	@TermColor
	public static String BRIGHT_BLACK = color(Color.BLACK, true, true, true);
	@TermColor
	public static String BRIGHT_WHITE = color(Color.WHITE, true, true, true);

	/*
	 * Standard background colors
	 */
	@TermColor
	public static String BG_RED = color(Color.RED, false, false, false);
	@TermColor
	public static String BG_GREEN = color(Color.GREEN, false, false, false);
	@TermColor
	public static String BG_BLUE = color(Color.BLUE, false, false, false);
	@TermColor
	public static String BG_YELLOW = color(Color.YELLOW, false, false, false);
	@TermColor
	public static String BG_CYAN = color(Color.CYAN, false, false, false);
	@TermColor
	public static String BG_MAGENTA = color(Color.MAGENTA, false, false, false);
	@TermColor
	public static String BG_BLACK = color(Color.BLACK, false, false, false);
	@TermColor
	public static String BG_WHITE = color(Color.WHITE, false, false, false);

	/*
	 * Bright background colors
	 */
	@TermColor
	public static String BG_BRIGHT_RED = color(Color.RED, true, false, false);
	@TermColor
	public static String BG_BRIGHT_GREEN = color(Color.GREEN, true, false, false);
	@TermColor
	public static String BG_BRIGHT_BLUE = color(Color.BLUE, true, false, false);
	@TermColor
	public static String BG_BRIGHT_YELLOW = color(Color.YELLOW, true, false, false);
	@TermColor
	public static String BG_BRIGHT_CYAN = color(Color.CYAN, true, false, false);
	@TermColor
	public static String BG_BRIGHT_MAGENTA = color(Color.MAGENTA, true, false, false);
	@TermColor
	public static String BG_BRIGHT_BLACK = color(Color.BLACK, true, false, false);
	@TermColor
	public static String BG_BRIGHT_WHITE = color(Color.WHITE, true, false, false);

	@TermColor
	public static String BLINKON = special("blinkon");
	@TermColor
	public static String BLINKOFF = special("blinkoff");

	@TermColor
	public static String BOLD = special("bold");
	@TermColor
	public static String STRIKE = special("strike");
	@TermColor
	public static String UNDERLINE = special("underline");
	@TermColor
	public static String ITALIC = special("italic");

	@TermColor
	public static String RESET = special("reset");

	private static final Map<String, String> DEFAULTS = new HashMap<String, String>();
	private static List<Field> fields = null;

	private static List<Field> fields() {
		if(fields == null) {
			fields = new ArrayList<Field>();
			for(Field f : TermColors.class.getFields()) {
				if(f.getAnnotation(TermColor.class) != null) {
					fields.add(f);
					try {
						DEFAULTS.put(f.getName(), (String) f.get(null));
					} catch (IllegalArgumentException | IllegalAccessException ex) {
						Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}
		return fields;
	}

	/**
	 * Enables colors.
	 */
	public static void EnableColors() {
		for(Field f : fields()) {
			try {
				f.set(null, DEFAULTS.get(f.getName()));
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Disables colors.
	 */
	public static void DisableColors() {
		for(Field f : fields()) {
			try {
				f.set(null, "");
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Returns true or false if colors are enabled or not.
	 *
	 * @return
	 */
	public static boolean ColorsDisabled() {
		return RED == null;
	}

	private static String special(String type) {

		// On windows, these effects (except for reset) get printed as a changed background color.
		// This is consistent with how broadcast() messages are printed to the console.
		switch(type) {
			case "blinkon":
				return "\033[5m";
			case "blinkoff":
				return "\033[25m";
			case "bold":
				return "\033[1m";
			case "strike":
				return "\033[9m";
			case "underline":
				return "\033[4m";
			case "italic":
				return "\033[3m";
			case "reset":
				return "\033[m";
			default:
				return "";
		}
	}

	public static String reset() {
		return RESET;
	}

	/**
	 * Returns the specified color code, foreground, dark and prefixed with an ANSI reset.
	 *
	 * @param c
	 * @return
	 */
	public static String color(Color c) {
		return color(c, false, true, true);
	}

	/**
	 * This is not the preferred method, however, if you must, you can use this function to get the specified colors,
	 * given an awt Color. Not all colors are supported, and bad colors will just return white.
	 *
	 * @param c The color to set.
	 * @param bright
	 * @param foreground True to set the color of the foreground, false to set the color of the background.
	 * @param resetCurrent Resets ANSI modifiers before this ANSI color.
	 * @return
	 */
	private static String color(Color c, boolean bright, boolean foreground, boolean resetCurrent) {

		int color = 37;
		if(c.equals(Color.RED)) {
			color = 31;
		} else if(c.equals(Color.GREEN)) {
			color = 32;
		} else if(c.equals(Color.BLUE)) {
			color = 34;
		} else if(c.equals(Color.YELLOW)) {
			color = 33;
		} else if(c.equals(Color.CYAN)) {
			color = 36;
		} else if(c.equals(Color.MAGENTA)) {
			color = 35;
		} else if(c.equals(Color.BLACK)) {
			color = 30;
		} else if(c.equals(Color.WHITE)) {
			color = 37;
		}
		if(!foreground) {
			color += 10;
		}
		// ANSI: 0 = reset, 1 = bright_intensity, 22 = normal_intensity.
		return "\033[" + (resetCurrent ? "0;" : "") + color + ";" + (bright ? "1" : "22") + "m";
	}

	public static void p(CharSequence c) {
		StreamUtils.GetSystemOut().print(c);
		StreamUtils.GetSystemOut().flush();
	}

	private static Scanner scanner;

	public static String prompt() {
		if(scanner == null) {
			scanner = new Scanner(System.in);
		}
		p(">" + MAGENTA);
		StreamUtils.GetSystemOut().flush();
		String ret = scanner.nextLine();
		p(WHITE);
		return ret;
	}

	public static void pl(CharSequence c) {
		StreamUtils.GetSystemOut().println(c + WHITE);
	}

	public static void pl() {
		pl("");
	}

	/**
	 * THIS BLOCK MUST REMAIN AT THE BOTTOM
	 */
	static {
		if(SYSTEM == SYS.WINDOWS) {
			DisableColors();
		} else {
			EnableColors();
		}
	}
}
