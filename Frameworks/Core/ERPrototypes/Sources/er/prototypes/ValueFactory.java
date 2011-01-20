package er.prototypes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.webobjects.foundation.NSForwardException;

/**
 * ValueFactory provides static methods that produce EOAttribute values
 * from values stored in the database.
 */
public class ValueFactory {
	private static final Logger log = Logger.getLogger(ValueFactory.class);
	private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	
	public static Duration duration(String value) {
		try {
			Duration d = DatatypeFactory.newInstance().newDuration(value);
			return d;
		} catch (DatatypeConfigurationException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
	
	public static LocalDate jodaLocalDate(Date value) {
		GregorianCalendar gc = new GregorianCalendar(GMT);
		gc.setTime(value);
		int years = gc.get(Calendar.YEAR);
		int months = gc.get(Calendar.MONTH) + 1;
		int days = gc.get(Calendar.DAY_OF_MONTH);
		LocalDate ld = new LocalDate(years, months, days);
		return ld;
	}
	
	public static LocalDateTime jodaLocalDateTime(Date value) {
		GregorianCalendar gc = new GregorianCalendar(GMT);
		gc.setTime(value);
		int years = gc.get(Calendar.YEAR);
		int months = gc.get(Calendar.MONTH) + 1;
		int days = gc.get(Calendar.DAY_OF_MONTH);
		int hours = gc.get(Calendar.HOUR_OF_DAY);
		int minutes = gc.get(Calendar.MINUTE);
		int seconds = gc.get(Calendar.SECOND);
		int millis = gc.get(Calendar.MILLISECOND);
		LocalDateTime ldt = new LocalDateTime(years, months, days, hours, minutes, seconds, millis);
		return ldt;
	}
	
	public static LocalTime jodaLocalTime(Date value) {
		GregorianCalendar gc = new GregorianCalendar(GMT);
		gc.setTime(value);
		int hours = gc.get(Calendar.HOUR_OF_DAY);
		int minutes = gc.get(Calendar.MINUTE);
		int seconds = gc.get(Calendar.SECOND);
		int millis = gc.get(Calendar.MILLISECOND);
		LocalTime time = new LocalTime(hours, minutes, seconds, millis);
		return time;
	}

	public static DateTime jodaDateTime(Date value) {
		DateTime dt = new DateTime(value.getTime());
		return dt;
	}

}
