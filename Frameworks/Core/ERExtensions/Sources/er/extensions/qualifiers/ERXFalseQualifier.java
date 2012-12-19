package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueArchiving;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSCoding;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

/**
 * An EOQualifier that always evaluates to a <code>false</code> result.
 */
public class ERXFalseQualifier extends EOQualifier implements NSCoding, EOKeyValueArchiving {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void addQualifierKeysToSet(NSMutableSet keys) {
	}

	@Override
	public EOQualifier qualifierWithBindings(NSDictionary bindings, boolean requireAll) {
		return this;
	}

	@Override
	public void validateKeysWithRootClassDescription(EOClassDescription classDescription) {
	}

	@Override
	public boolean evaluateWithObject(Object object) {
		return false;
	}
	
	@Override
	public String toString() {
		return "(false)";
	}

    public Class classForCoder() {
    	return getClass();
    }
    
	public static Object decodeObject(NSCoder coder) {
		return new ERXFalseQualifier();
	}

	public void encodeWithCoder(NSCoder coder) {}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new ERXFalseQualifier();
	}
}
