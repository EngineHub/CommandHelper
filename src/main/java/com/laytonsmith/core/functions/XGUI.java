package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 */
@core
public class XGUI {

	public static String docs() {
		return "This provides extremely limited gui control functions. This entire class is experimental, and will probably be removed at"
				+ " some point.";
	}

	private static Map<Integer, Window> windows = new HashMap<>();
	private static final AtomicInteger WINDOW_IDS = new AtomicInteger(0);

	static {
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			@Override
			public void run() {
				for(Window w : windows.values()) {
					w.dispose();
				}
				windows.clear();
			}
		});
	}

	@api
	@hide("experimental")
	@noboilerplate
	public static class x_create_window extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			JFrame frame = new JFrame();
			int id = WINDOW_IDS.incrementAndGet();
			String title = "";
			int width = 300;
			int height = 300;
			if(args.length > 0) {
				title = args[0].val();
			}
			if(args.length > 1) {
				width = Static.getInt32(args[1], t);
			}
			if(args.length > 2) {
				height = Static.getInt32(args[2], t);
			}
			frame.setTitle(title);
			frame.setSize(width, height);
			frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			JPanel panel = new JPanel();
			frame.add(panel);
			windows.put(id, frame);
			return new CInt(id, t);
		}

		@Override
		public String getName() {
			return "x_create_window";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2, 3};
		}

		@Override
		public String docs() {
			return "int {[title], [width], [height]} Creates a window with the specified title, width and height. All are optional"
					+ " parameters, and they default to reasonable defaults. The id, which represents the window can be used for "
					+ " manipulating the window in future calls. The contents of the window will be blank. The window will initially"
					+ " not be visible. You'll need to call x_show_window to make it visible.";
		}

		@Override
		public Version since() {
			return MSVersion.V0_0_0;
		}

	}

	@api
	@hide("Expreimental")
	@noboilerplate
	public static class x_show_window extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			boolean show = true;
			if(args.length > 1) {
				show = Static.getBoolean(args[1], t);
			}
			Window w = windows.get(id);
			w.setVisible(show);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_show_window";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {windowID, [show]} Shows (or hides, if \"show\" is false) the specified window.";
		}

		@Override
		public Version since() {
			return MSVersion.V0_0_0;
		}

	}

	@api
	@hide("experimental")
	@noboilerplate
	public static class x_set_window_pixel extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int windowID = Static.getInt32(args[0], t);
			int x = Static.getInt32(args[1], t);
			int y = Static.getInt32(args[2], t);
			int red = Static.getInt32(args[3], t);
			int green = Static.getInt32(args[4], t);
			int blue = Static.getInt32(args[5], t);
			Window w = windows.get(windowID);
			while(true) {
				try {
					JPanel panel = (JPanel) w.findComponentAt(x, y);
					panel.getGraphics().setColor(new Color(red, green, blue));
					panel.getGraphics().draw3DRect(x, y, 1, 1, true);
					return CVoid.VOID;
				} catch (ClassCastException ex) {
					//?
					return CVoid.VOID;
				}
			}
		}

		@Override
		public String getName() {
			return "x_set_window_pixel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{6};
		}

		@Override
		public String docs() {
			return "void {windowID, x, y, red, green, blue} Sets a pixel in the specified window. x and y are relative to the top"
					+ " left of the window.";
		}

		@Override
		public Version since() {
			return MSVersion.V0_0_0;
		}

	}

	@api
	@hide("experimental")
	@noboilerplate
	public static class x_launch_browser extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String url = args[0].val();
			try {
				if(Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(url));
				}
			} catch (URISyntaxException ex1) {
				throw new CREFormatException(ex1.getMessage(), t);
			} catch (IOException ex) {
				try {
					// Last ditch effort
					Runtime rt = Runtime.getRuntime();
					switch(OSUtils.GetOS()) {
						case WINDOWS:
							rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
							break;
						case MAC:
							rt.exec("open " + url);
							break;
						case LINUX:
						default:
							// Try the other OSes as linux
							String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
								"netscape", "opera", "links", "lynx"};

							StringBuilder cmd = new StringBuilder();
							for(int i = 0; i < browsers.length; i++) {
								cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
							}

							rt.exec(new String[]{"sh", "-c", cmd.toString()});
							break;
					}
				} catch (IOException ex1) {
					throw new CREIOException(ex1.getMessage(), t, ex1);
				}
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_launch_browser";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {url} Launches the desktop's default browser with the given url. On headless systems, this"
					+ " will throw an exception.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

}
