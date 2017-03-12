// ERCLogEntry.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXGenericRecord;

public class ERCLogEntry extends _ERCLogEntry {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERCLogEntry() {
        super();
    }

    @Override
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
            ERCLogEntry logEntry = ERCLogEntry.clazz.createAndInsertObject(editingContext);
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
