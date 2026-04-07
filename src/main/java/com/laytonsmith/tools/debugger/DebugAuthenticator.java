package com.laytonsmith.tools.debugger;

import com.laytonsmith.PureUtilities.Common.SSHKeyPair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the pre-DAP KEYPAIR authentication handshake for the debug server.
 *
 * <p>The handshake occurs on the raw TCP socket before any DAP traffic. The protocol is:
 * <ol>
 *   <li>Server sends: magic bytes ({@code MSDBG\1}), then a 32-byte random nonce</li>
 *   <li>Client sends: a digital signature of the nonce, plus its public key string</li>
 *   <li>Server verifies: (a) the public key is in the authorized keys file,
 *       (b) the signature is valid for the nonce</li>
 *   <li>Server sends: a result byte (1 = success, 0 = failure) and an error message on failure</li>
 * </ol>
 *
 * <p>All multi-byte integers are sent as big-endian (network byte order) via {@link DataInputStream}
 * and {@link DataOutputStream}.
 */
public class DebugAuthenticator {

	/**
	 * Protocol magic bytes sent by the server to identify this as a MethodScript debug connection.
	 * Format: "MSDBG" + protocol version byte (currently 1).
	 */
	public static final byte[] MAGIC = {'M', 'S', 'D', 'B', 'G', 0x01};

	/**
	 * Size of the random nonce in bytes.
	 */
	private static final int NONCE_SIZE = 32;

	/**
	 * Maximum allowed size for a signature or public key payload, to prevent denial of service
	 * from a malicious client sending enormous data.
	 */
	private static final int MAX_PAYLOAD_SIZE = 8192;

	private static final byte AUTH_SUCCESS = 0x01;
	private static final byte AUTH_FAILURE = 0x00;

	private final List<String> authorizedKeys;

	/**
	 * Creates an authenticator that verifies clients against the given authorized keys file.
	 * The file contains one SSH-format public key per line ({@code ssh-rsa <base64> <label>}).
	 * Empty lines and lines starting with {@code #} are ignored.
	 *
	 * @param authorizedKeysFile The file containing authorized public keys.
	 * @throws IOException If the file cannot be read.
	 * @throws IllegalArgumentException If the file does not exist.
	 */
	public DebugAuthenticator(File authorizedKeysFile) throws IOException {
		if(!authorizedKeysFile.exists()) {
			throw new IllegalArgumentException("Authorized keys file does not exist: "
					+ authorizedKeysFile.getAbsolutePath());
		}
		this.authorizedKeys = loadKeys(authorizedKeysFile);
		if(authorizedKeys.isEmpty()) {
			System.err.println("WARNING: Authorized keys file is empty: "
					+ authorizedKeysFile.getAbsolutePath());
			System.err.println("No clients will be able to authenticate.");
		}
	}

	/**
	 * Creates an authenticator with a pre-loaded list of authorized public key strings.
	 * Primarily for testing.
	 *
	 * @param authorizedKeys The list of SSH-format public key strings.
	 */
	DebugAuthenticator(List<String> authorizedKeys) {
		this.authorizedKeys = new ArrayList<>(authorizedKeys);
	}

	/**
	 * Performs the server-side authentication handshake on the given socket streams.
	 * This must be called before handing the streams to the DAP launcher.
	 *
	 * @param in The socket input stream.
	 * @param out The socket output stream.
	 * @return {@code true} if the client authenticated successfully.
	 * @throws IOException If a communication error occurs.
	 */
	public boolean authenticate(InputStream in, OutputStream out) throws IOException {
		DataInputStream dataIn = new DataInputStream(in);
		DataOutputStream dataOut = new DataOutputStream(out);

		// Step 1: Generate and send nonce
		byte[] nonce = new byte[NONCE_SIZE];
		new SecureRandom().nextBytes(nonce);

		dataOut.write(MAGIC);
		dataOut.writeInt(nonce.length);
		dataOut.write(nonce);
		dataOut.flush();

		// Step 2: Read client's signature and public key
		int sigLen = dataIn.readInt();
		if(sigLen <= 0 || sigLen > MAX_PAYLOAD_SIZE) {
			sendFailure(dataOut, "Invalid signature length");
			return false;
		}
		byte[] signature = new byte[sigLen];
		dataIn.readFully(signature);

		int keyLen = dataIn.readInt();
		if(keyLen <= 0 || keyLen > MAX_PAYLOAD_SIZE) {
			sendFailure(dataOut, "Invalid public key length");
			return false;
		}
		byte[] keyBytes = new byte[keyLen];
		dataIn.readFully(keyBytes);
		String clientPublicKey = new String(keyBytes, StandardCharsets.UTF_8).trim();

		// Step 3: Verify the public key is authorized
		if(!isAuthorized(clientPublicKey)) {
			sendFailure(dataOut, "Public key is not authorized");
			return false;
		}

		// Step 4: Verify the signature
		try {
			SSHKeyPair keyPair = new SSHKeyPair(null, clientPublicKey);
			if(!keyPair.verify(nonce, signature)) {
				sendFailure(dataOut, "Signature verification failed");
				return false;
			}
		} catch(RuntimeException e) {
			sendFailure(dataOut, "Signature verification failed: " + e.getMessage());
			return false;
		}

		// Step 5: Send success
		dataOut.writeByte(AUTH_SUCCESS);
		dataOut.flush();
		return true;
	}

	private void sendFailure(DataOutputStream dataOut, String message) throws IOException {
		dataOut.writeByte(AUTH_FAILURE);
		byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
		dataOut.writeInt(msgBytes.length);
		dataOut.write(msgBytes);
		dataOut.flush();
	}

	private boolean isAuthorized(String clientPublicKey) {
		// Compare the key type and key data, ignoring the label (third field)
		String clientKeyData = extractKeyData(clientPublicKey);
		if(clientKeyData == null) {
			return false;
		}
		for(String authorizedKey : authorizedKeys) {
			String authorizedKeyData = extractKeyData(authorizedKey);
			if(clientKeyData.equals(authorizedKeyData)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts the key type and base64 key data from an SSH public key string,
	 * ignoring the label. Returns {@code "type <base64>"} or {@code null} if invalid.
	 */
	private static String extractKeyData(String sshPublicKey) {
		if(sshPublicKey == null) {
			return null;
		}
		String[] parts = sshPublicKey.trim().split("\\s+");
		if(parts.length < 2) {
			return null;
		}
		return parts[0] + " " + parts[1];
	}

	private static List<String> loadKeys(File keyFile) throws IOException {
		byte[] raw = Files.readAllBytes(keyFile.toPath());
		boolean isUtf16 = raw.length >= 2
				&& ((raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xFE
				|| (raw[0] & 0xFF) == 0xFE && (raw[1] & 0xFF) == 0xFF);
		String content = new String(raw,
				isUtf16 ? StandardCharsets.UTF_16 : StandardCharsets.UTF_8);
		List<String> keys = new ArrayList<>();
		for(String line : content.split("\\r?\\n")) {
			line = line.trim();
			if(!line.isEmpty() && !line.startsWith("#")) {
				keys.add(line);
			}
		}
		return keys;
	}
}
