package er.example.erxpartials.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.directtoweb.D2W;

public class Main extends WOComponent {
	private static final long serialVersionUID = 1L;
    public String username;
    public String password;
    public boolean wantsWebAssistant = false;

    public Main(WOContext aContext) {
        super(aContext);
    }

    public WOComponent defaultPage() {
    	if (isAssistantCheckboxVisible()) {
    		D2W.factory().setWebAssistantEnabled(wantsWebAssistant);
    	}
        return D2W.factory().defaultPage(session());
    }
    
    public boolean isAssistantCheckboxVisible() {
    	String s = System.getProperty("D@WWebAssistant Enabled");
    	return s == null || NSPropertyListSerialization.booleanForString(s);
    }

}
