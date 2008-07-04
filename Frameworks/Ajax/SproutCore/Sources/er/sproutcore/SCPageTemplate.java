package er.sproutcore;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.components.ERXNonSynchronizingComponent;

/**
 * Template for a page.
 * 
 * @author ak
 * 
 */
public class SCPageTemplate extends ERXNonSynchronizingComponent {

    public static final String CLIENT_JS = "javascripts_for_client";
    public static final String CLIENT_CSS = "stylesheets_for_client";
    public static final String PAGE_CSS = "@content_for_page_styles";
    public static final String PAGE_JS = "pageJavaScript";
    public static final String RESOURCES = "@content_for_resources";
    
    public SCPageTemplate(WOContext context) {
        super(context);
    }

    public String bodyClass() {
        return (String) (valueForBinding("theme") != null ? valueForBinding("theme") : "sc-theme");
    }

    public String renderTree() {
        return SCItem.pageItem().toString();
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
    }
}