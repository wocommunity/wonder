// ERCPreference.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.EOEnterpriseObjectClazz;

public class ERCPreference extends _ERCPreference {
    static final Logger log = Logger.getLogger(ERCPreference.class);

    public ERCPreference() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    
    // Class methods go here
    
    public static class ERCPreferenceClazz extends _ERCPreferenceClazz {
    	//AK: compatibility with new templates
    	public NSArray preferencesWithKey(EOEditingContext ec, String key) {
    		return objectsForPreferences(ec, key);
    	}
    	//AK: compatibility with new templates
    	public NSArray userPrefsWithKeyId(EOEditingContext ec, String key, Number id) {
    		return objectsForUserPrefs(ec, id, key);
    	}
    }

    public static ERCPreferenceClazz preferenceClazz() { return (ERCPreferenceClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCPreference"); }
}
