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

    /**
     * Do I need to update serialVersionUID?
     * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the
     * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
     */
    private static final long serialVersionUID = 1L;

    public ERXImage(WOContext context) {
        super(context);
    }

    @Override
    public void reset() {
        super.reset();
        src = null;
    }

    protected String src;
    public String src() {
        if (src == null) {
            StringBuilder url = new StringBuilder();
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

    public com.webobjects.foundation.NSData data() {
        throw new IllegalArgumentException(
                     "The data binding is not implemented in ERXImage, though perhaps it should be. Make sure you are using WOImage, or "+
                     "tell WOLips that you are using WOImage.");
    }

    public void setData(com.webobjects.foundation.NSData value) {
        throw new IllegalArgumentException(
                     "The data binding is not implemented in ERXImage, though perhaps it should be. Make sure you are using WOImage, or "+
                     "tell WOLips that you are using WOImage.");
    }
}
