package com.webobjects.appserver;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSTimestamp;

import er.erxtest.ERXTestCase;

public class WORequestTest extends ERXTestCase {


	NSTimestamp timeDesired;

	public void setup() {
		timeDesired = new NSTimestamp(2001, 7, 4, 12, 8, 56, TimeZone.getTimeZone("PDT"));
	}

	public void testDateFormValueForKey() {

		WORequest req = new TestRequest();

		req.takeValueForKey("2001.07.04 AD at 12:08:56 PDT", "str");
		SimpleDateFormat format01 = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

		try {
			NSTimestamp t1 = req.dateFormValueForKey("str", format01);
			fail("Not throwing an NPE?");
			assertEquals(timeDesired, t1);
		} catch (java.lang.NullPointerException npe) {
			// Why is this throwing an NPE?
		}

		// If the basic above is working, these can be checked. They represent different
		// formats and create the same NSTimestamp. But there is no reason to check them now.
		//
		// "yyyy.MM.dd G 'at' HH:mm:ss z"     2001.07.04 AD at 12:08:56 PDT
		// "EEE, MMM d, ''yy"                 Wed, Jul 4, '01
		// "h:mm a"                           12:08 PM
		// "hh 'o''clock' a, zzzz"            12 o'clock PM, Pacific Daylight Time
		// "K:mm a, z"                        0:08 PM, PDT
		// "yyyyy.MMMMM.dd GGG hh:mm aaa"     02001.July.04 AD 12:08 PM
		// "EEE, d MMM yyyy HH:mm:ss Z"       Wed, 4 Jul 2001 12:08:56 -0700
		// "yyMMddHHmmssZ"                    010704120856-0700
		// "yyyy-MM-dd'T'HH:mm:ss.SSSZ"       2001-07-04T12:08:56.235-0700
	}

	public void testNumericFormValueForKey() {

		WORequest req = new TestRequest();

		req.takeValueForKey("2001", "str");
		NSNumberFormatter format01 = new NSNumberFormatter("0");

		try {
			Number n1 = req.numericFormValueForKey("str", format01);
			fail("Not throwing an NPE?");
			Number numberDesired = new Integer(2001);
			assertEquals(numberDesired, n1);
		} catch (java.lang.NullPointerException npe) {
			// Why is this throwing an NPE?
		}
	}

	public static class TestRequest extends WORequest {
		public String str;
	}
}
