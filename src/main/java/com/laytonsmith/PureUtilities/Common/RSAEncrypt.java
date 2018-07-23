package com.laytonsmith.PureUtilities.Common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;

/**
 * Given a public/private key pair, this class uses RSA to encrypt/decrypt data.
 */
public class RSAEncrypt {

	/**
	 * The RSA algorithm key.
	 */
	private static final String ALGORITHM = "RSA";

	/**
	 * Generates a new key, and stores the value in the RSA
	 *
	 * @param label The label that will be associated with the public key
	 * @return
	 */
	public static RSAEncrypt generateKey(String label) {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
		keyGen.initialize(1024);
		KeyPair key = keyGen.generateKeyPair();
		RSAEncrypt enc = new RSAEncrypt(toString(key.getPrivate()), toString(key.getPublic(), label));
		return enc;
	}

	/**
	 * Given a public key and a label, produces an ssh compatible rsa public key string.
	 *
	 * @param key
	 * @param label
	 * @return
	 */
	public static String toString(PublicKey key, String label) {
		Objects.requireNonNull(label);
		ByteArrayOutputStream pubBOS = new ByteArrayOutputStream();
		try {
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(pubBOS);
			publicKeyOS.writeObject(key);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		String publicKey = Base64.encodeBase64String(pubBOS.toByteArray());
		publicKey = "ssh-rsa " + publicKey + " " + label;
		return publicKey;
	}

	/**
	 * Given a private key, produces an ssh compatible rsa private key string.
	 *
	 * @param key
	 * @return
	 */
	private static String toString(PrivateKey key) {
		ByteArrayOutputStream privBOS = new ByteArrayOutputStream();
		ObjectOutputStream privateKeyOS;
		try {
			privateKeyOS = new ObjectOutputStream(privBOS);
			privateKeyOS.writeObject(key);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		String privateKey = Base64.encodeBase64String(privBOS.toByteArray());

		StringBuilder privBuilder = new StringBuilder();
		privBuilder.append("-----BEGIN RSA PRIVATE KEY-----");
		for(int i = 0; i < privateKey.length(); i++) {
			if(i % 64 == 0) {
				privBuilder.append(StringUtils.nl());
			}
			privBuilder.append(privateKey.charAt(i));
		}
		privBuilder.append(StringUtils.nl()).append("-----END RSA PRIVATE KEY-----").append(StringUtils.nl());
		privateKey = privBuilder.toString();
		return privateKey;
	}

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String label;

	/**
	 * Creates a new RSAEncrypt object, based on the ssh compatible private/public key pair. Only one key needs to be
	 * provided. If so, only those methods for the key provided will work.
	 *
	 * @param privateKey
	 * @param publicKey
	 * @throws IllegalArgumentException If the keys are not the correct type. They must be ssh compatible.
	 */
	public RSAEncrypt(String privateKey, String publicKey) throws IllegalArgumentException {
		if(privateKey != null) {
			//private key processing
			//replace all newlines with nothing
			privateKey = privateKey.replaceAll("\r", "");
			privateKey = privateKey.replaceAll("\n", "");
			//Remove the BEGIN/END tags
			privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "");
			privateKey = privateKey.replace("-----END RSA PRIVATE KEY-----", "");
			ObjectInputStream privOIS;
			try {
				privOIS = new ObjectInputStream(new ByteArrayInputStream(Base64.decodeBase64(privateKey)));
				this.privateKey = (PrivateKey) privOIS.readObject();
			} catch (IOException | ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}

		if(publicKey != null) {
			//public key processing
			String[] split = publicKey.split(" ");
			if(split.length != 3) {
				throw new IllegalArgumentException("Invalid public key passed in.");
			}
			if(!"ssh-rsa".equals(split[0])) {
				throw new IllegalArgumentException("Invalid public key type. Expecting ssh-rsa, but found \"" + split[0] + "\"");
			}
			this.label = split[2];
			ObjectInputStream pubOIS;
			try {
				pubOIS = new ObjectInputStream(new ByteArrayInputStream(Base64.decodeBase64(split[1])));
				this.publicKey = (PublicKey) pubOIS.readObject();
			} catch (IOException | ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Encrypts the data with the public key, which can be decrypted with the private key. This is only valid if the
	 * public key was provided.
	 *
	 * @param data
	 * @return
	 */
	public byte[] encryptWithPublic(byte[] data) {
		Objects.requireNonNull(publicKey);
		return crypt(data, publicKey, Cipher.ENCRYPT_MODE);
	}

	/**
	 * Encrypts the data with the private key, which can be decrypted with the public key. This is only valid if the
	 * private key was provided.
	 *
	 * @param data
	 * @return
	 * @throws InvalidKeyException
	 */
	public byte[] encryptWithPrivate(byte[] data) throws InvalidKeyException {
		Objects.requireNonNull(privateKey);
		return crypt(data, privateKey, Cipher.ENCRYPT_MODE);
	}

	/**
	 * Decrypts the data with the public key, which will have been encrypted with the private key. This is only valid if
	 * the public key was provided.
	 *
	 * @param data
	 * @return
	 */
	public byte[] decryptWithPublic(byte[] data) {
		Objects.requireNonNull(publicKey);
		return crypt(data, publicKey, Cipher.DECRYPT_MODE);
	}

	/**
	 * Decrypts the data with the private key, which will have been encrypted with the public key. This is only valid if
	 * the private key was provided.
	 *
	 * @param data
	 * @return
	 */
	public byte[] decryptWithPrivate(byte[] data) {
		Objects.requireNonNull(privateKey);
		return crypt(data, privateKey, Cipher.DECRYPT_MODE);
	}

	/**
	 * Utility method that actually does the de/encrypting.
	 *
	 * @param data
	 * @param key
	 * @param cryptMode
	 * @return
	 */
	private byte[] crypt(byte[] data, Key key, int cryptMode) {
		byte[] cipherValue = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(cryptMode, key);
			cipherValue = cipher.doFinal(data);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new RuntimeException(ex);
		}
		return cipherValue;
	}

	/**
	 * Returns the private key string.
	 *
	 * @return
	 */
	public String getPrivateKey() {
		return toString(privateKey);
	}

	/**
	 * Returns the public key string.
	 *
	 * @return
	 */
	public String getPublicKey() {
		return toString(publicKey, label);
	}

	/**
	 * Returns the label on the public key.
	 *
	 * @return
	 */
	public String getLabel() {
		return label;
	}

}
