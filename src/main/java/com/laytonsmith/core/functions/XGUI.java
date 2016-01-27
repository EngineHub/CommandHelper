
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.awt.Color;
import java.awt.Window;
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
	public static String docs(){
		return "This provides extremely limited gui control functions. This entire class is experimental, and will probably be removed at"
				+ " some point.";
	}
	
	private static Map<Integer, Window> windows = new HashMap<>();
	private static AtomicInteger windowIDs = new AtomicInteger(0);
	
	static {
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			@Override
			public void run() {
				for(Window w : windows.values()){
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			JFrame frame = new JFrame();
			int id = windowIDs.incrementAndGet();
			String title = "";
			int width = 300;
			int height = 300;
			if(args.length > 0){
				title = args[0].val();
			}
			if(args.length > 1){
				width = Static.getInt32(args[1], t);
			}
			if(args.length > 2){
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
			return CHVersion.V0_0_0;
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int id = Static.getInt32(args[0], t);
			boolean show = true;
			if(args.length > 1){
				show = Static.getBoolean(args[1]);
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
			return CHVersion.V0_0_0;
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			int windowID = Static.getInt32(args[0], t);
			int x = Static.getInt32(args[1], t);
			int y = Static.getInt32(args[2], t);
			int red = Static.getInt32(args[3], t);
			int green = Static.getInt32(args[4], t);
			int blue = Static.getInt32(args[5], t);
			Window w = windows.get(windowID);
			while(true){
				try{
					JPanel panel = (JPanel)w.findComponentAt(x, y);
					panel.getGraphics().setColor(new Color(red, green, blue));
					panel.getGraphics().draw3DRect(x, y, 1, 1, true);
					return CVoid.VOID;
				} catch(ClassCastException ex){
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
			return CHVersion.V0_0_0;
		}
		
	}
	
}
