package com.laytonsmith.PureUtilities.Common;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Provides common UI utilites
 */
public class UIUtils {

	/**
	 * Centers the window on the current "active" monitor. The active monitor is defined as the monitor that the mouse
	 * is currently in.
	 *
	 * @param w
	 */
	public static void centerWindow(Window w) {
		// For multimonitor support, we need to iterate the monitors
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();
		Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		Rectangle primary = null;
		for(GraphicsDevice gg : devices) {
			Rectangle r = gg.getDefaultConfiguration().getBounds();
			primary = r; // Set this as the primary, so that primary will never be null.
			if(mousePoint.x > r.x && mousePoint.x < (r.x + r.width)
					&& mousePoint.y > r.y && mousePoint.y < (r.y + r.height)) {
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
	 *
	 * @param windowToCenter The window that will be moved
	 * @param windowUponWhichToCenterOn The window that will be centered on
	 */
	public static void centerWindowOnWindow(Window windowToCenter, Window windowUponWhichToCenterOn) {
		windowToCenter.setLocationRelativeTo(windowUponWhichToCenterOn);
	}

	/**
	 * Provides an easy way to setEnabled on multiple components at once.
	 *
	 * @param enabled
	 * @param components
	 */
	public static void setEnabled(boolean enabled, Component... components) {
		for(Component component : components) {
			component.setEnabled(enabled);
		}
	}

	/**
	 * Opens the system's default browser to the specified URI.
	 *
	 * @param uri
	 * @throws java.io.IOException
	 */
	public static void openWebpage(URI uri) throws IOException {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(uri);
		}
	}

	/**
	 * Opens the system's default browser to the specified URL.
	 *
	 * @param url
	 * @throws java.io.IOException
	 * @throws java.net.URISyntaxException
	 */
	public static void openWebpage(URL url) throws IOException, URISyntaxException {
		openWebpage(url.toURI());
	}
}
