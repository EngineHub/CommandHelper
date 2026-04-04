package com.laytonsmith.PureUtilities.Common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;

/**
 * Given a public/private key pair, this class uses RSA to encrypt/decrypt data.
 *
 * <p>Keys are stored in standard formats:
 * <ul>
 *   <li>Private key: PKCS#8 PEM ({@code -----BEGIN PRIVATE KEY-----})</li>
 *   <li>Public key: OpenSSH format ({@code ssh-rsa <base64> <label>})</li>
 * </ul>
 * These formats are interoperable with OpenSSH, Node.js crypto, and other standard tools.
 */
public class RSAEncrypt {

	private static final String ALGORITHM = "RSA";
	private static final int KEY_SIZE = 2048;

	/**
	 * Generates a new RSA key pair.
	 *
	 * @param label The label that will be associated with the public key
	 *     (e.g. "user@host")
	 * @return A new RSAEncrypt instance with both keys
	 */
	public static RSAEncrypt generateKey(String label) {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		} catch(NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
		keyGen.initialize(KEY_SIZE);
		KeyPair key = keyGen.generateKeyPair();
		RSAEncrypt enc = new RSAEncrypt(
				privateKeyToPem(key.getPrivate()),
				publicKeyToSsh(key.getPublic(), label));
		return enc;
	}

	/**
	 * Encodes a private key as a PKCS#8 PEM string.
	 */
	private static String privateKeyToPem(PrivateKey key) {
		String base64 = Base64.encodeBase64String(key.getEncoded());
		StringBuilder sb = new StringBuilder();
		sb.append("-----BEGIN PRIVATE KEY-----");
		for(int i = 0; i < base64.length(); i++) {
			if(i % 64 == 0) {
				sb.append(StringUtils.nl());
			}
			sb.append(base64.charAt(i));
		}
		sb.append(StringUtils.nl()).append("-----END PRIVATE KEY-----").append(StringUtils.nl());
		return sb.toString();
	}

	/**
	 * Encodes a public key in OpenSSH format: {@code ssh-rsa <base64> <label>}.
	 * The base64 payload uses the SSH wire format (RFC 4253):
	 * string "ssh-rsa", mpint e, mpint n.
	 */
	private static String publicKeyToSsh(PublicKey key, String label) {
		Objects.requireNonNull(label);
		RSAPublicKey rsaKey = (RSAPublicKey) key;
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(buf);
			byte[] typeBytes = "ssh-rsa".getBytes("UTF-8");
			dos.writeInt(typeBytes.length);
			dos.write(typeBytes);
			byte[] eBytes = rsaKey.getPublicExponent().toByteArray();
			dos.writeInt(eBytes.length);
			dos.write(eBytes);
			byte[] nBytes = rsaKey.getModulus().toByteArray();
			dos.writeInt(nBytes.length);
			dos.write(nBytes);
			dos.flush();
			return "ssh-rsa " + Base64.encodeBase64String(buf.toByteArray()) + " " + label;
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Parses an OpenSSH public key string and returns the PublicKey.
	 * Reads the SSH wire format: string "ssh-rsa", mpint e, mpint n.
	 */
	private static PublicKey sshToPublicKey(String base64Part) {
		byte[] decoded = Base64.decodeBase64(base64Part);
		ByteBuffer bb = ByteBuffer.wrap(decoded);
		// Read key type string
		int typeLen = bb.getInt();
		byte[] typeBytes = new byte[typeLen];
		bb.get(typeBytes);
		// Read exponent
		int eLen = bb.getInt();
		byte[] eBytes = new byte[eLen];
		bb.get(eBytes);
		java.math.BigInteger e = new java.math.BigInteger(eBytes);
		// Read modulus
		int nLen = bb.getInt();
		byte[] nBytes = new byte[nLen];
		bb.get(nBytes);
		java.math.BigInteger n = new java.math.BigInteger(nBytes);
		try {
			java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(n, e);
			KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
			return kf.generatePublic(spec);
		} catch(NoSuchAlgorithmException | InvalidKeySpecException ex) {
			throw new RuntimeException(ex);
		}
	}

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String label;

	/**
	 * Creates a new RSAEncrypt object from PEM/SSH key strings. Only one key needs to be
	 * provided. If so, only the methods for that key will work.
	 *
	 * <p>The private key should be PKCS#8 PEM format ({@code -----BEGIN PRIVATE KEY-----}).
	 * The public key should be OpenSSH format ({@code ssh-rsa <base64> <label>}).
	 *
	 * @param privateKey The private key PEM string, or null
	 * @param publicKey The public key SSH string, or null
	 * @throws IllegalArgumentException If a key string cannot be parsed
	 */
	public RSAEncrypt(String privateKey, String publicKey) throws IllegalArgumentException {
		if(privateKey != null) {
			privateKey = privateKey.replaceAll("\r", "");
			privateKey = privateKey.replaceAll("\n", "");
			privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", "");
			privateKey = privateKey.replace("-----END PRIVATE KEY-----", "");
			// Also strip PKCS#1 headers for compatibility with ssh-keygen keys
			privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "");
			privateKey = privateKey.replace("-----END RSA PRIVATE KEY-----", "");
			try {
				byte[] keyBytes = Base64.decodeBase64(privateKey);
				PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
				KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
				this.privateKey = kf.generatePrivate(spec);
			} catch(NoSuchAlgorithmException | InvalidKeySpecException ex) {
				throw new IllegalArgumentException("Failed to parse private key", ex);
			}
		}

		if(publicKey != null) {
			String[] split = publicKey.trim().split("\\s+");
			if(split.length < 2) {
				throw new IllegalArgumentException("Invalid public key format.");
			}
			if(!"ssh-rsa".equals(split[0])) {
				throw new IllegalArgumentException(
						"Invalid public key type. Expecting ssh-rsa, but found \""
						+ split[0] + "\"");
			}
			this.label = split.length >= 3 ? split[2] : "";
			this.publicKey = sshToPublicKey(split[1]);
		}
	}

	/**
	 * Encrypts the data with the public key, which can be decrypted with the private key.
	 */
	public byte[] encryptWithPublic(byte[] data) {
		Objects.requireNonNull(publicKey);
		return crypt(data, publicKey, Cipher.ENCRYPT_MODE);
	}

	/**
	 * Encrypts the data with the private key, which can be decrypted with the public key.
	 */
	public byte[] encryptWithPrivate(byte[] data) throws InvalidKeyException {
		Objects.requireNonNull(privateKey);
		return crypt(data, privateKey, Cipher.ENCRYPT_MODE);
	}

	/**
	 * Decrypts the data with the public key, which will have been encrypted with the private key.
	 */
	public byte[] decryptWithPublic(byte[] data) {
		Objects.requireNonNull(publicKey);
		return crypt(data, publicKey, Cipher.DECRYPT_MODE);
	}

	/**
	 * Decrypts the data with the private key, which will have been encrypted with the public key.
	 */
	public byte[] decryptWithPrivate(byte[] data) {
		Objects.requireNonNull(privateKey);
		return crypt(data, privateKey, Cipher.DECRYPT_MODE);
	}

	private byte[] crypt(byte[] data, Key key, int cryptMode) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(cryptMode, key);
			return cipher.doFinal(data);
		} catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the private key as a PKCS#8 PEM string.
	 */
	public String getPrivateKey() {
		return privateKeyToPem(privateKey);
	}

	/**
	 * Returns the public key as an OpenSSH format string.
	 */
	public String getPublicKey() {
		return publicKeyToSsh(publicKey, label);
	}

	/**
	 * Returns the label on the public key.
	 */
	public String getLabel() {
		return label;
	}

}
