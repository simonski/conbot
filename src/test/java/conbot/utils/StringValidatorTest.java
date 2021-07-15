package conbot.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringValidatorTest {

	@Test
	public void testGoodDateTime() {
		StringValidator validator = new StringValidator();
		String value = "2001-04-05T13:23:23.982547447Z";
		assertEquals(true, validator.isDateTime(value));
	}

	@Test
	public void testBadDateTime1() {
		StringValidator validator = new StringValidator();
		String value = "zz001-04-05T13:23:23.982547447Z";
		assertEquals(false, validator.isDateTime(value));
	}

	@Test
	public void testBadDateTime2() {
		StringValidator validator = new StringValidator();
		String value = "fred";
		assertEquals(false, validator.isDateTime(value));
	}

	@Test
	public void testSplitNanos1() {
		StringValidator validator = new StringValidator();
		String candidate = "2021-07-08T11:42:48.464122912Z";
		assertEquals(true, validator.isDateTime(candidate));
		String[] expected = validator.splitNanos(candidate);
		System.out.println("split nanos returned " + expected);
		assertEquals("2021-07-08T11:42:48.464122", expected[0]);
		assertEquals("912", expected[1]);
	}

	@Test
	public void testSplitNanos2() {
		StringValidator validator = new StringValidator();
		String candidate = "2021-07-08T07:47:25.145026262Z";
		assertEquals(true, validator.isDateTime(candidate));
		String[] expected = validator.splitNanos(candidate);
		System.out.println("split nanos returned " + expected);
		assertEquals("2021-07-08T07:47:25.145026", expected[0]);
		assertEquals("262", expected[1]);
	}

	@Test
	public void testBadDateTime3() {
		StringValidator validator = new StringValidator();
		String value = "";
		assertEquals(false, validator.isDateTime(value));
	}

	@Test
	public void testBadDateTime4() {
		StringValidator validator = new StringValidator();
		String value = null;
		assertEquals(false, validator.isDateTime(value));
	}

}
