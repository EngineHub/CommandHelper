package com.laytonsmith.PureUtilities.Common;

import java.awt.Window;

/**
 *
 * @author lsmith
 */
public class UIUtils {
	
	public static void centerWindow(Window w){
		w.setLocationRelativeTo(null);
	}
	
	public static void centerWindowOnWindow(Window windowToCenter, Window windowUponWhichToCenterOn){
		windowToCenter.setLocationRelativeTo(windowUponWhichToCenterOn);
	}
}
