package er.calendar;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

/**
 * ERSimpleEvent is an simple implementation of an event class,
 * implementing the {@link ERCalendarEvent ERCalendarEvent interface},
 * for use by {@link ERPublishCalendarPage the ERPublishCalendarPage component}.
 * <p>
 * ERSimpleEvent objects can be created corresponding to your events
 * and added to ERPublishCalendarPage to create a calendar.
 * <p>
 * Subclass ERSimpleEvent if more advanced features are needed.
 *
 * @author 	Johan Carlberg &lt;johan@oops.se&gt;
 * @version 	1.0, 2002-09-30
 */

public class ERSimpleEvent implements ERCalendarEvent {
    protected NSTimestamp endTime;
    protected NSTimestamp startTime;
    protected String status;
    protected String summary;
    protected String uniqueId;
    protected boolean wholeDay;

    /**
     * @param	aStartTime	start time of this event
     * @param	anEndTime	end time of this event
     * @param	aSummary	summary or textual description of this
     *				event
     * @param	aUniqueId	a persistent, globally unique identifier
     *				for this event
     * @see	ERCalendarEvent#startTime()
     * @see	ERCalendarEvent#endTime()
     * @see	ERCalendarEvent#summary()
     * @see	ERCalendarEvent#uniqueId()
     */
    public ERSimpleEvent (NSTimestamp aStartTime, NSTimestamp anEndTime, String aSummary, String aUniqueId) {
	startTime = aStartTime;
	endTime = anEndTime == null ? aStartTime : anEndTime;
	summary = aSummary;
	uniqueId = aUniqueId;
    }

    /**
     * @return	always returns <code>false</code>.
     *		ERSimpleEvent doesn't support whole-day events.
     * @see     ERCalendarEvent#wholeDay()
     */
    public boolean wholeDay() {
	return false;
    }

    /**
     * @return  the start time of this event as specified in the
     * {@link #ERSimpleEvent(NSTimestamp, NSTimestamp, String, String) constructor}
     * @see     ERCalendarEvent#startTime()
     */
    public NSTimestamp startTime() {
	return startTime;
    }

    /**
     * @return  the end time of this event as specified in the
     * {@link #ERSimpleEvent(NSTimestamp, NSTimestamp, String, String) constructor}
     * @see     ERCalendarEvent#endTime()
     */
    public NSTimestamp endTime() {
	return endTime;
    }

    /**
     * @return  the change counter of this event.
     *		Computed from the current time, and will increase every
     *		ten seconds.
     * @see     ERCalendarEvent#sequence()
     */
    public int sequence() {
	return (int)(new NSTimestamp().getTime() / 10000);
    }

    /**
     * @return  <code>null</code> since ERSimpleEvent doesn't support
     *		event status.
     * @see     ERCalendarEvent#status()
     */
    public String status() {
	return status;
    }

    /**
     * @return  the summary of this event as specified in the
     * {@link #ERSimpleEvent(NSTimestamp, NSTimestamp, String, String) constructor}
     * @see     ERCalendarEvent#summary()
     */
    public String summary() {
	return summary;
    }

    /**
     * @return  the unique id of this event as specified in the
     * {@link #ERSimpleEvent(NSTimestamp, NSTimestamp, String, String) constructor}
     * @see     ERCalendarEvent#uniqueId()
     */
    public String uniqueId() {
	return uniqueId;
    }

    /**
     * @return  0 indicating a non-repeating event. ERSimpleEvent doesn't
     *		support repeating events.
     */
    public int repeatFrequency() {
	return 0;
    }

    /**
     * @return  1 indicating a one-time event (although this method is
     *		never called since {@link #repeatFrequency} always return 0).
     * @see     #repeatFrequency
     */
    public int repeatCount() {
	return 1;
    }

    /**
     * @return  0 indicating unspecified day of week (although this
     *		method is never called since {@link #repeatFrequency}
     *		always returns 0).
     * @see	#repeatFrequency
     */
    public int repeatDayOfWeek() {
	return 0;
    }

    /**
     * @return  0 indicating unspecified day of week in month
     *		(although this method is never called since
     *		{@link #repeatFrequency} always returns 0).
     * @see	#repeatFrequency
     */
    public int repeatDayOfWeekInMonth() {
	return 0;
    }

    /**
     * @return  <code>null</code> indicating unspecified days
     *		within a month (although this method is never
     *		called since {@link #repeatFrequency} always
     *		returns 0.
     * @see	#repeatFrequency
     */
    public NSArray repeatDaysOfMonth() {
	return null;
    }
}
