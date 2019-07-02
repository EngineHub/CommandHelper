package com.laytonsmith.PureUtilities;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class wraps the JSch library, to make atomic operations easier to do.
 *
 *
 */
public final class SSHWrapper {

	private SSHWrapper() {
	}

	private static final Map<String, Session> SESSION_LIST = new HashMap<>();

	/**
	 * Sessions are cached, and should be closed after use.
	 */
	public static void closeSessions() {
		for(Session s : SESSION_LIST.values()) {
			s.disconnect();
		}
		SESSION_LIST.clear();
	}

	/**
	 * Copies a file from/to a remote host, via ssh. Currently, both paths being remote is not supported. A path can
	 * look like the following: user@remote[:port[:password]]:path/to/remote/file If the password is not specified, then
	 * public key authentication will be assumed. The port must be specified if the password is specified, but setting
	 * it to 0 will use the default (22), allowing it to be bypassed.
	 *
	 * @param from
	 * @param to
	 * @return false, if the file is being pushed to the remote, yet it was already the same, thus no changes were made,
	 * true otherwise
	 */
	public static boolean SCP(String from, String to) throws IOException {
		return SCP(from, to, null);
	}

	/**
	 * Copies a file from/to a remote host, via ssh. Currently, both paths being remote is not supported. A path can
	 * look like the following: user@remote[:port[:password]]:path/to/remote/file If the password is not specified, then
	 * public key authentication will be assumed. The port must be specified if the password is specified, but setting
	 * it to 0 will use the default (22), allowing it to be bypassed.
	 *
	 * @param from
	 * @param to
	 * @param privateKeyLocation If the private key is not in the default location, it can be provided here
	 * @return false, if the file is being pushed to the remote, yet it was already the same, thus no changes were made,
	 * true otherwise
	 */
	public static boolean SCP(String from, String to, String privateKeyLocation) throws IOException {
		if((from.contains("@") && to.contains("@")) || (!from.contains("@") && !to.contains("@"))) {
			throw new IOException("Paths cannot be both remote, or both local.");
		}
		//Now that we've handled the case where both paths are remote, we
		//can determine which one is the remote path, and proceed from there.
		String remote = to;
		if(from.contains("@")) {
			remote = from;
		}
		//Now, parse the remote connection for information
		Matcher m = Pattern.compile("(.+?)@(.+?)(?:\\:(.+?)(?:\\:(.+?))?)?\\:(.+)").matcher(remote);
		String syntaxErrorMsg = "Remote host connection must match the following syntax: user@host[:port[:password]]:path/to/file";
		if(m.find()) {
			String user = m.group(1);
			String host = m.group(2);
			String sport = m.group(3);
			int port = 22;
			final String password = m.group(4);
			String file = m.group(5);

			try {
				if(sport != null) {
					port = Integer.parseInt(sport);
				}
				if(port == 0) {
					port = 22;
				}
			} catch (NumberFormatException e) {
				//They may have been trying this:
				//user@host:password:/file/path
				//If that's the case, password will
				//be null, so let's give them a better error message.
				if(password == null) {
					throw new IOException(syntaxErrorMsg + " (It appears as though you may have been trying a password"
							+ " in place of the port. You may specify the port to be 0 if you want it to use the default,"
							+ " to bypass the port parameter.)");
				}
			}
			if(port < 1 || port > 65535) {
				throw new IOException("Port numbers must be between 1 and 65535");
			}
			try {
				JSch jsch = new JSch();
				Session sshSession = null;
				File knownHosts = new File(System.getProperty("user.home") + "/.ssh/known_hosts");
				if(!knownHosts.exists()) {
					if(password == null) {
						throw new IOException("No known hosts file exists at " + knownHosts.getAbsolutePath() + ", and no password was provided");
					}
				} else {
					jsch.setKnownHosts(knownHosts.getAbsolutePath());
				}
				if(password == null) {
					//We need to try public key authentication
					String idRsa = System.getProperty("user.home") + "/.ssh/id_rsa";
					if(privateKeyLocation != null) {
						idRsa = privateKeyLocation;
					}
					File privKey = new File(idRsa);
					if(privKey.exists()) {
						jsch.addIdentity(privKey.getAbsolutePath());
					} else {
						throw new IOException("No password provided, and no private key exists at " + privKey.getAbsolutePath());
					}
				}
				if(!SESSION_LIST.containsKey(user + host + port)) {
					sshSession = jsch.getSession(user, host, port);
					sshSession.setUserInfo(new UserInfo() {
						@Override
						public String getPassphrase() {
							//This may need to be made more granular later
							return password;
						}

						@Override
						public String getPassword() {
							return password;
						}

						@Override
						public boolean promptPassword(String message) {
							return true;
						}

						@Override
						public boolean promptPassphrase(String message) {
							return true;
						}

						@Override
						public boolean promptYesNo(String message) {
							StreamUtils.GetSystemOut().println(message + " (Automatically responding with 'Yes')");
							return true;
						}

						@Override
						public void showMessage(String message) {
							StreamUtils.GetSystemOut().println(message);
						}
					});
					//15 second timeout
					sshSession.connect(10 * 1500);
					SESSION_LIST.put(user + host + port, sshSession);
				} else {
					sshSession = SESSION_LIST.get(user + host + port);
				}
				// http://www.jcraft.com/jsch/examples/
				if(from.contains("@")) {
					//We are pulling a remote file here, so we need to use SCPFrom
					File localFile = new File(to);
					SCPFrom(file, localFile, sshSession);
				} else {
					//We are pushing a local file to a remote, so we need to use SCPTo
					File localFile = new File(from);
					return SCPTo(localFile, file, sshSession);
				}

				return true;

			} catch (JSchException | SftpException ex) {
				throw new IOException(ex);
			}
		} else {
			throw new IOException(syntaxErrorMsg);
		}
	}

	/**
	 *
	 * @param lfile
	 * @param rfile
	 * @param session
	 * @return true, if the file was uploaded, false if the file is the same on the remote, thus the file is not
	 * uploaded
	 * @throws JSchException
	 * @throws IOException
	 * @throws SftpException
	 */
	private static boolean SCPTo(File lfile, String rfile, Session session) throws JSchException, IOException, SftpException {
		ChannelSftp channel = null;
		try {
			channel = (ChannelSftp) session.openChannel("sftp");

			channel.connect();
			String[] folders = rfile.split("/");
			File frfile = new File(rfile);
			try {
				// Try to cd to the parent folder
				channel.cd(frfile.getParent());
			} catch (SftpException ex) {
				// But if that doesn't work, we need to create one or more of the folders, so we start at the beginning
				channel.cd("/");
				for(int i = 0; i < folders.length - 1; i++) {
					String folder = folders[i];
					if(folder.length() > 0) {
						try {
							channel.cd(folder);
						} catch (SftpException e) {
							channel.mkdir(folder);
							channel.cd(folder);
						}
					}
				}
			}
			String remote = getRemoteMD5(rfile, session);
			String local = getLocalMD5(lfile);
			if(!remote.equals(local)) {
				// only upload if it's different
				channel.put(new FileInputStream(lfile), frfile.getName());
				return true;
			}
			return false;
		} finally {
			if(channel != null) {
				channel.exit();
				channel.disconnect();
			}
		}
	}

	/**
	 * Returns the md5 sum of a local file
	 *
	 * @param localFile
	 * @return
	 * @throws IOException
	 */
	public static String getLocalMD5(File localFile) throws IOException {
		try {
			byte[] f = StreamUtils.GetBytes(new FileInputStream(localFile));
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(f);
			String hash = StringUtils.toHex(digest.digest()).toLowerCase();
			return hash;
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static String getRemoteMD5(String remoteFile, Session session) throws JSchException, IOException {
		ChannelExec channel = null;
		final StringBuilder sb = new StringBuilder();
		try {
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand("openssl md5 " + remoteFile);
			channel.setInputStream(null);
			channel.setOutputStream(null);
			channel.setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();

			byte[] tmp = new byte[1024];
			while(true) {
				while(in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if(i < 0) {
						break;
					}
					sb.append(new String(tmp, 0, i));
				}
				if(channel.isClosed()) {
					if(in.available() > 0) {
						continue;
					}
					if(channel.getExitStatus() != 0) {
						// Something went wrong, fail the comparison
						return "invalidMD5sum";
					}
					break;
				}
				try {
					Thread.sleep(1);
				} catch (Exception ee) {
				}
			}
		} finally {
			if(channel != null) {
				channel.disconnect();
			}
		}
		if("".equals(sb.toString())) {
			// Something went wrong, and so we're going to be forced to re-upload anyways. Using a nonsense value
			// ensures that the comparison will fail. This can happen if openssl is not installed on the remote,
			// or if the file simply doesn't exist.
			return "invalidMD5sum";
		}
		String opensslReturn = sb.toString();
		// This will be something like MD5(/path/to/file)= 798ffb41da648e405ca160fb547e3a09
		// and we just need 798ffb41da648e405ca160fb547e3a09
		opensslReturn = opensslReturn.replaceAll("MD5\\(.*\\)= ", "");
		return opensslReturn.replaceAll("\n|\r", "");
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

		while(true) {
			int c = checkAckFrom(in);
			if(c != 'C') {
				break;
			}

			// read '0644 '
			in.read(buf, 0, 5);

			long filesize = 0L;
			while(true) {
				if(in.read(buf, 0, 1) < 0) {
					// error
					break;
				}
				if(buf[0] == ' ') {
					break;
				}
				filesize = filesize * 10L + (long) (buf[0] - '0');
			}

			String file = null;
			for(int i = 0;; i++) {
				in.read(buf, i, 1);
				if(buf[i] == (byte) 0x0a) {
					file = new String(buf, 0, i);
					break;
				}
			}

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			String prefix = null;
			if(local.isDirectory()) {
				prefix = local.getPath() + File.separator;
			}
			// read a content of lfile
			FileOutputStream fos = new FileOutputStream(prefix == null ? local.getPath() : prefix + file);
			int foo;
			while(true) {
				if(buf.length < filesize) {
					foo = buf.length;
				} else {
					foo = (int) filesize;
				}
				foo = in.read(buf, 0, foo);
				if(foo < 0) {
					// error
					break;
				}
				fos.write(buf, 0, foo);
				filesize -= foo;
				if(filesize == 0L) {
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

		if(b == 1 || b == 2) {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while(c != '\n');
			if(b == 1) { // error
				throw new IOException(sb.toString());
			}
			if(b == 2) { // fatal error
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
		if(b == 0) {
			return b;
		}
		if(b == -1) {
			return b;
		}

		if(b == 1 || b == 2) {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while(c != '\n');
			if(b == 1) { // error
				throw new IOException(sb.toString());
			}
			if(b == 2) { // fatal error
				throw new IOException(sb.toString());
			}
		}
		return b;
	}

	/**
	 * Given an input stream, writes it out to a remote file system. The path given (to) must be a remote path.
	 *
	 * @param is
	 * @return true, if the file on the remote was changed, false, if it was already at this version, thus no changes
	 * were made
	 */
	public static boolean SCPWrite(InputStream is, String to) throws IOException {
		return SCPWrite(is, to, null);
	}

	/**
	 * Given an input stream, writes it out to a remote file system. The path given (to) must be a remote path.
	 *
	 * @param is
	 * @return true, if the file on the remote was changed, false, if it was already at this version, thus no changes
	 * were made
	 */
	public static boolean SCPWrite(InputStream is, String to, String idRsa) throws IOException {
		File temp = File.createTempFile("methodscript-temp-file", ".tmp");
		FileOutputStream fos = new FileOutputStream(temp);
		StreamUtils.Copy(is, fos);
		fos.close();
		try {
			return SCP(temp.getAbsolutePath(), to, idRsa);
		} finally {
			temp.delete();
			temp.deleteOnExit();
		}
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
