// Difficulty.java
// 
package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.logging.ERXLogger;

public class Difficulty extends _Difficulty {
    static final ERXLogger log = ERXLogger.getERXLogger(Difficulty.class);

    public Difficulty() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class DifficultyClazz extends _DifficultyClazz {
        
    }

    public static DifficultyClazz clazz = (DifficultyClazz) EOEnterpriseObjectClazz.clazzForEntityNamed("Difficulty");
}
