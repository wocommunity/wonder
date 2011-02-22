package er.extensions.eof;

import com.webobjects.jdbcadaptor.EOObjectBinding;

/**
 * This class models an SQL binding based on a Java object.
 * 
 * @deprecated Use EOObjectBinding
 */
@Deprecated
public class ERXObjectBinding extends EOObjectBinding implements ERXSQLBinding {

    /**
     * Creates a new object binding wrapper for the given object.
     * <p>
     * value must be an instance of the following classes (or any subsclass of these): String,
     * BigDecimal, Number, NSData, NSTimestamp.
     * </p>
     * @param value
     *          Object to be wrapped
     */
    public ERXObjectBinding(Object value) {
        super(value);
    }
    
}
