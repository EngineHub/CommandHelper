package com.laytonsmith.core.events;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.C;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 *
 *
 */
public class PrefiltersTest {

	Environment env;

	public PrefiltersTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of match method, of class Prefilters.
	 */
	@Test
	public void testRegexMatch() {
		Map<String, Mixed> map = new HashMap<>();
		map.put("x", C.String("/1|2|3/"));
		try {
			Prefilters.match(map, "x", C.Int(2), PrefilterType.REGEX, env);
		} catch (PrefilterNonMatchException e) {
			fail("Expected a match here");
		}
		try {
			Prefilters.match(map, "x", C.Int(4), PrefilterType.REGEX, env);
			fail("Did not expect a match here");
		} catch (PrefilterNonMatchException e) {
		}
	}

	@Test
	public void testStringMatch() {
		Map<String, Mixed> map = new HashMap<>();
		map.put("x", C.String("test"));
		try {
			Prefilters.match(map, "x", "test", PrefilterType.STRING_MATCH, env);
		} catch (PrefilterNonMatchException e) {
			fail("Expected a match here");
		}
		try {
			Prefilters.match(map, "x", "nope", PrefilterType.STRING_MATCH, env);
			fail("Did not expect a match here");
		} catch (PrefilterNonMatchException e) {
		}
	}

	@Test
	public void testMathMatch() {
		Map<String, Mixed> map = new HashMap<>();
		map.put("x", C.String("2"));
		try {
			Prefilters.match(map, "x", "2.0", PrefilterType.MATH_MATCH, env);
		} catch (PrefilterNonMatchException e) {
			fail("Expected a match here");
		}
		try {
			Prefilters.match(map, "x", "2.00001", PrefilterType.MATH_MATCH, env);
			fail("Did not expect a match here");
		} catch (PrefilterNonMatchException e) {
		}
	}

	@Test
	public void testExpressionMatch() {
		Map<String, Mixed> map = new HashMap<>();
		map.put("x", C.String("(x > 4)"));
		try {
			try {
				Prefilters.match(map, "x", "5", PrefilterType.EXPRESSION, env);
			} catch (PrefilterNonMatchException e) {
				fail("Expected a match here");
			}
			try {
				Prefilters.match(map, "x", "4", PrefilterType.EXPRESSION, env);
				fail("Did not expect a match here");
			} catch (PrefilterNonMatchException e) {
			}

			map.put("x", C.String("(x == 5)"));
			try {
				Prefilters.match(map, "x", "5", PrefilterType.EXPRESSION, env);
			} catch (PrefilterNonMatchException e) {
				fail("Expected a match here");
			}
			try {
				Prefilters.match(map, "x", "4", PrefilterType.EXPRESSION, env);
				fail("Did not expect a match here");
			} catch (PrefilterNonMatchException e) {
			}

			map.put("x", C.String("(2 + 3)"));
			try {
				Prefilters.match(map, "x", "5", PrefilterType.EXPRESSION, env);
			} catch (PrefilterNonMatchException e) {
				fail("Expected a match here");
			}
			try {
				Prefilters.match(map, "x", "4", PrefilterType.EXPRESSION, env);
				fail("Did not expect a match here");
			} catch (PrefilterNonMatchException e) {
			}
		} catch (ConfigRuntimeException e) {
			if(e.getCause() instanceof ClassNotFoundException) {
				// Nothing we can do during testing
			} else {
				fail("Test may be incorrectly formatted: " + e.getCause().getMessage());
			}
		}
	}
}
