package er.directtoweb.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation._NSUtilities;

import er.extensions.foundation.ERXStringUtilities;

/**
 * A simple assignment class to assign enums. An example rule:
 * <br>
 * 100: *true* =&gt; someEnumKey = package.EnumName.INSTANCE [ERDEnumAssignment]
 *
 */
public class ERDEnumAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public NSArray<String> dependentKeys(String keyPath) {
		return null;
	}

	public ERDEnumAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public ERDEnumAssignment(String key, Object value) {
		super(key, value);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new ERDEnumAssignment(unarchiver);
	}

	@Override
    public Object fire(D2WContext c) {
		String value = (String)value();
		String className = ERXStringUtilities.keyPathWithoutLastProperty(value);
		Class<? extends Enum<?>> klass = _NSUtilities.classWithName(className);
		if(klass != null && klass.isEnum()) {
			String instance = ERXStringUtilities.lastPropertyKeyInKeyPath(value);
			Enum<?>[] e = klass.getEnumConstants();
			for(int i = 0, length = e.length; i < length; ++i) {
				if(e[i].name().equals(instance)) {
					return e[i];
				}
			}
		}
		ClassNotFoundException ex = new ClassNotFoundException("No Enum found with name: " + value);
    	throw NSForwardException._runtimeExceptionForThrowable(ex);
    }
}
