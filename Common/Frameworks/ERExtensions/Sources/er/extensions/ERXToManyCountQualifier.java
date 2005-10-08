package er.extensions;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableSet;

/**
 * ERXToManyCountQualifier allows for queries against the number of entities in a to-many relationship.  For instance, if
 * you want all the Sales People that have no Sales, you can:
 * 
 * new ERXToManyCountQualifier("sales", ERXToManyQualifier.UNSPECIFIED, 0);
 * 
 * and fetch against the SalesPerson entity, which will produce SQL like:
 * 
 * SELECT t0.* FROM SalesPerson t0 WHERE (SELECT COUNT(*) FROM Sales WHERE t0.SalesPersonID = Sales.SalesPersonID) <= 0
 * 
 * Note that this qualifier requires that your database support subselects, though it supports in-memory evaluation as well.
 * 
 * @author Mike Schrag
 */
public class ERXToManyCountQualifier extends EOQualifier implements EOQualifierEvaluation, Cloneable {
  static {
    EOQualifierSQLGeneration.Support.setSupportForClass(new ERXToManyCountQualifier.CountSQLGenerationSupport(), ERXToManyCountQualifier.class);
  }

  /**
   * Constant to pass in for either minimum or maximum value if you don't want to specify one of them
   */
  public static final int UNSPECIFIED = -1;

  private String myToManyKeyPath;
  private EOQualifier myCountQualifier;
  private int myMinimumCount;
  private int myMaximumCount;

  /**
   * Constructs a new ERXToManyCountQualifier
   * 
   * @param _toManyKey the keypath of the relationship to count
   * @param _minimumCount the minimum number of related entities (or UNSPECIFIED)
   * @param _maximumCount the maximum number of related entities (or UNSPECIFIED)
   */
  public ERXToManyCountQualifier(String _toManyKeyPath, int _minimumCount, int _maximumCount, EOQualifier _countQualifier) {
    myToManyKeyPath = _toManyKeyPath;
    myMinimumCount = _minimumCount;
    myMaximumCount = _maximumCount;
    myCountQualifier = _countQualifier;
    if (myMaximumCount == ERXToManyCountQualifier.UNSPECIFIED && myMinimumCount == ERXToManyCountQualifier.UNSPECIFIED) {
      throw new IllegalArgumentException("Minimum count and maximum count cannot both be UNSPECIFIED.");
    }
  }

  public EOQualifier countQualifier() {
    return myCountQualifier;
  }

  public String toManyKeyPath() {
    return myToManyKeyPath;
  }

  public int minimumCount() {
    return myMinimumCount;
  }

  public boolean hasMinimumCount() {
    return myMinimumCount != ERXToManyCountQualifier.UNSPECIFIED;
  }

  public int maximumCount() {
    return myMaximumCount;
  }

  public boolean hasMaximumCount() {
    return myMaximumCount != ERXToManyCountQualifier.UNSPECIFIED;
  }

  public Object clone() {
    return new ERXToManyCountQualifier(myToManyKeyPath, myMinimumCount, myMaximumCount, myCountQualifier);
  }

  public EOQualifier qualifierWithBindings(NSDictionary _bindings, boolean _requiresAll) {
    throw new IllegalStateException(getClass().getName() + " doesn't support bindings");
  }

  public void validateKeysWithRootClassDescription(EOClassDescription _classDescription) {
  }

  public void addQualifierKeysToSet(NSMutableSet _qualifierKeys) {
    throw new IllegalStateException(getClass().getName() + " doesn't support adding keys");
  }

  public boolean evaluateWithObject(Object _obj) {
    NSArray values = (NSArray) NSKeyValueCoding.Utility.valueForKey(_obj, myToManyKeyPath);
    int count;
    if (values == null) {
      count = 0;
    }
    else if (myCountQualifier == null) {
      count = values.count();
    }
    else {
      count = EOQualifier.filteredArrayWithQualifier(values, myCountQualifier).count();
    }

    boolean evaluation;
    if (hasMinimumCount() && hasMaximumCount()) {
      evaluation = count >= myMinimumCount && count <= myMaximumCount;
    }
    else if (hasMinimumCount()) {
      evaluation = count >= myMinimumCount;
    }
    else if (hasMaximumCount()) {
      evaluation = count <= myMaximumCount;
    }
    else {
      throw new IllegalArgumentException("Qualifier has no minimum OR maximum.");
    }

    return evaluation;
  }

  public static class CountSQLGenerationSupport extends EOQualifierSQLGeneration.Support {
    public String sqlStringForSQLExpression(EOQualifier _qualifier, EOSQLExpression _sqlExpression) {
      ERXToManyCountQualifier qualifier = (ERXToManyCountQualifier) _qualifier;

      EOEntity entity = _sqlExpression.entity();
      EOAdaptor adaptor = EOAdaptor.adaptorWithModel(entity.model());
      EOSQLExpression expression = adaptor.expressionFactory().createExpression(entity);
      expression.setUseAliases(_sqlExpression.useAliases());
      expression.setUseBindVariables(_sqlExpression.useBindVariables());

      StringBuffer result = new StringBuffer();
      result.append("(SELECT COUNT(*) FROM ");
      expression._aliasForRelationshipPath(qualifier.toManyKeyPath());
      expression.joinExpression();
      
      // Nasty hack where we need to join the outer query, so we have to
      // pop off the first table alias in the subexpression so T0 is defined in
      // the outer query instead of the inner query ... I'm not
      // sure what the proper way to do this is though :(
      String tableList = expression.tableListWithRootEntity(entity);
      tableList = tableList.substring(tableList.indexOf(',') + 1);
      
      result.append(tableList);
      result.append(" WHERE ");
      result.append(expression.joinClauseString());

      EOQualifier countQualifier = qualifier.countQualifier();
      if (countQualifier != null) {
        EOSQLExpression countExpression = adaptor.expressionFactory().createExpression(entity);
        EOQualifierSQLGeneration.Support countQualifierSupport = EOQualifierSQLGeneration.Support.supportForClass(countQualifier.getClass());
        EOQualifier countSchemaQualifier = countQualifierSupport.schemaBasedQualifierWithRootEntity(countQualifier, entity);
        String qualifierStr = countQualifierSupport.sqlStringForSQLExpression(countSchemaQualifier, expression);
        result.append(" AND ");
        result.append(qualifierStr);
      }      

      result.append(")");

      if (qualifier.hasMinimumCount() && qualifier.hasMaximumCount()) {
        int minimumCount = qualifier.minimumCount();
        int maximumCount = qualifier.maximumCount();
        if (maximumCount == minimumCount) {
          result.append(" = ");
          result.append(minimumCount);
        }
        else {
          result.append(" BETWEEN ");
          result.append(minimumCount);
          result.append(" AND ");
          result.append(maximumCount);
        }
      }
      else if (qualifier.hasMinimumCount()) {
        result.append(" >= ");
        result.append(qualifier.minimumCount());
      }
      else if (qualifier.hasMaximumCount()) {
        result.append(" <= ");
        result.append(qualifier.maximumCount());
      }
      else {
        throw new IllegalArgumentException("Qualifier has no minimum OR maximum.");
      }

      return result.toString();
    }

    public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier _qualifier, EOEntity _entity) {
      return _qualifier;
    }

    public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier _qualifier, EOEntity _entity, String _relationshipPath) {
      return _qualifier;
    }
  }
}
