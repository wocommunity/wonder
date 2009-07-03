package er.directtoweb.components.strings._xhtml;

import com.webobjects.appserver.WOContext;
import er.directtoweb.components.strings.ERD2WEditString;

/**
 * Adding bindings for id and title from d2wContext
 * NOTE: name is now the same as id
 * 
 * @See ERDEditString
 * @author mendis
 *
 * @deprecated in favour of parent component
 */
@Deprecated
public class ERD2WEditString2 extends ERD2WEditString {
    public ERD2WEditString2(WOContext context) {
        super(context);
    }
}