/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class ERD2WGroupingPrinterFriendlyListPageTemplate extends ERD2WGroupingListPageTemplate {

    public ERD2WGroupingPrinterFriendlyListPageTemplate(WOContext context) {super(context);}
    
  // we don't ever want to batch
    public int numberOfObjectsPerBatch() {
      return 0;
    }

}
