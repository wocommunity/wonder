package er.sproutcore.views.collection;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;

import er.extensions.components.ERXComponentUtilities;
import er.sproutcore.views.SCView;

public class SCCollectionView extends SCView {

    public SCCollectionView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        moveProperty("enabled", "isEnabled");
        moveProperty("selectable", "isSelectable");
        moveProperty("toggle", "useToggleSelection");
        moveProperty("contentValueEditable", "contentValueIsEditable");
        setProperty("hasContentIcon", new WOConstantValueAssociation(Boolean.valueOf(hasProperty("contentIcon"))));
        setProperty("hasContentBranch", new WOConstantValueAssociation(Boolean.valueOf(hasProperty("contentIsBranch"))));
        setProperty("acceptsFirstReponder", ERXComponentUtilities.TRUE);
    }

    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
