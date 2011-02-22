package er.extensions.eof;

import com.webobjects.jdbcadaptor.EOKeyValueBinding;

/**
 * This class models an SQL binding based on a model keypath/value pair.
 * 
 * @deprecated Use EOKeyValueBinding
 */
@Deprecated
public class ERXKeyValueBinding extends EOKeyValueBinding implements ERXSQLBinding {
    
    /**
     * Obtains new binding wrapper for passed in EOF key and value
     * 
     * @param keyPath
     *          The EOF key
     * @param value
     *          The value to bind
     */
    public ERXKeyValueBinding(String keyPath, Object value) {
        super(keyPath, value);
    }
    
    /**
     * Obtains new binding wrapper for passed in EOF key and value
     * 
     * @param keyPath
     *          The EOF key
     * @param value
     *          The value to bind
     */
    public ERXKeyValueBinding(String entityName, String keyPath, Object value) {
        super(entityName, keyPath, value);
    }
    
}
