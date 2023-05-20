package er.mysql;

import com.webobjects.jdbcadaptor.JDBCPlugIn;
import com.webobjects.jdbcadaptor._MySQLPlugIn;

public class MySQLPlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(_MySQLPlugIn.class.getName(), "mysql");
		JDBCPlugIn.setPlugInNameForSubprotocol(_MySQLPlugIn.class.getName(), "mariadb");
	}
}
