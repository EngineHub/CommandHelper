/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.hamcrest.core.Is.is;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import org.junit.Before;

/**
 * Testing concurrency is super hard, so we'll not do that, I guess. But the singleton policy should be easily testable.
 *
 * @author cailin
 */
public class ConcurrentSingletonHashMapTest {

	ConcurrentSingletonHashMap.ValueGenerator<String, Object> generator;
	AtomicInteger counter = new AtomicInteger(0);

	public ConcurrentSingletonHashMapTest() {
		generator = (String key) -> {
			counter.incrementAndGet();
			return new Object();
		};
	}

	@Before
	public void before() {
		counter = new AtomicInteger(0);
	}

	@Test
	public void testSingletonFunctionality() {
		Map<String, Object> m = new ConcurrentSingletonHashMap<>(generator);
		Object o1 = m.get("key1");
		Object o2 = m.get("key1");
		Object o3 = m.get("key2");
		assertThat(m.size(), is(2));
		// == not .equals
		assertTrue(o1 == o2);
		assertFalse(o1 == o3);
		assertThat(counter.get(), is(2));
	}

}
