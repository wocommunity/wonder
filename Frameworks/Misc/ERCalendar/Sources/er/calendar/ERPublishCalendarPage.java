package er.calendar;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

/**
 * ERPublishCalendarPage is a WebObjects component for dynamically
 * generated iCalendar documents.
 * <p>
 * The response created by ERPublishCalendarPage is an iCalendar
 * document (.ics) containing the events added to ERPublishCalendarPage
 * by the application (see {@link #addEvent(ERCalendarEvent) addEvent}).
 * An iCalendar-aware application, such as Apple's iCal, can
 * subscribe to such a calendar, provided that the page has a fixed
 * URL (either is the "Main" page, or a direct action serves the page).
 * <p>
 * Events added to a ERPublishCalendarPage is objects of any class that
 * implements {@link ERCalendarEvent the ERCalendarEvent interface}.
 * Existing classes (for example EOCustomObject subclasses), that
 * correspond to calendar events, can easily be modified to
 * implement {@link ERCalendarEvent ERCalendarEvent} and thus be added
 * directly to ERPublishCalendarPage. If existing classes does not
 * directly correspond to calendar events, create events from business
 * data (or some algorithm) using either the included
 * {@link ERSimpleEvent ERSimpleEvent class}, a subclass of
 * {@link ERSimpleEvent ERSimpleEvent}, or any other class implementing
 * {@link ERCalendarEvent the ERCalendarEvent interface}.
 *
 * @author 	Johan Carlberg &lt;johan@oops.se&gt;
 * @version 	1.0, 2002-09-30
 */
public class ERPublishCalendarPage extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected String calendarName;
    protected String calendarTimeZone;
    protected final int maxLineLength = 75;
    public static String newline = System.getProperty("line.separator");

    protected NSMutableArray events;
    public ERCalendarEvent event;
    protected NSTimestamp eventTimestamp;
    protected NSTimestampFormatter dateTimeFormatter;
    protected NSTimestampFormatter dateFormatter;
    protected NSTimestampFormatter utcDateTimeFormatter;
    protected NSTimestampFormatter timeZoneFormatter;

    /**
     * Standard constructor for WOComponent subclasses.
     *
     * @param context   context of a transaction
     * @see             WOComponent#pageWithName(String)
     * @see             WOApplication#pageWithName(String, WOContext)
     */
    public ERPublishCalendarPage(WOContext context) {
        super(context);

	events = new NSMutableArray();
	calendarTimeZone = new NSTimestampFormatter ("%Z").format (new NSTimestamp());
	dateTimeFormatter = new NSTimestampFormatter ("%Y%m%dT%H%M%S");
	dateTimeFormatter.setDefaultFormatTimeZone (NSTimeZone.localTimeZone());
	dateFormatter = new NSTimestampFormatter ("%Y%m%d");
	dateFormatter.setDefaultFormatTimeZone (NSTimeZone.localTimeZone());
	utcDateTimeFormatter = new NSTimestampFormatter ("%Y%m%dT%H%M%SZ");
	utcDateTimeFormatter.setDefaultFormatTimeZone (NSTimeZone.timeZoneWithName ("UTC", false));
	timeZoneFormatter = new NSTimestampFormatter ("%Z");
	timeZoneFormatter.setDefaultFormatTimeZone (NSTimeZone.localTimeZone());
    }

    /**
     * Modifies content encoding to UTF8, and content type to text/calendar.
     *
     * @param aResponse  the HTTP response that an application returns to a Web server to complete a cycle of the request-response loop
     * @param aContext   context of a transaction
     */
    @Override
    public void appendToResponse (WOResponse aResponse, WOContext aContext)
    {
	eventTimestamp = new NSTimestamp();
	aResponse.setContentEncoding("UTF-8");
	super.appendToResponse (aResponse, aContext);
	aResponse.setHeader ("text/calendar","content-type");
	try {
	    aResponse.setContent(new NSData(foldLongLinesInString(new String(aResponse.content().bytes(), "UTF-8")).getBytes("UTF-8")));
	} catch (java.io.UnsupportedEncodingException exception) {
	    // If encoding is not supported, content of response is left unmodified
	    // (although exceptions will be thrown elsewhere if UTF-8 is unsupported).
	}
    }

    /**
     * Adds an event to the calendar.
     *
     * @param event the event to be included in the calendar
     * @see         ERCalendarEvent
     * @see	    #addEventsFromArray(NSArray)
     */
    public void addEvent (ERCalendarEvent event) {
	events.addObject (event);
    }

    /**
     * Adds an array of events to the calendar.
     *
     * @param eventsArray the events to be included in the calendar
     * @see		  ERCalendarEvent
     * @see		  #addEvent(ERCalendarEvent)
     */
    public void addEventsFromArray (NSArray eventsArray) {
	events.addObjectsFromArray (eventsArray);
    }

    /**
     * Removes a previously added event from the calendar.
     *
     * @param event the event to be removed from the calendar
     * @see         ERCalendarEvent
     * @see	    #removeEventsInArray(NSArray)
     */
    public void removeEvent (ERCalendarEvent event) {
	events.removeObject (event);
    }

    /**
     * Removes an array of previously added events from the calendar.
     *
     * @param eventsArray the events to be removed from the calendar
     * @see		  ERCalendarEvent
     * @see		  #removeEvent(ERCalendarEvent)
     */
    public void removeEventsInArray (NSArray eventsArray) {
	events.removeObjectsInArray (eventsArray);
    }

    public NSMutableArray events() {
	return events;
    }

    /**
     * @return  name of the calendar
     * @see     #setCalendarName(String)
     */
    public String calendarName()
    {
	return calendarName;
    }
    /**
     * @return  name of the calendar, backslash escaped for inclusion in
     *		iCalendar document.
     * @see     #calendarName
     */
    public String escapedCalendarName()
    {
	return escapedString (calendarName);
    }
    /**
     * Sets the name of the calendar.
     *
     * @param value  name of the calendar
     * @see          #calendarName
     */
    public void setCalendarName (String value)
    {
	calendarName = value;
    }

    /**
     * @return  originating time zone for the calendar (name of the
     *          system default time zone, if not changed by
     *          {@link #setCalendarTimeZone(String) setCalendarTimeZone}
     * @see     #setCalendarTimeZone(String)
     */
    public String calendarTimeZone()
    {
	return calendarTimeZone;
    }
    /**
     * @return  time zone name of the calendar, backslash escaped
     *		for inclusion in iCalendar document.
     * @see     #calendarTimeZone
     */
    public String escapedCalendarTimeZone()
    {
	return escapedString (calendarTimeZone);
    }
    /**
     * Sets the name of the time zone for the calendar.
     *
     * @param value  name of the time zone
     * @see          #calendarTimeZone
     */
    public void setCalendarTimeZone (String value)
    {
	calendarTimeZone = value;
    }

    /**
     * @return  status of the current event, backslash escaped
     *		for inclusion in iCalendar document.
     * @see     ERCalendarEvent#status()
     */
    public String escapedEventStatus()
    {
	return escapedString (event.status());
    }

    /**
     * @return  summary of the current event, backslash escaped
     *		for inclusion in iCalendar document.
     * @see     ERCalendarEvent#summary()
     */
    public String escapedEventSummary()
    {
	return escapedString (event.summary());
    }

    /**
     * @return  unique id of the current event, backslash escaped
     *		for inclusion in iCalendar document.
     * @see     ERCalendarEvent#uniqueId
     */
    public String escapedEventUniqueId()
    {
	return escapedString (event.uniqueId());
    }

    /**
     * @return  timestamp of the current event.
     *		This will always be the current time, as this is
     *		the time the event is converted to an iCalendar
     *		event.
     */
    public NSTimestamp eventTimestamp()
    {
	return eventTimestamp;
    }

    /**
     * @return  the recurring rule frequency, as one of "YEARLY", "MONTHLY",
     *		"WEEKLY", "DAILY", "HOURLY", "MINUTELY", "SECONDLY" depending
     *		on the value returned by {@link ERCalendarEvent#repeatFrequency
     *		repeatFrequency}.
     * @see	ERCalendarEvent#repeatFrequency()
     */
    public String eventRepeatFrequency()
    {
	switch (event.repeatFrequency()) {
	    case Calendar.YEAR:
		return "YEARLY";
	    case Calendar.MONTH:
		return "MONTHLY";
	    case Calendar.WEEK_OF_YEAR:
		return "WEEKLY";
	    case Calendar.DAY_OF_MONTH:
		return "DAILY";
	    case Calendar.HOUR_OF_DAY:
		return "HOURLY";
	    case Calendar.MINUTE:
		return "MINUTELY";
	    case Calendar.SECOND:
		return "SECONDLY";
	    default:
		return null;
	}
    }

    /**
     * @return  month number of a repeating event for use in
     *		the "BYMONTH" parameter.
     */
    public Number eventRepeatMonth()
    {
	GregorianCalendar calendarDate = new GregorianCalendar();

	calendarDate.setTime (event.startTime());
	return Integer.valueOf(calendarDate.get(Calendar.MONTH) + 1);
    }

    /**
     * @return  day of week of a repeating event for use in
     *		the "BYDAY" parameter.
     */
    public String eventRepeatDayOfWeekString()
    {
	String byDay = "";

	if (event.repeatDayOfWeekInMonth() != 0) {
	    byDay = Integer.valueOf(event.repeatDayOfWeekInMonth()).toString();
	}
	switch (event.repeatDayOfWeek()) {
	    case Calendar.SUNDAY:
		byDay += "SU";
		break;
	    case Calendar.MONDAY:
		byDay += "MO";
		break;
	    case Calendar.TUESDAY:
		byDay += "TU";
		break;
	    case Calendar.WEDNESDAY:
		byDay += "WE";
		break;
	    case Calendar.THURSDAY:
		byDay += "TH";
		break;
	    case Calendar.FRIDAY:
		byDay += "FR";
		break;
	    case Calendar.SATURDAY:
		byDay += "SA";
		break;
	}

	return byDay;
    }

    /**
     * @return  days of month of a repeating event for use in
     *		the "BYMONTHDAY" parameter.
     */
    public String eventRepeatDaysOfMonthString()
    {
	return event.repeatDaysOfMonth().componentsJoinedByString (",");
    }

    /**
     * @return  formatter for date/time.
     *		Will format date/times as "20021003T191234",
     *		specified in the local time zone.
     */
    public NSTimestampFormatter dateTimeFormatter()
    {
	return dateTimeFormatter;
    }

    /**
     * @return  formatter for dates.
     *		Will format dates as "20021003",
     *		specified in the local time zone.
     */
    public NSTimestampFormatter dateFormatter()
    {
	return dateFormatter;
    }

    /**
     * @return  formatter for date/time stamps.
     *		Will format date/times as "20021003T171234Z",
     *		specified in UTC (GMT).
     */
    public NSTimestampFormatter utcDateTimeFormatter()
    {
	return utcDateTimeFormatter;
    }

    /**
     * @return  formatter for time zone.
     *		Will format date/times as only the name
     *		of the local time zone.
     */
    public NSTimestampFormatter timeZoneFormatter()
    {
	return timeZoneFormatter;
    }

    /**
     * @return	backslash escaped text string, with special characters
     *		replaced with its backslash escaped equivalent.
     */
    protected String escapedString (String string)
    {
	StringBuffer escapedString = new StringBuffer (string);
	int index;

	for (index = escapedString.length() - 1; index >= 0; index -= 1) {
	    switch (escapedString.charAt (index)) {
		case '"':
		case ';':
		case ':':
		case '\\':
		case ',':
		    escapedString.insert (index, '\\');
		    break;

		case '\n':
		    escapedString.setCharAt (index, 'n');
		    escapedString.insert (index, '\\');
		    break;
	    }
	}

	return escapedString.toString();
    }

    /**
     * Folds lines that are longer than the maximum allowed 75 characters.
     *
     * @param	content	unfolded iCalendar content
     * @return	folded content, with no line longer than 75 characters
     */
    protected String foldLongLinesInString (String content) {
	Enumeration enumerator = NSArray.componentsSeparatedByString (content, newline).objectEnumerator();
	NSMutableArray foldedContent = new NSMutableArray();
	String line;

	while (enumerator.hasMoreElements()) {
	    line = (String)enumerator.nextElement();
	    while (line.length() > maxLineLength) {
		foldedContent.addObject (line.substring (0, 75));
		line = " " + line.substring (75);
	    }
	    foldedContent.addObject (line);
	}

	return foldedContent.componentsJoinedByString (newline);
    }
}
