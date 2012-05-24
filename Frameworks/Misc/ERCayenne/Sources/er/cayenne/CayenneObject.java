package er.cayenne;

import org.apache.cayenne.CayenneDataObject;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * Adds Key Value Coding (KVC) support to CayenneDataObject to make it suitable for use in WO applications.
 * 
 * @author john
 *
 */
public class CayenneObject extends CayenneDataObject implements NSKeyValueCodingAdditions, NSKeyValueCoding.ErrorHandling {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public CayenneObject() {
	}

	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	public void takeValueForKeyPath(Object value, String keyPath) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
	}

	public Object valueForKeyPath(String keyPath) {
		return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
	}

	public Object handleQueryWithUnboundKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
	}

	public void handleTakeValueForUnboundKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
	}

	public void unableToSetNullForKey(String key) {
		NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
	}

	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public Object readProperty(String propertyName) {
		return super.readProperty(propertyName);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public Object readPropertyDirectly(String propertyName) {
		return super.readPropertyDirectly(propertyName);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public Object readNestedProperty(String path) {
		return super.readNestedProperty(path);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public void writeProperty(String propertyName, Object value) {
		super.writeProperty(propertyName, value);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public void writePropertyDirectly(String propertyName, Object value) {
		super.writePropertyDirectly(propertyName, value);
	}
	
}
