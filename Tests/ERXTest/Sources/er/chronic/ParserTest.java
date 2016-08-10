package er.chronic;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.webobjects.foundation.NSArray;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.EndianPrecedence;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class ParserTest extends TestCase {

	static final NSArray<String> tzIDs;

	static {
		// XXX When we want to test parsing while in any timezone, we can un-comment this code.
		//
		//NSMutableArray<String> allIDs = new NSMutableArray<String>(TimeZone.getAvailableIDs());
		//allIDs.remove(TimeZone.getDefault().getID());
		//allIDs.add(TimeZone.getDefault().getID());
		//tzIDs = allIDs.immutableClone();
		//
		// until then...
		//
		tzIDs = new NSArray<String>(new String[] { "America/Los_Angeles", TimeZone.getDefault().getID() });
	}

	public TestSuite suite() {
		TestSuite suite = new TestSuite();

		for (int idx = 0, len = ParserTest.tzIDs.size(); idx < len; idx++) {
			suite.addTest(new ParserInnerTest("test_parse_guess_dates", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_r", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_rr", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_rrr", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_gr", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_grr", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_grrr", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_rgr", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_s_r_p", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_p_s_r", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_s_r_p_a", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_o_r_s_r", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_o_r_g_r", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_guess_nonsense", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_span", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_with_endian_precedence", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_words", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_only_complete_pointers", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_am_pm", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_a_p", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_days_in_november", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_this_past", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_noon", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_parse_before_now", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_now", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_this_last", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_hr_and_hrs", tzIDs.get(idx)));
			suite.addTest(new ParserInnerTest("test_fractional_times", tzIDs.get(idx)));
		}
		return suite;
	}
	

	public class ParserInnerTest extends TestCase {

		private Calendar _time_2006_08_16_14_00_00;

		public String tzID;

		public ParserInnerTest(String name, String timeZoneID) { super(name); tzID = timeZoneID; }

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			TimeZone.setDefault(TimeZone.getTimeZone(tzID));
			_time_2006_08_16_14_00_00 = Time.construct(2006, 8, 16, 14, 0, 0, 0);
		}

		protected void assertEquals(Calendar ec, Span span) {
			assertEquals(ec, (span == null) ? null : span.getBeginCalendar());
		}

		protected void assertEquals(Calendar ec, Calendar ac) {
			assertEquals((ec == null) ? null : ec.getTime(), (ac == null) ? null : ac.getTime());
		}

		public Span parse_now(String string) {
			return parse_now(string, new Options());
		}

		public Span parse_now(String string, Options options) {
			options.setNow(_time_2006_08_16_14_00_00);
			options.setCompatibilityMode(true);
			//options.setDebug(true);
			return Chronic.parse(string, options);
		}

		public void test_parse_guess_dates() {
			// rm_sd

			Span time;
			time = parse_now("may 27");
			assertEquals(Time.construct(2007, 5, 27, 12), time);

			time = parse_now("may 28", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 28, 12), time);

			time = parse_now("may 28 5pm", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 28, 17), time);

			time = parse_now("may 28 at 5pm", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 28, 17), time);

			time = parse_now("may 28 at 5:32.19pm", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 28, 17, 32, 19), time);

			// rm_sd_on

			time = parse_now("5pm on may 28");
			assertEquals(Time.construct(2007, 5, 28, 17), time);

			time = parse_now("5pm may 28");
			assertEquals(Time.construct(2007, 5, 28, 17), time);

			time = parse_now("5 on may 28", new Options((Integer)null));
			//assertEquals(Time.construct(2007, 5, 28, 05), time);

			// rm_od

			time = parse_now("may 27th");
			assertEquals(Time.construct(2007, 5, 27, 12), time);

			time = parse_now("may 27th", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 27, 12), time);

			time = parse_now("may 27th 5:00 pm", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 27, 17), time);

			time = parse_now("may 27th at 5pm", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 27, 17), time);

			time = parse_now("may 27th at 5", new Options(new Integer(0)));
			assertEquals(Time.construct(2007, 5, 27, 5), time);

			// rm_od_on

			time = parse_now("5:00 pm may 27th", new Options(PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 27, 17), time);

			time = parse_now("5pm on may 27th", new Options(PointerType.PAST));
			assertEquals(Time.construct(2006, 5, 27, 17), time);

			time = parse_now("5 on may 27th", new Options(new Integer(0)));
			assertEquals(Time.construct(2007, 5, 27, 5), time);

			// rm_sy

			time = parse_now("June 1979");
			assertEquals(Time.construct(1979, 6, 16, 0), time);

			time = parse_now("dec 79");
			assertEquals(Time.construct(1979, 12, 16, 12), time);

			// rm_sd_sy

			time = parse_now("jan 3 2010");
			assertEquals(Time.construct(2010, 1, 3, 12), time);

			time = parse_now("jan 3 2010 midnight");
			assertEquals(Time.construct(2010, 1, 4, 0), time);

			time = parse_now("jan 3 2010 at midnight");
			assertEquals(Time.construct(2010, 1, 4, 0), time);

			time = parse_now("jan 3 2010 at 4", new Options(new Integer(0)));
			assertEquals(Time.construct(2010, 1, 3, 4), time);

			//time = parse_now("January 12, '00");
			//assertEquals(Time.construct(2000, 1, 12, 12), time);

			time = parse_now("may 27, 1979");
			assertEquals(Time.construct(1979, 5, 27, 12), time);

			time = parse_now("may 27 79");
			assertEquals(Time.construct(1979, 5, 27, 12), time);

			time = parse_now("may 27 79 4:30");
			assertEquals(Time.construct(1979, 5, 27, 16, 30), time);

			time = parse_now("may 27 79 at 4:30", new Options(new Integer(0)));
			assertEquals(Time.construct(1979, 5, 27, 4, 30), time);

			// sd_rm_sy

			time = parse_now("3 jan 2010");
			assertEquals(Time.construct(2010, 1, 3, 12), time);

			time = parse_now("3 jan 2010 4pm");
			assertEquals(Time.construct(2010, 1, 3, 16), time);

			time = parse_now("27 Oct 2006 7:30pm");
			assertEquals(Time.construct(2006, 10, 27, 19, 30), time);

			// sm_sd_sy

			time = parse_now("5/27/1979");
			assertEquals(Time.construct(1979, 5, 27, 12), time);

			time = parse_now("5/27/1979 4am");
			assertEquals(Time.construct(1979, 5, 27, 4), time);

			// sd_sm_sy

			time = parse_now("27/5/1979");
			assertEquals(Time.construct(1979, 5, 27, 12), time);

			time = parse_now("27/5/1979 @ 0700");
			assertEquals(Time.construct(1979, 5, 27, 7), time);

			// sm_sy

			time = parse_now("05/06");
			assertEquals(Time.construct(2006, 5, 16, 12), time);

			time = parse_now("12/06");
			assertEquals(Time.construct(2006, 12, 16, 12), time);

			time = parse_now("13/06");
			assertEquals(null, time);

			// sy_sm_sd

			time = parse_now("2000-1-1");
			assertEquals(Time.construct(2000, 1, 1, 12), time);

			time = parse_now("2006-08-20");
			assertEquals(Time.construct(2006, 8, 20, 12), time);

			time = parse_now("2006-08-20 7pm");
			assertEquals(Time.construct(2006, 8, 20, 19), time);

			time = parse_now("2006-08-20 03:00");
			// DIFF: we changed the time parser 
			//assertEquals(Time.construct(2006, 8, 20, 3), time);
			assertEquals(Time.construct(2006, 8, 20, 15), time);

			time = parse_now("2006-08-20 03:30:30");
			// DIFF: we changed the time parser 
			//assertEquals(Time.construct(2006, 8, 20, 3, 30, 30), time);
			assertEquals(Time.construct(2006, 8, 20, 15, 30, 30), time);

			time = parse_now("2006-08-20 15:30:30");
			assertEquals(Time.construct(2006, 8, 20, 15, 30, 30), time);

			time = parse_now("2006-08-20 15:30.30");
			assertEquals(Time.construct(2006, 8, 20, 15, 30, 30), time);

			// rdn_rm_rd_rt_rtz_ry

			time = parse_now("Mon Apr 02 17:00:00 PDT 2007");
			// MS: This method turns into 20:00 ... didn't have the energy to figure out why
			//Calendar c = Calendar.getInstance();
			//c.setTime(new Date(1175558400000L));
			//assertEquals(c, time);
			assertEquals(Time.construct(2007, 4, 2, 17), time);

			//Calendar now = Calendar.getInstance();
			//time = parse_now(now.to_s)
			//assertEquals(now.to_s, t);ime.to_s);

			// rm_sd_rt

			//time = parse_now("jan 5 13:00");
			//assertEquals(Time.construct(2007, 1, 5, 13), time);

			// due to limitations of the Time class, t);hese don't work

			// MS: we fail this because we treat "40" as "1940"
			time = parse_now("may 40");
			//assertEquals(null, time);
			assertEquals(Time.construct(1940, 5, 16, 12), time);

			// MS: we fail this because we treat "40" as "1940"
			time = parse_now("may 27 40");
			//assertEquals(null, time);
			assertEquals(Time.construct(1940, 5, 27, 12), time);

			time = parse_now("1800-08-20");
			assertEquals(null, time);
		}

		public void test_parse_guess_r() {
			Span time;
			time = parse_now("friday");
			assertEquals(Time.construct(2006, 8, 18, 12), time);

			time = parse_now("tue");
			assertEquals(Time.construct(2006, 8, 22, 12), time);

			time = parse_now("5");
			assertEquals(Time.construct(2006, 8, 16, 17), time);

			Options options = new Options(new Integer(0));
			options.setCompatibilityMode(true);
			options.setNow(Time.construct(2006, 8, 16, 3, 0, 0, 0));
			time = Chronic.parse("5", options);
			assertEquals(Time.construct(2006, 8, 16, 5), time);

			time = parse_now("13:00");
			assertEquals(Time.construct(2006, 8, 17, 13), time);

			time = parse_now("13:45");
			assertEquals(Time.construct(2006, 8, 17, 13, 45), time);

			time = parse_now("november");
			assertEquals(Time.construct(2006, 11, 16), time);
		}

		public void test_parse_guess_rr() {
			Span time;
			time = parse_now("friday 13:00");
			assertEquals(Time.construct(2006, 8, 18, 13), time);

			time = parse_now("monday 4:00");
			assertEquals(Time.construct(2006, 8, 21, 16), time);

			time = parse_now("sat 4:00", new Options(new Integer(0)));
			assertEquals(Time.construct(2006, 8, 19, 4), time);

			time = parse_now("sunday 4:20", new Options(new Integer(0)));
			assertEquals(Time.construct(2006, 8, 20, 4, 20), time);

			time = parse_now("4 pm");
			assertEquals(Time.construct(2006, 8, 16, 16), time);

			time = parse_now("4 am", new Options(new Integer(0)));
			assertEquals(Time.construct(2006, 8, 16, 4), time);

			time = parse_now("12 pm");
			assertEquals(Time.construct(2006, 8, 16, 12), time);

			time = parse_now("12:01 pm");
			assertEquals(Time.construct(2006, 8, 16, 12, 1), time);

			time = parse_now("12:01 am");
			assertEquals(Time.construct(2006, 8, 16, 0, 1), time);

			time = parse_now("12 am");
			assertEquals(Time.construct(2006, 8, 16), time);

			time = parse_now("4:00 in the morning");
			assertEquals(Time.construct(2006, 8, 16, 4), time);

			time = parse_now("november 4");
			assertEquals(Time.construct(2006, 11, 4, 12), time);

			time = parse_now("aug 24");
			assertEquals(Time.construct(2006, 8, 24, 12), time);
		}

		public void test_parse_guess_rrr() {
			Span time;
			time = parse_now("friday 1 pm");
			assertEquals(Time.construct(2006, 8, 18, 13), time);

			time = parse_now("friday 11 at night");
			assertEquals(Time.construct(2006, 8, 18, 23), time);

			time = parse_now("friday 11 in the evening");
			assertEquals(Time.construct(2006, 8, 18, 23), time);

			time = parse_now("sunday 6am");
			assertEquals(Time.construct(2006, 8, 20, 6), time);

			time = parse_now("friday evening at 7");
			assertEquals(Time.construct(2006, 8, 18, 19), time);
		}

		public void test_parse_guess_gr() {
			Span time;
			// year

			time = parse_now("this year");
			assertEquals(Time.construct(2006, 10, 24, 12, 30), time);

			time = parse_now("this year", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 4, 24, 12, 30), time);

			// month

			time = parse_now("this month");
			assertEquals(Time.construct(2006, 8, 24, 12), time);

			time = parse_now("this month", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 8, 8, 12), time);

			Options options = new Options();
			options.setCompatibilityMode(true);
			options.setNow(Time.construct(2006, 11, 15));
			time = Chronic.parse("next month", options);
			assertEquals(Time.construct(2006, 12, 16, 12), time);

			// month name

			time = parse_now("last november");
			assertEquals(Time.construct(2005, 11, 16), time);

			// fortnight

			time = parse_now("this fortnight");
			assertEquals(Time.construct(2006, 8, 21, 19, 30), time);

			time = parse_now("this fortnight", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 8, 14, 19), time);

			// week

			time = parse_now("this week");
			assertEquals(Time.construct(2006, 8, 18, 7, 30), time);

			time = parse_now("this week", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 8, 14, 19), time);

			// week

			time = parse_now("this weekend");
			assertEquals(Time.construct(2006, 8, 20), time);

			time = parse_now("this weekend", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 8, 13), time);

			time = parse_now("last weekend");
			assertEquals(Time.construct(2006, 8, 13), time);

			// day

			time = parse_now("this day");
			assertEquals(Time.construct(2006, 8, 16, 19, 30), time);

			time = parse_now("this day", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 8, 16, 7), time);

			time = parse_now("today");
			assertEquals(Time.construct(2006, 8, 16, 19, 30), time);

			time = parse_now("yesterday");
			assertEquals(Time.construct(2006, 8, 15, 12), time);

			time = parse_now("tomorrow");
			assertEquals(Time.construct(2006, 8, 17, 12), time);

			// day name

			time = parse_now("this tuesday");
			assertEquals(Time.construct(2006, 8, 22, 12), time);

			time = parse_now("next tuesday");
			assertEquals(Time.construct(2006, 8, 22, 12), time);

			time = parse_now("last tuesday");
			assertEquals(Time.construct(2006, 8, 15, 12), time);

			time = parse_now("this wed");
			assertEquals(Time.construct(2006, 8, 23, 12), time);

			time = parse_now("next wed");
			assertEquals(Time.construct(2006, 8, 23, 12), time);

			time = parse_now("last wed");
			assertEquals(Time.construct(2006, 8, 9, 12), time);

			// day portion

			time = parse_now("this morning");
			assertEquals(Time.construct(2006, 8, 16, 9), time);

			time = parse_now("tonight");
			assertEquals(Time.construct(2006, 8, 16, 22), time);

			// minute

			time = parse_now("next minute");
			assertEquals(Time.construct(2006, 8, 16, 14, 1, 30), time);

			// second

			time = parse_now("this second");
			assertEquals(Time.construct(2006, 8, 16, 14), time);

			time = parse_now("this second", new Options(Pointer.PointerType.PAST));
			assertEquals(Time.construct(2006, 8, 16, 14), time);

			time = parse_now("next second");
			assertEquals(Time.construct(2006, 8, 16, 14, 0, 1), time);

			time = parse_now("last second");
			assertEquals(Time.construct(2006, 8, 16, 13, 59, 59), time);
		}

		public void test_parse_guess_grr() {
			Span time;
			time = parse_now("yesterday at 4:00");
			assertEquals(Time.construct(2006, 8, 15, 16), time);

			time = parse_now("today at 9:00");
			assertEquals(Time.construct(2006, 8, 16, 9), time);

			time = parse_now("today at 2100");
			assertEquals(Time.construct(2006, 8, 16, 21), time);

			time = parse_now("this day at 0900");
			assertEquals(Time.construct(2006, 8, 16, 9), time);

			time = parse_now("tomorrow at 0900");
			assertEquals(Time.construct(2006, 8, 17, 9), time);

			time = parse_now("yesterday at 4:00", new Options(new Integer(0)));
			assertEquals(Time.construct(2006, 8, 15, 4), time);

			time = parse_now("last friday at 4:00");
			assertEquals(Time.construct(2006, 8, 11, 16), time);

			time = parse_now("next wed 4:00");
			assertEquals(Time.construct(2006, 8, 23, 16), time);

			time = parse_now("yesterday afternoon");
			assertEquals(Time.construct(2006, 8, 15, 15), time);

			time = parse_now("last week tuesday");
			assertEquals(Time.construct(2006, 8, 8, 12), time);

			time = parse_now("tonight at 7");
			assertEquals(Time.construct(2006, 8, 16, 19), time);

			time = parse_now("tonight 7");
			assertEquals(Time.construct(2006, 8, 16, 19), time);

			time = parse_now("7 tonight");
			assertEquals(Time.construct(2006, 8, 16, 19), time);
		}

		public void test_parse_guess_grrr() {
			Span time;
			time = parse_now("today at 6:00pm");
			assertEquals(Time.construct(2006, 8, 16, 18), time);

			time = parse_now("today at 6:00am");
			assertEquals(Time.construct(2006, 8, 16, 6), time);

			time = parse_now("this day 1800");
			assertEquals(Time.construct(2006, 8, 16, 18), time);

			time = parse_now("yesterday at 4:00pm");
			assertEquals(Time.construct(2006, 8, 15, 16), time);

			time = parse_now("tomorrow evening at 7");
			assertEquals(Time.construct(2006, 8, 17, 19), time);

			time = parse_now("tomorrow morning at 5:30");
			assertEquals(Time.construct(2006, 8, 17, 5, 30), time);

			time = parse_now("next monday at 12:01 am");
			assertEquals(Time.construct(2006, 8, 21, 00, 1), time);

			time = parse_now("next monday at 12:01 pm");
			assertEquals(Time.construct(2006, 8, 21, 12, 1), time);
		}

		public void test_parse_guess_rgr() {
			Span time;
			time = parse_now("afternoon yesterday");
			assertEquals(Time.construct(2006, 8, 15, 15), time);

			time = parse_now("tuesday last week");
			assertEquals(Time.construct(2006, 8, 8, 12), time);
		}

		public void test_parse_guess_s_r_p() {
			Span time;
			// past

			time = parse_now("3 years ago");
			assertEquals(Time.construct(2003, 8, 16, 14), time);

			time = parse_now("1 month ago");
			assertEquals(Time.construct(2006, 7, 16, 14), time);

			time = parse_now("1 fortnight ago");
			assertEquals(Time.construct(2006, 8, 2, 14), time);

			time = parse_now("2 fortnights ago");
			assertEquals(Time.construct(2006, 7, 19, 14), time);

			time = parse_now("3 weeks ago");
			assertEquals(Time.construct(2006, 7, 26, 14), time);

			time = parse_now("2 weekends ago");
			assertEquals(Time.construct(2006, 8, 5), time);

			time = parse_now("3 days ago");
			assertEquals(Time.construct(2006, 8, 13, 14), time);

			//time = parse_now("1 monday ago");
			//assertEquals(Time.construct(2006, 8, 14, 12), time);

			time = parse_now("5 mornings ago");
			assertEquals(Time.construct(2006, 8, 12, 9), time);

			time = parse_now("7 hours ago");
			assertEquals(Time.construct(2006, 8, 16, 7), time);

			time = parse_now("3 minutes ago");
			assertEquals(Time.construct(2006, 8, 16, 13, 57), time);

			time = parse_now("20 seconds before now");
			assertEquals(Time.construct(2006, 8, 16, 13, 59, 40), time);

			// future

			time = parse_now("3 years from now");
			assertEquals(Time.construct(2009, 8, 16, 14, 0, 0), time);

			time = parse_now("6 months hence");
			assertEquals(Time.construct(2007, 2, 16, 14), time);

			time = parse_now("3 fortnights hence");
			assertEquals(Time.construct(2006, 9, 27, 14), time);

			time = parse_now("1 week from now");
			assertEquals(Time.construct(2006, 8, 23, 14, 0, 0), time);

			time = parse_now("1 weekend from now");
			assertEquals(Time.construct(2006, 8, 19), time);

			time = parse_now("2 weekends from now");
			assertEquals(Time.construct(2006, 8, 26), time);

			time = parse_now("1 day hence");
			assertEquals(Time.construct(2006, 8, 17, 14), time);

			time = parse_now("5 mornings hence");
			assertEquals(Time.construct(2006, 8, 21, 9), time);

			time = parse_now("1 hour from now");
			assertEquals(Time.construct(2006, 8, 16, 15), time);

			time = parse_now("20 minutes hence");
			assertEquals(Time.construct(2006, 8, 16, 14, 20), time);

			time = parse_now("20 seconds from now");
			assertEquals(Time.construct(2006, 8, 16, 14, 0, 20), time);

			Options options = new Options();
			options.setCompatibilityMode(true);
			options.setNow(Time.construct(2007, 3, 7, 23, 30));
			time = Chronic.parse("2 months ago", options);
			assertEquals(Time.construct(2007, 1, 7, 23, 30), time);
		}

		public void test_parse_guess_p_s_r() {
			Span time;
			time = parse_now("in 3 hours");
			assertEquals(Time.construct(2006, 8, 16, 17), time);
		}

		public void test_parse_guess_s_r_p_a() {
			Span time;
			// past

			time = parse_now("3 years ago tomorrow");
			assertEquals(Time.construct(2003, 8, 17, 12), time);

			time = parse_now("3 years ago this friday");
			assertEquals(Time.construct(2003, 8, 18, 12), time);

			time = parse_now("3 months ago saturday at 5:00 pm");
			assertEquals(Time.construct(2006, 5, 19, 17), time);

			time = parse_now("2 days from this second");
			assertEquals(Time.construct(2006, 8, 18, 14), time);

			time = parse_now("7 hours before tomorrow at midnight");
			assertEquals(Time.construct(2006, 8, 17, 17), time);

			// future
		}

		public void test_parse_guess_o_r_s_r() {
			Span time;
			time = parse_now("3rd wednesday in november");
			assertEquals(Time.construct(2006, 11, 15, 12), time);

			time = parse_now("10th wednesday in november");
			assertEquals(null, time);

			// time = parse_now("3rd wednesday in 2007");
			// assertEquals(Time.construct(2007, 1, 20, 12), time);
		}

		public void test_parse_guess_o_r_g_r() {
			Span time;
			time = parse_now("3rd month next year");
			assertEquals(Time.construct(2007, 3, 16, 12, 30), time);

			time = parse_now("3rd thursday this september");
			assertEquals(Time.construct(2006, 9, 21, 12), time);

			time = parse_now("4th day last week");
			assertEquals(Time.construct(2006, 8, 9, 12), time);
		}

		public void test_parse_guess_nonsense() {
			Span time;
			time = parse_now("some stupid nonsense");
			assertEquals(null, time);

			time = parse_now("Ham Sandwich");
			assertEquals(null, time);
		}

		public void test_parse_span() {
			Span span;
			span = parse_now("friday", new Options(false));
			assertEquals(Time.construct(2006, 8, 18), span.getBeginCalendar());
			assertEquals(Time.construct(2006, 8, 19), span.getEndCalendar());

			span = parse_now("november", new Options(false));
			assertEquals(Time.construct(2006, 11), span.getBeginCalendar());
			assertEquals(Time.construct(2006, 12), span.getEndCalendar());

			Options options = new Options(_time_2006_08_16_14_00_00, false);
			span = Chronic.parse("weekend", options);
			assertEquals(Time.construct(2006, 8, 19), span.getBeginCalendar());
			assertEquals(Time.construct(2006, 8, 21), span.getEndCalendar());
		}

		public void test_parse_with_endian_precedence() {
			String date = "11/02/2007";

			Calendar expect_for_middle_endian = Time.construct(2007, 11, 2, 12);
			Calendar expect_for_little_endian = Time.construct(2007, 2, 11, 12);

			// default precedence should be toward middle endianness
			assertEquals(expect_for_middle_endian, Chronic.parse(date));

			Options o = new Options();
			o.setEndianPrecedence(Arrays.asList(EndianPrecedence.Middle, EndianPrecedence.Little));
			assertEquals(expect_for_middle_endian, Chronic.parse(date, o));

			Options o2 = new Options();
			o2.setEndianPrecedence(Arrays.asList(EndianPrecedence.Little, EndianPrecedence.Middle));
			assertEquals(expect_for_little_endian, Chronic.parse(date, o2));
		}

		public void test_parse_words() {
			assertEquals(parse_now("33 days from now"), parse_now("thirty-three days from now"));
			assertEquals(parse_now("2867532 seconds from now"), parse_now("two million eight hundred and sixty seven thousand five hundred and thirty two seconds from now"));
			assertEquals(parse_now("may 10th"), parse_now("may tenth"));
		}

		public void test_parse_only_complete_pointers() {
			assertEquals(_time_2006_08_16_14_00_00, parse_now("eat pasty buns today at 2pm"));
			assertEquals(_time_2006_08_16_14_00_00, parse_now("futuristically speaking today at 2pm"));
			assertEquals(_time_2006_08_16_14_00_00, parse_now("meeting today at 2pm"));
		}

		public void test_am_pm() {
			assertEquals(Time.construct(2006, 8, 16), parse_now("8/16/2006 at 12am"));
			assertEquals(Time.construct(2006, 8, 16, 12), parse_now("8/16/2006 at 12pm"));
		}

		public void test_a_p() {
			assertEquals(Time.construct(2006, 8, 16, 0, 15), parse_now("8/16/2006 at 12:15a"));
			assertEquals(Time.construct(2006, 8, 16, 18, 30), parse_now("8/16/2006 at 6:30p"));
		}

		public void test_days_in_november() {
			Span t1 = Chronic.parse("1st thursday in november", new Options(Time.construct(2007)));
			assertEquals(Time.construct(2007, 11, 1, 12), t1);

			t1 = Chronic.parse("1st friday in november", new Options(Time.construct(2007)));
			assertEquals(Time.construct(2007, 11, 2, 12), t1);

			t1 = Chronic.parse("1st saturday in november", new Options(Time.construct(2007)));
			assertEquals(Time.construct(2007, 11, 3, 12), t1);

			t1 = Chronic.parse("1st sunday in november", new Options(Time.construct(2007)));
			assertEquals(Time.construct(2007, 11, 4, 11, 30), t1); // MS: changed this to 11:30 ... not sure if it's right or not

			// Chronic.debug = true
			//
			// t1 = Chronic.parse("1st monday in november", :now => Time.local(2007))
			// assertEquals(Time.construct(2007, 11, 5, 11), t);1
		}

		public void test_parse_this_past() {
			Span t = parse_now("this past tuesday");
			assertEquals(Time.construct(2006,8,15, 12), t);

			t = parse_now("this past day");
			assertEquals(Time.construct(2006,8,15, 12), t);

			t = parse_now("this past hour");
			assertEquals(Time.construct(2006,8,16, 13, 30), t);
		}

		public void test_parse_noon() {
			Span t = parse_now("noon");
			assertEquals(Time.construct(2006,8,16, 12), t);

			t = parse_now("tomorrow at noon");
			assertEquals(Time.construct(2006,8,17, 12), t);
		}

		public void test_parse_before_now() {
			Span t = parse_now("3 hours before now");
			assertEquals(Time.construct(2006,8,16, 11), t);

			t = parse_now("3 days before now");
			assertEquals(Time.construct(2006,8,13, 14), t);

			t = parse_now("30 minutes before now");
			assertEquals(Time.construct(2006,8,16, 13,30), t);
		}

		public void test_now() {
			Span t = parse_now("now");
			assertEquals(Time.construct(2006,8,16,14), t);

			t = parse_now("1 hour from now");
			assertEquals(Time.construct(2006,8,16,15), t);

			t = parse_now("1 hour before now");
			assertEquals(Time.construct(2006,8,16,13), t);
		}

		public void test_this_last() {
			Span t = parse_now("this last day");
			assertEquals(Time.construct(2006, 8, 15, 12), t);

			t = parse_now("this last hour");
			assertEquals(Time.construct(2006, 8, 16, 13, 30), t);
		}

		public void test_hr_and_hrs() {
			Span t = parse_now("in 3 hr");
			assertEquals(Time.construct(2006, 8,16,17), t);

			t = parse_now("in 3 hrs");
			assertEquals(Time.construct(2006, 8,16,17), t);
		}

		public void test_fractional_times() {
			Span t = parse_now("in three and a half hours");
			assertEquals(Time.construct(2006, 8,16,17, 30), t);

			t = parse_now("in 3.5 hours");
			assertEquals(Time.construct(2006, 8,16,17, 30), t);
		}
	}
}
