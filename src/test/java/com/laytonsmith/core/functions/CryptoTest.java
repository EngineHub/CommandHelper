

package com.laytonsmith.core.functions;

import com.laytonsmith.core.exceptions.ConfigCompileException;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Layton
 */
public class CryptoTest {

    public CryptoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test(timeout=10000)
    public void testRot13() throws ConfigCompileException {
        assertEquals("hello world", SRun("rot13(uryyb jbeyq)", null));
    }

    @Test(timeout=10000)
    public void testMd5() throws ConfigCompileException {
        assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", SRun("md5('hello world')", null));
    }

    @Test(timeout=10000)
    public void testSha1() throws ConfigCompileException {
        assertEquals("2aae6c35c94fcfb415dbe95f408b9ce91ee846ed", SRun("sha1('hello world')", null));
    }

	@Test(timeout=10000)
	public void testHmacMd5() throws ConfigCompileException {
		assertEquals("61c95854c1cd8179128b54c19ac01c28", SRun("hmac_md5('secret_key', 'hello world')", null));
	}

	@Test(timeout=10000)
	public void testHmacSha1() throws ConfigCompileException {
		assertEquals("15272f929f45d7f15e2bbfd7237741538847de8a", SRun("hmac_sha1('secret_key', 'hello world')", null));
	}

	@Test(timeout=10000)
	public void testHmacSha256() throws ConfigCompileException {
		assertEquals("cf1a418afaafc798df48fd804a2abf6970283afd8c40b41f818ad9b6ca4f8ca8", SRun("hmac_sha256('secret_key', 'hello world')", null));
	}

}
