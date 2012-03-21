//
// ERXImage.java: Class file for WO Component 'ERXImage'
// Project ERExtensions
//
// Created by Max Muller on Fri Jan 31 2003
//
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * 
 */
public class ERXImage extends ERXStatelessComponent {

    public ERXImage(WOContext context) {
        super(context);
    }

    public void reset() {
        super.reset();
        src = null;
    }
    
    protected String src;
    public String src() {
        if (src == null) {
            StringBuffer url = new StringBuffer();
            if (host() != null) {
                url.append("http://");
                url.append(host());
            }
            url.append(relativePath());
            src = url.toString();
        }
        return src;
    }

    public String relativePath() {
        return (String)valueForBinding("relativePath");
    }

    public String host() {
        return (String)valueForBinding("host");
    }
}
