package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * A thin D2W wrapper around ERAjaxTagField. To use it, you will have to include
 * ERTaggable in your dependencies.
 * 
 * @author fpeters
 */
public class ERD2WEditTags extends ERDCustomEditComponent {

    private static final long serialVersionUID = 1L;

    public ERD2WEditTags(WOContext aContext) {
        super(aContext);
    }

    /**
     * @return the entity's "taggable" relationship
     */
    public Object taggable() {
        return object().valueForKey("taggable");
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
    }

}
