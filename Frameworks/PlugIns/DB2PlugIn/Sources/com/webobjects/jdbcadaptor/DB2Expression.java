package com.webobjects.jdbcadaptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSStringUtilities;

/**
 * DB2 use SQL92 join statements.  Has a FETCH FIRST n ROWS statement for limit
 * 
 * @author simpson:  Took the postgress plugin and made it into a db2 plugin
 * @author ak: Regex, NSData
 * @author Giorgio Valoti: refactoring, typecasting, schema building
 * @author Arturo Perez: JOIN clauses
 * @author David Teran: Timestamps handling
 * @author Tim Cummings: case sensitive table and column names
 * @author cug: hacks for identifier quoting while creating tables
 */
public class DB2Expression extends JDBCExpression {

    /**
     * formatter to use when handling date columns
     */
	protected static final NSTimestampFormatter DATE_FORMATTER = new NSTimestampFormatter("%Y-%m-%d");

    /**
     * formatter to use when handling timestamps
     */
    protected static final NSTimestampFormatter TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

    /**
     * Method to get the string value from a BigDecimals from.
     */
    protected static Method _bigDecimalToString = null;

    /**
     * If true, queries will be created by using 
     */
    private Boolean _disableBindVariables = null;
    
    /**
     * Holds array of join clauses.
     */
    protected NSMutableArray<JoinClause> _alreadyJoined = new NSMutableArray<JoinClause>();
    
    /**
     * Fetch spec limit ivar
     */
    protected int _fetchLimit;
     
    private Boolean _enableIdentifierQuoting;
    
    private Boolean _enableBooleanQuoting;

	private Boolean _useLowercaseForCaseInsensitiveLike;

	
    
    /**
     * Overridden to remove the rtrim usage. The original implementation
     * will remove every trailing space from character based column which 
     * should not be OK for DB2.
     * @param entity entity for this expression
     */
    public DB2Expression(EOEntity entity) {
        super(entity);

    	if (useLowercaseForCaseInsensitiveLike()) {
    		_upperFunctionName = "LOWER";
    	}
}
    
	/**
     * Checks the system property
     * <code>com.webobjects.jdbcadaptor.DB2Expression.enableBooleanQuoting</code>
     * to enable or disable quoting (default) of boolean items.
     * 
     */
    private boolean enableBooleanQuoting() {
        if(_enableBooleanQuoting == null) {
            _enableBooleanQuoting = Boolean.getBoolean(getClass().getName() + ".enableBooleanQuoting") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _enableBooleanQuoting.booleanValue();
    }
    
    /**
     * Checks the system property
     * <code>com.webobjects.jdbcadaptor.DB2Expression.enableIdentifierQuoting</code>
     * to enable or disable quoting (default) of schema names, table names and
     * field names. Required if names which are case sensitive or reserved words
     * or have special characters.
     * 
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
    public NSMutableDictionary<String, Object> bindVariableDictionaryForAttribute(EOAttribute eoattribute, Object obj) {
        NSMutableDictionary<String, Object> result =  super.bindVariableDictionaryForAttribute(eoattribute, obj);
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
     * Change from Postgres to use Fetch First rows only rather then limit
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
        // JVS: Just a note that any column in the order by clause with distinct needs to be in the column list 
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
            sb.append(" FETCH FIRST ");
            sb.append(_fetchLimit);
            sb.append(" ROWS ONLY ");
        }        
        return sb.toString();
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
    @Override
    public String formatValueForAttribute(Object obj, EOAttribute eoattribute) {
        String value;
        if(obj instanceof NSData) {
            value = sqlStringForData((NSData)obj);
        } else if((obj instanceof NSTimestamp) && isTimestampAttribute(eoattribute)) {
            value = "'" + TIMESTAMP_FORMATTER.format(obj) + "'";
        } else if((obj instanceof NSTimestamp) && isDateAttribute(eoattribute)) {
            value = "'" + DATE_FORMATTER.format(obj) + "'";
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
     * @param eoattribute
     */
    private boolean isDateAttribute(EOAttribute eoattribute) {
        return "D".equals(eoattribute.valueType());
    }

    /**
     * Helper to check for timestamp columns that have a "T" value type.
     * @param eoattribute
     */
    private boolean isTimestampAttribute(EOAttribute eoattribute) {
        return "T".equals(eoattribute.valueType());
    }

    /**
     * Helper to check for data columns that are not keys.
     * @param eoattribute
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
     * 
     * 
     * cug: Also handles identifier quoting now
     * 
     * @param relationship  the relationship
     * @param sourceColumns the source columns for the constraints
     * @param destinationColumns    the destination columns for the constraints
     */
    @Override
	public void prepareConstraintStatementForRelationship(EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns) {
		
    	EOEntity entity = relationship.entity();
		String tableName = entity.externalName();
		
		int lastDot = tableName.lastIndexOf('.');
		
		if (lastDot >= 0) {
			tableName = tableName.substring(lastDot + 1);
		}
		
		String constraintName = _NSStringUtilities.concat(tableName, "_", relationship.name(), "_FK");
		
		// quotes the identifier in the array
		
		String sourceKeyList = quoteArrayContents(sourceColumns).componentsJoinedByString(", ");
		String destinationKeyList = quoteArrayContents(destinationColumns).componentsJoinedByString(", ");
		
		EOModel sourceModel = entity.model();
		EOModel destModel = relationship.destinationEntity().model();
		if (sourceModel != destModel && !sourceModel.connectionDictionary().equals(destModel.connectionDictionary())) {
			throw new IllegalArgumentException((new StringBuilder()).append("prepareConstraintStatementForRelationship unable to create a constraint for ").append(relationship.name()).append(" because the source and destination entities reside in different databases").toString());
		} 
		setStatement((new StringBuilder())
				.append("ALTER TABLE ")
				.append(sqlStringForSchemaObjectName(tableName))
				.append(" ADD CONSTRAINT ")
				.append(quoteIdentifier(constraintName))
				.append(" FOREIGN KEY (")
				.append(sourceKeyList)
				.append(") REFERENCES ")
				.append(sqlStringForSchemaObjectName(relationship.destinationEntity().externalName()))
				.append(" (")
				.append(destinationKeyList)
				.append(")")
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
     * @param attributes   the array of attributes
     * @param lock  locking flag
     * @param eofetchspecification  the fetch specification
     */
    @Override
    public void prepareSelectExpressionWithAttributes(NSArray<EOAttribute> attributes, boolean lock, EOFetchSpecification eofetchspecification) {
        if(!eofetchspecification.promptsAfterFetchLimit()) {
            _fetchLimit = eofetchspecification.fetchLimit();
        }
        super.prepareSelectExpressionWithAttributes(attributes, lock, eofetchspecification);
    }
    

    protected boolean shouldAllowNull(EOAttribute attribute) {
      boolean shouldAllowNull = attribute.allowsNull();
      // If you allow nulls, then there's never a problem ...
      if (!shouldAllowNull) {
        EOEntity entity = attribute.entity();
        EOEntity parentEntity = entity.parentEntity();
        String externalName = entity.externalName();
        if (externalName != null) {
          // If you have a parent entity and that parent entity shares your table name, then you're single table inheritance
          boolean singleTableInheritance = (parentEntity != null && externalName.equals(parentEntity.externalName()));
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
        defaultValue = userInfo.valueForKey("er.extensions.eoattribute.default");
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
     * Helper class that stores a join definition and
     * helps <code>DB2Expression</code> to assemble
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
            if( obj == null || !(obj instanceof JoinClause) ) {
                return false;
            }
            return toString().equals( obj.toString() );
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
     * Checks the system property <code>com.webobjects.jdbcadaptor.DB2Expression.disableBindVariables</code> to enable
     * or disable bind variables in general.
     */
    private boolean disableBindVariables() {
    	if (_disableBindVariables == null) {
    		_disableBindVariables = Boolean.getBoolean("com.webobjects.jdbcadaptor.DB2Expression.disableBindVariables") ? Boolean.TRUE : Boolean.FALSE;
    	}
    	return _disableBindVariables.booleanValue();
    }
    
    /**
     * Overridden to return the negated value of <code>disableBindVariables</code>.
     */
    @Override
    public boolean useBindVariables() {
        return !disableBindVariables();
    }
    
    /**
     * Overridden to set the <code>disableBindVariables</code> value correctly.
     * @param value new value
     */
    @Override
    public void setUseBindVariables(boolean value) {
    	_disableBindVariables = (value ? Boolean.FALSE : Boolean.TRUE);
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
     * <code>com.webobjects.jdbcadaptor.DB2Expression.useLowercaseForCaseInsensitiveLike</code>
     * to use the "lower" function for caseInsensitive compares
     */
    private boolean useLowercaseForCaseInsensitiveLike() {
		if (_useLowercaseForCaseInsensitiveLike == null) {
			_useLowercaseForCaseInsensitiveLike = Boolean.getBoolean(getClass().getName() + ".useLowercaseForCaseInsensitiveLike") ? Boolean.TRUE : Boolean.FALSE;
		}
		return _useLowercaseForCaseInsensitiveLike.booleanValue();
	}
}
