package er.luceneadaptor;

import java.lang.reflect.Field;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

/**
 * This stub exists only to make memory adaptor migrations function.
 * 
 * @author mschrag
 */
public class ERLuceneSynchronizationFactory extends EOSynchronizationFactory implements EOSchemaGeneration {
	public ERLuceneSynchronizationFactory(EOAdaptor adaptor) {
		super(adaptor);
		// MS: This is because of the ridiculous mess that is the
		// EOSynchronizationFactory API's in 5.4
		try {
			Field schemaSynchronizationFactory = getClass().getDeclaredField("_schemaSynchronizationFactory");
			schemaSynchronizationFactory.setAccessible(true);
			schemaSynchronizationFactory.set(this, this);
		} catch (NoSuchFieldException e) {
			// This means you're in WO 5.3
		} catch (IllegalArgumentException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} catch (IllegalAccessException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	protected NSArray noopExpressions() {
		EOSQLExpression expression = new ERLuceneExpression(null); // until
																	// adaptor().expressionFactory()
																	// returns a
																	// real
																	// expression
																	// factory,
																	// just
																	// hardcode
																	// it ...
		expression.setStatement("--");
		return new NSArray(expression);
	}

	@Override
	public void appendExpressionToScript(EOSQLExpression arg0, StringBuffer arg1) {
	}

	@Override
	public NSArray createDatabaseStatementsForConnectionDictionary(NSDictionary arg0, NSDictionary arg1) {
		return noopExpressions();
	}

	@Override
	public NSArray createTableStatementsForEntityGroup(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray createTableStatementsForEntityGroups(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray dropDatabaseStatementsForConnectionDictionary(NSDictionary arg0, NSDictionary arg1) {
		return noopExpressions();
	}

	@Override
	public NSArray dropPrimaryKeySupportStatementsForEntityGroup(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray dropTableStatementsForEntityGroup(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray dropTableStatementsForEntityGroups(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray foreignKeyConstraintStatementsForRelationship(EORelationship arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray primaryKeyConstraintStatementsForEntityGroup(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray primaryKeyConstraintStatementsForEntityGroups(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray primaryKeySupportStatementsForEntityGroup(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public NSArray primaryKeySupportStatementsForEntityGroups(NSArray arg0) {
		return noopExpressions();
	}

	@Override
	public String schemaCreationScriptForEntities(NSArray arg0, NSDictionary arg1) {
		return "--";
	}

	@Override
	public NSArray schemaCreationStatementsForEntities(NSArray arg0, NSDictionary arg1) {
		return noopExpressions();
	}

	@Override
	public NSArray statementsToInsertColumnForAttribute(EOAttribute attribute, NSDictionary options) {
		return noopExpressions();
	}
}
