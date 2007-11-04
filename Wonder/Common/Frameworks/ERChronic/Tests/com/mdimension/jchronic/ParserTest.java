package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;

public class ParserTest extends TestCase {
  public static final Calendar TIME_2006_08_16_14_00_00 = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  private Calendar _time_2006_08_16_14_00_00;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _time_2006_08_16_14_00_00 = TIME_2006_08_16_14_00_00;
  }

  protected void assertEquals(Calendar ec, Span span) {
    assertEquals(ec, (span == null) ? null : span.getBeginCalendar());
  }

  protected void assertEquals(Calendar ec, Calendar ac) {
    assertEquals((ec == null) ? null : ec.getTime(), (ac == null) ? null : ac.getTime());
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

    // rm_od

    time = parse_now("may 27th");
    assertEquals(Time.construct(2007, 5, 27, 12), time);

    time = parse_now("may 27th", new Options(Pointer.PointerType.PAST));
    assertEquals(Time.construct(2006, 5, 27, 12), time);

    time = parse_now("may 27th 5:00 pm", new Options(Pointer.PointerType.PAST));
    assertEquals(Time.construct(2006, 5, 27, 17), time);

    time = parse_now("may 27th at 5pm", new Options(Pointer.PointerType.PAST));
    assertEquals(Time.construct(2006, 5, 27, 17), time);

    time = parse_now("may 27th at 5", new Options(0));
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

    time = parse_now("jan 3 2010 at 4", new Options(0));
    assertEquals(Time.construct(2010, 1, 3, 4), time);

    //time = parse_now("January 12, '00");
    //assertEquals(Time.construct(2000, 1, 12, 12), time);

    time = parse_now("may 27 79");
    assertEquals(Time.construct(1979, 5, 27, 12), time);

    time = parse_now("may 27 79 4:30");
    assertEquals(Time.construct(1979, 5, 27, 16, 30), time);

    time = parse_now("may 27 79 at 4:30", new Options(0));
    assertEquals(Time.construct(1979, 5, 27, 4, 30), time);

    // sd_rm_sy

    time = parse_now("3 jan 2010");
    assertEquals(Time.construct(2010, 1, 3, 12), time);

    time = parse_now("3 jan 2010 4pm");
    assertEquals(Time.construct(2010, 1, 3, 16), time);

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
    assertEquals(Time.construct(2007, 4, 2, 17), time);

    //Calendar now = Calendar.getInstance();
    //time = parse_now(now.to_s)
    //assertEquals(now.to_s, time.to_s);

    // rm_sd_rt

    //time = parse_now("jan 5 13:00");
    //assertEquals(Time.construct(2007, 1, 5, 13), time);

    // due to limitations of the Time class, these don't work

    time = parse_now("may 40");
    assertEquals(null, time);

    time = parse_now("may 27 40");
    assertEquals(null, time);

    time = parse_now("1800-08-20");
    assertEquals(null, time);
  }

  public void test_foo() {
    Chronic.parse("two months ago this friday");
  }

  public void test_parse_guess_r() {
    Span time;
    time = parse_now("friday");
    assertEquals(Time.construct(2006, 8, 18, 12), time);

    time = parse_now("tue");
    assertEquals(Time.construct(2006, 8, 22, 12), time);

    time = parse_now("5");
    assertEquals(Time.construct(2006, 8, 16, 17), time);

    Options options = new Options(0);
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

    time = parse_now("sat 4:00", new Options(0));
    assertEquals(Time.construct(2006, 8, 19, 4), time);

    time = parse_now("sunday 4:20", new Options(0));
    assertEquals(Time.construct(2006, 8, 20, 4, 20), time);

    time = parse_now("4 pm");
    assertEquals(Time.construct(2006, 8, 16, 16), time);

    time = parse_now("4 am", new Options(0));
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

    time = parse_now("yesterday at 4:00", new Options(0));
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

  public Span parse_now(String string) {
    return parse_now(string, new Options());
  }

  public Span parse_now(String string, Options options) {
    options.setNow(TIME_2006_08_16_14_00_00);
    options.setCompatibilityMode(true);
    return Chronic.parse(string, options);
  }
}
