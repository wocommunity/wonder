package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class Main extends WOComponent  {
    String backgroundSrc;
    
    public Main(WOContext context) {
        super(context);
    }
    
    /*
    * accessors
    */
    public String backgroundSrc() {
        if (backgroundSrc == null) {
            WOResourceManager resourceManager = application().resourceManager();
            backgroundSrc = resourceManager.urlForResourceNamed("background.html", null, null, this.context().request());
        } return backgroundSrc;
    }
}