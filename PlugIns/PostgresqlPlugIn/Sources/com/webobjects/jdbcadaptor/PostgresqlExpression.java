package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

/**
* Postgres needs special handling of NSData conversion, special
 * escape char and has a regex query selector.
 * @author ak
 */
public class PostgresqlExpression extends JDBCExpression {

    static private final String _DEFERRABLE_MODIFIER = " INITIALLY DEFERRED";
    /**
    * Lookup table for conversion of bytes -> hex.
     */
    private static final char _HEX_VALUES[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    static private final char _SQL_ESCAPE_CHAR = '|';

    public PostgresqlExpression(EOEntity entity) {
        super(entity);
    }

    /**
        * Overridden because the original version throws when the
     * data contains negative byte values.
     */
    public String formatValueForAttribute(Object obj, EOAttribute eoattribute) {
        String value;
        if(obj instanceof NSData) {
            value = sqlStringForData((NSData)obj);
        } else {
            value = super.formatValueForAttribute(obj, eoattribute);
        }
        return value;
    }

    /**
        * Overrides the parent implementation to add an <code>INITIALLY DEFERRED</code> to the generated statement.
     * Useful you want to generate the schema-building SQL from a pure java environment.
     */
    public void prepareConstraintStatementForRelationship( EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns ) {
        super.prepareConstraintStatementForRelationship( relationship, sourceColumns, destinationColumns );
        setStatement( statement() + _DEFERRABLE_MODIFIER );
    }

    /**
        * Overridden because Postgres uses "|" instead of "\" as any other database system.
     */
    public char sqlEscapeChar() {
        return _SQL_ESCAPE_CHAR;
    }

    /**
        * Overriden so we can put a regex-match qualifier in the display groups
     * query bindings. You can bind '~*' to queryOperator.someKey and '.*foo' to
     * queryMatch.someKey and will get the correct results.
     */
    public String sqlStringForSelector(NSSelector selector, Object value) {
        if(selector.name().equals("~*")) {
            return selector.name();
        }
        return super.sqlStringForSelector(selector, value);
    }

    /**
        * Overridden because the original version throws when the
     * data contains negative byte values.
     */
    public String sqlStringForData(NSData data) {
        int length = data.length();
        byte bytes[] = data.bytes();
        char hex[] = new char[2 * length];
        int nibbles = 0;
        for(int i = 0; i < length; i++)  {
            byte b = bytes[i];
            hex[nibbles++] = _HEX_VALUES[((b >> 4) + 16) % 16];
            hex[nibbles++] = _HEX_VALUES[((b & 15) + 16) % 16];
        }
        return "decode('" + new String(hex) + "','hex')";
    }
/*
    public String sqlStringForValue(Object v, String kp) {
        EOAttribute attribute = entity().attributeNamed(kp);
        String suffix = "";
        if(attribute != null) {
            //ENHANCEME ak: we should hande key paths
            return super.sqlStringForValue(v,kp) + "::" + columnTypeStringForAttribute(attribute);
        }
        return super.sqlStringForValue(v,kp);
    }
*/    
}
