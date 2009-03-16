//
//  PMPage.java
//  PMBasicFramework
//
//  Created by Jacek Kaczmarek on Wed Oct 15 2003.
//  Copyright (c) 2003 PowerMedia. All rights reserved.
//
package pm.util;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;


/**
 * Example implementation of PMPage for WOLogging.
 */

public class PMPage extends WOComponent {

		protected final String WO_LOGGING_CUSTOM_ENV_NAME = "x-webobjects-customenv";
		protected final String WO_LOGGING_SEPARATOR = ";";
	
    public PMPage(WOContext context) {
        super(context);
    }

      public void appendToResponse(WOResponse response, WOContext context) {
          super.appendToResponse(response, context);
	        response.setHeader(descriptionForResponse(), WO_LOGGING_CUSTOM_ENV_NAME );
      }

      /**
       * @return	8-digit alphanumeric sessionID substring
       */
      protected String sessionIDSubstring() {
          return session().sessionID().substring( 0, 7 ).toUpperCase();
      }

      /**
       * default page information: app name, sessionID MD5 and page name.
       */
      protected String defaultDescriptionForResponse() {
          return
          WOApplication.application().name()
          + WO_LOGGING_SEPARATOR
          + sessionIDSubstring()
          + WO_LOGGING_SEPARATOR
          + name()
          + WO_LOGGING_SEPARATOR
          ;
      }

      /**
       * override this method to log more information. This is invoked in appendToResponse.
       */
      public String descriptionForResponse() {
          return defaultDescriptionForResponse();
      }

      /**
       * overridden implementation of descriptionForResponse(WOResponse,WOContext) 
       * so that it returns descriptionForResponse() with no params.
       */
      public String descriptionForResponse(WOResponse aResponse, WOContext aContext) {
          return descriptionForResponse();
      }
}
