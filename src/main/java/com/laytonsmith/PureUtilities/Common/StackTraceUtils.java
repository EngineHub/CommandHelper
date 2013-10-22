package com.laytonsmith.PureUtilities.Common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author lsmith
 */
public class StackTraceUtils {
	
	private StackTraceUtils(){}
    
    public static String GetStacktrace(Throwable t){
        final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		boolean first = true;
		Throwable tt = t;
		do {
			if(!first){
				printWriter.append("Caused by: ");
			}
			first = false;
			tt.printStackTrace(printWriter);
		} while((tt = tt.getCause()) != null);
        return result.toString();
    }
}
