package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;

/**
 * Keeps a session open by continuously calling a direct action.
 * @author ak
 *
 */
public class ERXJSLifebeat extends ERXStatelessComponent {

	public ERXJSLifebeat(WOContext arg0) {
		super(arg0);
	}

	public static class Action extends WODirectAction {

		public Action(WORequest arg0) {
			super(arg0);
		}
		
		public WOActionResults keepAliveAction() {
			return pageWithName("ERXEmptyComponent");
		}
	}
}
