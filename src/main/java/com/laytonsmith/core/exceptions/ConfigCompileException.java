package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.util.Objects;

/**
 *
 *
 */
public class ConfigCompileException extends Exception {

	final String message;
	final int lineNum;
	final File file;
	final int col;
	final Target t;

	public ConfigCompileException(String message, Target t) {
		this(message, t, null);
	}

	public ConfigCompileException(String message, Target t, Throwable cause) {
		super(cause);
		this.message = message;
		this.lineNum = t.line();
		this.file = t.file();
		this.col = t.col();
		this.t = t;
	}

	/**
	 * This turns a ConfigRuntimeException into a compile time exception. Typically only used during optimization.
	 *
	 * @param e
	 */
	public ConfigCompileException(ConfigRuntimeException e) {
		this(e.getMessage(), e.getTarget(), e);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public String getLineNum() {
		return Integer.toString(lineNum);
	}

	public int getColumn() {
		return col;
	}

	public Target getTarget() {
		return t;
	}

	@Override
	public String toString() {
		if(lineNum != 0) {
			return "Configuration Compile Exception: " + message + " near line " + lineNum + ". Please "
					+ "check your code and try again. " + (file != null ? "(" + file.getAbsolutePath() + ")" : "");
		} else {
			return "Configuration Compile Exception: " + message + ". Please check your code and try again. "
					+ (file != null ? "(" + file.getAbsolutePath() + ")" : "");
		}
	}

	public File getFile() {
		return this.file;
	}

	public String getSimpleFile() {
		if(this.file != null) {
			return this.file.getName();
		} else {
			return null;
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 31 * hash + Objects.hashCode(this.message);
		hash = 31 * hash + this.lineNum;
		hash = 31 * hash + Objects.hashCode(this.file);
		hash = 31 * hash + this.col;
		hash = 31 * hash + Objects.hashCode(this.t);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final ConfigCompileException other = (ConfigCompileException) obj;
		if(!Objects.equals(this.message, other.message)) {
			return false;
		}
		if(this.lineNum != other.lineNum) {
			return false;
		}
		if(!Objects.equals(this.file, other.file)) {
			return false;
		}
		if(this.col != other.col) {
			return false;
		}
		if(!Objects.equals(this.t, other.t)) {
			return false;
		}
		return true;
	}

}
