package er.prototypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

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
	public static Date localTime(LocalTime value) {
		return Time.valueOf(value);
	}

	public static Date localDate(LocalDate value) {
		return java.sql.Date.valueOf(value);
	}

	public static Date localDateTime(LocalDateTime value) {
		return Timestamp.valueOf(value);
	}

	public static Date dateTime(OffsetDateTime value) {
		return Timestamp.valueOf(value.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
	}

	/**
	 * @deprecated should use localTime instead
	 */
	@Deprecated
	public static Date jodaLocalTime(org.joda.time.LocalTime value) {
		Date javaTime = value.toDateTimeToday().toDate();
		return javaTime;
	}

	/**
	 * @deprecated should use localDate instead
	 */
	@Deprecated
	public static Date jodaLocalDate(org.joda.time.LocalDate value) {
		Date javaDate = value.toDate();
		return javaDate;
	}

	/**
	 * @deprecated should use localDateTime instead
	 */
	@Deprecated
	public static Date jodaLocalDateTime(org.joda.time.LocalDateTime value) {
		Date javaDate = value.toDate();
		return javaDate;
	}

	/**
	 * @deprecated should use dateTime instead
	 */
	@Deprecated
	public static Date jodaDateTime(org.joda.time.DateTime value) {
		long dateInMillis = value.toInstant().getMillis();
		int offset = TimeZone.getDefault().getOffset(dateInMillis);
		Date javaDate = new Date(dateInMillis - offset);
		return javaDate;
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
