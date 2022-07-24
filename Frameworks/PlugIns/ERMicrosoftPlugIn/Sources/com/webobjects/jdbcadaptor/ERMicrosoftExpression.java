package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import java.sql.Timestamp;

/**
 * Overrides {@code MicrosoftExpression} in order to add TIMESTAMP values including milliseconds.
 *
 * @author hprange (most code was copied from {@code EROracleExpression})
 */
public class ERMicrosoftExpression extends MicrosoftPlugIn.MicrosoftExpression {
    private static final NSTimestampFormatter _TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

    public ERMicrosoftExpression(EOEntity entity) {
        super(entity);
    }

    /**
     * Overridden in order to add milliseconds to the value. This applies only if value is an instance of
     * {@code NSTimestamp} and if valueType from the attribute is {@code 'T'}.
     *
     * @param attribute the EOAttribute associated with the bind variable dictionary
     * @param value     the value associated with the bind variable dictionary
     * @return the modified bind variable dictionary for {@code attribute} and {@code value}
     */
    @Override
    public NSMutableDictionary<String, Object> bindVariableDictionaryForAttribute(EOAttribute attribute, Object value) {
        NSMutableDictionary<String, Object> result = super.bindVariableDictionaryForAttribute(attribute, value);

        if (value instanceof NSTimestamp && isTimestampAttribute(attribute)) {
            NSTimestamp nstimestamp = (NSTimestamp) value;
            long millis = nstimestamp.getTime();
            // AK: since NSTimestamp places fractional millis in the getTime,
            // the driver is getting very confused and refuses to update the columns as
            // they get translated to 0 as the fractional values.
            Timestamp timestamp = new Timestamp(millis);
            timestamp.setNanos(timestamp.getNanos() + nstimestamp.getNanos());
            result.setObjectForKey(timestamp, "BindVariableValue");
        }

        return result;
    }

    /**
     * Overridden to add milliseconds to the value. This applies only if value is an instance of {@code NSTimestamp} and
     * if valueType from the attribute is {@code T}.
     *
     * @param value     an object to be used in a SQL statement
     * @param attribute an EOAttribute to be used in influencing the format
     * @return the modified formatted string
     */
    @Override
    public String formatValueForAttribute(Object value, EOAttribute attribute) {
        if (value instanceof NSTimestamp && isTimestampAttribute(attribute)) {
            return "'" + _TIMESTAMP_FORMATTER.format(value) + "'";
        }

        return super.formatValueForAttribute(value, attribute);
    }

    private boolean isTimestampAttribute(EOAttribute attribute) {
        return "T".equals(attribute.valueType());
    }
}
