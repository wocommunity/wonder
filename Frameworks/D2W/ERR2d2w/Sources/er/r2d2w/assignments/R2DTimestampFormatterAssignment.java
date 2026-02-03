package er.r2d2w.assignments;

import java.text.SimpleDateFormat;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.extensions.appserver.ERXSession;

public class R2DTimestampFormatterAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2DTimestampFormatterAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DTimestampFormatterAssignment(String key, Object value) {
		super(key, value);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver u) {
		return new R2DTimestampFormatterAssignment(u);
	}
	
	@Override
	public Object fireNow(D2WContext c) {
		String pattern = (String) c.valueForKey(D2WModel.FormatterKey);
		ERXSession session = (ERXSession) c.valueForKey(D2WModel.SessionKey);
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(session.timeZone());
		return format;
	}

}
