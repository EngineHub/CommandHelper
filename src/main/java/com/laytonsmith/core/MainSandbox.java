package com.laytonsmith.core;

import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.util.Locale;


/**
 * This class is for testing concepts
 */
public class MainSandbox {


    public static void main(String[] args) throws Exception {
        // This font is < 35Kb.
        URL fontUrl = new URL("http://webpagepublicity.com/" +
            "free-fonts/a/Airacobra%20Condensed.ttf");
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);
        for(Font f : ge.getAllFonts()) {
			System.out.println(f.getFamily());
			System.out.println(f.getName());
			System.out.println();
		}
    }

}
