package er.calendar;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

/**
 * ERCalendarEvent is an interface for events used by
 * {@link ERPublishCalendarPage the ERPublishCalendarPage component}.
 * <p>
 * {@link ERPublishCalendarPage ERPublishCalendarPage} can use objects of any class
 * that implements this interface. Existing classes (for example
 * EOCustomObject subclasses), that correspond to calendar events,
 * can easily be modified to implement this interface and thus be
 * added directly to {@link ERPublishCalendarPage ERPublishCalendarPage}. If existing
 * classes does not directly correspond to calendar events, create
 * events from business data (or some algorithm) using either the
 * included {@link ERSimpleEvent ERSimpleEvent class}, a subclass of
 * {@link ERSimpleEvent ERSimpleEvent}, or any other class implementing
 * this interface.
 *
 * @author 	Johan Carlberg &lt;johan@oops.se&gt;
 * @version 	1.0, 2002-09-30
 */

public interface ERCalendarEvent {

    /**
     * Return <code>false</code> if this event have specified
     *		start and ending times.
     *		<code>true</code> if this event is a whole-day
     *		event without a specific starting and ending hour
     *		and minute.
     * @see	ERSimpleEvent#wholeDay()
     */
    public boolean wholeDay();

    /**
     * Return the start time of this event. For whole-day events,
     *          the time part of the NSTimestamp is ignored.
     * @see     #endTime
     */
    public NSTimestamp startTime();

    /**
     * Return the end time of this event. For whole-day events,
     *          this should be a time on the day following the
     *          last day of this event (the time part of the
     *          NSTimestamp is ignored, and the iCalendar standard
     *          requires that the end time of an event is later
     *          than the start time.
     * @see     #startTime
     */
    public NSTimestamp endTime();

    /**
     * Return the change counter of this event. The sequence of a
     *          calendar event is supposed to increase every time
     *		any information in the event is modified, so that
     *		updates to events can be ordered.
     * @see     ERSimpleEvent#sequence()
     */
    public int sequence();

    /**
     * Return the status of this event. Values defined by the iCalendar
     *		standard are: "TENTATIVE", "CONFIRMED", "CANCELLED".
     *		Return null if event status is unspecified.
     */
    public String status();

    /**
     * Return a summary or textual description of this event.
     */
    public String summary();

    /**
     * Return a persistent, globally unique identifier for this
     *          event.
     *		The unique identifier <b>must</b> be a globally unique
     *		identifier. The generator of the identifier <b>must</b>
     *		guarantee that the identifier is unique. There are
     *		several algorithms that can be used to accomplish this.
     *		The identifier is recommended to be the identical
     *		syntax to the RFC 822 addr-spec. A good method to
     *		assure uniqueness is to put the domain name or a
     *		domain literal IP address of the host on which the
     *		identifier was created on the right hand side of the
     *		"@", and on the left hand side, put a combination of
     *		the current calendar date and time of day (that is,
     *		formatted in as a date-time value) along with some
     *		other currently unique (perhaps sequential) identifier
     *		available on the system (for example, a process id
     *		number). Using a date/time value on the left hand side
     *		and a domain name or domain literal on the right hand
     *		side makes it possible to guarantee uniqueness since no
     *		two hosts should be using the same domain name or IP
     *		address at the same time. Though other algorithms will
     *		work, it is recommended that the right hand side
     *		contain some domain identifier (either of the host
     *		itself or otherwise) such that the generator of the
     *		message identifier can guarantee the uniqueness of the left
     *		hand side within the scope of that domain.
     * @see     ERSimpleEvent#sequence()
     */
    public String uniqueId();

    /**
     * Return the frequency of a repeating event, or 0 for a one time
     *		event. Can be specified as one of the
     *		<code>java.util.Calendar</code> field numbers
     *		<code>YEAR</code>, <code>MONTH</code>,
     *		<code>WEEK_OF_YEAR</code>, <code>DAY_OF_MONTH</code>,
     *		<code>HOUR_OF_DAY</code>, <code>MINUTE</code> or
     *		<code>SECOND</code>.
     * @see	#repeatCount
     */
    public int repeatFrequency();

    /**
     * Return the number of occurences of a repeating event. Ignored
     *		if {@link #repeatFrequency} returns 0.
     * @see     #repeatFrequency
     */
    public int repeatCount();

    /**
     * Return the day of week of a repeating event, or 0 for unspecified
     *		day of week. Can be specified as one of the
     *		<code>java.util.Calendar</code> <code>DAY_OF_WEEK</code>
     *		field values <code>SUNDAY</code>, <code>MONDAY</code>,
     *		<code>TUESDAY</code>, <code>WEDNESDAY</code>,
     *		<code>THURSDAY</code>, <code>FRIDAY</code>,
     *		<code>SATURDAY</code> or as the value 0. Ignored if
     *		{@link #repeatFrequency} returns 0.
     * @see	#repeatFrequency
     */
    public int repeatDayOfWeek();

    /**
     * Return the ordinal number of day of the week within a month
     *		at which the event repeats. Together with
     *		{@link #repeatDayOfWeek} it uniquely specifies a day
     *		within the month. Ignored if {@link #repeatFrequency}
     *		returns 0.
     * @see	#repeatDayOfWeek
     */
    public int repeatDayOfWeekInMonth();

    /**
     * Return an array of {@link Integer} indicating the days
     *		within a month, at which the event repeats, or
     *		<code>null</code> for not restricting repeating
     *		to certain days.
     *		Together with {@link #repeatDayOfWeek} it uniquely
     *		specifies a day within the month (Keep in mind
     *		that iCal 1.0 does not properly handle this).
     *		Ignored if {@link #repeatFrequency} returns 0.
     * @see	#repeatDayOfWeek
     */
    public NSArray repeatDaysOfMonth();
}
