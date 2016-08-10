package er.seleniumexample;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class Main extends WOComponent {
    
	public Main(WOContext context) {
        super(context);
    }

    public WOComponent showAction() {
    	return pageWithName(Message.class.getName());
    }
}
