package er.jdbcadaptor.mysql;

import com.webobjects.jdbcadaptor.JDBCPlugIn;

public class MySQLPlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(_MySQLPlugIn.class.getName(), "mysql");
		JDBCPlugIn.setPlugInNameForSubprotocol(_MySQLPlugIn.class.getName(), "mariadb");
	}
}
