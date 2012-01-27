/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.awt.Color;

/**
 *
 * @author layton
 */
public class TermColors {
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
                System.out.println();
            }
        } else {           
            System.out.print("\u001b[2J");
            System.out.flush();
        }
    }
    
    /*
     * Standard foreground colors 
     */
    public static final String RED = color(Color.RED).toString();
    public static final String GREEN = color(Color.GREEN).toString();
    public static final String BLUE = color(Color.BLUE).toString();
    public static final String YELLOW = color(Color.YELLOW).toString();
    public static final String CYAN = color(Color.CYAN).toString();
    public static final String MAGENTA = color(Color.MAGENTA).toString();
    public static final String BLACK = color(Color.BLACK).toString();
    public static final String WHITE = color(Color.WHITE).toString();
    
    /*
     * Bright foreground colors
     */
    public static final String BRIGHT_RED = color(Color.RED, true, true);
    public static final String BRIGHT_GREEN = color(Color.GREEN, true, true);
    public static final String BRIGHT_BLUE = color(Color.BLUE, true, true);
    public static final String BRIGHT_YELLOW = color(Color.YELLOW, true, true);
    public static final String BRIGHT_CYAN = color(Color.CYAN, true, true);
    public static final String BRIGHT_MAGENTA = color(Color.MAGENTA, true, true);
    public static final String BRIGHT_BLACK = color(Color.BLACK, true, true);
    public static final String BRIGHT_WHITE = color(Color.WHITE, true, true);
    
    /*
     * Standard background colors 
     */
    public static final String BG_RED = color(Color.RED, false, false).toString();
    public static final String BG_GREEN = color(Color.GREEN, false, false).toString();
    public static final String BG_BLUE = color(Color.BLUE, false, false).toString();
    public static final String BG_YELLOW = color(Color.YELLOW, false, false).toString();
    public static final String BG_CYAN = color(Color.CYAN, false, false).toString();
    public static final String BG_MAGENTA = color(Color.MAGENTA, false, false).toString();
    public static final String BG_BLACK = color(Color.BLACK, false, false).toString();
    public static final String BG_WHITE = color(Color.WHITE, false, false).toString();
    
    /*
     * Bright background colors
     */
    public static final String BG_BRIGHT_RED = color(Color.RED, true, false);
    public static final String BG_BRIGHT_GREEN = color(Color.GREEN, true, false);
    public static final String BG_BRIGHT_BLUE = color(Color.BLUE, true, false);
    public static final String BG_BRIGHT_YELLOW = color(Color.YELLOW, true, false);
    public static final String BG_BRIGHT_CYAN = color(Color.CYAN, true, false);
    public static final String BG_BRIGHT_MAGENTA = color(Color.MAGENTA, true, false);
    public static final String BG_BRIGHT_BLACK = color(Color.BLACK, true, false);
    public static final String BG_BRIGHT_WHITE = color(Color.WHITE, true, false);
    
    public static final String BLINKON = special("blinkon");
    public static final String BLINKOFF = special("blinkoff");
    
    private static String special(String type){
        if(SYSTEM.equals(SYS.UNIX)){
            if(type.equals("blinkon")){
                return "\033[5m";
            }
            if(type.equals("blinkoff")){
                return "\033[25m";
            }
        }
        return "";
    }
    
    public static String reset(){
        if(SYSTEM.equals(SYS.WINDOWS)){
            return ""; //lol, it's already reset
        }
        return "\033[0m";
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
    public static String color(Color c, boolean bright, boolean foreground) {
        if (SYSTEM.equals(SYS.WINDOWS)) {
            return "";
        }

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
}
