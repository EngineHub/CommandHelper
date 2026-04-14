package com.laytonsmith.PureUtilities.Common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import org.apache.commons.codec.binary.Base64;

/**
 * Handles SSH key pair operations for multiple key types: RSA, Ed25519, and ECDSA.
 *
 * <p>Keys are stored in standard formats:
 * <ul>
 *   <li>Private key: PKCS#8 PEM ({@code -----BEGIN PRIVATE KEY-----})</li>
 *   <li>Public key: OpenSSH format ({@code ssh-rsa/ssh-ed25519/ecdsa-sha2-nistp256 <base64> <label>})</li>
 * </ul>
 * These formats are interoperable with OpenSSH, Node.js crypto, and other standard tools.
 */
public class SSHKeyPair {

	/**
	 * Supported SSH key types.
	 */
	public enum KeyType {
		RSA("ssh-rsa", "RSA", "SHA256withRSA", 2048),
		ED25519("ssh-ed25519", "Ed25519", "Ed25519", 0),
		ECDSA_256("ecdsa-sha2-nistp256", "EC", "SHA256withECDSA", 256);

		private final String sshName;
		private final String jcaAlgorithm;
		private final String signatureAlgorithm;
		private final int keySize;

		KeyType(String sshName, String jcaAlgorithm, String signatureAlgorithm, int keySize) {
			this.sshName = sshName;
			this.jcaAlgorithm = jcaAlgorithm;
			this.signatureAlgorithm = signatureAlgorithm;
			this.keySize = keySize;
		}

		public String getSshName() {
			return sshName;
		}

		public String getJcaAlgorithm() {
			return jcaAlgorithm;
		}

		public String getSignatureAlgorithm() {
			return signatureAlgorithm;
		}

		/**
		 * Returns the KeyType for the given SSH key type name.
		 * @param sshName The SSH key type name (e.g. "ssh-rsa", "ssh-ed25519")
		 * @return The matching KeyType
		 * @throws IllegalArgumentException If the key type is not supported
		 */
		public static KeyType fromSshName(String sshName) {
			for(KeyType type : values()) {
				if(type.sshName.equals(sshName)) {
					return type;
				}
			}
			throw new IllegalArgumentException("Unsupported SSH key type: " + sshName);
		}
	}

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private KeyType keyType;
	private String label;

	/**
	 * Generates a new key pair of the given type.
	 *
	 * @param type The key type to generate
	 * @param label The label for the public key (e.g. "user@host")
	 * @return A new SSHKeyPair instance with both keys
	 */
	public static SSHKeyPair generateKey(KeyType type, String label) {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(type.jcaAlgorithm);
			switch(type) {
				case RSA:
					keyGen.initialize(type.keySize);
					break;
				case ED25519:
					keyGen.initialize(new NamedParameterSpec("Ed25519"));
					break;
				case ECDSA_256:
					keyGen.initialize(new ECGenParameterSpec("secp256r1"));
					break;
				default:
					throw new UnsupportedOperationException("Unknown key type: " + type);
			}
			KeyPair pair = keyGen.generateKeyPair();
			SSHKeyPair result = new SSHKeyPair();
			result.keyType = type;
			result.privateKey = pair.getPrivate();
			result.publicKey = pair.getPublic();
			result.label = label;
			return result;
		} catch(NoSuchAlgorithmException | java.security.InvalidAlgorithmParameterException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates an SSHKeyPair from PEM/SSH key strings. Either key may be null,
	 * but at least one must be provided. The key type is auto-detected from
	 * the key data.
	 *
	 * <p>The private key should be PKCS#8 PEM format ({@code -----BEGIN PRIVATE KEY-----}).
	 * The public key should be OpenSSH format ({@code type base64 label}).
	 *
	 * @param privateKeyPem The private key PEM string, or null
	 * @param publicKeySsh The public key OpenSSH string, or null
	 * @throws IllegalArgumentException If a key string cannot be parsed
	 */
	public SSHKeyPair(String privateKeyPem, String publicKeySsh) throws IllegalArgumentException {
		if(publicKeySsh != null) {
			String[] split = publicKeySsh.trim().split("\\s+");
			if(split.length < 2) {
				throw new IllegalArgumentException("Invalid public key format.");
			}
			this.keyType = KeyType.fromSshName(split[0]);
			this.label = split.length >= 3 ? split[2] : "";
			this.publicKey = sshToPublicKey(keyType, split[1]);
		}

		if(privateKeyPem != null) {
			this.privateKey = parsePrivateKey(privateKeyPem);
			if(this.keyType == null) {
				this.keyType = detectKeyType(privateKey);
			}
		}
	}

	private SSHKeyPair() {
		// Used by generateKey
	}

	/**
	 * Signs the given data with the private key.
	 *
	 * @param data The data to sign
	 * @return The signature bytes
	 */
	public byte[] sign(byte[] data) {
		Objects.requireNonNull(privateKey, "Private key is required for signing");
		try {
			Signature sig = Signature.getInstance(keyType.signatureAlgorithm);
			sig.initSign(privateKey);
			sig.update(data);
			return sig.sign();
		} catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Verifies a signature against the given data using the public key.
	 *
	 * @param data The original data that was signed
	 * @param signature The signature to verify
	 * @return true if the signature is valid
	 */
	public boolean verify(byte[] data, byte[] signature) {
		Objects.requireNonNull(publicKey, "Public key is required for verification");
		try {
			Signature sig = Signature.getInstance(keyType.signatureAlgorithm);
			sig.initVerify(publicKey);
			sig.update(data);
			return sig.verify(signature);
		} catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the private key as a PKCS#8 PEM string.
	 */
	public String getPrivateKeyPem() {
		Objects.requireNonNull(privateKey);
		String base64 = Base64.encodeBase64String(privateKey.getEncoded());
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
	 * Returns the public key as an OpenSSH format string.
	 */
	public String getPublicKeySsh() {
		Objects.requireNonNull(publicKey);
		return publicKeyToSsh(keyType, publicKey, label);
	}

	/**
	 * Returns the key type.
	 */
	public KeyType getKeyType() {
		return keyType;
	}

	/**
	 * Returns the label on the public key.
	 */
	public String getLabel() {
		return label;
	}

	// --- Private key parsing ---

	private static PrivateKey parsePrivateKey(String pem) {
		pem = pem.replaceAll("\r", "").replaceAll("\n", "");
		// Strip all known PEM headers
		pem = pem.replace("-----BEGIN PRIVATE KEY-----", "");
		pem = pem.replace("-----END PRIVATE KEY-----", "");
		pem = pem.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		pem = pem.replace("-----END RSA PRIVATE KEY-----", "");
		pem = pem.replace("-----BEGIN EC PRIVATE KEY-----", "");
		pem = pem.replace("-----END EC PRIVATE KEY-----", "");
		pem = pem.trim();
		byte[] keyBytes = Base64.decodeBase64(pem);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

		// Try each algorithm until one works
		String[] algorithms = {"Ed25519", "EC", "RSA"};
		for(String alg : algorithms) {
			try {
				KeyFactory kf = KeyFactory.getInstance(alg);
				return kf.generatePrivate(spec);
			} catch(NoSuchAlgorithmException | InvalidKeySpecException ex) {
				// Try next
			}
		}
		throw new IllegalArgumentException("Failed to parse private key. "
				+ "Supported types: RSA, Ed25519, ECDSA (PKCS#8 format).");
	}

	private static KeyType detectKeyType(PrivateKey key) {
		return switch(key.getAlgorithm()) {
			case "RSA" -> KeyType.RSA;
			case "Ed25519", "EdDSA" -> KeyType.ED25519;
			case "EC" -> KeyType.ECDSA_256;
			default -> throw new IllegalArgumentException(
					"Unsupported private key algorithm: " + key.getAlgorithm());
		};
	}

	// --- OpenSSH public key encoding/decoding ---

	private static String publicKeyToSsh(KeyType type, PublicKey key, String label) {
		Objects.requireNonNull(label);
		try {
			byte[] wireBytes = encodePublicKeyWire(type, key);
			return type.sshName + " " + Base64.encodeBase64String(wireBytes) + " " + label;
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static byte[] encodePublicKeyWire(KeyType type, PublicKey key) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(buf);
		byte[] typeBytes = type.sshName.getBytes("UTF-8");
		dos.writeInt(typeBytes.length);
		dos.write(typeBytes);

		switch(type) {
			case RSA: {
				RSAPublicKey rsaKey = (RSAPublicKey) key;
				byte[] eBytes = rsaKey.getPublicExponent().toByteArray();
				dos.writeInt(eBytes.length);
				dos.write(eBytes);
				byte[] nBytes = rsaKey.getModulus().toByteArray();
				dos.writeInt(nBytes.length);
				dos.write(nBytes);
				break;
			}
			case ED25519: {
				// Ed25519 public key is the raw 32-byte key from X.509 encoding
				byte[] rawKey = extractEd25519RawPublicKey(key);
				dos.writeInt(rawKey.length);
				dos.write(rawKey);
				break;
			}
			case ECDSA_256: {
				ECPublicKey ecKey = (ECPublicKey) key;
				// Write curve identifier
				byte[] curveBytes = "nistp256".getBytes("UTF-8");
				dos.writeInt(curveBytes.length);
				dos.write(curveBytes);
				// Write uncompressed point (04 || x || y)
				byte[] point = encodeEcPoint(ecKey);
				dos.writeInt(point.length);
				dos.write(point);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown key type: " + type);
		}
		dos.flush();
		return buf.toByteArray();
	}

	private static PublicKey sshToPublicKey(KeyType type, String base64Part) {
		byte[] decoded = Base64.decodeBase64(base64Part);
		ByteBuffer bb = ByteBuffer.wrap(decoded);
		// Skip key type string
		int typeLen = bb.getInt();
		byte[] typeBytes = new byte[typeLen];
		bb.get(typeBytes);

		try {
			return switch(type) {
				case RSA -> decodeRsaPublicKey(bb);
				case ED25519 -> decodeEd25519PublicKey(bb);
				case ECDSA_256 -> decodeEcdsaPublicKey(bb);
			};
		} catch(NoSuchAlgorithmException | InvalidKeySpecException ex) {
			throw new RuntimeException("Failed to parse " + type.sshName + " public key", ex);
		}
	}

	private static PublicKey decodeRsaPublicKey(ByteBuffer bb)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		int eLen = bb.getInt();
		byte[] eBytes = new byte[eLen];
		bb.get(eBytes);
		BigInteger e = new BigInteger(eBytes);
		int nLen = bb.getInt();
		byte[] nBytes = new byte[nLen];
		bb.get(nBytes);
		BigInteger n = new BigInteger(nBytes);
		java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(n, e);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	private static PublicKey decodeEd25519PublicKey(ByteBuffer bb)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		int keyLen = bb.getInt();
		byte[] rawKey = new byte[keyLen];
		bb.get(rawKey);
		// Wrap raw 32 bytes into X.509 SubjectPublicKeyInfo for Ed25519
		// The X.509 prefix for Ed25519 is fixed: 30 2a 30 05 06 03 2b 65 70 03 21 00
		byte[] x509Prefix = {
			0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00
		};
		byte[] x509Key = new byte[x509Prefix.length + rawKey.length];
		System.arraycopy(x509Prefix, 0, x509Key, 0, x509Prefix.length);
		System.arraycopy(rawKey, 0, x509Key, x509Prefix.length, rawKey.length);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(x509Key);
		KeyFactory kf = KeyFactory.getInstance("Ed25519");
		return kf.generatePublic(spec);
	}

	private static PublicKey decodeEcdsaPublicKey(ByteBuffer bb)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Skip curve identifier string
		int curveLen = bb.getInt();
		byte[] curveBytes = new byte[curveLen];
		bb.get(curveBytes);
		// Read uncompressed EC point
		int pointLen = bb.getInt();
		byte[] pointBytes = new byte[pointLen];
		bb.get(pointBytes);
		java.security.spec.ECPoint point = decodeEcPoint(pointBytes);
		java.security.spec.ECPublicKeySpec spec = new java.security.spec.ECPublicKeySpec(
				point, getP256Params());
		KeyFactory kf = KeyFactory.getInstance("EC");
		return kf.generatePublic(spec);
	}

	// --- EC point encoding/decoding helpers ---

	private static byte[] encodeEcPoint(ECPublicKey key) {
		byte[] x = key.getW().getAffineX().toByteArray();
		byte[] y = key.getW().getAffineY().toByteArray();
		// Pad/trim to 32 bytes each for P-256
		x = padOrTrimTo(x, 32);
		y = padOrTrimTo(y, 32);
		byte[] result = new byte[1 + 32 + 32];
		result[0] = 0x04; // uncompressed
		System.arraycopy(x, 0, result, 1, 32);
		System.arraycopy(y, 0, result, 33, 32);
		return result;
	}

	private static java.security.spec.ECPoint decodeEcPoint(byte[] data) {
		if(data[0] != 0x04) {
			throw new IllegalArgumentException("Only uncompressed EC points are supported");
		}
		int coordLen = (data.length - 1) / 2;
		byte[] xBytes = new byte[coordLen];
		byte[] yBytes = new byte[coordLen];
		System.arraycopy(data, 1, xBytes, 0, coordLen);
		System.arraycopy(data, 1 + coordLen, yBytes, 0, coordLen);
		return new java.security.spec.ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));
	}

	private static java.security.spec.ECParameterSpec getP256Params() {
		try {
			java.security.AlgorithmParameters params =
					java.security.AlgorithmParameters.getInstance("EC");
			params.init(new ECGenParameterSpec("secp256r1"));
			return params.getParameterSpec(java.security.spec.ECParameterSpec.class);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static byte[] padOrTrimTo(byte[] input, int length) {
		if(input.length == length) {
			return input;
		}
		byte[] result = new byte[length];
		if(input.length > length) {
			// Trim leading bytes (BigInteger may have a leading zero for sign)
			System.arraycopy(input, input.length - length, result, 0, length);
		} else {
			// Pad with leading zeros
			System.arraycopy(input, 0, result, length - input.length, input.length);
		}
		return result;
	}

	/**
	 * Extracts the raw 32-byte Ed25519 public key from the X.509 encoding.
	 */
	private static byte[] extractEd25519RawPublicKey(PublicKey key) {
		byte[] x509 = key.getEncoded();
		// X.509 for Ed25519: 12-byte prefix + 32-byte raw key
		byte[] raw = new byte[32];
		System.arraycopy(x509, x509.length - 32, raw, 0, 32);
		return raw;
	}
}
