/* Text.java created by rmendis on Thu 08-Feb-2001 */
package org.svgobjects.examples.hello;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class Text extends WOComponent {
    public String text;
    public String styleComponent;

    public Text(WOContext context) {
        super(context);
    }
    
    /*
     * request/response
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);

        // set the header
        response.setHeader("image/svg-xml", "Content-Type");
    }
}
