// Difficulty.java
// 
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

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

    public static DifficultyClazz clazz = (DifficultyClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("Difficulty");
}
