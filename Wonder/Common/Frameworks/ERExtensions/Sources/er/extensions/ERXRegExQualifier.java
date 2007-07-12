package er.extensions;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSSelector;

/**
 * Provides regular expression matching of attributes. You can also bind a WODisplayGroup queryOperator to 
 * <code>matches</code> and yoiu should be able to have qualifier strings with "foo matches bar". <br />
 * This class does not do any conversion of the regular expression, so you'd need to have your syntax in 
 * a way that is understood by both the java code and the DB.
 * 
 * @author ak
 */
public class ERXRegExQualifier extends EOKeyValueQualifier {

	public static String MatchesSelectorName = "matches";
	public static NSSelector MatchesSelector = new NSSelector("matches", new Class[] {String.class});

	static {
		EOQualifierSQLGeneration.Support.setSupportForClass(new ERXRegExQualifier.Support(), ERXRegExQualifier.class);
	}

	public ERXRegExQualifier(String aKey, String aValue) {
		super(aKey, MatchesSelector, aValue);
	}

	public boolean evaluateWithObject(Object object) {
		Object objectValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key());
		if(objectValue instanceof String) {
			return ((String)objectValue).matches((String)value());
		}
		return false;
	}

	public static class Support extends EOQualifierSQLGeneration._KeyValueQualifierSupport {

		// AK: Should go somewhere else so we can create other enhanced qualifiers, too
		
		private String sqlStringForKeyValueQualifier(EOKeyValueQualifier qualifier, EOSQLExpression e) {
			String key = qualifier.key();
			String keyString = e.sqlStringForAttributeNamed(key);
			if (keyString == null) {
				throw new IllegalStateException("sqlStringForKeyValueQualifier: attempt to generate SQL for " + qualifier.getClass().getName() + " " + qualifier + " failed because attribute identified by key '" + key + "' was not reachable from from entity '" + e.entity().name() + "'");
			}
			Object qualifierValue = qualifier.value();
			if (qualifierValue instanceof EOQualifierVariable) {
				throw new IllegalStateException("sqlStringForKeyValueQualifier: attempt to generate SQL for " + qualifier.getClass().getName() + " " + qualifier + " failed because the qualifier variable '$" + ((EOQualifierVariable) qualifierValue).key() + "' is unbound.");
			}
			keyString = e.formatSQLString(keyString, e.entity()._attributeForPath(key).readFormat());
			Object value = qualifierValue;
			String valueString = e.sqlStringForValue(value, key);
			
			String result =  ERXSQLHelper.newSQLHelper(e).sqlForRegularExpressionQuery(keyString, valueString);
			return result;
		}


		public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
			String result = sqlStringForKeyValueQualifier((EOKeyValueQualifier)eoqualifier, e);
			return result;
		}

		public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
			EOQualifier result = super.schemaBasedQualifierWithRootEntity(qualifier, entity);
			return result;
		}

		public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier qualifier, EOEntity entity, String s) {
			EOQualifier result = super.qualifierMigratedFromEntityRelationshipPath(qualifier, entity, s);
			return result;
		}
	}

}
