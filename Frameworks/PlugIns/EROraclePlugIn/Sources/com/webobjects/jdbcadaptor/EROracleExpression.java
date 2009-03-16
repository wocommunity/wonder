package com.webobjects.jdbcadaptor;

import java.sql.Timestamp;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
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

    public void addCreateClauseForAttribute(EOAttribute attribute) {
      NSDictionary userInfo = attribute.userInfo();
      Object defaultValue = null;
      if (userInfo != null) {
        defaultValue = userInfo.valueForKey("er.extensions.eoattribute.default");
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
    public boolean shouldUseBindVariableForAttribute(EOAttribute attribute) {
        return true;
    }

    /**
     * @return true to indicate that the Oracle jdbc driver should use
     * bind variables
     */
    public boolean mustUseBindVariableForAttribute(EOAttribute attribute) {
        return true;
    }
    
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
}
