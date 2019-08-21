package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CRE.CREUnsupportedOperationException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author cailin
 */
@SuppressWarnings("UseSpecificCatch")
public class Clipboard {

	public static String docs() {
		return "Provides functions for managing the system clipboard";
	}

	private static java.awt.datatransfer.Clipboard clipboard;

	static {
		try {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		} catch (Throwable ex) {
			clipboard = null;
		}
	}

	@api
	@noboilerplate
	public static class get_clipboard extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREInsufficientPermissionException.class,
				CREUnsupportedOperationException.class};
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
			Cmdline.requireCmdlineMode(environment, this, t);
			if(clipboard == null) {
				throw new CREUnsupportedOperationException(
						"Clipboard functions are not supported on this platform.", t);
			}
			Transferable tr = clipboard.getContents(null);
			if(tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				try {
					String data = (String) tr.getTransferData(DataFlavor.stringFlavor);
					return new CString(data, t);
				} catch (UnsupportedFlavorException ex) {
					// Can't happen
					throw new RuntimeException(ex);
				} catch (IOException ex) {
					throw new CREIOException(ex.getMessage(), t, ex);
				}
			} else {
				throw new CREFormatException("Clipboard value does not support parsing as text", t);
			}
		}

		@Override
		public String getName() {
			return "get_clipboard";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "string {[flavor]} Returns the contents of the system clipboard. Can only be used in cmdline mode."
					+ " Flavor defaults to null, and is currently unused. Only strings are currently supported."
					+ " If a string version of the clipboard contents cannot be parsed, a FormatException is thrown."
					+ " If the platform doesn't support clipboard operations, an UnsupportedOperationException is"
					+ " thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	@noboilerplate
	public static class set_clipboard extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInsufficientPermissionException.class, CREUnsupportedOperationException.class};
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
			Cmdline.requireCmdlineMode(environment, this, t);
			if(clipboard == null) {
				throw new CREUnsupportedOperationException(
						"Clipboard functions are not supported on this platform.", t);
			}
			String data = args[0].val();
			StringSelection s = new StringSelection(data);
			clipboard.setContents(s, s);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "set_clipboard";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {value, [flavor]} Sets the contents of the system clipboard, to the given value. Can only be"
					+ " used in cmdline mode. Flavor defaults to null, and is currently unused. Only strings are"
					+ " currently supported. If the platform doesn't support clipboard operations, an"
					+ " UnsupportedOperationException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}
}
