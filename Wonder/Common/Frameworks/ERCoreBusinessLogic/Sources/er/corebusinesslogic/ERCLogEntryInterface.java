//
//  ERCLogEntryInterface.java
//  ERCoreBusinessLogic
//
//  Created by Max Muller on Thu Nov 07 2002.
//
package er.corebusinesslogic;

import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Used in conjunction with {@link ERCStampedEnterpriseObject} to optionally
 * allow a log entry to be created when something changes on the object.
 */
public interface ERCLogEntryInterface {

    /**
     * Name of the to many relationship to add
     * log entries to when logging.
     * @return relationship name
     */
    public String relationshipNameForLogEntry();

    /**
     * The type of the log entry to set when
     * creating the log entry.
     * @return log entry type.
     */
    public EOEnterpriseObject logEntryType();
}
