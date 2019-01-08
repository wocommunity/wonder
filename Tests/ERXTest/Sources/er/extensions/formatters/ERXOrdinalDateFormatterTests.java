package er.extensions.formatters;

import java.text.Format;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Assert;

import junit.framework.TestCase;

import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;

/**
 * Tests for ERXOrdinalDateFormatter. 
 *
 * @author qdolan
 */
public class ERXOrdinalDateFormatterTests extends TestCase {
  public void testFormatNSTimestamp() {
    ERXOrdinalDateFormatter formatter = new ERXOrdinalDateFormatter("d'th'");
    formatter.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    GregorianCalendar cal = new GregorianCalendar(2000, 0, 1, 23, 59, 59);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    Assert.assertEquals("1st", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("2nd", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("3rd", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("4th", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 7);
    Assert.assertEquals("11th", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("12th", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("13th", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("14th", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 7);
    Assert.assertEquals("21st", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("22nd", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("23rd", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 1);
    Assert.assertEquals("24th", formatter.format(new NSTimestamp(cal.getTime())));
    cal.add(GregorianCalendar.DAY_OF_MONTH, 7);
    Assert.assertEquals("31st", formatter.format(new NSTimestamp(cal.getTime())));
  }
  public void testFormatThenParse() throws ParseException {
    Format dateFormatter = new ERXOrdinalDateFormatter("d'th 'MMM' 'yyyy' 'HH':'mm':'ss");
    NSTimestamp timestamp = new NSTimestamp(new GregorianCalendar(2000, 0, 1).getTime());
    String formatted = dateFormatter.format(timestamp);
    NSTimestamp parsed = (NSTimestamp) dateFormatter.parseObject(formatted);
    String formatted2 = dateFormatter.format(parsed);
    assertEquals(formatted, formatted2);
  }
}
