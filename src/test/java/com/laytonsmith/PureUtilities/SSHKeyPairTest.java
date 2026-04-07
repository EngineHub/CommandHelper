package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.SSHKeyPair;
import com.laytonsmith.PureUtilities.Common.SSHKeyPair.KeyType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.util.Arrays;
import java.util.Collection;

/**
 * Tests for SSHKeyPair supporting RSA, Ed25519, and ECDSA key types.
 */
@RunWith(Parameterized.class)
public class SSHKeyPairTest {

	@Parameters(name = "{0}")
	public static Collection<Object[]> keyTypes() {
		return Arrays.asList(new Object[][]{
			{KeyType.RSA},
			{KeyType.ED25519},
			{KeyType.ECDSA_256},
		});
	}

	private final KeyType keyType;

	public SSHKeyPairTest(KeyType keyType) {
		this.keyType = keyType;
	}

	@Test
	public void testSignAndVerify() throws Exception {
		SSHKeyPair pair = SSHKeyPair.generateKey(keyType, "test@host");
		byte[] data = "test data".getBytes("UTF-8");
		byte[] sig = pair.sign(data);
		assertTrue(pair.verify(data, sig));
	}

	@Test
	public void testRoundTripThroughSerialization() throws Exception {
		SSHKeyPair original = SSHKeyPair.generateKey(keyType, "label@host");
		String privPem = original.getPrivateKeyPem();
		String pubSsh = original.getPublicKeySsh();
		assertTrue(pubSsh.startsWith(keyType.getSshName() + " "));
		assertTrue(pubSsh.endsWith(" label@host"));

		SSHKeyPair signer = new SSHKeyPair(privPem, null);
		SSHKeyPair verifier = new SSHKeyPair(null, pubSsh);
		byte[] data = "round trip test".getBytes("UTF-8");
		byte[] sig = signer.sign(data);
		assertTrue(verifier.verify(data, sig));
	}

	@Test
	public void testKeyTypeDetectedFromPrivateKeyOnly() throws Exception {
		SSHKeyPair pair = SSHKeyPair.generateKey(keyType, "test");
		SSHKeyPair fromPriv = new SSHKeyPair(pair.getPrivateKeyPem(), null);
		assertEquals(keyType, fromPriv.getKeyType());
	}

	@Test
	public void testGetKeyType() {
		SSHKeyPair pair = SSHKeyPair.generateKey(keyType, "test");
		assertEquals(keyType, pair.getKeyType());
	}

	@Test
	public void testGetLabel() {
		SSHKeyPair pair = SSHKeyPair.generateKey(keyType, "user@example.com");
		assertEquals("user@example.com", pair.getLabel());
	}

	@Test
	public void testPrivateKeyPemFormat() {
		SSHKeyPair pair = SSHKeyPair.generateKey(keyType, "test");
		String pem = pair.getPrivateKeyPem();
		assertTrue(pem.contains("-----BEGIN PRIVATE KEY-----"));
		assertTrue(pem.contains("-----END PRIVATE KEY-----"));
	}

	@Test
	public void testKeyTypeFromSshName() {
		for(KeyType kt : KeyType.values()) {
			assertEquals(kt, KeyType.fromSshName(kt.getSshName()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testKeyTypeFromSshNameUnknown() {
		KeyType.fromSshName("ssh-unknown");
	}
}
