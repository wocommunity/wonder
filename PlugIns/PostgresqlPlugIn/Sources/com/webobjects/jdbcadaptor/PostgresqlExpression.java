package com.webobjects.jdbcadaptor;

import java.sql.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Postgres needs special handling of NSData conversion, special
 * escape char, has a regex query selector and handles JOIN clauses correctly.
 * In addition to that, it attaches a LIMIT clause if a fetch spec has a limit set,
 * which can greatly speed up some queries.
 * @author ak: Regex, NSData, fetch limit
 * @author Giorgio Valito: refactoring, typecasting, schema building
 * @author Arturo Perez: JOIN clauses
 */
public class PostgresqlExpression extends JDBCExpression {

    static private final String _DEFERRABLE_MODIFIER = " INITIALLY DEFERRED";

    static private final boolean enableJoinHandling = Boolean.getBoolean(PostgresqlExpression.class + ".enableJoinHandling");

    /**
    * Lookup table for conversion of bytes -> hex.
     */
    private static final char _HEX_VALUES[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    static private final char _SQL_ESCAPE_CHAR = '|';


    private int _fetchLimit;
    
    public PostgresqlExpression(EOEntity entity) {
        super(entity);
    }

    
    /**
     * Overridden to support milliseconds to work with Postgres
     */
    public boolean shouldUseBindVariableForAttribute(EOAttribute eoattribute) {
        String type = columnTypeStringForAttribute(eoattribute);
        if ("timestamp".equals(type)) {
            return false;
        } else {
            return true;
        }
    }

    /** 
     * Overridden to support milliseconds to work with Postgres
     */
    public boolean mustUseBindVariableForAttribute(EOAttribute eoattribute) {
        String type = columnTypeStringForAttribute(eoattribute);
        if ("timestamp".equals(type)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Overridden so we can get the fetch limit from the fetchSpec.
     */
    public void prepareSelectExpressionWithAttributes(NSArray nsarray, boolean flag, EOFetchSpecification eofetchspecification) {
        if(!eofetchspecification.promptsAfterFetchLimit()) {
            _fetchLimit = eofetchspecification.fetchLimit();
        }
        super.prepareSelectExpressionWithAttributes(nsarray, flag, eofetchspecification);
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
     * Overridden because Postgres uses "|" instead of "\" like any
     * other database system.
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

    /**
     * Overridden to add typecasts after the value. I.e. '2'::char. This is
     * done because Postgres can be very strict regarding type conversions.
     * NULL values are excluded from casting.
     * Also contains a bugfix to handle milli seconds in timestamps
     */
    static NSTimestampFormatter timestampFormatter = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S");

    public String sqlStringForValue(Object v, String kp) {
        String result = super.sqlStringForValue(v,kp);
        if(v != null && v != NSKeyValueCoding.NullValue) {
            EOAttribute attribute;
            int lastDotIdx = kp.lastIndexOf(".");
            if (lastDotIdx == -1) {
                attribute = entity().attributeNamed(kp);
            } else {
                EOEntity kpEntity = entityForKeyPath(kp);
                attribute = kpEntity.attributeNamed(kp.substring(lastDotIdx+1));
            }
            if(attribute != null) {
                String s = columnTypeStringForAttribute(attribute);
                //handel millis seconds, too.
                if (v instanceof NSTimestamp) {
                    NSTimestamp t = (NSTimestamp)v;
                    String timestampString = "'"+timestampFormatter.format(t);
                    Timestamp ts = new Timestamp(t.getTime());
                    String nanoString = ts.getNanos() + "";
                    nanoString = nanoString.substring(0, 3);
                    timestampString += "." + nanoString + "'";
                    result = timestampString;
                }
                result = result + "::" + s;
            }
        }
        return result;
    }

    /** Helper class to store a join definition */
    private class JoinClause {
        String table1;
        String op;
        String table2;
        String joinCondition;
        
        public String toString() {
            return table1 + op + table2 + joinCondition;
        }
    }

    /** Holds array of JoinClause. */
    private NSMutableArray _alreadyJoined = new NSMutableArray();

    /**
     * Overriden to handle correct placements of join conditions.
     */
    public String assembleSelectStatementWithAttributes(NSArray attributes,
                                                        boolean lock,
                                                        com.webobjects.eocontrol.EOQualifier qualifier,
                                                        NSArray fetchOrder,
                                                        String selectString,
                                                        String columnList,
                                                        String tableList,
                                                        String whereClause,
                                                        String joinClause,
                                                        String orderByClause,
                                                        String lockClause) {
        StringBuffer sb = new StringBuffer();
        if(enableJoinHandling) {
            sb.append("SELECT ");
            sb.append(columnList);
            sb.append(" FROM ");

            if (_alreadyJoined.count() > 0) {
                sb.append(" ");
                sb.append(joinClauseString());
            } else {
                sb.append(tableList);
            }
            if (whereClause != null && whereClause.length() > 0) {
                sb.append(" WHERE ");
                sb.append(whereClause);
            }
            if (orderByClause != null && orderByClause.length() > 0) {
                sb.append(" ORDER BY ");
                sb.append(orderByClause);
            }
            if (_fetchLimit != 0) {
                sb.append(" LIMIT ");
                sb.append(_fetchLimit);
            }
            if (lockClause != null && lockClause.length() > 0) {
                sb.append(" ");
                sb.append(lockClause);
            }
        } else {
            sb.append(super.assembleSelectStatementWithAttributes(attributes, lock, qualifier, fetchOrder, selectString, columnList, tableList, whereClause, joinClause, orderByClause, lockClause));
            if (_fetchLimit != 0) {
                sb.append(" LIMIT ");
                sb.append(_fetchLimit);
            }
        }
        
        return sb.toString();
    }

    /**
     * Overridden to compose the final string expression for the join clause
     * from the array we constructed previously.
     */
    public String joinClauseString() {
        if(enableJoinHandling) {
            NSMutableDictionary seenIt = new NSMutableDictionary();
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < _alreadyJoined.count(); i++) {
                JoinClause jc = (JoinClause)_alreadyJoined.objectAtIndex(i);
                boolean seenTable1 = seenIt.objectForKey(jc.table1) != null;
                boolean seenTable2 = seenIt.objectForKey(jc.table2) != null;

                if (!seenTable1 && !seenTable2) {
                    sb.append(jc.table1);
                    sb.append(jc.op);
                    sb.append(jc.table2);
                } else if (!seenTable2) {
                    sb.append(jc.op);
                    sb.append(jc.table1);
                } else {
                    sb.append(jc.op);
                    sb.append(jc.table2);
                }
                sb.append(jc.joinCondition);
                sb.append(" ");

                seenIt.setObjectForKey(jc.table1, jc.table1);
                seenIt.setObjectForKey(jc.table2, jc.table2);
            }
            return sb.toString();
        } else {
            return super.joinClauseString();
        }
    }
    
    /**
     * Overriden to not call the super implementation.
     */
    public void addJoinClause(String leftName,
                              String rightName,
                              int semantic) {
        if(enableJoinHandling) {
            JoinClause clause = createJoinClause(leftName, rightName, semantic);
            _alreadyJoined.insertObjectAtIndex(clause, 0);
        } else {
            super.addJoinClause(leftName, rightName, semantic);
        }
    }

    
    /**
     * Overriden to construct a valid SQL92 JOIN clause as opposed to
     * the Oracle-like SQL the superclass produces.
     */
    public String assembleJoinClause(String leftName, String rightName, int semantic) {
        if(enableJoinHandling) {
            return createJoinClause(leftName, rightName, semantic).toString();
        }
        return super.assembleJoinClause(leftName, rightName, semantic);
    }

    /**
     * Utility to construct a JoinClause.
     */
    private JoinClause createJoinClause(String leftName, String rightName, int semantic) {
        String leftAlias = leftName.substring(0, leftName.indexOf("."));
        String rightAlias = rightName.substring(0, rightName.indexOf("."));

        String leftCol = leftName.substring(leftName.indexOf(".") + 1);
        String rightCol = rightName.substring(rightName.indexOf(".") + 1);

        NSArray k;
        EOEntity rightEntity;
        EOEntity leftEntity;

        if (leftAlias.equals("t0")) {
            leftEntity = entity();
        } else {
            k = aliasesByRelationshipPath().allKeysForObject(leftAlias);
            String leftKey = k.count()>0? (String)k.lastObject() : "";
            leftEntity = entityForKeyPath(leftKey);
        }

        if (rightAlias.equals("t0")) {
            rightEntity = entity();
        } else {
            k = aliasesByRelationshipPath().allKeysForObject(rightAlias);
            String rightKey = k.count()>0? (String)k.lastObject() : "";
            rightEntity = entityForKeyPath(rightKey);
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

        if (leftCol.equals(rightCol)) {
            jc.joinCondition = " USING (" + leftCol + ")";
        } else {
            jc.joinCondition = " ON " + leftName + " = " + rightName;
        }
        return jc;
    }

    /**
     * Utility that traverses a key path to find the last destination entity
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
}
