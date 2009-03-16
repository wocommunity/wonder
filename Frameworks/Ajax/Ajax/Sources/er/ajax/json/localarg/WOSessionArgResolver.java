package er.ajax.json.localarg;

import org.jabsorb.localarg.LocalArgResolveException;
import org.jabsorb.localarg.LocalArgResolver;

import com.webobjects.appserver.WOContext;

public class WOSessionArgResolver implements LocalArgResolver {
	public Object resolveArg(Object obj) throws LocalArgResolveException {
		if (!(obj instanceof WOContext)) {
			throw new LocalArgResolveException("Invalid context.");
		}
		return ((WOContext) obj).session();
	}

}
