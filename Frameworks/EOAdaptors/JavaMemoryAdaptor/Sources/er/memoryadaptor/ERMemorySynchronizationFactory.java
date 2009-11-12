package er.memoryadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * This stub exists only to make memory adaptor migrations function.
 * 
 * @author mschrag
 */
public class ERMemorySynchronizationFactory extends EOSynchronizationFactory implements EOSchemaGeneration {
  public ERMemorySynchronizationFactory(EOAdaptor adaptor) {
    super(adaptor);
  }

  protected NSArray noopExpressions() {
    EOSQLExpression expression = adaptor().expressionFactory().createExpression(null);
    expression.setStatement("--");
    return new NSArray(expression);
  }

  public void appendExpressionToScript(EOSQLExpression arg0, StringBuffer arg1) {
  }

  public NSArray createDatabaseStatementsForConnectionDictionary(NSDictionary arg0, NSDictionary arg1) {
    return noopExpressions();
  }

  public NSArray createTableStatementsForEntityGroup(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray createTableStatementsForEntityGroups(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray dropDatabaseStatementsForConnectionDictionary(NSDictionary arg0, NSDictionary arg1) {
    return noopExpressions();
  }

  public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray dropTableStatementsForEntityGroup(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray dropTableStatementsForEntityGroups(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship arg0) {
    return noopExpressions();
  }

  public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray primaryKeyConstraintStatementsForEntityGroups(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray primaryKeySupportStatementsForEntityGroup(NSArray arg0) {
    return noopExpressions();
  }

  public NSArray primaryKeySupportStatementsForEntityGroups(NSArray arg0) {
    return noopExpressions();
  }

  public String schemaCreationScriptForEntities(NSArray arg0, NSDictionary arg1) {
    return "--";
  }

  public NSArray schemaCreationStatementsForEntities(NSArray arg0, NSDictionary arg1) {
    return noopExpressions();
  }
}
