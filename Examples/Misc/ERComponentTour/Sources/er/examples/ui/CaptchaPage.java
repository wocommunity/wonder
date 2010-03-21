package er.examples.ui;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class CaptchaPage extends ERXComponent {

    public CaptchaPage(WOContext context) {
        super(context);
    }

    public boolean attempted;
    public boolean userOk;

    public WOActionResults submit() {
    	attempted = true;
    	return null;
    }
}