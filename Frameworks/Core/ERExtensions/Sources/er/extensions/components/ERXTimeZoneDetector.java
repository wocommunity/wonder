package er.extensions.components;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXArrayUtilities;

/**
 * This component adds javascript to a page to grab the system time zone
 * from the browser and write the time zone to the <code>timeZone</code>
 * attribute of the session via a call to the <code>setTimeZone()</code>
 * method. The information is sent to the session using an ajax call.
 * This code determines a time zone based on minutes offset from
 * GMT, whether the time zone observes DST, and if DST, whether the time
 * zone is in the northern or southern hemisphere. Since there may be more
 * than one time zone that matches these values, the array of possible
 * values is compared against an array of preferred values if one is
 * supplied.  If no preferred values are supplied, the zone selected is
 * pulled from the list of possible options in no particular order. Use
 * of an {@link ERXSession} is expected/required.
 * 
 * @binding preferredTimeZones an array of preferred TimeZone objects. This
 * array takes precedence over the preferredTimeZoneIDs binding.
 * @binding preferredTimeZoneIDs an array of preferred TimeZone id strings
 * 
 * @author Ramsey Gurley
 *
 */
public class ERXTimeZoneDetector extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(ERXTimeZoneDetector.class);

	public static final String TIMEZONE_SESSION_KEY = "detectedTimeZone";

	private static final String TIMEZONE_DATA_KEY = "_timezone";

	private static volatile NSArray<TimeZone> allZones;

	public ERXTimeZoneDetector(WOContext context) {
		super(context);
	}

	public static NSArray<TimeZone> allZones() {
		if (allZones == null) {
			synchronized (ERXTimeZoneDetector.class) {
				if (allZones == null) {
					String[] ids = TimeZone.getAvailableIDs();
					NSMutableArray<TimeZone> tzs = new NSMutableArray<TimeZone>(ids.length);
					for (int i = 0; i < ids.length; i++) {
						TimeZone tz = TimeZone.getTimeZone(ids[i]);
						tzs.addObject(tz);
					}
					ERXS.sort(tzs, ERXS.asc("displayName"));
					allZones = tzs.immutableClone();
				}
			}
		}
		return allZones;
	}

	public static NSArray<TimeZone> zonesWithRawOffset(int minutes, boolean dst, boolean southern) {
		int rawOffset = minutes * 60 * 1000;
		EOQualifier q = ERXQ.equals("rawOffset", rawOffset);
		q = ERXQ.and(q, dst ? ERXQ.isTrue("useDaylightTime") : ERXQ.isFalse("useDaylightTime"));
		NSArray<TimeZone> result = EOQualifier.filteredArrayWithQualifier(allZones(), q);
		if (dst) {
			Date d = new NSTimestamp(2010, southern ? 0 : 5, 1, 0, 0, 0, TimeZone.getTimeZone("GMT"));
			NSMutableArray<TimeZone> tzs = new NSMutableArray<TimeZone>();
			for (TimeZone tz : result) {
				if (tz.inDaylightTime(d)) {
					tzs.addObject(tz);
				}
			}
			result = tzs.immutableClone();
		}
		return result;
	}

	public TimeZone zoneWithRawOffset(int minutes, boolean dst, boolean southern) {
		NSArray<TimeZone> zones = zonesWithRawOffset(minutes, dst, southern);
		TimeZone tz = zones.firstObjectCommonWithArray(preferredTimeZones());
		if(tz == null) { tz = ERXArrayUtilities.firstObject(zones); }
		return tz;
	}

	/**
	 * Returns true if the component should include a script to post time zone
	 * data back to the server. This remains true until the time zone data is
	 * captured.
	 * 
	 * @return true if ajax script should be included
	 */
	public boolean shouldPostData() {
		ERXSession session = (ERXSession) context().session();
		return !(session.objectStore().valueForKey(TIMEZONE_SESSION_KEY) instanceof String);
	}

	/**
	 * @return key used to identify timezone form value
	 */
	public String formValueKey() {
		return TIMEZONE_DATA_KEY;
	}

	/**
	 * The ajax request URL for this component.
	 * @return the post URL for the ajax post request
	 */
	public String postURL() {
		String key = WOApplication.application().ajaxRequestHandlerKey();
		return context().componentActionURL(key);
	}
	
	/**
	 * Overridden to capture the time zone data being sent from the client.
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		if (shouldPostData() && request.formValueForKey(TIMEZONE_DATA_KEY) != null) {
			ERXSession session = ERXSession.session();
			String zoneString = request.stringFormValueForKey(TIMEZONE_DATA_KEY);
			session.objectStore().takeValueForKey(zoneString, TIMEZONE_SESSION_KEY);
			session.setJavaScriptEnabled(true);

			String[] data = StringUtils.split(zoneString, ',');
			int rawOffset = Integer.valueOf(data[0]).intValue();
			boolean dst = "1".equals(data[1]);
			boolean southern = "1".equals(data[2]);
			TimeZone tz = zoneWithRawOffset(rawOffset, dst, southern);
			// Call ERXSession.setTimeZone() if tz is not null
			// https://github.com/wocommunity/wonder/issues/774
			if (tz != null) {
				session.setTimeZone(tz);
			}
			else {
				log.warn("Unable to find a timezone for '{}'.", zoneString);
			}
		}
	}

	public NSArray<TimeZone> preferredTimeZones() {
		NSArray<TimeZone> result;
		result = (NSArray<TimeZone>)valueForBinding("preferredTimeZones");
		if(result != null) {
			return result;
		}
		
		NSArray<String> ids = (NSArray<String>)valueForBinding("preferredTimeZoneIDs");
		if(ids == null) {
			result = NSArray.emptyArray();
			return result;
		}
		
		NSMutableArray<TimeZone> tzs = new NSMutableArray<TimeZone>(ids.count());
		for (int i = 0; i < ids.count(); i++) {
			TimeZone tz = TimeZone.getTimeZone(ids.objectAtIndex(i));
			tzs.addObject(tz);
		}
		result = tzs.immutableClone();
		return result;
	}
}
