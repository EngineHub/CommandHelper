package com.laytonsmith.PureUtilities.Common;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

/**
 * Provides common UI utilites
 */
public class UIUtils {

	/**
	 * Centers the window on the current "active" monitor. The active monitor is
	 * defined as the monitor that the mouse is currently in.
	 * @param w
	 */
	public static void centerWindow(Window w){
		// For multimonitor support, we need to iterate the monitors
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();
		Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		Rectangle primary = null;
		for(GraphicsDevice gg : devices){
			Rectangle r = gg.getDefaultConfiguration().getBounds();
			primary = r; // Set this as the primary, so that primary will never be null.
			if(mousePoint.x > r.x && mousePoint.x < (r.x + r.width)
					&& mousePoint.y > r.y && mousePoint.y < (r.y + r.height)){
				// This is the "primary" monitor
				primary = r;
				break;
			}
		}

		assert primary != null;

		// Find the center point of primary
		Point center = new Point((primary.width / 2) + primary.x, (primary.height / 2) + primary.y);
		Point offset = new Point(center.x - (w.getWidth() / 2), center.y - (w.getHeight() / 2));
		// Now set the window at the offset.
		w.setLocation(offset);
	}

	/**
	 * Centers a window on another window.
	 * @param windowToCenter The window that will be moved
	 * @param windowUponWhichToCenterOn The window that will be centered on
	 */
	public static void centerWindowOnWindow(Window windowToCenter, Window windowUponWhichToCenterOn){
		windowToCenter.setLocationRelativeTo(windowUponWhichToCenterOn);
	}
}
