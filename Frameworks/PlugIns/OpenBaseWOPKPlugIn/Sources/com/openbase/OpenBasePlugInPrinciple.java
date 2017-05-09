package com.openbase;

import com.webobjects.jdbcadaptor.JDBCPlugIn;
import com.webobjects.jdbcadaptor._OpenBasePlugIn;

public class OpenBasePlugInPrinciple {
  
  static { JDBCPlugIn.setPlugInNameForSubprotocol(_OpenBasePlugIn.class.getName(), "openbase");}
}
