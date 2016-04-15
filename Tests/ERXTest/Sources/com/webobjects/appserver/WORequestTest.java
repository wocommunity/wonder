package com.webobjects.appserver;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSTimestamp;

import er.erxtest.ERXTestCase;

public class WORequestTest extends ERXTestCase {

	private static Number asInt(Number n) { return new Integer(n.intValue()); }

	static TimeZone pdt;
	static TimeZone pst;

	static {
		pdt = TimeZone.getTimeZone("GMT-7");
		pst = TimeZone.getTimeZone("GMT-8");
	}

	public void testDateFormValueForKey() {

		WORequest req = new TestRequest();

		try {
			req.takeValueForKey("2001.07.04 AD at 12:08:56 PDT", "str");
			assertEquals(new NSTimestamp(2001, 7, 4, 12, 8, 56, pdt), req.dateFormValueForKey("str", new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("Wed, Jul 4, '01", "str");
			assertEquals(new NSTimestamp(2001, 7, 4, 0, 0, 0, TimeZone.getDefault()), req.dateFormValueForKey("str", new SimpleDateFormat("EEE, MMM d, ''yy", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("12:08 PM", "str");
			assertEquals(new NSTimestamp(1970, 1, 1, 12, 8, 0, TimeZone.getDefault()), req.dateFormValueForKey("str", new SimpleDateFormat("h:mm a", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("12 o'clock PM, Pacific Daylight Time", "str");
			assertEquals(new NSTimestamp(1970, 1, 1, 12, 0, 0, pdt), req.dateFormValueForKey("str", new SimpleDateFormat("hh 'o''clock' a, zzzz", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("0:08 PM, PDT", "str");
			assertEquals(new NSTimestamp(1970, 1, 1, 12, 8, 0, pdt), req.dateFormValueForKey("str", new SimpleDateFormat("K:mm a, z", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("02001.July.04 AD 12:08 PM", "str");
			assertEquals(new NSTimestamp(2001, 7, 4, 12, 8, 0, TimeZone.getDefault()), req.dateFormValueForKey("str", new SimpleDateFormat("yyyyy.MMMMM.dd GGG hh:mm aaa", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("Wed, 4 Jul 2001 12:08:56 -0700", "str");
			assertEquals(new NSTimestamp(2001, 7, 4, 12, 8, 56, pdt), req.dateFormValueForKey("str", new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("010704120856-0700", "str");
			assertEquals(new NSTimestamp(2001, 7, 4, 12, 8, 56, pdt), req.dateFormValueForKey("str", new SimpleDateFormat("yyMMddHHmmssZ", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }

		try {
			req.takeValueForKey("2001-07-04T12:08:56.235-0700", "str");
			assertEquals(new NSTimestamp((new NSTimestamp(2001, 7, 4, 12, 8, 56, pdt)).getTime() + 235), req.dateFormValueForKey("str", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)));
			fail("Expecting a ClassCastException");
		} catch (java.lang.ClassCastException e) { }
	}

	public void testNumericFormValueForKey() {

		WORequest req = new TestRequest();

		req.takeValueForKey("2001", "str");
		NSNumberFormatter format01 = new NSNumberFormatter("0");
		assertEquals(new Integer(2001), asInt(req.numericFormValueForKey("str", format01)));
	}

	/**
	 * Subclass of WORequest that substitutes <code>valueForKey</code> for <code>formValueForKey</code> so
	 * that it is easy to test the WORequest form value processing methods.
	 */
	public static class TestRequest extends WORequest {

		public String str;

		@Override
		public String formValueForKey(String aKey) {
			Object value = valueForKey(aKey);
			return (value != null) ? value.toString() : null;
		}
	}
}
