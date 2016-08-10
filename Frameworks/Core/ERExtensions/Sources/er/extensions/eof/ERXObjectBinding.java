package er.extensions.eof;

import java.math.BigDecimal;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

/**
 * This class models an SQL binding based on a Java object.
 * <p>
 * When using this class to model a binding, the EOF SQL generation layer will not
 * be able to inspect the model and obtain the correct value types and conversions.
 * A best guess will be made based on the class of the wrapped object. This class
 * supports the five basic types EOF supports (String, BigDecimal, Number, NSData and
 * NSTimestamp). Also, boolean values true and false are converted to "true" and "false"
 * strings. If boolean representation is different on the database you are using (like
 * integers) you have to handle it yourself by wrapping an object of the appropriate
 * class.
 * </p>
 * <p>
 * A note about SQL generation: due to the internal architecture of database plugins,
 * this class will not be able to ask the plugin for an appropriate placeholder string
 * for the binding values. This won't matter for databases where the placeholder string
 * is "?". However, some databases, like PostgreSQL 7.4 and older, require the placeholder
 * string to contain the value type, like "?::varchar(1000)". Using this class may cause
 * problems and unexpected results on such databases. If that's the case, consider using
 * {@link ERXKeyValueBinding}.
 * </p>
 */
public class ERXObjectBinding implements ERXSQLBinding {

    /**
     * Wrapped object.
     */
    private Object _value;
    
    /**
     * Helper EOAttributed used for SQL generation.
     */
    private EOAttribute _attribute;
    
    /**
     * Creates a new object binding wrapper for the given object.
     * <p>
     * value must be an instance of the following classes (or any subsclass of these): String,
     * BigDecimal, Number, NSData, NSTimestamp.
     * </p>
     * @param value
     *          Object to be wrapped
     */
    public ERXObjectBinding( Object value ) {
        super();
        initForValue( value );
    }
    
    /**
     * Initializes instance based on the wrapped object class.
     * 
     * @param originalValue
     *          Object to be wrapped
     */
    private void initForValue(Object originalValue) {
        Object value = originalValue;
        if( originalValue instanceof Boolean ) {
            value = originalValue.toString();
        }
        EOAttribute attribute = new EOAttribute();
        attribute.setName(""); // Must have a name, or EOSQLExpression will scream
        if( value instanceof String ) {
            attribute.setClassName( String.class.getName() );
        } else if( value instanceof BigDecimal ) {
            attribute.setClassName( BigDecimal.class.getName() );
        } else if( value instanceof Number ) {
            attribute.setClassName( Number.class.getName() );
        } else if( value instanceof NSData ) {
            attribute.setClassName( NSData.class.getName() );
        } else if( value instanceof NSTimestamp ) {
            attribute.setClassName( NSTimestamp.class.getName() );
        } else {
            throw new RuntimeException("Unsupported object class: " + value.getClass().getName() );
        }
        _value = value;
        _attribute = attribute;
    }

    public String sqlStringForBindingOnExpression(EOSQLExpression expression) {
        NSDictionary binding = expression.bindVariableDictionaryForAttribute( _attribute, _value );
        expression.addBindVariableDictionary(binding);
        return (String) binding.objectForKey(EOSQLExpression.BindVariablePlaceHolderKey);
    }

}
