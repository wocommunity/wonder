// ERCStatic.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class ERCStatic extends _ERCStatic {
    static final ERXLogger log = ERXLogger.getLogger(ERCStatic.class);

    public ERCStatic() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
    }
    
    
    // Class methods go here
    
    public static class ERCStaticClazz extends _ERCStaticClazz {
        public ERCStatic objectMatchingKey(EOEditingContext ec, String value) {
            NSArray arr = preferencesWithKey(ec, value);
            if(arr.count() == 1) {
                return (ERCStatic)arr.objectAtIndex(0);
            }
            return null;
        }
    }

    public static ERCStaticClazz staticClazz() { return (ERCStaticClazz)EOGenericRecordClazz.clazzForEntityNamed("ERCStatic"); }
}
