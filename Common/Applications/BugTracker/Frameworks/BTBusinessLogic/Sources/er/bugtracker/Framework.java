// Framework.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class Framework extends _Framework {
    static final ERXLogger log = ERXLogger.getLogger(Framework.class);

    public Framework() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class FrameworkClazz extends _FrameworkClazz {
        
    }

    public static FrameworkClazz frameworkClazz() { return (FrameworkClazz)EOGenericRecordClazz.clazzForEntityNamed("Framework"); }
}
