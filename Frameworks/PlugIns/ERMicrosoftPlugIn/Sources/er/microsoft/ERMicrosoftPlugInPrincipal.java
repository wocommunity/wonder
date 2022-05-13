package er.microsoft;

import com.webobjects.jdbcadaptor.ERMicrosoftPlugIn;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

/**
 * {@code ERMicrosoftPlugIn} framework principal class.
 *
 * @author hprange
 */
public class ERMicrosoftPlugInPrincipal {
    static {
        JDBCPlugIn.setPlugInNameForSubprotocol(ERMicrosoftPlugIn.class.getName(), "sqlserver");
    }
}
