package er.jdbcadaptor.oracle;

import com.webobjects.jdbcadaptor.JDBCPlugIn;

public class EROraclePlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(EROraclePlugIn.class.getName(), "oracle");
	}
}
