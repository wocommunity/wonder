package er.prototypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.foundation.ERXMutableArray;
import er.extensions.foundation.ERXMutableDictionary;

/**
 * ValueFactory provides static methods that produce EOAttribute values
 * from values stored in the database.
 */
public class ValueFactory {
	public static Duration duration(String value) {
		try {
			Duration d = DatatypeFactory.newInstance().newDuration(value);
			return d;
		} catch (DatatypeConfigurationException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public static LocalDate localDate(Date value) {
		LocalDate ld = null;
		if (value instanceof java.sql.Date) {
			ld = ((java.sql.Date) value).toLocalDate();
		} else {
			throw new IllegalArgumentException("Expected java.sql.Date object but got <" + value.getClass().getCanonicalName() + ">.");
		}
		return ld;
	}

	public static LocalDateTime localDateTime(Date value) {
		LocalDateTime ldt = null;
		if (value instanceof java.sql.Timestamp) {
			ldt = ((java.sql.Timestamp) value).toLocalDateTime();
		} else {
			throw new IllegalArgumentException("Expected java.sql.Timestamp object but got <" + value.getClass().getCanonicalName() + ">.");
		}
		return ldt;
	}

	public static LocalTime localTime(Date value) {
		LocalTime time = null;
		if (value instanceof java.sql.Time) {
			time = ((java.sql.Time) value).toLocalTime();
		} else {
			throw new IllegalArgumentException("Expected java.sql.Time object but got <" + value.getClass().getCanonicalName() + ">.");
		}
		return time;
	}

	public static OffsetDateTime dateTime(Date value) {
		OffsetDateTime odt = null;
		if (value instanceof java.sql.Timestamp) {
			LocalDateTime ldt = ((java.sql.Timestamp) value).toLocalDateTime();
			Instant instant = ldt.toInstant(ZoneOffset.UTC);
			ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
			odt = zdt.toOffsetDateTime();
		} else {
			throw new IllegalArgumentException("Expected java.sql.Timestamp object but got <" + value.getClass().getCanonicalName() + ">.");
		}
		return odt;
	}

	/**
	 * @deprecated should use localDate instead
	 */
	@Deprecated
	public static org.joda.time.LocalDate jodaLocalDate(Date value) {
		org.joda.time.LocalDate ld = new org.joda.time.LocalDate(value.getTime());
		return ld;
	}

	/**
	 * @deprecated should use localDateTime instead
	 */
	@Deprecated
	public static org.joda.time.LocalDateTime jodaLocalDateTime(Date value) {
		org.joda.time.LocalDateTime ldt = new org.joda.time.LocalDateTime(value.getTime());
		return ldt;
	}

	/**
	 * @deprecated should use localTime instead
	 */
	@Deprecated
	public static org.joda.time.LocalTime jodaLocalTime(Date value) {
		org.joda.time.LocalTime time = new org.joda.time.LocalTime(value.getTime());
		return time;
	}

	/**
	 * @deprecated should use dateTime instead
	 */
	@Deprecated
	public static org.joda.time.DateTime jodaDateTime(Date value) {
		long dateInMillis = value.getTime();
		int offset = TimeZone.getDefault().getOffset(dateInMillis);
		org.joda.time.DateTime dateTime = new org.joda.time.DateTime(dateInMillis + offset);
		return dateTime;
	}

	@SuppressWarnings("rawtypes")
	public static NSArray stringArray(String value) {
		return (NSArray)NSPropertyListSerialization.propertyListFromString(value);
	}
	
	@SuppressWarnings("rawtypes")
	public static NSArray blobArray(NSData value) {
		return ERXMutableArray.fromBlob(value);
	}

	@SuppressWarnings("rawtypes")
	public static NSDictionary stringDictionary(String value) {
		return (NSDictionary) NSPropertyListSerialization.propertyListFromString(value);
	}
	
	@SuppressWarnings("rawtypes")
	public static NSDictionary blobDictionary(NSData value) {
		return ERXMutableDictionary.fromBlob(value);
	}
	
	public static Serializable serializable(byte[] value) {		
		ObjectInputStream ois = null;
		
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(value);
			ois = new ObjectInputStream(bais);
			Serializable obj = (Serializable)ois.readObject();
			if(obj instanceof Collection) {
				obj = (Serializable)Collections.unmodifiableCollection((Collection<?>)obj);
			}
			return obj;
		} catch(IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch(ClassNotFoundException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} finally {
			try{ if(ois != null){ois.close();} } catch(IOException e) {}
		}
	}
}
