// ERCLogEntry.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class ERCLogEntry extends _ERCLogEntry {
    static final ERXLogger log = ERXLogger.getLogger(ERCLogEntry.class);

    public ERCLogEntry() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
        setCreated(new NSTimestamp());
        EOEnterpriseObject actor = ERCoreBusinessLogic.actor(ec);
        if (actor!=null) {
            setUserID((Integer)((ERXGenericRecord)actor).rawPrimaryKey());
        }
    }
    
    
    // Class methods go here
    
    public static class ERCLogEntryClazz extends _ERCLogEntryClazz {
        
    }

    public static ERCLogEntryClazz logEntryClazz() { return (ERCLogEntryClazz)EOGenericRecordClazz.clazzForEntityNamed("ERCLogEntry"); }
}
