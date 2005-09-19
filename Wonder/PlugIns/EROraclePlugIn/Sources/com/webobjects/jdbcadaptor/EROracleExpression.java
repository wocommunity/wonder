package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import java.sql.*;
import com.webobjects.jdbcadaptor.OraclePlugIn.*;

/** overrides OracleExpression in order to add
 * TIMESTAMP values including milliseconds. The
 * normal EOF Oracle PlugIn does not add milliseconds
 * to the TIMESTAMP value
 *  
 * @author David Teran
 *
 */
public class EROracleExpression extends OracleExpression {
    private static final NSTimestampFormatter _TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

    
    public EROracleExpression(EOEntity eoentity) {
        super(eoentity);
    }

    /** Overridden in order to add milliseconds to the value. This
     * applies only if obj is an instance of NSTimestamp and if 
     * valueType from the eoattribute is T
     * 
     * @param obj
     * @param eoattribute
     * 
     * @return the modified bindVariableDictionary
     */
    public NSMutableDictionary bindVariableDictionaryForAttribute(EOAttribute eoattribute, Object obj) {
        NSMutableDictionary result =  super.bindVariableDictionaryForAttribute(eoattribute, obj);
        if((obj instanceof NSTimestamp) && (isTimestampAttribute(eoattribute))) {
            NSTimestamp nstimestamp = (NSTimestamp)obj;
            long millis = nstimestamp.getTime();
            // AK: since NSTimestamp places fractional millis in the getTime,
            // the driver is getting very confused and refuses to update the columns as 
            // they get translated to 0 as the fractional values.
            Timestamp timestamp = new Timestamp(millis);
            timestamp.setNanos(timestamp.getNanos()+nstimestamp.getNanos());
            result.setObjectForKey(timestamp, "BindVariableValue");
        }
        return result;
    }

    /** Overridden in order to add milliseconds to the value. This
     * applies only if obj is an instance of NSTimestamp and if 
     * valueType from the eoattribute is T
     * 
     * @param obj
     * @param eoattribute
     * 
     * @return the modified string
     */
    public String formatValueForAttribute(Object obj, EOAttribute eoattribute) {
        String value;
        if((obj instanceof NSTimestamp) && isTimestampAttribute(eoattribute)) {
            value = "'" + _TIMESTAMP_FORMATTER.format(obj) + "'";
        } else {
            value = super.formatValueForAttribute(obj, eoattribute);
        }
        return value;
    }
    
    private boolean isTimestampAttribute(EOAttribute eoattribute) {
        return "T".equals(eoattribute.valueType());
    }

    /**
     * @return true to indicate that the Oracle jdbc driver should use
     * bind variables
     */
    public boolean useBindVariables() {
        return true;
    }

    /**
     * @return true to indicate that the Oracle jdbc driver should use
     * bind variables
     */
    public boolean shouldUseBindVariableForAttribute(EOAttribute arg0) {
        return true;
    }

    /**
     * @return true to indicate that the Oracle jdbc driver should use
     * bind variables
     */
    public boolean mustUseBindVariableForAttribute(EOAttribute arg0) {
        return true;
    }
}
