package er.h2;

import com.webobjects.jdbcadaptor.JDBCPlugIn;
import com.webobjects.jdbcadaptor._H2PlugIn;

/**
 * This principal class registers the _H2PlugIn class as the PlugIn
 * for the H2 database for the "h2" subprotocol.
 * 
 * 5.4 declares the same class name for the H2PlugIn. If your classpath isn't
 * exactly right, they'll win, so we pushed the real code into _H2PlugIn and
 * we set a custom principal class that registers the _ variant that is
 * "guaranteed" to not have collisions as the plugin for the "h2"
 * subprotocol.
 *
 * @author ldeck
 */
public class ERH2PlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(_H2PlugIn.class.getName(), "h2");
	}
}
