package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;

/**
 * Contains utilities to execute an external process and retrieve various results from it.
 *
 */
public class CommandExecutor {

	/**
	 * If you're in a hurry, and all you want is to get the output of System.out from a process started with a string,
	 * this will do it for you.
	 *
	 * @param command
	 * @return
	 * @throws java.lang.InterruptedException
	 * @throws java.io.IOException
	 */
	public static String Execute(String command) throws InterruptedException, IOException {
		return Execute(new File("."), command);
	}
	/**
	 * If you're in a hurry, and all you want is to get the output of System.out from a process started with a string,
	 * this will do it for you.
	 *
	 * @param workingDir
	 * @param command
	 * @return
	 * @throws java.lang.InterruptedException
	 * @throws java.io.IOException
	 */
	public static String Execute(File workingDir, String command) throws InterruptedException, IOException {
		return Execute(StringToArray(command));
	}

	/**
	 * If you're in a hurry, and all you want is to get the output of System.out from a process started with a list of
	 * arguments, this will do it for you.
	 *
	 * @param args
	 * @return
	 * @throws java.lang.InterruptedException
	 * @throws java.io.IOException
	 */
	public static String Execute(String... args) throws InterruptedException, IOException {
		return Execute(new File("."), args);
	}

	/**
	 * If you're in a hurry, and all you want is to get the output of System.out from a process started with a list of
	 * arguments, this will do it for you.
	 *
	 * @param workingDir
	 * @param args
	 * @return
	 * @throws java.lang.InterruptedException
	 * @throws java.io.IOException
	 */
	public static String Execute(File workingDir, String... args) throws InterruptedException, IOException {
		final List<Byte> output = new ArrayList<>();
		CommandExecutor c = new CommandExecutor(args);
		c.setWorkingDir(workingDir);
		OutputStream os = new BufferedOutputStream(new OutputStream() {

			@Override
			public void write(int next) throws IOException {
				output.add((byte) next);
			}
		});
		c.setSystemOut(os);
		c.start();
		c.waitFor();
		byte[] bytes = new byte[output.size()];
		for(int i = 0; i < output.size(); i++) {
			bytes[i] = output.get(i);
		}

		return new String(bytes, "UTF-8");
	}

	private static String[] StringToArray(String s) {
		List<String> argList = StringUtils.ArgParser(s);
		String[] args = new String[argList.size()];
		args = argList.toArray(args);
		return args;
	}

	public CommandExecutor(String command) {
		this(StringToArray(command));
	}

	private String[] args;
	private Process process;
	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private File workingDir = null;
	private Thread outThread;
	private Thread errThread;
	private Thread inThread;
	private boolean inheritStandards = false;

	public CommandExecutor(String... command) {
		args = command;
	}

	/**
	 * Starts this CommandExecutor. Afterwards, you can call {@link #waitFor()} to wait until the process has finished.
	 *
	 * @return
	 * @throws IOException
	 */
	public CommandExecutor start() throws IOException {
		ProcessBuilder builder = new ProcessBuilder(args);
		if(inheritStandards) {
			builder.redirectError(ProcessBuilder.Redirect.INHERIT);
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
		}
		builder.directory(workingDir);
		process = builder.start();
		outThread = new Thread(() -> {
			int ret;
			try(InputStream bout = new BufferedInputStream(process.getInputStream());) {
				while((ret = bout.read()) != -1) {
					if(out != null) {
						out.write(ret);
					}
				}
				if(out != null) {
					out.flush();
				}
			} catch (IOException ex) {
				Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch (IOException ex) {
						Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}, Arrays.toString(args) + "-output");
		outThread.start();
		errThread = new Thread(() -> {
			int ret;
			try(InputStream berr = new BufferedInputStream(process.getErrorStream());) {
				while((ret = berr.read()) != -1) {
					if(err != null) {
						err.write(ret);
					}
				}
				if(err != null) {
					err.flush();
				}
			} catch (IOException ex) {
				Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				if(err != null) {
					try {
						err.close();
					} catch (IOException ex) {
						Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}, Arrays.toString(args) + "-error");
		errThread.start();
		if(in != null) {
			inThread = new Thread(() -> {
				OutputStream bin = new BufferedOutputStream(process.getOutputStream());
				try(BufferedReader br = new BufferedReader(new InputStreamReader(in));) {
					// This is necessary, because InputStream.read() blocks forever, and
					// is not interruptable, so to prevent memory leaks, we must unfortunately
					// busy wait.
					while(!inThread.isInterrupted()) {
						if(br.ready()) {
							int ret;
							if((ret = br.read()) != -1) {
								bin.write(ret);
							}
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException ex) {
							// Will pick up in the while loop.
							inThread.interrupt();
						}
					}
				} catch (IOException ex) {
					Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}, Arrays.toString(args) + "-input");
			inThread.start();
		}
		return this;
	}

	/**
	 * Sets the InputStream for the program. If passing in {@code System.in}, it's important to note that you should
	 * wrap this in a {@link CloseShieldInputStream}, as the code in general will attempt to close all streams after the
	 * program is done.
	 *
	 * @param input
	 * @return
	 */
	public CommandExecutor setSystemIn(InputStream input) {
		if(process != null) {
			throw new RuntimeException("Process is already started! Cannot set a new InputStream!");
		}
		in = input;
		return this;
	}

	/**
	 * Sets the standard OutputStream for the program. If passing in {@code System.out} or {@code System.err},
	 * it's important to note that you
	 * should wrap this in a {@link CloseShieldOutputStream}, as the code in general will attempt to close all streams
	 * after the program is done.
	 * @param output The output stream
	 * @return {@code this} for chaining.
	 */
	public CommandExecutor setSystemOut(OutputStream output) {
		if(process != null) {
			throw new RuntimeException("Process is already started! Cannot set a new InputStream!");
		}
		out = output;
		return this;
	}

	/**
	 * Sets the error OutputStream for the program. If passing in {@code System.err} or {@code System.out},
	 * it's important to note that you
	 * should wrap this in a {@link CloseShieldOutputStream}, as the code in general will attempt to close all streams
	 * after the program is done.
	 * @param error The output stream
	 * @return {@code this} for chaining
	 */
	public CommandExecutor setSystemErr(OutputStream error) {
		if(process != null) {
			throw new RuntimeException("Process is already started! Cannot set a new OutputStream!");
		}
		err = error;
		return this;
	}

	public InputStream getSystemIn() {
		return in;
	}

	public OutputStream getSystemOut() {
		return out;
	}

	public OutputStream getSystemErr() {
		return err;
	}

	public CommandExecutor setWorkingDir(File workingDir) {
		if(process != null) {
			throw new RuntimeException("Process is already started! Cannot set a new working directory!");
		}
		this.workingDir = workingDir;
		return this;
	}

	/**
	 * Blocks until the underlying process has finished. If the process has already finished, the method will return
	 * immediately.
	 *
	 * @return The process's exit code
	 * @throws InterruptedException
	 */
	public int waitFor() throws InterruptedException {
		int ret = process.waitFor();
		if(out != null) {
			try {
				out.flush();
			} catch (IOException ex) {
				Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if(err != null) {
			try {
				err.flush();
			} catch (IOException ex) {
				Logger.getLogger(CommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		outThread.join();
		errThread.join();
		if(inThread != null) {
			inThread.interrupt();
		}
		return ret;
	}

	/**
	 * Sets the inputs and outputs to be System.in, StreamUtils.GetSystemOut(), and StreamUtils.GetSystemErr().
	 * <p>
	 * System.in is wrapped in a {@link CloseShieldInputStream}, so it won't be accidentally closed, and likewise
	 * out and err are wrapped in {@link CloseShieldOutputStream}.
	 *
	 * @return
	 */
	public CommandExecutor setSystemInputsAndOutputs() {
		setSystemOut(new CloseShieldOutputStream(StreamUtils.GetSystemOut()));
		setSystemErr(new CloseShieldOutputStream(StreamUtils.GetSystemErr()));
		setSystemIn(new CloseShieldInputStream(System.in));
		inheritStandards = true;
		return this;
	}

}
