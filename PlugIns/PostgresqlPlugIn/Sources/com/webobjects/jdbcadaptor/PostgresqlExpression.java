package com.webobjects.jdbcadaptor;

import java.sql.Timestamp;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

/**
 * Postgres needs special handling of NSData conversion, special
 * escape char, has a regex query selector and handles JOIN clauses correctly.
 * @author ak: Regex, NSData
 * @author Giorgio Valoti: refactoring, typecasting, schema building
 * @author Arturo Perez: JOIN clauses
 * @author David Teran: Timestamps handling
 * @author Tim Cummings: case sensitive table and column names
 */
public class PostgresqlExpression extends JDBCExpression {

    /**
     * Selector used for case insensitive regular expressions.
     **/
    public static final NSSelector CaseInsensitiveRegexOperator = new NSSelector( "~*", new Class[]{ Object.class });
    
    /**
     * Selector used for case sensitive regular expressions.
     */
    public static final NSSelector RegexOperator = new NSSelector( "~", new Class[]{ Object.class });
    
    /**
     * if true, don't use typecasting. 
     */
    private Boolean disableTypeCasting = null;
    
    private Boolean disableBindVariables = null;
    
    /**
     * Holds array of join clauses.
     */
    private NSMutableArray _alreadyJoined = new NSMutableArray();    
    
    /**
     * Fetch spec limit ivar
     */
    private int _fetchLimit;
    
    /**
     * Lookup table for conversion of bytes -> hex.
     */
    private static final char _HEX_VALUES[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    private static final char _SQL_ESCAPE_CHAR = '|';

    private static final String _EXTERNAL_NAME_QUOTE_CHARACTER = "\"";   

    private static final NSTimestampFormatter _TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");
    
    /**
     * Overridden to remove the rtrim usage. The original implementation
     * will remove every trailing space from character based column which 
     * should not be OK for Postgres.
     */
    public PostgresqlExpression(EOEntity entity) {
        super(entity);
    }
    
    /**
     * Overridden to fix an issue with NStimestamp classes and "T" value-typed attributes. 
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

    /**
     * Overriden to not call the super implementation.
     * 
     * @param leftName  the table name on the left side of the clause
     * @param rightName the table name on the right side of the clause
     * @param semantic  the join semantic
     */
    public void addJoinClause(String leftName,
                              String rightName,
                              int semantic) {
        assembleJoinClause(leftName, rightName, semantic);
    }
    
    /**
     * Overriden to contruct a valid SQL92 JOIN clause as opposed to
     * the Oracle-like SQL the superclass produces.
     *
     * @param leftName  the table name on the left side of the clause
     * @param rightName the table name on the right side of the clause
     * @param semantic  the join semantic
     * @return  the join clause
     */
    public String assembleJoinClause(String leftName,
                                     String rightName,
                                     int semantic) {
        if (!useAliases()) {
            return super.assembleJoinClause(leftName, rightName, semantic);
        }
        
        String leftAlias = leftName.substring(0, leftName.indexOf("."));
        String rightAlias = rightName.substring(0, rightName.indexOf("."));
        
        NSArray k;
        EOEntity rightEntity;
        EOEntity leftEntity;
        String relationshipKey = null;
        EORelationship r;
        
        
        if (leftAlias.equals("t0")) {
            leftEntity = entity();
        } else {
            k = aliasesByRelationshipPath().allKeysForObject(leftAlias);
            relationshipKey = k.count()>0? (String)k.lastObject() : "";
            leftEntity = entityForKeyPath(relationshipKey);
        }
        
        if (rightAlias.equals("t0")) {
            rightEntity = entity();
        } else {
            k = aliasesByRelationshipPath().allKeysForObject(rightAlias);
            relationshipKey = k.count()>0? (String)k.lastObject() : "";
            rightEntity = entityForKeyPath(relationshipKey);
        }
        int dotIndex = relationshipKey.indexOf( "." );
        relationshipKey = dotIndex == -1
            ? relationshipKey
            : relationshipKey.substring( relationshipKey.lastIndexOf( "." ) + 1 );
        r = rightEntity.anyRelationshipNamed( relationshipKey );
        // fix from Michael MŸller for the case Foo.fooBars.bar has a Bar.foo relationship (instead of Bar.foos)
        if( r == null || r.destinationEntity() != leftEntity ) {
            r = leftEntity.anyRelationshipNamed( relationshipKey );
        }
        //timc 2006-02-26 IMPORTANT or quotes are ignored and mixed case field names won't work
        String rightTable = rightEntity.valueForSQLExpression(this);
        String leftTable = leftEntity.valueForSQLExpression(this); 
         
        JoinClause jc = new JoinClause();
        
        jc.table1 = leftTable + " " + leftAlias;
        
        switch (semantic) {
            case EORelationship.LeftOuterJoin:
                jc.op = " LEFT OUTER JOIN ";
                break;
            case EORelationship.RightOuterJoin:
                jc.op = " RIGHT OUTER JOIN ";
                break;
            case EORelationship.FullOuterJoin:
                jc.op = " FULL OUTER JOIN ";
                break;
            case EORelationship.InnerJoin:
                jc.op = " INNER JOIN ";
                break;
        }
        
        jc.table2 = rightTable + " " + rightAlias;
        NSArray joins = r.joins();
        int joinsCount = joins.count();
        NSMutableArray joinStrings = new NSMutableArray( joinsCount );
        for( int i = 0; i < joinsCount; i++ ) {
            EOJoin currentJoin = (EOJoin)joins.objectAtIndex(i);
            String left = leftAlias +"."+ sqlStringForSchemaObjectName(currentJoin.sourceAttribute().columnName());
            String right =  rightAlias +"."+ sqlStringForSchemaObjectName(currentJoin.destinationAttribute().columnName());
            joinStrings.addObject( left + " = " + right);
        }
        jc.joinCondition = " ON " + joinStrings.componentsJoinedByString( " AND " );
        if( !_alreadyJoined.containsObject( jc ) ) {
            _alreadyJoined.insertObjectAtIndex(jc, 0);
            return jc.toString();
        }
        return null;
    }
    
    /**
     * Overriden to handle correct placements of join conditions and 
     * to handle DISTINCT fetches with compareCaseInsensitiveA(De)scending sort orders.
     *
     * @param attributes    the attributes to select
     * @param lock  flag for locking rows in the database
     * @param qualifier the qualifier to restrict the selection
     * @param fetchOrder    specifies the fetch order
     * @param columnList    the SQL columns to be fetched
     * @param tableList the the SQL tables to be fetched
     * @param whereClause   the SQL where clause
     * @param joinClause    the SQL join clause
     * @param orderByClause the SQL sort order clause
     * @param lockClause    the SQL lock clause
     * @return  the select statement
     */
    public String assembleSelectStatementWithAttributes(NSArray attributes,
                                                        boolean lock,
                                                        EOQualifier qualifier,
                                                        NSArray fetchOrder,
                                                        String selectString,
                                                        String columnList,
                                                        String tableList,
                                                        String whereClause,
                                                        String joinClause,
                                                        String orderByClause,
                                                        String lockClause) {
        StringBuffer sb = new StringBuffer();
        sb.append(selectString);
        sb.append(columnList);
        // AK: using DISTINCT with ORDER BY UPPER(foo) is an error if it is not also present in the columns list...
        // This implementation sucks, but should be good enough for the normal case
        if(selectString.indexOf(" DISTINCT") != -1) {
            String [] columns = orderByClause.split(",");
            for(int i = 0; i < columns.length; i++) {
                String column = columns[i].replaceFirst("\\s+(ASC|DESC)\\s*", "");
                if(columnList.indexOf(column) == -1) {
                    sb.append(", ");
                    sb.append(column);
                }
            }
        }
        sb.append(" FROM ");
        String fieldString;
        if (_alreadyJoined.count() > 0) {
            fieldString = joinClauseString();
        } else {
            fieldString = tableList;
        }
        sb.append(fieldString);
        if ((whereClause != null && whereClause.length() > 0) ||
            (joinClause != null && joinClause.length() > 0)) {
            sb.append(" WHERE ");
            if (joinClause != null && joinClause.length() > 0) {
                sb.append(joinClause);
                if (whereClause != null && whereClause.length() > 0)
                    sb.append(" AND ");
            }
            
            if (whereClause != null && whereClause.length() > 0) {
                sb.append(whereClause);
            }
        }
        if (orderByClause != null && orderByClause.length() > 0) {
            sb.append(" ORDER BY ");
            sb.append(orderByClause);
        }
        if (lockClause != null && lockClause.length() > 0) {
            sb.append(" ");
            sb.append(lockClause);
        }
        if (_fetchLimit != 0) {
            sb.append(" LIMIT ");
            sb.append(_fetchLimit);
        }        
        return sb.toString();
    }
    
    /**
     * Overrides the parent implementation to provide support
     * for array data types.
     *
     * @param attribute the EOattribute
     * @return  the PostgreSQL specific type string for <code>attribute</code>
     */
    public String columnTypeStringForAttribute( EOAttribute attribute ) {
        if( attribute.externalType().endsWith( "[]" ) ) {
            return attribute.externalType();
        }
        return super.columnTypeStringForAttribute( attribute );
    }
    
    /**
     * Utility that traverses a key path to find the last destination entity
     *
     * @param keyPath   the key path
     * @return  the entity at the end of the keypath
     */
    private EOEntity entityForKeyPath(String keyPath) {
        NSArray keys = NSArray.componentsSeparatedByString(keyPath, ".");
        EOEntity ent = entity();
        
        for (int i = 0; i < keys.count(); i++) {
            String k = (String)keys.objectAtIndex(i);
            EORelationship rel = ent.anyRelationshipNamed(k);
            if (rel == null) {
                // it may be an attribute 
                if (ent.anyAttributeNamed(k) != null) {
                    break;
                }
                throw new IllegalArgumentException("relationship " + keyPath + " generated null");
            }
            ent = rel.destinationEntity();
        }
        return ent;
    }
    
    /**
     * Overridden because the original version throws when the
     * data contains negative byte values.
     *
     * @param obj   the object used in the SQL statement
     * @param eoattribute   the attribute associated with <code>obj</code>
     * @return  the formatted string
     */
    public String formatValueForAttribute(Object obj, EOAttribute eoattribute) {
        String value;
        if(obj instanceof NSData) {
            value = sqlStringForData((NSData)obj);
        } else if((obj instanceof NSTimestamp) && isTimestampAttribute(eoattribute)) {
            value = "'" + _TIMESTAMP_FORMATTER.format(obj) + "'";
        } else if(obj instanceof String) {
        	value = formatStringValue((String)obj);
        } else if(obj instanceof Number) {
        	value = (String) eoattribute.adaptorValueByConvertingAttributeValue(obj).toString();
        } else if(obj instanceof Boolean) {
        	value = ((Boolean)obj).toString();
        } else if(obj instanceof Timestamp) {
        	value = "'" + ((Timestamp)obj).toString() + "'";
        } else if (obj == null || obj == NSKeyValueCoding.NullValue) {
        	value = "NULL";
        } else {
        	// AK: I don't really like this, but we might want to prevent infinite recursion
        	try {
        		Object adaptorValue = eoattribute.adaptorValueByConvertingAttributeValue(obj);
        		if(adaptorValue instanceof NSData || adaptorValue instanceof NSTimestamp
        				|| adaptorValue instanceof String || adaptorValue instanceof Number 
        				|| adaptorValue instanceof Boolean) {
        			value = formatValueForAttribute(adaptorValue, eoattribute);
        		} else {
        			NSLog.err.appendln(this.getClass().getName() +  ": Can't convert: " + obj + ":" + obj.getClass() + " -> " + adaptorValue + ":" +adaptorValue.getClass() );
        			value = obj.toString();
        		}
        	} catch(Exception ex) {
        		NSLog.err.appendln(this.getClass().getName() +  ": Exception while converting " + obj.getClass().getName());
        		NSLog.err.appendln(ex);
        		value = obj.toString();
        	}
        }
        return value;
    }

   /**
     * Helper to check for timestamp columns that have a "T" value type.
     * @param eoattribute
     * @return
     */
    private boolean isTimestampAttribute(EOAttribute eoattribute) {
        return "T".equals(eoattribute.valueType());
    }

    /**
     * Helper to check for data columns that are not keys.
     * @param eoattribute
     * @return
     */
    private boolean isDataAttribute(EOAttribute attribute) {
        return (attribute.className().equals("com.webobjects.foundation.NSData") ||
    	attribute.externalType().equals("bytea") ||
    	attribute.externalType().equals("bit")) 
    	&& entity().classProperties().containsObject(attribute);
    }

    /**
     * Overrides the parent implementation to compose the final string
     * expression for the join clauses.
     */
    public String joinClauseString() {
        NSMutableDictionary seenIt = new NSMutableDictionary();
        StringBuffer sb = new StringBuffer();
        JoinClause jc;
        EOSortOrdering.sortArrayUsingKeyOrderArray
            ( _alreadyJoined, new NSArray( EOSortOrdering.sortOrderingWithKey( "sortKey", EOSortOrdering.CompareCaseInsensitiveAscending ) ) );
        if (_alreadyJoined.count() > 0) {
            jc = (JoinClause)_alreadyJoined.objectAtIndex(0);
            
            sb.append(jc);
            seenIt.setObjectForKey(Boolean.TRUE, jc.table1);
            seenIt.setObjectForKey(Boolean.TRUE, jc.table2);
        }
        
        for (int i = 1; i < _alreadyJoined.count(); i++) {
            jc = (JoinClause)_alreadyJoined.objectAtIndex(i);
            
            sb.append(jc.op);
            if (seenIt.objectForKey(jc.table1) == null) {
                sb.append(jc.table1);
                seenIt.setObjectForKey(Boolean.TRUE, jc.table1);
            }
            else if (seenIt.objectForKey(jc.table2) == null) {
                sb.append(jc.table2);
                seenIt.setObjectForKey(Boolean.TRUE, jc.table2);
            }
            sb.append(jc.joinCondition);
        }
        return sb.toString();
    }
    
    
    /**
     * Overrides the parent implementation to add an <code>INITIALLY DEFERRED</code> to the generated statement.
     * Useful you want to generate the schema-building SQL from a pure java environment.
     * 
     * @param relationship  the relationship
     * @param sourceColumns the source columns for the constraints
     * @param destinationColumns    the destination columns for the constraints
     */
    public void prepareConstraintStatementForRelationship( EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns ) {
        super.prepareConstraintStatementForRelationship( relationship, sourceColumns, destinationColumns );
        setStatement( statement() + " INITIALLY DEFERRED" );
    }
    
    
    /**
     * Overridden so we can get the fetch limit from the fetchSpec.
     *
     * @param nsarray   the array of attributes
     * @param flag  locking flag
     * @param eofetchspecification  the fetch specification
     */
    public void prepareSelectExpressionWithAttributes(NSArray nsarray, boolean flag, EOFetchSpecification eofetchspecification) {
        if(!eofetchspecification.promptsAfterFetchLimit()) {
            _fetchLimit = eofetchspecification.fetchLimit();
        }
        super.prepareSelectExpressionWithAttributes(nsarray, flag, eofetchspecification);
    }
    
    /**
     * Overridden because Postgres uses "|" instead of "\" like any
     * other database system.
     */
    public char sqlEscapeChar() {
        return _SQL_ESCAPE_CHAR;
    }
    
    /**
     * Overridden because PostgreSQL does not use the default quote character in EOSQLExpression.externalNameQuoteCharacter() which is an empty string.
     * 
     */
    public String externalNameQuoteCharacter() { 
        return _EXTERNAL_NAME_QUOTE_CHARACTER; 
    }
   
    /**
     * Overridden because the original version does not correctly quote mixed case fields in all cases.
     * SELECT statements were OK (useAliases is true) INSERT, UPDATE, DELETE didn't quote mixed case field names.
     * 
     * @param data  the attribute (column name) to be converted to a SQL string
     * @return  the SQL string for the attribute
     */
    public String sqlStringForAttribute(EOAttribute attribute) {
        String sql = null;
        if ( attribute.isDerived() || useAliases() || attribute.columnName() == null ) {
            sql = super.sqlStringForAttribute(attribute);
        } else {
            sql = sqlStringForSchemaObjectName(attribute.columnName());
        }
        //NSLog.out.appendln("PostgresqlExpression.sqlStringForAttribute " + attribute.columnName() + ", isDerived() = " + attribute.isDerived() + ", useAliases() = " + useAliases() + ", sql = " + sql); 
        return sql;
    }  
    
    /**
     * Overridden because the original version does not correctly quote mixed case table names in all cases.
     * SELECT statements were OK (useAliases is true) INSERT, UPDATE, DELETE didn't quote mixed case field names.
     * 
     * @return  the SQL string for the table names
     */
    public String tableListWithRootEntity(EOEntity entity) {
        String sql = null;
        if ( useAliases() ) {
            sql = super.tableListWithRootEntity(entity);
        } else {
            sql = entity.valueForSQLExpression(this); //which must be sqlStringForSchemaObjectName(entity.externalName());
        }
        //NSLog.out.appendln("PostgresqlExpression.tableListWithRootEntity " + entity.externalName() + ", useAliases() = " + useAliases() + ", sql = " + sql); 
        return sql;
    }
    

    /**
     * Overridden because the original version throws an exception when the
     * data contains negative byte values. 
     * This method is only for smaller values like binary primary keys or such.
     *
     * @param data  the data to be converted to a SQL string
     * @return  the SQL string for raw data
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
        
    /**
     * Overriden so we can put a regex-match qualifier in the display groups
     * query bindings. You can bind '~*' or '~' to queryOperator.someKey and '.*foo' to
     * queryMatch.someKey and will get the correct results.
     *
     * @param selector  the selector that specifies the SQL operator
     * @param value the value to be associated with <code>selector</code>
     * @return  the SQL operator string
     */
    public String sqlStringForSelector(NSSelector selector, Object value){
        if(CaseInsensitiveRegexOperator.name().equals( selector.name() ) || RegexOperator.name().equals( selector.name() ) ) {
            return selector.name();
        }
        return super.sqlStringForSelector(selector, value);
    }
    
    /**
     * Overrides the parent implementation to:
     * <ul>
     * <li>add typecasts after the value, i.e. '2'::char,
     * which is required with certain PostgreSQL versions (<=7.4.x) for the correct query processing, 
     * particularly with index usage. 
     * <li>quotes values if bind variables are disabled on this attribute. 
     * </ul>
     * NULL values are excluded from casting. <br/>
     * You can set the System default <code>com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting</code>
     * to true to disable both fixes (the former you might want to disable when PG says it can't cast a certain value and
     * the second when you have values with a greater resolution already in the DB).
     * @param v the value
     * @param kp    the keypath associated with the value
     */
    public String sqlStringForValue(Object v, String kp) {
        if(disableTypeCasting()) {
            return super.sqlStringForValue(v,kp);
        }
        EOAttribute attribute;
        int lastDotIdx = kp.lastIndexOf(".");
        if (lastDotIdx == -1) {
        	attribute = entity().attributeNamed(kp);
        } else {
        	EOEntity kpEntity = entityForKeyPath(kp);
        	attribute = kpEntity.attributeNamed(kp.substring(lastDotIdx+1));
        }
        if(attribute != null && v != null && v != NSKeyValueCoding.NullValue) {
        	String s = columnTypeStringForAttribute(attribute);
        	return super.sqlStringForValue(v, kp) + "::" + s;
        } 
        
        return super.sqlStringForValue(v,kp);
    }
    
    /**
     * Helper class that stores a join definition and
     * helps <code>PostgresqlExpression</code> to assemble
     * the correct join clause.
     */
    public class JoinClause {
        String table1;
        String op;
        String table2;
        String joinCondition;
        
        public String toString() {
            return table1 + op + table2 + joinCondition;
        }
        
        public boolean equals( Object obj ) {
            if( obj == null || !(obj instanceof JoinClause) ) {
                return false;
            }
            return toString().equals( obj.toString() );
        }
        
        /**
         * Property that makes this class "sortable". 
         * Needed to correctly assemble a join clause.
         */
        public String sortKey() {
            return table1.substring( table1.indexOf( " " ) + 1 );
        }
    }
    
    /**
     * Checks the system property <code>com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting</code> to enable or
     * disable typecasting (appending ::somepostgrestype) for attributes.
     * @return
     */
    private boolean disableTypeCasting() {
        if (disableTypeCasting == null) {
        	disableTypeCasting = Boolean.getBoolean("com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting") ? Boolean.TRUE : Boolean.FALSE;
        }
        return disableTypeCasting.booleanValue();
    }
    
    /**
     * Checks the system property <code>com.webobjects.jdbcadaptor.PostgresqlExpression.disableBindVariables</code> to enable
     * or disable bind variables in general.
     * @return
     */
    private boolean disableBindVariables() {
    	if (disableBindVariables == null) {
    		disableBindVariables = Boolean.getBoolean("com.webobjects.jdbcadaptor.PostgresqlExpression.disableBindVariables") ? Boolean.TRUE : Boolean.FALSE;
    	}
    	return disableBindVariables.booleanValue();
    }
    
    /**
     * Overriddden to return the negated value of {@see #disableBindVariables()}.
     */
    public boolean useBindVariables() {
        return !disableBindVariables();
    }
    
    /**
     * Overridden to set the <code>disableBindVariables</code> value correctly.
     * @param value
     */
    public void setUseBindVariables(boolean value) {
    	disableBindVariables = (value ? Boolean.FALSE : Boolean.TRUE);
    }

    /**
     * Overridden to return true only if bind variables are enabled or the is a data type.
     */
    public boolean shouldUseBindVariableForAttribute(EOAttribute attribute) {
        return useBindVariables() || isDataAttribute(attribute);
    }

    /**
     * Overridden to return true only if bind variables are enabled or the is a data type.
     */
    public boolean mustUseBindVariableForAttribute(EOAttribute attribute) {
    	return useBindVariables() || isDataAttribute(attribute);
     }

    /**
     * Replaces a given string by another string in a string.
     * @param old string to be replaced
     * @param newString to be inserted
     * @param buffer string to have the replacement done on it
     * @return string after having all of the replacement done.
     */
    public static String replaceStringByStringInString(String old, String newString, String buffer) {
        int begin, end;
        int oldLength = old.length();
        int length = buffer.length();
        StringBuffer convertedString = new StringBuffer(length + 100);

        begin = 0;
        while(begin < length)
        {
            end = buffer.indexOf(old, begin);
            if(end == -1)
            {
                convertedString.append(buffer.substring(begin));
                break;
            }
            if(end == 0)
                convertedString.append(newString);
            else {
                convertedString.append(buffer.substring(begin, end));
                convertedString.append(newString);
            }
            begin = end+oldLength;
        }
        return convertedString.toString();
    }
}
