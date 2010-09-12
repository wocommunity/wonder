package er.mysql;

import com.webobjects.jdbcadaptor.JDBCPlugIn;
import com.webobjects.jdbcadaptor._MySQLPlugIn;

public class MySQLPlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(_MySQLPlugIn.class.getName(), "mysql");
	}
}
