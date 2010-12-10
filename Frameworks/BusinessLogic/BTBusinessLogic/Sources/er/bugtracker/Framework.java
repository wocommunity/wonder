// Framework.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.logging.ERXLogger;

public class Framework extends _Framework {
    static final ERXLogger log = ERXLogger.getERXLogger(Framework.class);

    public Framework() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class FrameworkClazz extends _FrameworkClazz {
        
    }

    public static FrameworkClazz clazz = (FrameworkClazz) EOEnterpriseObjectClazz.clazzForEntityNamed("Framework");
}
