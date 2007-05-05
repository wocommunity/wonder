// Framework.java
// 
package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

public class Framework extends _Framework {
    static final Logger log = Logger.getLogger(Framework.class);

    public Framework() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class FrameworkClazz extends _FrameworkClazz {
        
    }

    public static FrameworkClazz clazz = new FrameworkClazz();
}
