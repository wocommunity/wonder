package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

/**
* Patch for WOResponse to fix most of the XML errors the dynamic elements do.
 * This class will should get instantiated in ERXApplication.createResponse()
 *
 * @created ak on Tue Oct 15 2002
 * @project ERExtensions
 */

public class ERXWOResponse extends WOResponse {
    /* logging support */
    protected static final ERXLogger log = ERXLogger.getERXLogger(ERXCompilerProxy.class.getName());
    /**
     * Public constructor
     * @param context the context
     */
    public ERXWOResponse() {
    }

    /** Overridden from WOResponse to show quotes and proper escaping.
    public void _appendTagAttributeAndValue(String string, String string2,
                                            boolean bool) {
        _content.append(string);
        _content.append("=\"");
        if (bool)
            _content.append
                (WOMessage.stringByEscapingHTMLAttributeValue(string2));
        else
            _content.append(string2);
        _content.append('\"');
    }*/
}
