package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.jdbcadaptor.EROracleExpression.Delegate;

public class EROracleExpressionDelegate implements Delegate {
  protected String constraintName(String tableName, String relationshipName) {
    String constraintName = _NSStringUtilities.concat(tableName, "_", relationshipName, "_FK");
    return constraintName;
  }

  public String constraintStatementForRelationship(EORelationship relationship, NSArray sourceColumns, NSArray destinationColumns) {
    EOEntity entity = relationship.entity();
    String tableName = entity.externalName();
    int lastDot = tableName.lastIndexOf('.');
    if (lastDot >= 0) {
      tableName = tableName.substring(lastDot + 1);
    }
    return constraintName(tableName, relationship.name());
  }

}
