
package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.awt.Color;
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
public final class TermColors {
    
    private TermColors(){}
    
    public enum SYS {

        WINDOWS,
        UNIX
    }
    public static final SYS SYSTEM;
    static {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            SYSTEM = SYS.WINDOWS;
        } else {
            SYSTEM = SYS.UNIX;
        }		
    }

    
    public static void cls(){
        if(SYSTEM.equals(SYS.WINDOWS)){
            //Fuck you windows.
            for(int i = 0; i < 50; i++){
                StreamUtils.GetSystemOut().println();
            }
        } else {           
            StreamUtils.GetSystemOut().print("\u001b[2J");
            StreamUtils.GetSystemOut().flush();
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    private @interface color{}
    
    /*
     * Standard foreground colors 
     */
    @color public static String RED = color(Color.RED).toString();
    @color public static String GREEN = color(Color.GREEN).toString();
    @color public static String BLUE = color(Color.BLUE).toString();
    @color public static String YELLOW = color(Color.YELLOW).toString();
    @color public static String CYAN = color(Color.CYAN).toString();
    @color public static String MAGENTA = color(Color.MAGENTA).toString();
    @color public static String BLACK = color(Color.BLACK).toString();
    @color public static String WHITE = color(Color.WHITE).toString();
    
    /*
     * Bright foreground colors
     */
    @color public static String BRIGHT_RED = color(Color.RED, true, true);
    @color public static String BRIGHT_GREEN = color(Color.GREEN, true, true);
    @color public static String BRIGHT_BLUE = color(Color.BLUE, true, true);
    @color public static String BRIGHT_YELLOW = color(Color.YELLOW, true, true);
    @color public static String BRIGHT_CYAN = color(Color.CYAN, true, true);
    @color public static String BRIGHT_MAGENTA = color(Color.MAGENTA, true, true);
    @color public static String BRIGHT_BLACK = color(Color.BLACK, true, true);
    @color public static String BRIGHT_WHITE = color(Color.WHITE, true, true);
    
    /*
     * Standard background colors 
     */
    @color public static String BG_RED = color(Color.RED, false, false).toString();
    @color public static String BG_GREEN = color(Color.GREEN, false, false).toString();
    @color public static String BG_BLUE = color(Color.BLUE, false, false).toString();
    @color public static String BG_YELLOW = color(Color.YELLOW, false, false).toString();
    @color public static String BG_CYAN = color(Color.CYAN, false, false).toString();
    @color public static String BG_MAGENTA = color(Color.MAGENTA, false, false).toString();
    @color public static String BG_BLACK = color(Color.BLACK, false, false).toString();
    @color public static String BG_WHITE = color(Color.WHITE, false, false).toString();
    
    /*
     * Bright background colors
     */
    @color public static String BG_BRIGHT_RED = color(Color.RED, true, false);
    @color public static String BG_BRIGHT_GREEN = color(Color.GREEN, true, false);
    @color public static String BG_BRIGHT_BLUE = color(Color.BLUE, true, false);
    @color public static String BG_BRIGHT_YELLOW = color(Color.YELLOW, true, false);
    @color public static String BG_BRIGHT_CYAN = color(Color.CYAN, true, false);
    @color public static String BG_BRIGHT_MAGENTA = color(Color.MAGENTA, true, false);
    @color public static String BG_BRIGHT_BLACK = color(Color.BLACK, true, false);
    @color public static String BG_BRIGHT_WHITE = color(Color.WHITE, true, false);
    
    @color public static String BLINKON = special("blinkon");
    @color public static String BLINKOFF = special("blinkoff");
    
    @color public static String BOLD = special("bold");
    @color public static String STRIKE = special("strike");
    @color public static String UNDERLINE = special("underline");
    @color public static String ITALIC = special("italic");
	
	@color public static String RESET = special("reset");
    
    private static Map<String, String> defaults = new HashMap<String, String>();
    private static List<Field> fields = null;
    
    private static List<Field> fields(){
        if(fields == null){
            fields = new ArrayList<Field>();
            for(Field f : TermColors.class.getFields()){
                if(f.getAnnotation(color.class) != null){
                    fields.add(f);
                    try {
                        defaults.put(f.getName(), (String)f.get(null));
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
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
    public static void EnableColors(){
        for(Field f : fields()){
            try {
                f.set(null, defaults.get(f.getName()));
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
	/**
	 * Disables colors.
	 */
    public static void DisableColors(){
        for(Field f : fields()){
            try {
                f.set(null, "");
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(TermColors.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
	
	/**
	 * Returns true or false if colors are enabled or not.
	 * @return 
	 */
	public static boolean ColorsDisabled(){
		return RED == null;
	}
    
    private static String special(String type){
        if(SYSTEM.equals(SYS.UNIX)){
            if(type.equals("blinkon")){
                return "\033[5m";
            }
            if(type.equals("blinkoff")){
                return "\033[25m";
            }
            if(type.equals("bold")){
                return "\033[1m";
            }
            if(type.equals("strike")){
                return "\033[9m";
            }
            if(type.equals("underline")){
                return "\033[4m";
            }
            if(type.equals("italic")){
                return "\033[3m";
            }
        }
		if(type.equals("reset")){
			return "\033[m";
		}
        return "";
    }
    
    public static String reset(){
		return RESET;
    }
    /**
     * Returns the specified color code, foreground, and dark.
     * @param c
     * @return 
     */
    public static String color(Color c){
        return color(c, false, true);
    }
    /**
     * This is not the preferred method, however, if you must, you
     * can use this function to get the specified colors, given an awt Color.
     * Not all colors are supported, and bad colors will just return white.
     * @param c
     * @return 
     */
    private static String color(Color c, boolean bright, boolean foreground) {

        int color = 37;
        if (c.equals(Color.RED)) {
            color = 31;
        } else if (c.equals(Color.GREEN)) {
            color = 32;
        } else if (c.equals(Color.BLUE)) {
            color = 34;
        } else if (c.equals(Color.YELLOW)) {
            color = 33;
        } else if (c.equals(Color.CYAN)) {
            color = 36;
        } else if (c.equals(Color.MAGENTA)) {
            color = 35;
        } else if (c.equals(Color.BLACK)) {
            color = 30;
        } else if (c.equals(Color.WHITE)) {
            color = 37;
        }
        if(!foreground){
            color += 10;
        }
        return "\033[" + (bright?"1;":"") + color + "m";
    }
    
    public static void p(CharSequence c) {
        StreamUtils.GetSystemOut().print(c);
        StreamUtils.GetSystemOut().flush();
    }

    public static void pl() {
        pl("");
    }
    
    public static String prompt(){
        if(scanner == null){
            scanner = new Scanner(System.in);
        }
        p(">" + MAGENTA);
        StreamUtils.GetSystemOut().flush();
        String ret = scanner.nextLine();
        p(WHITE);
        return ret;
    }

    private static Scanner scanner;
    public static void pl(CharSequence c) {
        StreamUtils.GetSystemOut().println(c + WHITE);
    }
	
	/**
	 * THIS BLOCK MUST REMAIN AT THE BOTTOM
	 */
	static{
		if(SYSTEM == SYS.WINDOWS){
			DisableColors();
		} else {
			EnableColors();
		}
	}
}
