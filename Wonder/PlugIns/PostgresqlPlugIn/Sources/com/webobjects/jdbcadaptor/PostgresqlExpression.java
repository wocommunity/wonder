package com.webobjects.jdbcadaptor;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

/**
 * Postgres needs special handling of NSData conversion, special
 * escape char, has a regex query selector and handles JOIN clauses correctly.
 * @author ak: Regex, NSData
 * @author Giorgio Valoti: refactoring, typecasting, schema building
 * @author Arturo Perez: JOIN clauses
 * @author David Teran: Timestamps handling
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
     * if true, don't use new join clause code. 
     *
     * @deprecated
     */
    private boolean disableOuterJoins = false;
    
    /**
     * if true, don't use typecasting. 
     */
    private boolean _disableTypeCasting = Boolean.getBoolean("com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting");
    
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
    
    static private final char _SQL_ESCAPE_CHAR = '|';
    
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
     * Overriden to not call the super implementation.
     * 
     * @param leftName  the table name on the left side of the clause
     * @param rightName the table name on the right side of the clause
     * @param semantic  the join semantic
     */
    public void addJoinClause(String leftName,
                              String rightName,
                              int semantic) {
        if (disableOuterJoins)
            super.addJoinClause(leftName, rightName, semantic);
        else
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
        if (disableOuterJoins == true || !useAliases()) {
            return super.assembleJoinClause(leftName, rightName, semantic);
        }
        
        StringBuffer sb = new StringBuffer();
        String leftAlias = leftName.substring(0, leftName.indexOf("."));
        String rightAlias = rightName.substring(0, rightName.indexOf("."));
        
        String leftCol = leftName.substring(leftName.indexOf(".") + 1);
        String rightCol = rightName.substring(rightName.indexOf(".") + 1);
        
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
        String rightTable = rightEntity.externalName();
        String leftTable = leftEntity.externalName();
        
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
        NSArray destCols = (NSArray) r.destinationAttributes().valueForKey( "columnName" );
        NSArray sourceCols = (NSArray) r.sourceAttributes().valueForKey( "columnName" );
        NSArray joins = r.joins();
        int joinsCount = joins.count();
        NSMutableArray joinStrings = new NSMutableArray( joinsCount );
        for( int i = 0; i < joinsCount; i++ ) {
            EOJoin currentJoin = (EOJoin)joins.objectAtIndex(i);
            joinStrings.addObject
                ( leftAlias +"."+ currentJoin.sourceAttribute().columnName() + " = " + 
                  rightAlias +"."+ currentJoin.destinationAttribute().columnName() );
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
        if (disableOuterJoins) {
            fieldString = tableList;
        } else {
            if (_alreadyJoined.count() > 0) {
                fieldString = joinClauseString();
            } else {
                fieldString = tableList;
            }
        }
        sb.append(fieldString);
        if ((whereClause != null && whereClause.length() > 0) ||
            (disableOuterJoins && (joinClause != null && joinClause.length() > 0))) {
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
        } else {
            value = super.formatValueForAttribute(obj, eoattribute);
        }
        return value;
    }
    
    /**
     * Overrides the parent implementation to compose the final string
     * expression for the join clauses.
     */
    public String joinClauseString() {
        if (disableOuterJoins)
            return super.joinClauseString();
        
        
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
     * Overridden to support milliseconds to work with Postgres
     *
     * @param eoattribute   the attribute
     */
    public boolean mustUseBindVariableForAttribute(EOAttribute eoattribute) {
        if ("T".equals(eoattribute.valueType())) {
            return false;
        } else {
            return true;
        }
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
     * Overridden to support milliseconds to work with Postgres
     *
     * @param eoattribute   the attribute
     * @return  yes/no
     */
    public boolean shouldUseBindVariableForAttribute(EOAttribute eoattribute) {
        if ("T".equals(eoattribute.valueType())) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Overridden because Postgres uses "|" instead of "\" like any
     * other database system.
     */
    public char sqlEscapeChar() {
        return _SQL_ESCAPE_CHAR;
    }

    /**
     * Overridden because the original version throws an exception when the
     * data contains negative byte values.
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
     * Overrides the parent implementation to add typecasts after the value, i.e. '2'::char,
     * which is required with certain PostgreSQL versions (<=7.4.x) for the correct query processing, 
     * particularly with index usage. 
     * Also contains a bugfix to handle milli seconds in timestamps
     * NULL values are excluded from casting. 
     * You can set the System default <code>com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting</code>
     * to true to disable both fixes (the former you might want to disable when PG says it can't cast a certain value and
     * the second when you have values with a greater resolution already in the DB).
     * @param v the value
     * @param kp    the keypath associated with the value
     */
    public String sqlStringForValue(Object v, String kp) {
        if(_disableTypeCasting) {
            return super.sqlStringForValue(v,kp);
        }
        EOAttribute attribute;
        int lastDotIdx = kp.lastIndexOf(".");
        if (lastDotIdx == -1) {
            attribute = entity().attributeNamed(kp);
        }
        else {
            EOEntity kpEntity = entityForKeyPath(kp);
            attribute = kpEntity.attributeNamed(kp.substring(lastDotIdx+1));
        }
        if(attribute != null && v != null && v != NSKeyValueCoding.NullValue) {
            String s = columnTypeStringForAttribute(attribute);
            if( v instanceof NSTimestamp ) {
                return "'"+_TIMESTAMP_FORMATTER.format((NSTimestamp)v) + "'::"+ s;
            }
            return super.sqlStringForValue(v,kp) + "::" + s;
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
        
}
