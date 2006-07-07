// ERCPreference.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.*;

import er.extensions.*;

public class ERCPreference extends _ERCPreference {
    static final Logger log = Logger.getLogger(ERCPreference.class);

    public ERCPreference() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class ERCPreferenceClazz extends _ERCPreferenceClazz {
        
    }

    public static ERCPreferenceClazz preferenceClazz() { return (ERCPreferenceClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCPreference"); }
}
