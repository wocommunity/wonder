package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.sproutcore.SCUtilities;

public class SCImageView extends SCView {

    public SCImageView(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);
    }
    
    @Override
    protected void addProperties() {
    	super.addProperties();
    	addProperty("content");
    	addProperty("value");
    }

    @Override
    public String elementName(WOContext context) {
        return "img";
    }

    @Override
    public void appendAttributesToResponse(WOResponse arg0, WOContext arg1) {
        String src = (String) valueForBinding("src", SCUtilities.staticUrl("blank.gif"), arg1.component());
        arg0.appendContentString("src=\"");
        arg0.appendContentHTMLAttributeValue(src);
        arg0.appendContentString("\"");
    }
    
    @Override
    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.doAppendToResponse(response, context);
    }
}
