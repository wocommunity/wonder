package er.extensions.eof;

import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOEditingContext;

/**
 * Interface for binding wrapper objects.
 */
public interface ERXSQLBinding {
    
    /**
     * Registers the binding on a given SQL expression, and obtains the
     * binding placeholder string for the binding value.
     * 
     * @param expression
     *          EOSQLExpression being built
     * @return the SQL placeholder string for the used DB, usually "?"
     */
    public String sqlStringForBindingOnExpression( EOSQLExpression expression, EOEditingContext ec );
    
    /**
     * True if the binded attribute is an existing EOModel attribute, false otherwise.
     * <p>
     * A binding should return true if the binding contains enough information to know what
     * model attribute the value will be binded to. Bindings that return true will benefit
     * from proper type conversion, as defined in the model (for instance, converting a
     * boolean to a string or integer).
     * </p>
     * 
     * @return True if the binded attribute is an existing EOModel attribute, false otherwise.
     */
    public boolean isModelAttribute();
    
}
