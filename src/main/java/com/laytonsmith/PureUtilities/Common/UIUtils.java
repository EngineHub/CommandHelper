package com.laytonsmith.PureUtilities.Common;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JOptionPane;

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
	 * Opens the system's default browser to the specified URI. Returns false if the web browser was
	 * definitely not opened. True returned means that as far as this code can be aware, the browser
	 * was launched, but there is no guarantee.
	 *
	 * @param uri
	 * @throws java.io.IOException
	 */
	public static boolean openWebpage(URI uri) throws IOException {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(uri);
			return true;
		}
		return false;
	}

	/**
	 * Opens the system's default browser to the specified URL. Returns false if the web browser was
	 * definitely not opened. True returned means that as far as this code can be aware, the browser
	 * was launched, but there is no guarantee.
	 *
	 * @param url
	 * @throws java.io.IOException
	 * @throws java.net.URISyntaxException
	 */
	public static boolean openWebpage(URL url) throws IOException, URISyntaxException {
		return openWebpage(url.toURI());
	}

	/**
	 * The various types of message boxes available. This primarily decides the icon to use, but generally determines
	 * the way the dialog looks.
	 */
	public static enum MessageType {
		ERROR(JOptionPane.ERROR_MESSAGE),
		INFORMATION(JOptionPane.INFORMATION_MESSAGE),
		WARNING(JOptionPane.WARNING_MESSAGE),
		QUESTION(JOptionPane.QUESTION_MESSAGE),
		PLAIN(JOptionPane.PLAIN_MESSAGE);

		private final int joptionPaneType;
		private MessageType(int joptionPaneType) {
			this.joptionPaneType = joptionPaneType;
		}

		public int getJOptionPaneType() {
			return joptionPaneType;
		}
	}

	/**
	 * Provides a simple Yes/No confirm dialog. If the user clicks Yes, then true is returned. The type defaults
	 * to QUESTION. The actual creation of
	 * the dialog is done on the main UI thread using invokeAndWait (if necessary).
	 * Normally this method throws an InterruptedException
	 * or InvocationTargetException, but these are wrapped in a RuntimeException and rethrown.
	 * @param parent
	 * @param title
	 * @param message
	 * @return
	 */
	public static boolean confirm(Window parent, String title, String message) {
		return confirm(parent, title, message, MessageType.QUESTION);
	}

	/**
	 * Provides a simple Yes/No confirm dialog. If the user clicks Yes, then true is returned. The actual creation of
	 * the dialog is done on the main UI thread using invokeAndWait (if necessary).
	 * Normally this method throws an InterruptedException
	 * or InvocationTargetException, but these are wrapped in a RuntimeException and rethrown.
	 * @param parent
	 * @param title
	 * @param message
	 * @param type
	 * @return
	 */
	public static boolean confirm(Window parent, String title, String message, MessageType type) {
		MutableObject<Boolean> ret = new MutableObject<>();
		Runnable r = () -> {
			Object[] options = {"Yes", "No"};
			int n = JOptionPane.showOptionDialog(parent,
					message,
					title,
					JOptionPane.YES_NO_OPTION,
					type.getJOptionPaneType(),
					null, //do not use a custom Icon
					options, //the titles of buttons
					options[0]); //default button title
			ret.setObject(n == 0);
		};
		if(EventQueue.isDispatchThread()) {
			r.run();
		} else {
			try {
				EventQueue.invokeAndWait(r);
			} catch (InterruptedException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		}
		return ret.getObject();
	}

	/**
	 * Shows an alert message to the user, defaulting to the INFORMATION type. The dialog is shown on the UI thread.
	 * @param parent
	 * @param title
	 * @param message
	 */
	public static void alert(Window parent, String title, String message) {
		alert(parent, title, message, MessageType.INFORMATION);
	}

	/**
	 * Shows an alert message to the user. The dialog is shown on the UI thread.
	 * @param parent
	 * @param title
	 * @param message
	 * @param type
	 */
	public static void alert(Window parent, String title, String message, MessageType type) {
		EventQueue.invokeLater(() -> {
			JOptionPane.showMessageDialog(parent,
				message,
				title,
				type.getJOptionPaneType());
		});
	}

	/**
	 * Prompts the user for input. The call is blocking, and returns the entered input as a string.
	 * @param parent
	 * @param title
	 * @param message
	 * @param type
	 * @return
	 */
	public static String prompt(Window parent, String title, String message, MessageType type) {
		return JOptionPane.showInputDialog(parent, message, title, type.getJOptionPaneType());
	}

	/**
	 * Prompts the user for input. The call is blocking, and returns the entered input as a string. The type
	 * will be a QUESTION type.
	 * @param parent
	 * @param title
	 * @param message
	 * @return
	 */
	public static String prompt(Window parent, String title, String message) {
		return JOptionPane.showInputDialog(parent, message, title, MessageType.QUESTION.getJOptionPaneType());
	}


}
