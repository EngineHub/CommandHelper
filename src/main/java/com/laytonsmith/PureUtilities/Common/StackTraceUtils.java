package com.laytonsmith.PureUtilities.Common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 *
 */
public final class StackTraceUtils {

	private StackTraceUtils() {
	}

	/**
	 * Gets the stack trace of an exception, as if it were being printed to the console.
	 * @param t The Throwable instance. May have a causedBy reason.
	 * @return
	 */
	public static String GetStacktrace(Throwable t) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		boolean first = true;
		Throwable tt = t;
		do {
			if(!first) {
				printWriter.append("Caused by: ");
			}
			first = false;
			tt.printStackTrace(printWriter);
		} while((tt = tt.getCause()) != null);
		return result.toString();
	}

	/**
	 * Returns the name of the class that called the method that is using this code. I.e., if
	 * the method a in class A calls the method b in class B, and the method b in class B
	 * calls getCallingClass(), then a reference to class A would be returned.
	 * @return
	 */
	public static Class<?> getCallingClass() {
		try {
			// This is the class that called us. Calls may bounce around that class,
			// but we ultimately want to return that class that called the original
			// method within this class.
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			String doNotReturn = st[2].getClassName();
			for(int i = 3; i < st.length; i++) {
				if(!st[i].getClassName().equals(doNotReturn)) {
					return Class.forName(st[i].getClassName());
				}
			}
			// The only way this can get here is if this were the bootstrap class.
			// I doubt the JVM is calling us first thing, so just throw an Error.
			throw new Error();
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the name of the current method. This method is lambda aware, that is, if a code inside a lambda
	 * calls this method, it continues walking up the chain until it finds the actual full method that's calling
	 * this method.
	 * @return
	 */
	public static String currentMethod() {
		// We unfortunately need to duplicate code here, since we can't add to the stack.
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String found = null;
		int trace = 1;
		while(found == null) {
			trace++;
			String m = st[trace].getMethodName();
			// If this is a lambda, we want to go up one more.
			if(!m.startsWith("lambda$")) {
				found = m;
			}
		}
		return found;
	}

	/**
	 * Returns the name of the current method. This method is lambda aware, that is, if a code inside a lambda
	 * calls this method, it continues walking up the chain until it finds the actual full method that's calling
	 * this method, unless includeLambdas is false.
	 * @param includeLambdas
	 * @return
	 */
	public static String currentMethod(boolean includeLambdas) {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String found = null;
		int trace = 1;
		while(found == null) {
			trace++;
			String m = st[trace].getMethodName();
			// If this is a lambda, we want to go up one more.
			if(includeLambdas || !m.startsWith("lambda$")) {
				found = m;
			}
		}
		return found;
	}
}
