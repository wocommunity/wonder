package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Class for DirectToWeb Component ERD2WCustomQueryBoolean.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Mon Dec 22 2003
 * @project ERDirectToWeb
 */

public class ERD2WCustomQueryBoolean extends D2WQueryBoolean {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WCustomQueryBoolean.class,"components");
    protected NSArray _choicesNames;
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WCustomQueryBoolean(WOContext context) {
        super(context);
    }

    public NSArray choicesNames() {
        if (_choicesNames == null)
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    public void reset(){
        super.reset();
        _choicesNames = null;
    }
    
    public String displayString() {
        NSArray choicesNames = choicesNames();
        if(choicesNames == null) {
            return super.displayString();
        }
        int choicesIndex = index == 0 ? 2 : index - 1;
        if(choicesIndex >= choicesNames.count()) {
            return super.displayString();
        }
        return (String)choicesNames.objectAtIndex(choicesIndex);
    }
}
