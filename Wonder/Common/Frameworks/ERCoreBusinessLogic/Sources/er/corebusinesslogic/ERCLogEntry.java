// ERCLogEntry.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.EOEnterpriseObjectClazz;
import er.extensions.ERXGenericRecord;

public class ERCLogEntry extends _ERCLogEntry {
    static final Logger log = Logger.getLogger(ERCLogEntry.class);

    public ERCLogEntry() {
        super();
    }

    public void init(EOEditingContext ec) {
        super.init(ec);
        setCreated(new NSTimestamp());
        EOEnterpriseObject actor = ERCoreBusinessLogic.actor(ec);
        if (actor != null) {
            setUserID((Integer) ((ERXGenericRecord) actor).rawPrimaryKey());
        }
    }

    // Class methods go here

    public static class ERCLogEntryClazz extends _ERCLogEntryClazz {
        // Logging support
        public ERCLogEntry createLogEntryLinkedToEO(EOEnterpriseObject type, String text, 
                EOEnterpriseObject eo, String relationshipKey) {
            EOEditingContext editingContext = eo.editingContext();
            ERCLogEntry logEntry = (ERCLogEntry) ERCLogEntry.clazz.createAndInsertObject(editingContext);
            if (type != null) {
                // CHECKME: (ak) what's type supposed to do??
                // logEntry.addObjectToBothSidesOfRelationshipWithKey(type,"type");
            }
            if (relationshipKey != null) {
                // CHECKME: (ak) what's relationshipKey supposed to do??
                // logEntry.addObjectToBothSidesOfRelationshipWithKey(eo,relationshipKey);
            }
            logEntry.setText(text);
            return logEntry;
        }

    }

    public static ERCLogEntryClazz clazz = new ERCLogEntryClazz();

}
