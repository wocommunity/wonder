package er.examples.textsearchdemo.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

public class PageWrapper extends ERXStatelessComponent {
	
    public PageWrapper(WOContext context) {
        super(context);
    }

	public boolean showBackButton() {
		return !( context().page().name().equals( Main.class.getName() ) );
	}

	public WOActionResults returnAction() {
		WOComponent backPage = (WOComponent)this.objectValueForBinding("backPage");
		if (backPage == null) {
			backPage = pageWithName(Main.class);
		}
		return backPage;
	}
	
	

}