package com.laytonsmith.PureUtilities;

import com.laytonsmith.abstraction.MCChatColor;
import java.util.ArrayList;

/**
 * This class provides a method for working around the not so pretty line breaks
 * that SMP does. The original class was written by Nossr50, with portions contributed
 * by Reil.
 * @author Layton
 */
public class rParser {

    protected static final int lineLength = 312;

    /*
     * Finds the last color sequence used in the string
     */
    public static String lastColor(String findColor) {
        int i = findColor.lastIndexOf("§");
        if (i != -1 && i != findColor.length() - 1) {
            return "§" + findColor.charAt(i + 1);
        } else {
            return "";
        }
    }

    /* 
     * 
     */
    public static String combineSplit(int beginHere, String[] split, String seperator) {
        StringBuilder combined = new StringBuilder(split[beginHere]);
        if (beginHere + 1 < split.length) {
            for (int i = beginHere + 1; i < split.length; i++) {
                combined.append(seperator + split[i]);
            }
        }
        return combined.toString();
    }

    public static String[] wordWrap(String msg) {
        return wordWrap(msg, "", lineLength);
    }

    public static String[] wordWrap(String msg, String prefix) {
        return wordWrap(msg, prefix, lineLength);
    }

    public static String[] wordWarp(String msg, int lineLength) {
        return wordWrap(msg, "", lineLength);
    }

    public static String[] wordWrap(String msg, String prefix, int lineLength) {
        //Split each word apart
        ArrayList<String> split = new ArrayList<String>();
        for (String in : msg.split(" ")) {
            split.add(in);
        }

        //Create an arraylist for the output
        ArrayList<String> out = new ArrayList<String>();
        //While i is less than the length of the array of words
        while (!split.isEmpty()) {
            int len = 0;

            //Create an arraylist to hold individual words
            ArrayList<String> words = new ArrayList<String>();

            //Loop through the words finding their length and increasing
            //j, the end point for the sub string
            while (!split.isEmpty() && split.get(0) != null && len <= lineLength) {
                int wordLength = msgLength(split.get(0)) + 4;

                //If a word is too long for a line
                if (wordLength > lineLength) {
                    String[] tempArray = wordCut(len, split.remove(0), lineLength);
                    words.add(tempArray[0]);

                    split.add(tempArray[1]);
                }

                //If the word is not too long to fit
                len += wordLength;
                if (len <= lineLength) {
                    words.add(split.remove(0));
                }
            }
            //Merge them and add them to the output array.
            String lastColor = "";
            if (!out.isEmpty()) {
                lastColor = lastColor(out.get(out.size() - 1));
            }
            String[] stringArray = words.toArray(new String[words.size()]);
            //if(stringArray.length != 0){
                out.add(lastColor
                        + combineSplit(0, stringArray, " ") + " ");
            //}
        }

        //Convert to an array and return
        return out.toArray(new String[out.size()]);
    }

    //=====================================================================
    //Function:	msgLength
    //Input:	String str: The string to find the length of
    //Output:	int: The length on the screen of a string
    //Use:		Finds the length on the screen of a string. Ignores MCChatColor.
    //=====================================================================
    public static int msgLength(String str) {
        int length = 0;
        //Loop through all the characters, skipping any color characters
        //and their following color codes
        for (int x = 0; x < str.length(); x++) {
            if (str.charAt(x) == '§' /*|| str.charAt(x) == MCChatColor.White.charAt(0)*/) {
                if (x + 1 != str.length()) {
                    if (colorChange(str.charAt(x + 1)) != null) {
                        x++;
                        continue;
                    }
                }
            }
            int len = charLength(str.charAt(x));
            length += len;
        }
        return length;
    }

    //=====================================================================
    //Function:	wordCut
    //Input:	String str: The string to find the length of
    //Output:	String[]: The cut up word
    //Use:		Cuts apart a word that is too long to fit on one line
    //=====================================================================
    private static String[] wordCut(int lengthBefore, String str, int lineLength) {
        int length = lengthBefore;
        //Loop through all the characters, skipping any color characters
        //and their following color codes
        String[] output = new String[2];
        int x = 0;
        while (length < lineLength && x < str.length()) {
            int len = charLength(str.charAt(x));
            if (len > 0) {
                length += len;
            } else {
                x++;
            }
            x++;
        }
        if (x > str.length()) {
            x = str.length();
        }
        //Add the substring to the output after cutting it
        output[0] = str.substring(0, x);
        //Add the last of the string to the output.
        output[1] = str.substring(x);
        return output;
    }

    //=====================================================================
    //Function:	charLength
    //Input:	char x: The character to find the length of.
    //Output:	int: The length of the character
    //Use:		Finds the visual length of the character on the screen.
    //=====================================================================
    private static int charLength(char x) {
        if ("i.:,;|!".indexOf(x) != -1) {
            return 2;
        } else if ("l'".indexOf(x) != -1) {
            return 3;
        } else if ("tI[]".indexOf(x) != -1) {
            return 4;
        } else if ("fk{}<>\"*()".indexOf(x) != -1) {
            return 5;
        } else if ("abcdeghjmnopqrsuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ1234567890\\/#?$%-=_+&^".indexOf(x) != -1) {
            return 6;
        } else if ("@~".indexOf(x) != -1) {
            return 7;
        } else if (x == ' ') {
            return 4;
        } else {
            return -1;
        }
    }

    //=====================================================================
    //Function:	colorChange
    //Input:	char colour: The color code to find the color for
    //Output:	String: The color that the code identified 
    //Use:		Finds a color giving a color code
    //=====================================================================
    public static String colorChange(char colour) {
        MCChatColor color;
        switch (colour) {
            case '0':
                color = MCChatColor.BLACK;
                break;
            case '1':
                color = MCChatColor.DARK_BLUE;
                break;
            case '2':
                color = MCChatColor.DARK_GREEN;
                break;
            case '3':
                color = MCChatColor.DARK_AQUA;
                break;
            case '4':
                color = MCChatColor.DARK_RED;
                break;
            case '5':
                color = MCChatColor.DARK_PURPLE;
                break;
            case '6':
                color = MCChatColor.GOLD;
                break;
            case '7':
                color = MCChatColor.GRAY;
                break;
            case '8':
                color = MCChatColor.DARK_GRAY;
                break;
            case '9':
                color = MCChatColor.BLUE;
                break;
            case 'a':
                color = MCChatColor.GREEN;
                break;
            case 'b':
                color = MCChatColor.AQUA;
                break;
            case 'c':
                color = MCChatColor.RED;
                break;
            case 'd':
                color = MCChatColor.LIGHT_PURPLE;
                break;
            case 'e':
                color = MCChatColor.YELLOW;
                break;
            case 'f':
                color = MCChatColor.WHITE;
                break;
            case 'A':
                color = MCChatColor.GREEN;
                break;
            case 'B':
                color = MCChatColor.AQUA;
                break;
            case 'C':
                color = MCChatColor.RED;
                break;
            case 'D':
                color = MCChatColor.LIGHT_PURPLE;
                break;
            case 'E':
                color = MCChatColor.YELLOW;
                break;
            case 'F':
                color = MCChatColor.WHITE;
                break;
            default:
                return null;
        }
        return color.toString();
    }
}
