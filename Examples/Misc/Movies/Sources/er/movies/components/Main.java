package er.movies.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {
	
    public Main(WOContext context) {
		super(context);
	}

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        ERXResponseRewriter.addStylesheetResourceInHead(response, context, "app", "Main.css");
    }
    
}
