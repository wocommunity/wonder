package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.jdbcadaptor.*;
import com.webobjects.foundation.*;

/**
 * Postgres needs special handling of NSData conversion, special
 * escape char and has a regex query selector.
 * @author ak
 */
public class PostgresqlExpression extends JDBCExpression {

    /**
     * Designated constructor.
     */
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
            hex[nibbles++] = hexValues[((b >> 4) + 16) % 16];
            hex[nibbles++] = hexValues[((b & 15) + 16) % 16];
        }
        return "decode('" + new String(hex) + "','hex')";
    }

    /**
     * Lookup table for conversion of bytes -> hex.
     */
    private static final char hexValues[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Overridden because Postgres uses "|" instead of "\" as any other database system. 
     */
     public char sqlEscapeChar() {
         return '|';
     }
}
