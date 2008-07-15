package er.ajax.json.localarg;

import org.jabsorb.localarg.LocalArgResolveException;
import org.jabsorb.localarg.LocalArgResolver;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;

public class JSONRPCBridgeWOSessionArgResolver implements LocalArgResolver {

	public Object resolveArg(Object obj) throws LocalArgResolveException {
		if (!(obj instanceof WOContext)) {
			throw new LocalArgResolveException("Invalid context.");
		}
		WOSession session = ((WOContext) obj).session();
		return session.valueForKey("JSONRPCBridge");
	}
}
