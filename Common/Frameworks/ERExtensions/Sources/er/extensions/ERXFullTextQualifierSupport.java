package er.extensions;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOQualifier;

/**
 * Qualifier support for ERXFullTextQualifier.
 * 
 * @author mschrag
 */
public class ERXFullTextQualifierSupport extends EOQualifierSQLGeneration.Support {
	@Override
	public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier qualifier, EOEntity entity, String path) {
		return qualifier;
	}

	@Override
	public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
		return qualifier;
	}

	@Override
	public String sqlStringForSQLExpression(EOQualifier qualifier, EOSQLExpression expression) {
		ERXFullTextQualifier fullTextQualifier = (ERXFullTextQualifier) qualifier;
		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(expression);
		return sqlHelper.sqlForFullTextQuery(fullTextQualifier, expression);
	}
}
