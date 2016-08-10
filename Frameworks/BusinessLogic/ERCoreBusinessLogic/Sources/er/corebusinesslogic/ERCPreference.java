// ERCPreference.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.eof.ERXQ;

public class ERCPreference extends _ERCPreference {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private final static String VALUE="_V";

    public ERCPreference() {
        super();
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    @Override
    public String userPresentableDescription() {
        return key() + ": " + decodedValue();
    }

    protected Object decodedValue() {
        NSDictionary d = (NSDictionary )NSPropertyListSerialization.propertyListFromString(value());
        if(d != null) {
            EOKeyValueUnarchiver u = new EOKeyValueUnarchiver(d);
            return u.decodeObjectForKey(VALUE);
        }
        return null;
    }    

    // Class methods go here
    
    public static class ERCPreferenceClazz extends _ERCPreferenceClazz {

        public NSArray preferencesWithKey(EOEditingContext ec, String key) {
    		return objectsMatchingKeyAndValue(ec, Key.KEY, key);
    	}
    	
        public NSArray<ERCPreference> userPrefsWithKeyId(EOEditingContext ec, String key, Number id) {
            EOQualifier q = ERXQ.and(ERXQ.equals(Key.USER_ID, id), ERXQ.equals(Key.KEY, key));
    		return objectsMatchingQualifier(ec, q);
    	}
    }

    public static ERCPreferenceClazz preferenceClazz() { return (ERCPreferenceClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCPreference"); }
}
