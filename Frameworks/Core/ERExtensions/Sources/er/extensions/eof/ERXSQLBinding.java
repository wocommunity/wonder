package er.extensions.eof;

import com.webobjects.eoaccess.EOSQLExpression;

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
    public String sqlStringForBindingOnExpression( EOSQLExpression expression );
}
