package com.webobjects.jdbcadaptor;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

public class DB2ForISeriesPlugIn extends DB2PlugIn {

	  static {
	      setPlugInNameForSubprotocol(DB2ForISeriesPlugIn.class.getName(), "as400");
	  }
	  
	public DB2ForISeriesPlugIn(JDBCAdaptor adaptor) {
		super(adaptor);
	}

	
	  /**
	   * Name of the driver.
	   */
	  public String defaultDriverName() {
	      return "com.ibm.as400.access.AS400JDBCDriver";
	  }

	  /**
	   * Returns a "pure java" synchronization factory.
	   * Useful for testing purposes.
	   */
	  @SuppressWarnings("deprecation")
	public EOSynchronizationFactory createSynchronizationFactory() {
	    try {
	      return new DB2ForISeriesSynchronizationFactory(adaptor());
	    }
	    catch (Exception e) {
	      throw new NSForwardException(e, "Couldn't create synchronization factory");
	    }
	  }

}
