package com.webobjects.jdbcadaptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSStringUtilities;

/**
 * Postgres needs special handling of NSData conversion, special
 * escape char, has a regex query selector and handles JOIN clauses correctly.
 * @author ak: Regex, NSData
 * @author Giorgio Valoti: refactoring, typecasting, schema building
 * @author Arturo Perez: JOIN clauses
 * @author David Teran: Timestamps handling
 * @author Tim Cummings: case sensitive table and column names
 * @author cug: hacks for identifier quoting while creating tables
 */
public class PostgresqlExpression extends JDBCExpression {

    /**
     * Selector used for case insensitive regular expressions.
     **/
    // CHECKME AK: why is this public??
    public static final NSSelector CASE_INSENSITIVE_REGEX_OPERATOR = new NSSelector( "~*", new Class[]{ Object.class });
    
    /**
     * Selector used for case sensitive regular expressions.
     */
    // CHECKME AK: why is this public??
    public static final NSSelector REGEX_OPERATOR = new NSSelector( "~", new Class[]{ Object.class });

    /**
     * Lookup table for conversion of bytes -> hex.
     */
    private static final char HEX_VALUES[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    /**
     * SQL escape character
     */
    private static final char SQL_ESCAPE_CHAR = '|';

    /**
     * Quote character when using case sensitive queries.
     */
    private static final String EXTERNAL_NAME_QUOTE_CHARACTER = "\"";   
    
    /**
     * Formatter to use when handling date columns. Each thread has its own copy.
     */
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("''yyyy-MM-dd''");
        }
    };

    /**
     * Formatter to use when handling timestamp columns. Each thread has its own copy.
     */
    private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss.SSS''");
        }
    };

    /**
     * Method to get the string value from a BigDecimals from.
     */
    private static Method _bigDecimalToString = null;

    /**
     * If true, don't use typecasting, ie: 'some text'::varchar(255)
     */
    private Boolean _disableTypeCasting = null;
    
    /**
     * If true, queries will be created by using 
     */
    private Boolean _disableBindVariables = null;
    
    /**
     * Holds array of join clauses.
     */
    private NSMutableArray<JoinClause> _alreadyJoined = new NSMutableArray<JoinClause>();
    
    /**
     * Fetch spec limit ivar
     */
    private int _fetchLimit;

    /**
     * Fetch spec range ivar
     */
    private NSRange _fetchRange;
    private final NSSelector<NSRange> _fetchRangeSelector = new NSSelector<NSRange>("fetchRange");

    private Boolean _enableIdentifierQuoting;
    
    private Boolean _enableBooleanQuoting;

	private Boolean _useLowercaseForCaseInsensitiveLike;
    
    /**
     * Overridden to remove the rtrim usage. The original implementation
     * will remove every trailing space from character based column which 
     * should not be OK for Postgres.
     */
    public PostgresqlExpression(EOEntity entity) {
        super(entity);

    	if (useLowercaseForCaseInsensitiveLike()) {
    		_upperFunctionName = "LOWER";
    	}
    	
    	String customFunctionName = customFunctionForStringComparison();

		if(customFunctionName != null) {
			_upperFunctionName = customFunctionName;
		}
	}
    
	/**
     * Checks the system property
     * <code>com.webobjects.jdbcadaptor.PostgresqlExpression.enableBooleanQuoting</code>
     * to enable or disable quoting (default) of boolean items.
     */

    private boolean enableBooleanQuoting() {
        if(_enableBooleanQuoting == null) {
            _enableBooleanQuoting = Boolean.getBoolean(getClass().getName() + ".enableBooleanQuoting") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _enableBooleanQuoting.booleanValue();
    }
    
    /**
     * Checks the system property
     * <code>com.webobjects.jdbcadaptor.PostgresqlExpression.enableIdentifierQuoting</code>
     * to enable or disable quoting (default) of schema names, table names and
     * field names. Required if names which are case sensitive or reserved words
     * or have special characters.
     */
    private boolean enableIdentifierQuoting() {
        if(_enableIdentifierQuoting == null) {
            _enableIdentifierQuoting = Boolean.getBoolean(getClass().getName() + ".enableIdentifierQuoting") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _enableIdentifierQuoting.booleanValue();
    }

    /**
     * Overridden to fix an issue with NStimestamp classes and "T" value-typed attributes. 
     */
    @Override
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
     * Overridden to not call the super implementation.
     * 
     * @param leftName  the table name on the left side of the clause
     * @param rightName the table name on the right side of the clause
     * @param semantic  the join semantic
     */
    @Override
    public void addJoinClause(String leftName,
                              String rightName,
                              int semantic) {
        assembleJoinClause(leftName, rightName, semantic);
    }
    
    /**
     * Overridden to construct a valid SQL92 JOIN clause as opposed to
     * the Oracle-like SQL the superclass produces.
     *
     * @param leftName  the table name on the left side of the clause
     * @param rightName the table name on the right side of the clause
     * @param semantic  the join semantic
     * @return  the join clause
     */
    @Override
    public String assembleJoinClause(String leftName,
                                     String rightName,
                                     int semantic) {
        if (!useAliases()) {
            return super.assembleJoinClause(leftName, rightName, semantic);
        }
        
        String leftAlias = leftName.substring(0, leftName.indexOf("."));
        String rightAlias = rightName.substring(0, rightName.indexOf("."));
        
        NSArray<String> k;
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
        if (relationshipKey == null) {
        	throw new IllegalStateException("Could not determine relationship for join.");
        }
        int dotIndex = relationshipKey.indexOf( "." );
        relationshipKey = dotIndex == -1
            ? relationshipKey
            : relationshipKey.substring( relationshipKey.lastIndexOf( "." ) + 1 );
        r = rightEntity.anyRelationshipNamed( relationshipKey );
        // fix from Michael Müller for the case Foo.fooBars.bar has a Bar.foo relationship (instead of Bar.foos)
        if( r == null || r.destinationEntity() != leftEntity ) {
            r = leftEntity.anyRelationshipNamed( relationshipKey );
        }
        //timc 2006-02-26 IMPORTANT or quotes are ignored and mixed case field names won't work
        String rightTable;
        String leftTable; 
        if(enableIdentifierQuoting()) {
            rightTable = rightEntity.valueForSQLExpression(this);
            leftTable = leftEntity.valueForSQLExpression(this);
        } else {
            rightTable = rightEntity.externalName(); 
            leftTable = leftEntity.externalName(); 
        }
        JoinClause jc = new JoinClause();
        
        jc.setTable1(leftTable, leftAlias);
        
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
        NSArray<EOJoin> joins = r.joins();
        int joinsCount = joins.count();
        NSMutableArray<String> joinStrings = new NSMutableArray<String>(joinsCount);
        for( int i = 0; i < joinsCount; i++ ) {
            EOJoin currentJoin = joins.objectAtIndex(i);
            String left;
            String right;
            if(enableIdentifierQuoting()) {
                left = leftAlias +"."+ sqlStringForSchemaObjectName(currentJoin.sourceAttribute().columnName());
                right =  rightAlias +"."+ sqlStringForSchemaObjectName(currentJoin.destinationAttribute().columnName());
            } else {
                left = leftAlias +"."+currentJoin.sourceAttribute().columnName();
                right = rightAlias +"."+currentJoin.destinationAttribute().columnName();
            }
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
     * Overridden to handle correct placements of join conditions and 
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
    @Override
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
        StringBuilder sb = new StringBuilder();
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
            sb.append(' ');
            sb.append(lockClause);
        }
        // fetchRange overrides fetchLimit
        if (_fetchRange != null) {
            sb.append(" LIMIT ");
            sb.append(_fetchRange.length());
            sb.append(" OFFSET ");
            sb.append(_fetchRange.location());
        } else if (_fetchLimit != 0) {
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
    @Override
    public String columnTypeStringForAttribute( EOAttribute attribute ) {
      String externalType = attribute.externalType();
        if (externalType != null && externalType.endsWith( "[]" ) ) {
            return externalType;
        }
        //CHECKME: Why isn't this found in jdbcinfo?
        if("integer".equals(externalType)) {
        	return externalType;
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
        NSArray<String> keys = NSArray.componentsSeparatedByString(keyPath, ".");
        EOEntity ent = entity();
        
        for (int i = 0; i < keys.count(); i++) {
            String k = keys.objectAtIndex(i);
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
    @Override
    public String formatValueForAttribute(Object obj, EOAttribute eoattribute) {
        String value;
        if(obj instanceof NSData) {
            value = sqlStringForData((NSData)obj);
        } else if((obj instanceof NSTimestamp) && isTimestampAttribute(eoattribute)) {
            value = TIMESTAMP_FORMATTER.get().format(obj);
        } else if((obj instanceof NSTimestamp) && isDateAttribute(eoattribute)) {
            value = DATE_FORMATTER.get().format(obj);
        } else if(obj instanceof String) {
            value = formatStringValue((String)obj);
        } else if(obj instanceof Number) {
            if(obj instanceof BigDecimal) {
                value = fixBigDecimal((BigDecimal) obj, eoattribute);
            } else {
              Object convertedObj = eoattribute.adaptorValueByConvertingAttributeValue(obj);
              if (convertedObj instanceof Number) {
                String valueType = eoattribute.valueType();
                if (valueType == null || "i".equals(valueType)) {
                  value = String.valueOf(((Number)convertedObj).intValue());  
                }
                else if ("l".equals(valueType)) {
                  value = String.valueOf(((Number)convertedObj).longValue());  
                }
                else if ("f".equals(valueType)) {
                  value = String.valueOf(((Number)convertedObj).floatValue());  
                }
                else if ("d".equals(valueType)) {
                  value = String.valueOf(((Number)convertedObj).doubleValue());  
                }
                else if ("s".equals(valueType)) {
                  value = String.valueOf(((Number)convertedObj).shortValue());  
                }
                else if ("c".equals(valueType)) {
                  return String.valueOf(((Number)convertedObj).intValue());
                }
                else {
                  throw new IllegalArgumentException("Unknown number value type '" + valueType + "' for attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
                  //value = convertedObj.toString();
                }
              }
              else if (convertedObj instanceof String) {
                String str = (String)convertedObj;
                String valueType = eoattribute.valueType();
                if (valueType == null || "i".equals(valueType)) {
                  return String.valueOf(Integer.parseInt(str));
                }
                else if ("l".equals(valueType)) {
                  return String.valueOf(Long.parseLong(str));
                }
                else if ("f".equals(valueType)) {
                  return String.valueOf(Float.parseFloat(str));
                }
                else if ("d".equals(valueType)) {
                  return String.valueOf(Double.parseDouble(str));
                }
                else if ("s".equals(valueType)) {
                  return String.valueOf(Short.parseShort(str));
                }
                else if ("c".equals(valueType)) {
                  return String.valueOf(Integer.parseInt(str));
                }
                else {
                  throw new IllegalArgumentException("Unknown number value type '" + valueType + "' for attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
                }
              }
              else {
                throw new IllegalArgumentException("Unknown number value '" + obj + "' for attribute " + eoattribute.entity().name() + "." + eoattribute.name() + ".");
              }
            }
        } else if(obj instanceof Boolean) {
        	// GN: when booleans are stored as strings in the db, we need the values quoted
        	if (enableBooleanQuoting() || "S".equals(eoattribute.valueType())) {
        		value = "'" + ((Boolean)obj).toString() + "'";
        	}
        	else if (!"bool".equals(eoattribute.externalType().toLowerCase()) && "NSNumber".equals(eoattribute.valueClassName()) || "java.lang.Number".equals(eoattribute.valueClassName()) || "Number".equals(eoattribute.valueClassName())) {
        		value = ((Boolean)obj).booleanValue() ? "1" : "0";
        	}
        	else {
        		value = ((Boolean)obj).toString();
        	}
        } else if(obj instanceof Timestamp) {
        	value = "'" + ((Timestamp)obj).toString() + "'";
        } else if (obj == null || obj == NSKeyValueCoding.NullValue) {
        	value = "NULL";
        } else {
        	// AK: I don't really like this, but we might want to prevent infinite recursion
        	try {
        		Object adaptorValue = eoattribute.adaptorValueByConvertingAttributeValue(obj);
        		if(adaptorValue instanceof Date && !(adaptorValue instanceof NSTimestamp)) {
        			//Support joda classes
        			Date date = (Date)adaptorValue;
        			adaptorValue = new NSTimestamp(date);
        		}
        		if(adaptorValue instanceof NSData || adaptorValue instanceof NSTimestamp
        				|| adaptorValue instanceof String || adaptorValue instanceof Number 
        				|| adaptorValue instanceof Boolean) {
        			value = formatValueForAttribute(adaptorValue, eoattribute);
        		} else {
        			throw new IllegalArgumentException(getClass().getName() +  ": Can't convert: " + obj + ":" + obj.getClass() + " -> " + adaptorValue + ":" +adaptorValue.getClass());
        		}
        	} catch(Exception ex) {
        		throw new IllegalArgumentException(getClass().getName() +  ": Exception while converting " + obj.getClass().getName(), ex);
        	}
        }
        return value;
    }

    /**
     * Fixes an incompatibility with JDK 1.5 and using toString() instead of toPlainString() for BigDecimals.
     * From what I understand, you will only need this if you disable bind variables.
     * @param value
     * @param eoattribute
     * @author ak
     */
    private String fixBigDecimal(BigDecimal value, EOAttribute eoattribute) {
        String result;
        if(System.getProperty("java.version").compareTo("1.5") >= 0) {
            try {
                if(_bigDecimalToString == null) {
                    _bigDecimalToString = BigDecimal.class.getMethod("toPlainString", (Class[])null);
                }
                result = (String) _bigDecimalToString.invoke(value, (Object[])null);
            } catch (IllegalArgumentException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            } catch (IllegalAccessException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            } catch (InvocationTargetException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            } catch (SecurityException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            } catch (NoSuchMethodException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
        } else {
            result = value.toString();
        }
        return result;
    }

    /**
     * Helper to check for timestamp columns that have a "D" value type.
     * @param eoattribute attribute to check
     * @return <code>true</code> if of type Date
     */
    private boolean isDateAttribute(EOAttribute eoattribute) {
        return "D".equals(eoattribute.valueType());
    }

    /**
     * Helper to check for timestamp columns that have a "T" value type.
     * @param eoattribute attribute to check
     * @return <code>true</code> if of type Timestamp
     */
    private boolean isTimestampAttribute(EOAttribute eoattribute) {
        return "T".equals(eoattribute.valueType());
    }

    /**
     * Helper to check for data columns that are not keys.
     * @param eoattribute attribute to check
     * @return <code>true</code> if of type Data
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
    @Override
    public String joinClauseString() {
        NSMutableDictionary<String, Boolean> seenIt = new NSMutableDictionary<String, Boolean>();
        StringBuilder sb = new StringBuilder();
        JoinClause jc;
        EOSortOrdering.sortArrayUsingKeyOrderArray
            ( _alreadyJoined, new NSArray<EOSortOrdering>( EOSortOrdering.sortOrderingWithKey( "sortKey", EOSortOrdering.CompareCaseInsensitiveAscending ) ) );
        if (_alreadyJoined.count() > 0) {
            jc = _alreadyJoined.objectAtIndex(0);
            
            sb.append(jc);
            seenIt.setObjectForKey(Boolean.TRUE, jc.table1);
            seenIt.setObjectForKey(Boolean.TRUE, jc.table2);
        }
        
        for (int i = 1; i < _alreadyJoined.count(); i++) {
            jc = _alreadyJoined.objectAtIndex(i);
            
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
     * cug: Also handles identifier quoting now
     * 
     * @param relationship  the relationship
     * @param sourceColumns the source columns for the constraints
     * @param destinationColumns    the destination columns for the constraints
     */
    @SuppressWarnings("unchecked")
    @Override
	public void prepareConstraintStatementForRelationship(EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns) {
		
    	EOEntity entity = relationship.entity();
		String tableName = entity.externalName();
		
		int lastDot = tableName.lastIndexOf('.');
		
		if (lastDot >= 0) {
			tableName = tableName.substring(lastDot + 1);
		}
		
		String constraintName = _NSStringUtilities.concat(tableName, "_", relationship.name(), "_fk");
		
		// quotes the identifier in the array
		
		String sourceKeyList = quoteArrayContents(sourceColumns).componentsJoinedByString(", ");
		String destinationKeyList = quoteArrayContents(destinationColumns).componentsJoinedByString(", ");
		
		EOModel sourceModel = entity.model();
		EOModel destModel = relationship.destinationEntity().model();
		if (sourceModel != destModel && !sourceModel.connectionDictionary().equals(destModel.connectionDictionary())) {
			throw new IllegalArgumentException(new StringBuilder().append("prepareConstraintStatementForRelationship unable to create a constraint for ").append(relationship.name()).append(" because the source and destination entities reside in different databases").toString());
		} 
		setStatement(new StringBuilder()
				.append("ALTER TABLE ")
				.append(sqlStringForSchemaObjectName(entity.externalName()))
				.append(" ADD CONSTRAINT ")
				.append(quoteIdentifier(constraintName))
				.append(" FOREIGN KEY (")
				.append(sourceKeyList)
				.append(") REFERENCES ")
				.append(sqlStringForSchemaObjectName(relationship.destinationEntity().externalName()))
				.append(" (")
				.append(destinationKeyList)
				.append(") INITIALLY DEFERRED")
				.toString());
	}
    
    /**
     * Takes an array of strings and quotes every single string, if set to do so
     * 
     * @param a - array of strings
     * 
     * @return array of quoted or unquoted strings, depends on enableIdentifierQuoting
     */
	private NSArray<String> quoteArrayContents(NSArray<String> a) {
    	Enumeration enumeration = a.objectEnumerator();
    	NSMutableArray<String> result = new NSMutableArray<String>();
    	while (enumeration.hasMoreElements()) {
    		String identifier = (String) enumeration.nextElement();
    		String quotedString = quoteIdentifier(identifier);
    		result.addObject(quotedString);
    	}
    	return result;
    }
    
    /**
     * Quotes the string if necessary (checks the Property)
     * 
     * @param identifier - the string to quote
     * 
     * @return quoted or unquoted string (check with enableIdentifierQuoting)
     */
    private String quoteIdentifier(String identifier) {
   		return externalNameQuoteCharacter() + identifier + externalNameQuoteCharacter();
    }
    
    /**
     * Overridden so we can get the fetch limit from the fetchSpec.
     *
     * @param attributes the array of attributes
     * @param lock locking flag
     * @param fetchSpec the fetch specification
     */
    @Override
    public void prepareSelectExpressionWithAttributes(NSArray<EOAttribute> attributes, boolean lock, EOFetchSpecification fetchSpec) {
        try {
            _fetchRange = _fetchRangeSelector.invoke(fetchSpec);
            // We will get an error when not using our custom ERXFetchSpecification subclass
            // We could have added ERExtensions to the classpath and checked for instanceof, but I thought
            // this is a little cleaner since people may be using this PlugIn and not Wonder in some legacy apps.
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        }
        // Only check for fetchLimit if fetchRange is not provided.
        if (_fetchRange == null && !fetchSpec.promptsAfterFetchLimit()) {
            _fetchLimit = fetchSpec.fetchLimit();
        }
        if (_fetchRange != null) {
            // if we have a fetch range disable the limit
            fetchSpec.setFetchLimit(0);
        }
        super.prepareSelectExpressionWithAttributes(attributes, lock, fetchSpec);
    }
    
    /**
     * Overridden because Postgres uses "|" instead of "\" like any
     * other database system.
     */
    @Override
    public char sqlEscapeChar() {
        return SQL_ESCAPE_CHAR;
    }
    
    /**
     * Overridden because PostgreSQL does not use the default quote character in EOSQLExpression.externalNameQuoteCharacter() which is an empty string.
     * 
     */
    @Override
    public String externalNameQuoteCharacter() { 
        return (enableIdentifierQuoting() ? EXTERNAL_NAME_QUOTE_CHARACTER : ""); 
    }

    protected boolean shouldAllowNull(EOAttribute attribute) {
      boolean shouldAllowNull = attribute.allowsNull();
      // If you allow nulls, then there's never a problem ...
      if (!shouldAllowNull) {
        EOEntity entity = attribute.entity();
        EOEntity parentEntity = entity.parentEntity();
        String externalName = entity.externalName();
        if (externalName != null && parentEntity != null) {
          // If you have a parent entity and that parent entity shares your table name, then you're single table inheritance
          boolean singleTableInheritance = externalName.equals(parentEntity.externalName());
          if (singleTableInheritance) {
            EOAttribute parentAttribute = parentEntity.attributeNamed(attribute.name());
            if (parentAttribute == null) {
              // If this attribute is new in the subclass, you have to allow nulls
              shouldAllowNull = true;
            }
          }
        }
      }
      return shouldAllowNull;
    }

    @Override
    public void addCreateClauseForAttribute(EOAttribute attribute) {
      NSDictionary userInfo = attribute.userInfo();
      Object defaultValue = null;
      if (userInfo != null) {
          defaultValue = userInfo.valueForKey("er.extensions.eoattribute.default"); // deprecated key
          if (defaultValue == null) {
            defaultValue = userInfo.valueForKey("default");
          }
      }
      String allowsNullClauseForConstraint = allowsNullClauseForConstraint(shouldAllowNull(attribute));
      String sql;
      if (defaultValue == null) {
          sql = _NSStringUtilities.concat(quoteIdentifier(attribute.columnName()), " ", columnTypeStringForAttribute(attribute), " ", allowsNullClauseForConstraint);
      }
      else {
          sql = _NSStringUtilities.concat(quoteIdentifier(attribute.columnName()), " ", columnTypeStringForAttribute(attribute), " DEFAULT ", formatValueForAttribute(defaultValue, attribute), " ", allowsNullClauseForConstraint);
      }
      appendItemToListString(sql, _listString());
    }

    /**
     * cug: Quick hack for bug in WebObjects 5.4 where the "not null" statement is added without a space, 
     * and "addCreateClauseForAttribute" is not called anymore. Will probably change.
     */
    @Override
    public String allowsNullClauseForConstraint(boolean allowsNull) {
        if(allowsNull)
            return "";
        Object value = jdbcInfo().objectForKey("NON_NULLABLE_COLUMNS");
        if(value != null && value.equals("T"))
            return " NOT NULL";
        return "";
    }
   
    /**
     * Overridden because the original version does not correctly quote mixed case fields in all cases.
     * SELECT statements were OK (useAliases is true) INSERT, UPDATE, DELETE didn't quote mixed case field names.
     * 
     * @param attribute the attribute (column name) to be converted to a SQL string
     * @return SQL string for the attribute
     */
    @Override
    public String sqlStringForAttribute(EOAttribute attribute) {
        String sql = null;
        if ( attribute.isDerived() || useAliases() || attribute.columnName() == null || !enableIdentifierQuoting()) {
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
    @Override
    public String tableListWithRootEntity(EOEntity entity) {
        String sql = null;
        if ( useAliases()) {
            sql = super.tableListWithRootEntity(entity);
        } else {
            sql = entity.valueForSQLExpression(this); 
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
    @Override
    public String sqlStringForData(NSData data) {
        int length = data.length();
        byte bytes[] = data.bytes();
        char hex[] = new char[2 * length];
        int nibbles = 0;
        for(int i = 0; i < length; i++)  {
            byte b = bytes[i];
            hex[nibbles++] = HEX_VALUES[((b >> 4) + 16) % 16];
            hex[nibbles++] = HEX_VALUES[((b & 15) + 16) % 16];
        }
        return "decode('" + new String(hex) + "','hex')";
    }
        
    /**
     * Overridden so we can put a regex-match qualifier in the display groups
     * query bindings. You can bind '~*' or '~' to queryOperator.someKey and '.*foo' to
     * queryMatch.someKey and will get the correct results.
     *
     * @param selector  the selector that specifies the SQL operator
     * @param value the value to be associated with <code>selector</code>
     * @return  the SQL operator string
     */
    @Override
    public String sqlStringForSelector(NSSelector selector, Object value) {
        if(CASE_INSENSITIVE_REGEX_OPERATOR.name().equals( selector.name() ) || REGEX_OPERATOR.name().equals( selector.name() ) ) {
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
    @Override
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
        // AK: inet6 addresses get handed down as "xxx:xxx:...:xxx%y", not "xxx:xxx:...:xxx/y"
        // note that this might break if you hand over a host name that contains percent chars (not sure if possible)
    	if(attribute != null && "inet".equals(attribute.externalType()) && v != null && v.toString().indexOf('%') > 0) {
    		v = v.toString().replace('%', '/');
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
    public static class JoinClause {
        String table1;
        String op;
        String table2;
        String joinCondition;
    	String sortKey;
        
    	@Override
        public String toString() {
            return table1 + op + table2 + joinCondition;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof JoinClause)) {
                return false;
            }
            return toString().equals(obj.toString());
        }
        
    	public void setTable1(String leftTable, String leftAlias) {
    		table1 = leftTable + " " + leftAlias;
    		sortKey = leftAlias.substring(1);
    		if (sortKey.length() < 2) {
    			// add padding for cases with >9 joins
    			sortKey = " " + sortKey;
    		}
    	}

        /**
         * Property that makes this class "sortable". 
         * Needed to correctly assemble a join clause.
         */
        public String sortKey() {
        	return sortKey;
        }
    }
    
    /**
     * Checks the system property <code>com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting</code> to enable or
     * disable typecasting (appending ::somepostgrestype) for attributes.
     */
    private boolean disableTypeCasting() {
        if (_disableTypeCasting == null) {
        	_disableTypeCasting = Boolean.getBoolean("com.webobjects.jdbcadaptor.PostgresqlExpression.disableTypeCasting") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _disableTypeCasting.booleanValue();
    }
    
    /**
     * Checks the system property <code>com.webobjects.jdbcadaptor.PostgresqlExpression.disableBindVariables</code> to enable
     * or disable bind variables in general.
     */
    private boolean disableBindVariables() {
    	if (_disableBindVariables == null) {
    		_disableBindVariables = Boolean.getBoolean("com.webobjects.jdbcadaptor.PostgresqlExpression.disableBindVariables") ? Boolean.TRUE : Boolean.FALSE;
    	}
    	return _disableBindVariables.booleanValue();
    }
    
    /**
     * Overridden to return the negated value of {@link #disableBindVariables()}.
     */
    @Override
    public boolean useBindVariables() {
        return !disableBindVariables();
    }
    
    /**
     * Overridden to set the <code>disableBindVariables</code> value correctly.
     * @param value
     */
    @Override
    public void setUseBindVariables(boolean value) {
    	_disableBindVariables = Boolean.valueOf(!value);
    }

    /**
     * Overridden to return true only if bind variables are enabled or the is a data type.
     */
    @Override
    public boolean shouldUseBindVariableForAttribute(EOAttribute attribute) {
        return useBindVariables() || isDataAttribute(attribute);
    }

    /**
     * Overridden to return true only if bind variables are enabled or the is a data type.
     */
    @Override
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
        StringBuilder convertedString = new StringBuilder(length + 100);

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
    
	/**
     * Checks the system property
     * <code>com.webobjects.jdbcadaptor.PostgresqlExpression.useLowercaseForCaseInsensitiveLike</code>
     * to use the "lower" function for caseInsensitive compares
     */
    private boolean useLowercaseForCaseInsensitiveLike() {
		if (_useLowercaseForCaseInsensitiveLike == null) {
			_useLowercaseForCaseInsensitiveLike = Boolean.getBoolean(getClass().getName() + ".useLowercaseForCaseInsensitiveLike") ? Boolean.TRUE : Boolean.FALSE;
		}
		return _useLowercaseForCaseInsensitiveLike.booleanValue();
	}
	
	/**
	 * Checks the system property
	 * <code>com.webobjects.jdbcadaptor.PostgresqlExpression.customFunctionForStringComparison</code>
	 * to use a custom function for caseInsensitive compares and order by
	 * clauses.
	 * <p>
	 * This property overrides the useLowercaseForCaseInsensitiveLike definition.
	 * 
	 * @return The name of the custom function to be used for comparison or <code>null</code> to use
	 * the default function
	 */
	private String customFunctionForStringComparison() {
		return System.getProperty(getClass().getName() + ".customFunctionForStringComparison");
	}
}
