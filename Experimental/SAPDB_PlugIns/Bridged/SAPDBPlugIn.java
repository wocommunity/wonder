//
//  SAPDBPlugIn.java
//  SAPDB_PlugIns
//
//  Created by Wojtek Narczynski on Mon Nov 25 2002.
//  Copyright (c) 2002 Power Media Sp. z o.o.
//  3/4 rights given up - BSD license.
//

//import com.webobjects.foundation.*;
import com.apple.yellow.eoaccess.*;

public class SAPDBPlugIn extends JDBCPlugIn {

    public SAPDBPlugIn( JDBCAdaptor theAdaptor) {
        super( theAdaptor );
    }
    
    
    /**
    * Returns a fully qualified name of the driver class that this
    * plugin prefers to use. The adaptor will attempt to load this
    * class when making a connection.
    */
    public String defaultDriverName() {
        return "com.sap.dbtech.jdbc.DriverSapDB";
    }


}
