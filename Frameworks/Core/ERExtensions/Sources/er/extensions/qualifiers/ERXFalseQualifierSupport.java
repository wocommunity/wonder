package er.extensions.qualifiers;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOQualifier;

/**
 * SQL generation support for {@link ERXFalseQualifier}.
 */
public class ERXFalseQualifierSupport extends EOQualifierSQLGeneration.Support {
	@Override
	public String sqlStringForSQLExpression(EOQualifier qualifier, EOSQLExpression expression) {
		return "0=1";
	}

	@Override
	public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
		return qualifier;
	}

	@Override
	public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier qualifier, EOEntity entity, String relationshipPath) {
		return qualifier;
	}
}
