// Framework.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

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

    public static FrameworkClazz clazz = (FrameworkClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("Framework");
}
