package er.extensions.eof.qualifiers;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSSelector;

import er.extensions.jdbc.ERXSQLHelper;
import er.extensions.qualifiers.ERXKeyValueQualifier;

/**
 * Provides regular expression matching of attributes. You can also bind a WODisplayGroup queryOperator to 
 * <code>matches</code> and you should be able to have qualifier strings with "foo matches bar".
 * <p>
 * This class does not do any conversion of the regular expression, so you'd need to have your syntax in 
 * a way that is understood by both the java code and the DB.
 * 
 * @author ak
 */
public class ERXRegExQualifier extends ERXKeyValueQualifier {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String MatchesSelectorName = "matches";
	public static final NSSelector MatchesSelector = new NSSelector("matches", new Class[] {String.class});

	static {
		EOQualifierSQLGeneration.Support.setSupportForClass(new ERXRegExQualifier.Support(), ERXRegExQualifier.class);
	}

	public ERXRegExQualifier(String aKey, String aValue) {
		super(aKey, MatchesSelector, aValue);
	}

	@Override
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


		@Override
		public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
			String result = sqlStringForKeyValueQualifier((EOKeyValueQualifier)eoqualifier, e);
			return result;
		}

		@Override
		public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
			EOQualifier result = super.schemaBasedQualifierWithRootEntity(qualifier, entity);
			return result;
		}

		@Override
		public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier qualifier, EOEntity entity, String s) {
			EOQualifier result = super.qualifierMigratedFromEntityRelationshipPath(qualifier, entity, s);
			return result;
		}
	}

}
