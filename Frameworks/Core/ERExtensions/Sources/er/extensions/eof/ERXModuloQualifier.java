//
// ERXInQualifier.java
// Project EOInQualifier
//
// Created by max on Mon Jul 15 2002
//
package er.extensions.eof;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * The ERXModuloQualifier is useful for creating qualifiers that
 * will generate SQL using the 'mod' key word.<br>
 * <br>
 * For example constructing this qualifer:<br>
 * <code>ERXModuloQualifier q = new ERXModuloQualifier("userId", 3, 5);</code>
 * Then this qualifier would generate SQL of the form:
 * ... where mod(userId,5)=3;
 * Note that this syntax is Oracle specific
 */
// ENHANCEME: Should support restrictive qualifiers, don't need to subclass KeyValueQualifier
public class ERXModuloQualifier extends EOKeyValueQualifier implements Cloneable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new ModuloQualifierSQLGenerationSupport(), ERXModuloQualifier.class);
    }
    
    private int _modulo;
    private int _index;
    
    public int modulo() { return _modulo; }
    public int index() { return _index; }
    
    
    public ERXModuloQualifier(String key, int modulo, int index) {
        super(key, EOQualifier.QualifierOperatorEqual, null);
        _modulo=modulo;
        _index=index;
    }

    
    /**
    * String representation of the in
     * qualifier.
     * @return string description of the qualifier
     */
    @Override
    public String toString() {
        return " <" + getClass().getName() + " key: " + key() + " > == "+index()+" mod "+modulo();
    }


    /** Tests if the given object's key is in the supplied values */ 
    // FIXME: this doesn't work right with EOs when the key() is keypath across a relationship
    @Override
    public boolean evaluateWithObject(Object object) {
        Object value = null;
        if(object instanceof EOEnterpriseObject) {
            EOEnterpriseObject eo = (EOEnterpriseObject)object;
            EOEditingContext ec = eo.editingContext();
            if(eo.classDescription().attributeKeys().containsObject(key())) {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo, key());
            } else if(EOUtilities.entityNamed(ec, eo.entityName()).primaryKeyAttributeNames().containsObject(key())) {
                // when object is an EO and key() is a cross-relationship keypath, we drop through to this case
                // and we'll fail.
                value = EOUtilities.primaryKeyForObject(ec,eo).objectForKey(key());
            }
        } else {
            value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key());
        }
        return value != null && (((Number)value).intValue() & modulo()) == index();
    }
    
    /*
     * EOF seems to be wanting to clone qualifiers when
     * they are inside an and-or qualifier without this
     * method, ERXInQualifier is cloned into
     * an EOKeyValueQualifier and the generated SQL is incorrect..
     * @return cloned primary key list qualifier.
     */
    @Override
    public Object clone() {
        return new ERXModuloQualifier(key(), modulo(), index());
    }

    /**
     * Adds SQL generation support. Note that the database needs to support
     * the MOD operator.
     */
    public static class ModuloQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {

        /**
         * Public constructor
         */
        public ModuloQualifierSQLGenerationSupport() {
            super();
        }

        /**
         * Generates the SQL string for an ERXInQualifier.
         * @param eoqualifier an in qualifier
         * @param e current eo sql expression
         * @return SQL for the current qualifier.
         */
        @Override
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            ERXModuloQualifier modQualifier = (ERXModuloQualifier)eoqualifier;
            StringBuilder sb = new StringBuilder();
            sb.append("mod(");
            sb.append(e.sqlStringForAttributeNamed(modQualifier.key()));
            sb.append(", ");
            sb.append(modQualifier.modulo());
            sb.append(")=");
            sb.append(modQualifier.index());
            return sb.toString();
        }

        // ENHANCEME: This should support restrictive qualifiers on the root entity
        @Override
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            return (EOQualifier)eoqualifier.clone();
        }

        @Override
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            // the key migration is the same as for EOKeyValueQualifier
            ERXModuloQualifier modQualifier=(ERXModuloQualifier)eoqualifier;
            return new ERXModuloQualifier(_translateKeyAcrossRelationshipPath(modQualifier.key(), s, eoentity),
                    modQualifier.modulo(), modQualifier.index());
        }
    }

    
}