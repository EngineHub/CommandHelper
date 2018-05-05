/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities.Common;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

/**
 *
 * @author cailin
 */
public class TimeConversionUtilTest {

	public TimeConversionUtilTest() {
	}

	@Test
	public void testMSMS() {
		assertThat(TimeConversionUtil.inMilliseconds(1, TimeConversionUtil.TimeUnit.MILLISECOND), is(1L));
		assertThat(TimeConversionUtil.inMilliseconds(50, TimeConversionUtil.TimeUnit.MILLISECOND), is(50L));
	}

	@Test
	public void testMSS() {
		assertThat(TimeConversionUtil.inMilliseconds(1, TimeConversionUtil.TimeUnit.SECOND), is(1000L));
		assertThat(TimeConversionUtil.inMilliseconds(50, TimeConversionUtil.TimeUnit.SECOND), is(50000L));
	}

	@Test
	public void testSMS() {
		assertThat(TimeConversionUtil.inSeconds(1, TimeConversionUtil.TimeUnit.MILLISECOND), is(0L));
		assertThat(TimeConversionUtil.inSeconds(1000, TimeConversionUtil.TimeUnit.MILLISECOND), is(1L));
		assertThat(TimeConversionUtil.inSeconds(1499, TimeConversionUtil.TimeUnit.MILLISECOND), is(1L));
		assertThat(TimeConversionUtil.inSeconds(1500, TimeConversionUtil.TimeUnit.MILLISECOND), is(2L));
		assertThat(TimeConversionUtil.inSeconds(2000, TimeConversionUtil.TimeUnit.MILLISECOND), is(2L));
	}

	@Test
	public void testSS() {
		assertThat(TimeConversionUtil.inSeconds(1, TimeConversionUtil.TimeUnit.SECOND), is(1L));
		assertThat(TimeConversionUtil.inSeconds(60, TimeConversionUtil.TimeUnit.SECOND), is(60L));
		assertThat(TimeConversionUtil.inSeconds(1, TimeConversionUtil.TimeUnit.HOUR), is(3600L));
	}

}
