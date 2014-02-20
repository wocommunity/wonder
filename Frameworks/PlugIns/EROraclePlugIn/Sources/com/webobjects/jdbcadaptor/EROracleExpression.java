package com.webobjects.jdbcadaptor;

import java.sql.Timestamp;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.jdbcadaptor.OraclePlugIn.OracleExpression;

/** overrides OracleExpression in order to add
 * TIMESTAMP values including milliseconds. The
 * normal EOF Oracle PlugIn does not add milliseconds
 * to the TIMESTAMP value
 *  
 * @author David Teran
 *
 */
public class EROracleExpression extends OracleExpression {

    /**
     * Holds array of join clauses.
     */
    private final NSMutableArray _alreadyJoined = new NSMutableArray();

    /**
     * Fetch spec limit ivar
     */
    private int _fetchLimit;

  public static interface Delegate {
    /**
     * Returns the constraint name for the given relationship.
     * 
     * @param relationship the relationship
     * @param sourceColumns the source columns
     * @param destinationColumns the destination columns
     * @return the constraint name (or null for default)
     */
    public String constraintStatementForRelationship(EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns);
  }
  
    private static final NSTimestampFormatter _TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");
    private static EROracleExpression.Delegate _delegate;

    /**
     * Sets the delegate for this expression.
     * 
     * @param delegate the delegate for this expression
     */
    public static void setDelegate(EROracleExpression.Delegate delegate) {
      EROracleExpression._delegate = delegate;
    }
    
    public EROracleExpression(EOEntity eoentity) {
        super(eoentity);
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
        defaultValue = userInfo.valueForKey("er.extensions.eoattribute.default"); // deprecated key
        if (defaultValue == null) {
          defaultValue = userInfo.valueForKey("default");
        }
      }
      String sql;
      String allowsNullClauseForConstraint = allowsNullClauseForConstraint(shouldAllowNull(attribute));
      if (defaultValue == null) {
          sql = _NSStringUtilities.concat(attribute.columnName(), " ", columnTypeStringForAttribute(attribute), " ", allowsNullClauseForConstraint);
      }
      else {
          sql = _NSStringUtilities.concat(attribute.columnName(), " ", columnTypeStringForAttribute(attribute), " DEFAULT ", formatValueForAttribute(defaultValue, attribute), " ", allowsNullClauseForConstraint);
      }
      appendItemToListString(sql, _listString());
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

    /** Overridden in order to add milliseconds to the value. This
     * applies only if obj is an instance of NSTimestamp and if 
     * valueType from the eoattribute is T
     * 
     * @param obj
     * @param eoattribute
     * 
     * @return the modified string
     */
    @Override
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
    @Override
    public boolean useBindVariables() {
        return true;
    }

    /**
     * @return true to indicate that the Oracle jdbc driver should use
     * bind variables
     */
    @Override
    public boolean shouldUseBindVariableForAttribute(EOAttribute attribute) {
        return true;
    }

    /**
     * @return true to indicate that the Oracle jdbc driver should use
     * bind variables
     */
    @Override
    public boolean mustUseBindVariableForAttribute(EOAttribute attribute) {
        return true;
    }
    
    @Override
    public void prepareConstraintStatementForRelationship(EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns) {
      EOEntity entity = relationship.entity();
      String tableName = entity.externalName();
      int lastDot = tableName.lastIndexOf('.');
      if (lastDot >= 0) {
        tableName = tableName.substring(lastDot + 1);
      }
      String constraintName = null;
      if (EROracleExpression._delegate != null) {
        constraintName = EROracleExpression._delegate.constraintStatementForRelationship(relationship, sourceColumns, destinationColumns);
      }
      if (constraintName == null && entity != null) {
        constraintName = System.getProperty("er.extensions.ERXModelGroup." + entity.name() + "." + relationship.name() + ".foreignKey");
      }
      if (constraintName == null) {
        constraintName = _NSStringUtilities.concat(tableName, "_", relationship.name(), "_FK");
      }
      String sourceKeyList = sourceColumns.componentsJoinedByString(", ");
      String destinationKeyList = destinationColumns.componentsJoinedByString(", ");

      EOModel sourceModel = entity.model();
      EOModel destModel = relationship.destinationEntity().model();
      if (sourceModel != destModel && !sourceModel.connectionDictionary().equals(destModel.connectionDictionary())) {
        throw new IllegalArgumentException("prepareConstraintStatementForRelationship unable to create a constraint for " + relationship.name() + " because the source and destination entities reside in different databases");
      }
      else {
        setStatement("ALTER TABLE " + entity.externalName() + " ADD CONSTRAINT " + constraintName + " FOREIGN KEY (" + sourceKeyList + ") REFERENCES " + relationship.destinationEntity().externalName() + " (" + destinationKeyList + ") DEFERRABLE INITIALLY DEFERRED");
      }
    }

    /**
     * Overriden to handle correct placements of join conditions and to handle
     * DISTINCT fetches with compareCaseInsensitiveA(De)scending sort orders.
     * Lifted directly from the PostgressExpression.java class.
     * 
     * @param attributes
     *            the attributes to select
     * @param lock
     *            flag for locking rows in the database
     * @param qualifier
     *            the qualifier to restrict the selection
     * @param fetchOrder
     *            specifies the fetch order
     * @param columnList
     *            the SQL columns to be fetched
     * @param tableList
     *            the the SQL tables to be fetched
     * @param whereClause
     *            the SQL where clause
     * @param joinClause
     *            the SQL join clause
     * @param orderByClause
     *            the SQL sort order clause
     * @param lockClause
     *            the SQL lock clause
     * @return the select statement
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
        // AK: using DISTINCT with ORDER BY UPPER(foo) is an error if it is not
        // also present in the columns list...
        // This implementation sucks, but should be good enough for the normal
        // case
        if (selectString.indexOf(" DISTINCT") != -1) {
            String[] columns = orderByClause.split(",");
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i].replaceFirst("\\s+(ASC|DESC)\\s*",
                                                        "");
                column = column.replaceFirst("(NULLS\\sFIRST|NULLS\\sLAST)","");
                if (columnList.indexOf(column) == -1) {
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
        if ((whereClause != null && whereClause.length() > 0)
                || (joinClause != null && joinClause.length() > 0)) {
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
        if (_fetchLimit != 0) {
            sb.append(" LIMIT ");
            sb.append(_fetchLimit);
        }
        return sb.toString();
    }
    
    /** 
     * Overridden to allow the Null Sorting behavior of Oracle to be modified by 
     * setting an application property. There are three options: 
     * 
     * 1) Nulls always first, irrespective of sorting: 
     * EROraclePlugIn.nullSortBehavior=NullsFirst 
     * 
     * 2) Nulls always last, irrespective of sorting (this is Oracle's default): 
     * EROraclePlugIn.nullSortBehavior=NullsLast 
     * 
     * 3) Nulls as the least or smallest value, the same as EOF: 
     * EROraclePlugIn.nullSortBehavior=EOFStyle.
     * 
     * If you want to use either NullsFirst or NullsLast, you will need to 
     * create a new EOSortOrdering.ComparisonSupport class and set it to be used 
     * at application startup otherwise EOF will still go and resort using nulls 
     * as the smallest value. 
     * 
     * @see com.webobjects.eoaccess.EOSQLExpression#addOrderByAttributeOrdering(com.webobjects.eocontrol.EOSortOrdering) 
     */ 
    @Override 
    public void addOrderByAttributeOrdering(EOSortOrdering sortOrdering) {
        super.addOrderByAttributeOrdering(sortOrdering); 
        String nullSortBehavior = NSProperties.getProperty("EROraclePlugin.nullSortBehavior"); 
        if (nullSortBehavior != null) { 
            if ("EOFStyle".equals(nullSortBehavior)) { 
                if (sortOrdering.selector() == EOSortOrdering.CompareCaseInsensitiveDescending || sortOrdering.selector() == EOSortOrdering.CompareDescending) { 
                    _orderByString().append(" NULLS LAST"); 
                }
                else { 
                    _orderByString().append(" NULLS FIRST"); 
                }
            }
            else if ("NullsFirst".equals(nullSortBehavior)) { 
                _orderByString().append(" NULLS FIRST"); 
            }
            else if ("NullsLast".equals(nullSortBehavior)) { 
                _orderByString().append(" NULLS LAST"); // Oracle's normal default 
            } 
        }
    }
}
