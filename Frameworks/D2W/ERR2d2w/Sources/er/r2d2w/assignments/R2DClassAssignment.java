package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.foundation.ERXStringUtilities;

public class R2DClassAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final String pageClasses = "pageClasses";
	private static final NSArray<String> pageClassesDependentKeys =
		new NSArray<String>(new String[] {"task","subtask","entity.name","pageConfiguration"});
	
	private static final NSDictionary<String, NSArray<String>> dependentKeys =
		new NSDictionary<String, NSArray<String>>(pageClassesDependentKeys, pageClasses);
	
	public R2DClassAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DClassAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DClassAssignment(unarchiver);
	}

	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeys.objectForKey(keyPath);
	}
	
	public String pageClasses(D2WContext c) {
		return classesForKey(c, pageClasses);
	}
	
	protected String classesForKey(D2WContext c, String keyPath) {
		StringBuilder sb = new StringBuilder();
		NSArray<String> keys = dependentKeys.objectForKey(keyPath);
		for(String key : keys) {
			String value = (String)c.valueForKeyPath(key);
			if(!ERXStringUtilities.stringIsNullOrEmpty(value)) {
				if(sb.length() > 0) { sb.append(" "); }
				sb.append(value);
			}
		}
		return sb.toString();
	}
}
