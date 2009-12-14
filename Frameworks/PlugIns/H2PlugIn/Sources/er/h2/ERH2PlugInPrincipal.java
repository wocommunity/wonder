package er.h2;

import er.h2.jdbcadaptor.ERH2PlugIn;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

/**
 * This principal class registers the ERH2PlugIn class as the PlugIn
 * for the "h2" database for the "h2" subprotocol.
 *
 * @author ldeck
 */
public class ERH2PlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(ERH2PlugIn.class.getName(), "h2");
	}
}
