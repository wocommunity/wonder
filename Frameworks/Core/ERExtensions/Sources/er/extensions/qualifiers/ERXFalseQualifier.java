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

public class ERXFalseQualifier extends EOQualifier implements NSCoding, EOKeyValueArchiving {
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
