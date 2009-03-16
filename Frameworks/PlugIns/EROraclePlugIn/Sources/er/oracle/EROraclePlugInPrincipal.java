package er.oracle;

import com.webobjects.jdbcadaptor.EROraclePlugIn;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

public class EROraclePlugInPrincipal {
  static {
    JDBCPlugIn.setPlugInNameForSubprotocol(EROraclePlugIn.class.getName(), "oracle");
  }
}
