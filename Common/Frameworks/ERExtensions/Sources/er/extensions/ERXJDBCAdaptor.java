package er.extensions;

/**
 * A class to bridge the migration to the jdbc sub package.
 * @deprecated use er.extensions.jdbc.ERXJDBCUtilities
 */
public class ERXJDBCAdaptor extends er.extensions.jdbc.ERXJDBCAdaptor {

    public ERXJDBCAdaptor(String name) {
        super(name);
    }

}
