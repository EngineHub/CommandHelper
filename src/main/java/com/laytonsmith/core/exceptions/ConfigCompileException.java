package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.util.Objects;

/**
 * This {@link Exception} can be thrown when a problem occurs during compilation.
 */
@SuppressWarnings("serial")
public class ConfigCompileException extends AbstractCompileException implements Comparable<ConfigCompileException> {

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
		return this.message;
	}

	public String getLineNum() {
		return Integer.toString(this.lineNum);
	}

	public int getColumn() {
		return this.col;
	}

	public Target getTarget() {
		return this.t;
	}

	@Override
	public String toString() {
		if(this.lineNum != 0) {
			return "Configuration Compile Exception: " + this.message + " near line " + lineNum + ". Please "
					+ "check your code and try again. " + (file != null ? "(" + file.getAbsolutePath() + ")" : "");
		} else {
			return "Configuration Compile Exception: " + this.message + ". Please check your code and try again. "
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

	/**
	 * This implementation can be used to sort {@link ConfigCompileException}s first on {@link Target} and then on
	 * exception message.
	 */
	@Override
	public int compareTo(ConfigCompileException cre) {
		int ret = this.t.compareTo(cre.t);
		if(ret != 0) {
			return ret;
		}
		return this.message.compareTo(cre.message);
	}

}
