package com.laytonsmith.PureUtilities.Common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Completely standalone generic functions live here.
 *
 */
public final class Misc {

	private Misc() {
	}

	public static String GetStacktrace(Throwable t) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		t.printStackTrace(printWriter);
		return result.toString();
	}
}
