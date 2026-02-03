package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.ERDAssignment;

public class R2DKeyValueArrayAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2DKeyValueArrayAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DKeyValueArrayAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DKeyValueArrayAssignment(unarchiver);
	}


	public NSArray<Object> dependentKeys(String keyPath) {
		return new NSArray<Object>(new Object[] {});
	}
	
	public Object fire(D2WContext c) {
		NSMutableArray<Object> results = new NSMutableArray<Object>();
		NSArray<String> keyPaths = (NSArray<String>)value();
		for(String path: keyPaths) {
			results.addObject(c.valueForKeyPath(path));
		}
		return results;
	}
	
}
