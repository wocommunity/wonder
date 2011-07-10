package er.websocketexample.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.woadaptor.ERWOAdaptorUtilities;

public class ChatRoom extends WOComponent {
    public ChatRoom(WOContext context) {
        super(context);
    }

	public String websocketURL() {
		return ERWOAdaptorUtilities.websocketUrlInContext(context());
	}
}