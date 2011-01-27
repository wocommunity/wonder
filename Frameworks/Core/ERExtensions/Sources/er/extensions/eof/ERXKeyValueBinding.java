package er.extensions.eof;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;

/**
 * This class models an SQL binding based on a model keypath/value pair.
 * <p>
 * When the {@link #ERXKeyValueBinding(String, Object)} constructor is used, <code>keypath</code>
 * refers to the base entity of the SQL expression. As an example, if the base entity is
 * Person, than a binding for person's first name would have a keypath of "firstName".
 * The name of the person's department would be "department.name". This constructor should
 * be used when the base entity is known. This happens on {@link ERXSQLQuery}
 * methods that receive an entity name.
 * </p>
 * <p>
 * When using the {@link #ERXKeyValueBinding(String, String, Object)} constructor, <code>keypath</code>
 * works the same way, but refers to the entity whose name is passed in as the constructor first
 * argument. This constructor should be used when the base entity is not specified, like in
 * {@link ERXSQLQuery#runQuery(EOEditingContext, String, String, ERXSQLBinding...)}.
 * </p>
 * <p>
 * The optional entity name and keypath are used to inspect the model and obtain the correct
 * {@link EOAttribute} used to handle SQL generation (value type, conversion, etc).
 * </p>
 */
public class ERXKeyValueBinding implements ERXSQLBinding {
    
    /**
     * The EOF key of the wrapped binding
     */
    private String _keyPath;
    
    /**
     * The value of the binded variable
     */
    private Object _value;
    
    /**
     * Optional entity name.
     */
    private String _entityName;
    
    /**
     * Obtains new binding wrapper for passed in EOF key and value
     * 
     * @param keyPath
     *          The EOF key
     * @param value
     *          The value to bind
     */
    public ERXKeyValueBinding( String keyPath, Object value ) {
        _keyPath = keyPath;
        _value = value;
    }
    
    /**
     * Obtains new binding wrapper for passed in EOF key and value
     * 
     * @param keyPath
     *          The EOF key
     * @param value
     *          The value to bind
     */
    public ERXKeyValueBinding( String entityName, String keyPath, Object value ) {
        _entityName = entityName;
        _keyPath = keyPath;
        _value = value;
    }

    /**
     * The EOF key of the wrapped binding
     * 
     * @return The EOF key of the wrapped binding
     */
    public String keyPath() {
        return _keyPath;
    }

    /**
     * The value of the binded variable
     * 
     * @return The value of the binded variable
     */
    public Object value() {
        return _value;
    }
    
    /**
     * The optional entity name.
     * <p>
     * If the entity name is defined, keyPath applies to that entity and not the base SQL
     * expression this binding will be applied to.
     * </p>
     * 
     * @return The entity name, if defined. Null otherwise.
     */
    public String entityName() {
        return _entityName;
    }

    public String sqlStringForBindingOnExpression(EOSQLExpression expression, EOEditingContext ec) {
        if (entityName() == null) {
            if (expression.entity() == null) {
                throw new RuntimeException("When using methods without an explicit entity name, the ERXKeyValueBinding(String entityName, String keyPath, Object value) constructor must be used.");
            }
            return expression.sqlStringForValue( value(), keyPath() );
        } else {
            // marroz: This is a very nasty trick. The rational behind this is that we want to use expression.sqlStringForValue
            // as much as possible, to obtain the database-specific variable placeholder. We could do something simpler like
            // ERXObjectBinding does, but we would not obtain that db-specific placeholder.
            EOEntity oldEntity = expression.entity();
            expression._setEntity(EOUtilities.entityNamed(ec, entityName()));
            String result = expression.sqlStringForValue( value(), keyPath() );
            expression._setEntity(oldEntity);
            return result;
        }
    }

    public boolean isModelAttribute() {
        return true;
    }
}
