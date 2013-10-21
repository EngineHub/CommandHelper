package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class wraps the JSch library, to make atomic operations easier to do.
 *
 * @author lsmith
 */
public class SSHWrapper {

	private SSHWrapper() {
	}

	/**
	 * Copies a file from/to a remote host, via ssh. Currently, both paths
	 * being remote is not supported. A path can look like the following:
	 * user@remote[:port[:password]]:path/to/remote/file If the password is
	 * not specified, then public key authentication will be assumed. The
	 * port must be specified if the password is specified, but setting it
	 * to 0 will use the default (22), allowing it to be bypassed.
	 * @param from
	 * @param to
	 */
	public static void SCP(String from, String to) throws IOException {
		if ((from.contains("@") && to.contains("@")) || (!from.contains("@") && !to.contains("@"))) {
			throw new IOException("Paths cannot be both remote, or both local.");
		}
		//Now that we've handled the case where both paths are remote, we
		//can determine which one is the remote path, and proceed from there.
		String remote = to;
		if (from.contains("@")) {
			remote = from;
		}
		//Now, parse the remote connection for information
		Matcher m = Pattern.compile("(.+?)@(.+?)(?:\\:(.+?)(?:\\:(.+?))?)?\\:(.+)").matcher(remote);
		String syntaxErrorMsg = "Remote host connection must match the following syntax: user@host[:port[:password]]:path/to/file";
		if (m.find()) {
			String user = m.group(1);
			String host = m.group(2);
			String sport = m.group(3);
			int port = 22;
			final String password = m.group(4);
			String file = m.group(5);

			try {
				if (sport != null) {
					port = Integer.parseInt(sport);
				}
				if (port == 0) {
					port = 22;
				}
			} catch (NumberFormatException e) {
				//They may have been trying this:
				//user@host:password:/file/path
				//If that's the case, password will
				//be null, so let's give them a better error message.				
				if (password == null) {
					throw new IOException(syntaxErrorMsg + " (It appears as though you may have been trying a password"
						+ " in place of the port. You may specify the port to be 0 if you want it to use the default,"
						+ " to bypass the port parameter.)");
				}
			}
			if (port < 1 || port > 65535) {
				throw new IOException("Port numbers must be between 1 and 65535");
			}
			try {
				JSch jsch = new JSch();
				Session sshSession = null;
				File known_hosts = new File(System.getProperty("user.home") + "/.ssh/known_hosts");
				if (!known_hosts.exists()) {
					throw new IOException("No known hosts file exists at " + known_hosts.getAbsolutePath());
				}
				jsch.setKnownHosts(known_hosts.getAbsolutePath());
				if (password == null) {
					//We need to try public key authentication					
					File privKey = new File(System.getProperty("user.home") + "/.ssh/id_rsa");
					if (privKey.exists()) {
						jsch.addIdentity(privKey.getAbsolutePath());
					} else {
						throw new IOException("No password provided, and no private key exists at " + privKey.getAbsolutePath());
					}
				}
				sshSession = jsch.getSession(user, host, port);
				sshSession.setUserInfo(new UserInfo() {
					public String getPassphrase() {
						//This may need to be made more granular later
						return password;
					}

					public String getPassword() {
						return password;
					}

					public boolean promptPassword(String message) {
						return true;
					}

					public boolean promptPassphrase(String message) {
						return true;
					}

					public boolean promptYesNo(String message) {
						System.out.println(message + " (Automatically responding with 'Yes')");
						return true;
					}

					public void showMessage(String message) {
						System.out.println(message);
					}
				});
				//10 second timeout
				sshSession.connect(10 * 1000);
				// http://www.jcraft.com/jsch/examples/
				if (from.contains("@")) {
					//We are pulling a remote file here, so we need to use SCPFrom
					File localFile = new File(to);
					SCPFrom(file, localFile, sshSession);
				} else {
					//We are pushing a local file to a remote, so we need to use SCPTo
					File localFile = new File(from);
					SCPTo(localFile, file, sshSession);
				}
				sshSession.disconnect();
			} catch (JSchException ex) {
				throw new IOException(ex);
			}
		} else {
			throw new IOException(syntaxErrorMsg);
		}
	}

	private static void SCPTo(File lfile, String rfile, Session session) throws JSchException, IOException {
		boolean ptimestamp = true;

		// exec 'scp -t rfile' remotely
		String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		checkAck(in);

		if (ptimestamp) {
			command = "T " + (lfile.lastModified() / 1000) + " 0";
			// The access time should be sent here,
			// but it is not accessible with JavaAPI ;-<
			command += (" " + (lfile.lastModified() / 1000) + " 0\n");
			out.write(command.getBytes());
			out.flush();
			checkAck(in);
		}

		// send "C0644 filesize filename", where filename should not include '/'
		long filesize = lfile.length();
		command = "C0644 " + filesize + " ";
		if (lfile.getPath().lastIndexOf('/') > 0) {
			command += lfile.getPath().substring(lfile.getPath().lastIndexOf('/') + 1);
		} else {
			command += lfile;
		}
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		checkAck(in);

		// send a content of lfile
		FileInputStream fis = new FileInputStream(lfile);
		byte[] buf = new byte[1024];
		while (true) {
			int len = fis.read(buf, 0, buf.length);
			if (len <= 0) {
				break;
			}
			out.write(buf, 0, len); //out.flush();
		}
		fis.close();
		fis = null;
		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		checkAck(in);
		out.close();

		channel.disconnect();
	}

	private static void SCPFrom(String remote, File local, Session session) throws IOException, JSchException {
		// exec 'scp -f rfile' remotely
		String command = "scp -f " + remote;
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		byte[] buf = new byte[1024];

		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();

		while (true) {
			int c = checkAckFrom(in);
			if (c != 'C') {
				break;
			}

			// read '0644 '
			in.read(buf, 0, 5);

			long filesize = 0L;
			while (true) {
				if (in.read(buf, 0, 1) < 0) {
					// error
					break;
				}
				if (buf[0] == ' ') {
					break;
				}
				filesize = filesize * 10L + (long) (buf[0] - '0');
			}

			String file = null;
			for (int i = 0;; i++) {
				in.read(buf, i, 1);
				if (buf[i] == (byte) 0x0a) {
					file = new String(buf, 0, i);
					break;
				}
			}


			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			String prefix = null;
			if (local.isDirectory()) {
				prefix = local.getPath() + File.separator;
			}
			// read a content of lfile
			FileOutputStream fos = new FileOutputStream(prefix == null ? local.getPath() : prefix + file);
			int foo;
			while (true) {
				if (buf.length < filesize) {
					foo = buf.length;
				} else {
					foo = (int) filesize;
				}
				foo = in.read(buf, 0, foo);
				if (foo < 0) {
					// error
					break;
				}
				fos.write(buf, 0, foo);
				filesize -= foo;
				if (filesize == 0L) {
					break;
				}
			}
			fos.close();
			fos = null;

			checkAckFrom(in);

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
		}

		channel.disconnect();
	}

	private static void checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				throw new IOException(sb.toString());
			}
			if (b == 2) { // fatal error
				throw new IOException(sb.toString());
			}
		}
	}

	static int checkAckFrom(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0) {
			return b;
		}
		if (b == -1) {
			return b;
		}

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				throw new IOException(sb.toString());
			}
			if (b == 2) { // fatal error
				throw new IOException(sb.toString());
			}
		}
		return b;
	}

	/**
	 * Given an input stream, writes it out to a remote file system. The
	 * path given (to) must be a remote path.
	 *
	 * @param is
	 */
	public static void SCPWrite(InputStream is, String to) throws IOException {
		File temp = File.createTempFile("methodscript-temp-file", ".tmp");
		FileOutputStream fos = new FileOutputStream(temp);
		StreamUtils.Copy(is, fos);
		fos.close();
		try {
			SCP(temp.getAbsolutePath(), to);
		} finally {
			temp.delete();
			temp.deleteOnExit();
		}
	}

	/**
	 * Returns an InputStream to a file on a remote file system.
	 *
	 * @param from
	 * @return
	 */
	public static InputStream SCPRead(String from) throws IOException {
		File temp = File.createTempFile("methodscript-temp-file", ".tmp");
		SCP(from, temp.getAbsolutePath());
		FileInputStream fis = new FileInputStream(temp);
		temp.deleteOnExit();
		return fis;
	}

	/**
	 * Writes some textual contents to a remote file.
	 *
	 * @param contents
	 * @param to
	 */
	public static void SCPWrite(String contents, String to) throws IOException {
		SCPWrite(StreamUtils.GetInputStream(contents), to);
	}

	public static String SCPReadString(String from) throws IOException {
		return StreamUtils.GetString(SCPRead(from));
	}
//	/**
//	 * Executes a command over ssh.
//	 */
//	public static void SSHExec(){
//		
//	}
}
