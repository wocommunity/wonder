package er.bugtracker.components;
import com.webobjects.appserver.*;

import er.directtoweb.ERDCustomEditComponent;
import er.taggable.ERTaggable;

/**
 * D2W compatible tag editor
 * 
 * @author ak
 */
public class EditTags extends ERDCustomEditComponent {
    public EditTags(WOContext context) {
        super(context);
    }
    
    public ERTaggable<?> taggable() {
        return  (ERTaggable<?>) object().valueForKey("taggable");
    }
}