package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.RSAEncrypt;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class RSAEncryptTest {

	RSAEncrypt enc;
	byte[] data;
	String sData;

	public RSAEncryptTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		enc = RSAEncrypt.generateKey("label@label");
		sData = "the test string";
		data = sData.getBytes("UTF-8");
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testPubToPriv() throws Exception {
		byte[] c = enc.encryptWithPublic(data);
		String s = new String(enc.decryptWithPrivate(c), "UTF-8");
		assertEquals(sData, s);
	}

	@Test
	public void testPrivToPub() throws Exception {
		byte[] c = enc.encryptWithPrivate(data);
		String s = new String(enc.decryptWithPublic(c), "UTF-8");
		assertEquals(sData, s);
	}

}
