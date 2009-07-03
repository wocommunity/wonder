package er.directtoweb.components.strings._xhtml;

import com.webobjects.appserver.WOContext;
import er.directtoweb.components.strings.ERD2WEditLargeString;

/**
 * Adding id and title bindings from d2wContext
 * NOTE: name is now the same as id
 * 
 * @See ERDEditLargeString
 * @author mendis
 *
 * @deprecated in favour of parent (with added bindings)
 */
@Deprecated
public class ERD2WEditLargeString2 extends ERD2WEditLargeString {
    public ERD2WEditLargeString2(WOContext context) {
        super(context);
    }
}