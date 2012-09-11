package er.extensions.eof;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOSQLExpression;

/**
 * This class models an SQL binding based on a model keypath/value pair.
 * <p>
 * The keypath refers to the base entity of the SQL expression. As an example, if the
 * base entity is Person, then a binding for person's first name would have a keypath
 * of "firstName". The name of the person's department would be "department.name". The
 * keypath is used to inspect the model and obtain the correct {@link EOAttribute} used
 * to handle SQL generation (value type, conversion, etc).
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

    public String sqlStringForBindingOnExpression(EOSQLExpression expression) {
        return expression.sqlStringForValue( value(), keyPath() );
    }
}
