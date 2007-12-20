// ERCPreference.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.EOEnterpriseObjectClazz;
import er.extensions.ERXQ;

public class ERCPreference extends _ERCPreference {
    static final Logger log = Logger.getLogger(ERCPreference.class);
    private final static String VALUE="_V";

    public ERCPreference() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    public String userPresentableDescription() {
        return key() + ": " + decodedValue();
    }
    
    protected Object decodedValue() {
        NSDictionary d = (NSDictionary )NSPropertyListSerialization.propertyListFromString(value());
        EOKeyValueUnarchiver u = new EOKeyValueUnarchiver(d);
        return u.decodeObjectForKey(VALUE);
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
