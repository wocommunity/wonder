package er.extensions;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
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

        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            String key = ((EOKeyValueQualifier)eoqualifier).key();
            String value = (String) ((EOKeyValueQualifier)eoqualifier).value();
            EOAttribute attribute = e.entity().attributeNamed(key);
            String columnName = attribute.valueForSQLExpression(e);
            value = e.formatStringValue(value);
            String result = null;
            //AK: yucky way to provide support for different DBs...
            String clazzName = e.getClass().getName().toLowerCase();
            if(clazzName.indexOf("mysql") > 0 ) {
                result = columnName + " REGEXP " + value + "";
            } else if(clazzName.indexOf("postgres") > 0 ) {
                result = columnName + " ~* " + value + "";
            } else if(clazzName.indexOf("oracle") > 0 ) {
                result = "REGEXP_LIKE(" + columnName + ", " + value + ")";
            } else  {
                throw new UnsupportedOperationException("Regex not known for class: " + e.getClass().getName());
            }
            return result;
        }

        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOQualifier result = super.schemaBasedQualifierWithRootEntity(eoqualifier, eoentity);
            return result;
        }

        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            EOQualifier result = super.qualifierMigratedFromEntityRelationshipPath(eoqualifier, eoentity, s);
            return result;
        }
    }

}
