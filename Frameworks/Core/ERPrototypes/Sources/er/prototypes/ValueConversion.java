package er.prototypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.foundation.ERXMutableArray;
import er.extensions.foundation.ERXMutableDictionary;

/**
 * ValueConversion provides static methods to convert EOAttribute values 
 * into values stored by the database.
 */
public class ValueConversion {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ValueConversion.class);
	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	
	public static Date jodaLocalTime(LocalTime value) {
		GregorianCalendar gc = new GregorianCalendar(GMT);
		gc.set(Calendar.HOUR_OF_DAY, value.getHourOfDay());
		gc.set(Calendar.MINUTE, value.getMinuteOfHour());
		gc.set(Calendar.SECOND, value.getSecondOfMinute());
		gc.set(Calendar.MILLISECOND, value.getMillisOfSecond());
		return gc.getTime();
	}

	public static Date jodaLocalDate(LocalDate value) {
		GregorianCalendar gc = new GregorianCalendar(GMT);
		gc.set(Calendar.YEAR, value.getYear());
		gc.set(Calendar.MONTH, value.getMonthOfYear() -1);
		gc.set(Calendar.DAY_OF_MONTH, value.getDayOfMonth());
		return gc.getTime();
	}
	
	public static Date jodaLocalDateTime(LocalDateTime value) {
		GregorianCalendar gc = new GregorianCalendar(GMT);
		gc.set(Calendar.YEAR, value.getYear());
		gc.set(Calendar.MONTH, value.getMonthOfYear() -1);
		gc.set(Calendar.DAY_OF_MONTH, value.getDayOfMonth());
		gc.set(Calendar.HOUR_OF_DAY, value.getHourOfDay());
		gc.set(Calendar.MINUTE, value.getMinuteOfHour());
		gc.set(Calendar.SECOND, value.getSecondOfMinute());
		gc.set(Calendar.MILLISECOND, value.getMillisOfSecond());
		return gc.getTime();
	}

	public static Date jodaDateTime(DateTime value) {
		Date d = new Date(value.getMillis());
		return d;
	}
	
	@SuppressWarnings("rawtypes")
	public static String stringArray(NSArray value) {
		return NSPropertyListSerialization.stringFromPropertyList(value);
	}
	
	@SuppressWarnings("rawtypes")
	public static NSData blobArray(NSArray value) {
		return ERXMutableArray.toBlob(value);
	}
	
	@SuppressWarnings("rawtypes")
	public static String stringDictionary(NSDictionary value) {
		return NSPropertyListSerialization.stringFromPropertyList(value);
	}
	
	@SuppressWarnings("rawtypes")
	public static NSData blobDictionary(NSDictionary value) {
		return ERXMutableDictionary.toBlob(value);
	}
	
	public static byte[] serializable(Serializable value) {
		ObjectOutputStream oout = null;
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oout = new ObjectOutputStream(baos);
			oout.writeObject(value);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch(IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} finally {
			try { if(oout != null){oout.close();} } catch(IOException e) {}
		}
	}
}
